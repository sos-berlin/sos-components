package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflows.getControllerId(), getControllerPermissions(modifyWorkflows
                    .getControllerId(), accessToken).map(p -> p.getOrders().getSuspendResume()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowsModify(Action.SUSPEND, modifyWorkflows);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse resumeWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflows modifyWorkflows = initRequest(Action.RESUME, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflows.getControllerId(), getControllerPermissions(modifyWorkflows
                    .getControllerId(), accessToken).map(p -> p.getOrders().getSuspendResume()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowsModify(Action.RESUME, modifyWorkflows);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private void postWorkflowsModify(Action action, ModifyWorkflows modifyWorkflows) throws Exception {
        
        String controllerId = modifyWorkflows.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflows.getAuditLog(), controllerId);
        
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
        
        List<WorkflowPath> workflowPaths2 = getCheckedWorkflows(action, workflowsStream, currentState, controllerId, withWorkflowPaths);

        if (!workflowPaths2.isEmpty()) {
            // TODO batch command or we need plural function controlWorkflows
            // max. 32 request possible -> We use 24
            // partition(workflowPaths2, 24).forEach(w -> command(controllerId, action, w, dbAuditLog));
            //workflowPaths2.forEach(w -> command(controllerId, action, w, dbAuditLog));
            
            ControllerApi.of(controllerId).executeCommand(JControllerCommand.batch(workflowPaths2.stream().filter(w -> !w.isEmpty()).map(
                    w -> command2(controllerId, action, w, dbAuditLog)).collect(Collectors.toList()))).thenAccept(either -> {
                        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                        if (either.isRight()) {
                            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPaths(workflowPaths2, dbAuditLog.getId(), controllerId).thenAccept(
                                    either2 -> ProblemHelper.postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                        }
                    });
        } else {
            throwControllerObjectNotExistException(action);
        }
    }

    private void throwControllerObjectNotExistException(Action action) throws ControllerObjectNotExistException {
        switch (action) {
        case RESUME:
            throw new ControllerObjectNotExistException("No suspended workflows found.");
        default: // Action.SUSPEND.equals(action)
            throw new ControllerObjectNotExistException("No unsuspended workflows found.");
        }
    }
    
    private List<WorkflowPath> getCheckedWorkflows(Action action, Stream<WorkflowPath> workflowsStream, JControllerState currentState,
            String controllerId, boolean withPostProblem) {

        Set<WorkflowPath> suspendedWorkflowsAtController = JavaConverters.asJavaCollection(currentState.asScala().pathToWorkflowPathControl()
                .values()).stream().filter(WorkflowPathControl::suspended).map(WorkflowPathControl::workflowPath).collect(Collectors.toSet());

        Map<Boolean, List<WorkflowPath>> suspendedWorkflows = workflowsStream.distinct().collect(Collectors.groupingBy(
                w -> suspendedWorkflowsAtController.contains(w), Collectors.toList()));

        switch (action) {
        case RESUME:
            if (withPostProblem && suspendedWorkflows.containsKey(Boolean.FALSE)) {
                String msg = suspendedWorkflows.get(Boolean.FALSE).stream().map(WorkflowPath::string).collect(Collectors.joining("', '",
                        "Workflows '", "' are not suspended"));
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(),
                        getJocError(), controllerId);
            }
            return suspendedWorkflows.getOrDefault(Boolean.TRUE, Collections.emptyList());
        default: // Action.SUSPEND.equals(action)
            if (withPostProblem && suspendedWorkflows.containsKey(Boolean.TRUE)) {
                String msg = suspendedWorkflows.get(Boolean.TRUE).stream().map(WorkflowPath::string).collect(Collectors.joining("', '", "Workflows '",
                        "' are already suspended"));
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(),
                        getJocError(), controllerId);
            }
            return suspendedWorkflows.getOrDefault(Boolean.FALSE, Collections.emptyList());
        }
    }
    
//    private void command(String controllerId, Action action, List<WorkflowPath> workflowPaths, DBItemJocAuditLog dbAuditLog) {
//        if (!workflowPaths.isEmpty()) {
//            boolean suspend = action.equals(Action.SUSPEND);
//            JControllerCommand commmand = JControllerCommand.controlWorkflowPath(workflowPaths.get(0), Optional.of(suspend), Collections.emptyMap());
//            ControllerApi.of(controllerId).executeCommand(commmand).thenAccept(either -> {
//                thenAcceptHandler(either, controllerId, action, workflowPaths, dbAuditLog);
//            });
//        }
//    }
    
    private JControllerCommand command2(String controllerId, Action action, WorkflowPath workflowPath, DBItemJocAuditLog dbAuditLog) {
        boolean suspend = action.equals(Action.SUSPEND);
        return JControllerCommand.controlWorkflowPath(workflowPath, Optional.of(suspend), Collections.emptyMap());
    }

    private ModifyWorkflows initRequest(Action action, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken, CategoryType.CONTROLLER);
        JsonValidator.validate(filterBytes, ModifyWorkflows.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflows.class);
    }
    
//    private void thenAcceptHandler(Either<Problem, Response> either, String controllerId, Action action, List<WorkflowPath> workflowPaths,
//            DBItemJocAuditLog dbAuditLog) {
//        WorkflowPath w = workflowPaths.remove(0);
//        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
//        if (either.isRight()) {
//            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPath(w, dbAuditLog, controllerId).thenAccept(either2 -> ProblemHelper
//                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
//        }
//        command(controllerId, action, workflowPaths, dbAuditLog);
//    }

    private static <T> Collection<List<T>> partition(List<T> collection, int n) {
        return IntStream.range(0, collection.size()).boxed().collect(Collectors.groupingBy(i -> i % n, Collectors.mapping(collection::get, Collectors
                .toList()))).values();
    }

}