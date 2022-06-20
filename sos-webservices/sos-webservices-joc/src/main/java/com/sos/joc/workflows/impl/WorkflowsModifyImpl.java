package com.sos.joc.workflows.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.workflow.ModifyWorkflows;
import com.sos.joc.workflows.resource.IWorkflowsModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.workflow.WorkflowControlState;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflowId;
import scala.collection.JavaConverters;

@Path("workflows")
public class WorkflowsModifyImpl extends JOCResourceImpl implements IWorkflowsModify {

    private static final String API_CALL = "./workflows/";

    private enum Action {
        SUSPEND, RESUME
    }

    @Override
    public JOCDefaultResponse suspendWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflows modifyWorkflows = initRequest(Action.SUSPEND, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyWorkflows.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflows.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowsModify(Action.SUSPEND, modifyWorkflows);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse resumeWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflows modifyWorkflows = initRequest(Action.RESUME, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyWorkflows.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflows.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowsModify(Action.RESUME, modifyWorkflows);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void postWorkflowsModify(Action action, ModifyWorkflows modifyWorkflows) throws Exception {
        
        String controllerId = modifyWorkflows.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflows.getAuditLog(), controllerId, CategoryType.CONTROLLER);
        
        if (modifyWorkflows.getAll() == Boolean.TRUE) {
            modifyWorkflows.setWorkflowPaths(null);
            modifyWorkflows.setFolders(null);
        }
        List<String> workflowPaths = modifyWorkflows.getWorkflowPaths();
        boolean withWorkflowPaths = workflowPaths != null && !workflowPaths.isEmpty();
        if (withWorkflowPaths) {
            modifyWorkflows.setFolders(null);
        }
        
        boolean withFolderFilter = modifyWorkflows.getFolders() != null && !modifyWorkflows.getFolders().isEmpty();
        Set<Folder> permittedFolders = addPermittedFolder(modifyWorkflows.getFolders());

        JControllerState currentState = Proxy.of(controllerId).currentState();

        Stream<WorkflowPath> workflowsStream = Stream.empty();
        if (modifyWorkflows.getAll() == Boolean.TRUE) {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection(API_CALL + action.name().toLowerCase());
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                workflowsStream = dbLayer.getWorkflowsIds(null, controllerId).stream().map(WorkflowId::getPath).filter(w -> canAdd(w,
                        permittedFolders)).map(JocInventory::pathToName).map(WorkflowPath::of).filter(w -> WorkflowsHelper.workflowCurrentlyExists(
                                currentState, w));
            } finally {
                Globals.disconnect(connection);
            }
        } else if (withWorkflowPaths) {
            workflowsStream = workflowPaths.stream().map(JocInventory::pathToName).filter(w -> canAdd(WorkflowPaths.getPath(w), permittedFolders))
                    .map(WorkflowPath::of).filter(w -> WorkflowsHelper.workflowCurrentlyExists(currentState, w));

        } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
            // no permission
        } else if (withFolderFilter && permittedFolders != null && !permittedFolders.isEmpty()) {
            workflowsStream = WorkflowsHelper.getWorkflowIdsStreamFromFolders(controllerId, permittedFolders.stream().collect(Collectors.toList()),
                    currentState, permittedFolders).map(JWorkflowId::path);
        }
        
        Set<WorkflowPath> workflowPaths2 = getCheckedWorkflows(action, workflowsStream, currentState, controllerId, withWorkflowPaths);

        if (!workflowPaths2.isEmpty()) {
            // TODO we need plural function controlWorkflows
            workflowPaths2.forEach(w -> command(controllerId, action, w, dbAuditLog));
        } else {
            throwControllerObjectNotExistException(action);
        }
    }

    private void throwControllerObjectNotExistException(Action action) throws ControllerObjectNotExistException {
        switch (action) {
        case RESUME:
            throw new ControllerObjectNotExistException("No suspended workflows found.");
        default:
            throw new ControllerObjectNotExistException("No unsuspended workflows found.");
        }
    }
    
    private Set<WorkflowPath> getCheckedWorkflows(Action action, Stream<WorkflowPath> workflowsStream, JControllerState currentState,
            String controllerId, boolean withPostProblem) {
        Set<WorkflowPath> suspendedWorkflowsAtController = JavaConverters.asJava(currentState.asScala().pathToWorkflowControlState_()).values()
                .stream().filter(c -> c.workflowControl().suspended()).map(WorkflowControlState::workflowPath).collect(Collectors.toSet());

        Map<Boolean, Set<WorkflowPath>> suspendedWorkflows = workflowsStream.collect(Collectors.groupingBy(w -> suspendedWorkflowsAtController
                .contains(w), Collectors.toSet()));

        switch (action) {
        case RESUME:
            if (withPostProblem && suspendedWorkflows.containsKey(Boolean.FALSE)) {
                String msg = suspendedWorkflows.get(Boolean.FALSE).stream().map(WorkflowPath::string).collect(Collectors.joining("', '",
                        "Workflows '", "' are not suspended"));
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
            }
            return suspendedWorkflows.getOrDefault(Boolean.TRUE, Collections.emptySet());
        case SUSPEND:
            if (withPostProblem && suspendedWorkflows.containsKey(Boolean.TRUE)) {
                String msg = suspendedWorkflows.get(Boolean.TRUE).stream().map(WorkflowPath::string).collect(Collectors.joining("', '", "Workflows '",
                        "' are already suspended"));
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
            }
            return suspendedWorkflows.getOrDefault(Boolean.FALSE, Collections.emptySet());
        default:
            return workflowsStream.collect(Collectors.toSet());
        }
    }

    private CompletableFuture<Void> command(String controllerId, Action action, WorkflowPath workflowPath, DBItemJocAuditLog dbAuditLog) {

        boolean suspend = action.equals(Action.SUSPEND);
        JControllerCommand commmand = JControllerCommand.controlWorkflow(workflowPath, suspend);
        return ControllerApi.of(controllerId).executeCommand(commmand).thenAccept(either -> {
            ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
            if (either.isRight()) {
                WorkflowsHelper.storeAuditLogDetailsFromWorkflowPath(workflowPath, dbAuditLog, controllerId).thenAccept(either2 -> ProblemHelper
                        .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
            }
        });
    }

    private ModifyWorkflows initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyWorkflows.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflows.class);
    }

}