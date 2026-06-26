package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.workflow.ModifyWorkflows;
import com.sos.joc.workflows.resource.IWorkflowsModify;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
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
            Map<Boolean, List<WorkflowPath>> workflows = getWorkflows(Action.SUSPEND, modifyWorkflows);
            JOCDefaultResponse jocDefaultResponse = initWorkflowPermissions(modifyWorkflows.getControllerId(), getControllerPermissions(
                    modifyWorkflows.getControllerId(), accessToken).map(p -> p.getOrders().getSuspendResume()), getWorkflows(Action.SUSPEND,
                            workflows).stream().map(WorkflowPath::string).collect(Collectors.toSet()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowsModify(Action.SUSPEND, modifyWorkflows, workflows);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse resumeWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflows modifyWorkflows = initRequest(Action.RESUME, accessToken, filterBytes);
            Map<Boolean, List<WorkflowPath>> workflows = getWorkflows(Action.RESUME, modifyWorkflows);
            JOCDefaultResponse jocDefaultResponse = initWorkflowPermissions(modifyWorkflows.getControllerId(), getControllerPermissions(
                    modifyWorkflows.getControllerId(), accessToken).map(p -> p.getOrders().getSuspendResume()), getWorkflows(Action.RESUME, workflows)
                            .stream().map(WorkflowPath::string).collect(Collectors.toSet()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowsModify(Action.RESUME, modifyWorkflows, workflows);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private void postWorkflowsModify(Action action, ModifyWorkflows modifyWorkflows, Map<Boolean, List<WorkflowPath>> suspendedWorkflows) throws Exception {
        
        String controllerId = modifyWorkflows.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflows.getAuditLog(), controllerId);
        boolean withWorkflowPaths = modifyWorkflows.getWorkflowPaths() != null && !modifyWorkflows.getWorkflowPaths().isEmpty();
        
        checkWorkflows(action, suspendedWorkflows.getOrDefault(action.equals(Action.SUSPEND), Collections.emptyList()), controllerId,
                withWorkflowPaths);
        List<WorkflowPath> workflows = getWorkflows(action, suspendedWorkflows);
        
        if (!workflows.isEmpty()) {
            ControllerApi.of(controllerId).executeCommand(JControllerCommand.batch(workflows.stream().filter(w -> !w.isEmpty()).map(
                    w -> command2(controllerId, action, w, dbAuditLog)).collect(Collectors.toList()))).thenAccept(either -> {
                        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                        if (either.isRight()) {
                            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPaths(workflows, dbAuditLog.getId(), controllerId).thenAccept(
                                    either2 -> ProblemHelper.postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                        }
                    });
        } else {
            throwControllerObjectNotExistException(action);
        }
    }
    
    private Map<Boolean, List<WorkflowPath>> getWorkflows(Action action, ModifyWorkflows modifyWorkflows) throws Exception {
        
        String controllerId = modifyWorkflows.getControllerId();
        setFolderPermissions(controllerId);
        
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
                workflowsStream = dbLayer.getWorkflowsIds(null, controllerId).stream().map(WorkflowId::getPath).distinct().filter(w -> canAdd(w,
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
        
        return getSuspendedWorkflows(workflowsStream, currentState);
    }

    private void throwControllerObjectNotExistException(Action action) throws ControllerObjectNotExistException {
        switch (action) {
        case RESUME:
            throw new ControllerObjectNotExistException("No suspended workflows found.");
        default: // Action.SUSPEND.equals(action)
            throw new ControllerObjectNotExistException("No unsuspended workflows found.");
        }
    }
    
    private Map<Boolean, List<WorkflowPath>> getSuspendedWorkflows(Stream<WorkflowPath> workflowsStream, JControllerState currentState) {
        
        Set<WorkflowPath> suspendedWorkflowsAtController = JavaConverters.asJavaCollection(currentState.asScala().pathToWorkflowPathControl()
                .values()).stream().filter(WorkflowPathControl::suspended).map(WorkflowPathControl::workflowPath).collect(Collectors.toSet());

        return workflowsStream.distinct().collect(Collectors.groupingBy(suspendedWorkflowsAtController::contains));
    }
    
    private List<WorkflowPath> getWorkflows(Action action, Map<Boolean, List<WorkflowPath>> suspendedWorkflows) {
        
        return suspendedWorkflows.getOrDefault(action.equals(Action.RESUME), Collections.emptyList());
    }
    
    private void checkWorkflows(Action action, List<WorkflowPath> workflows, String controllerId, boolean withPostProblem) {

        switch (action) {
        case RESUME:
            if (withPostProblem && !workflows.isEmpty()) {
                String msg = workflows.stream().map(WorkflowPath::string).collect(Collectors.joining("', '", "Workflows '", "' are not suspended"));
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
            }
        default: // Action.SUSPEND.equals(action)
            if (withPostProblem && !workflows.isEmpty()) {
                String msg = workflows.stream().map(WorkflowPath::string).collect(Collectors.joining("', '", "Workflows '",
                        "' are already suspended"));
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
            }
        }
    }
       
    private JControllerCommand command2(String controllerId, Action action, WorkflowPath workflowPath, DBItemJocAuditLog dbAuditLog) {
        boolean suspend = action.equals(Action.SUSPEND);
        return JControllerCommand.controlWorkflowPath(workflowPath, Optional.of(suspend), Collections.emptyMap());
    }

    private ModifyWorkflows initRequest(Action action, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken, CategoryType.CONTROLLER);
        JsonValidator.validate(filterBytes, ModifyWorkflows.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflows.class);
    }

}