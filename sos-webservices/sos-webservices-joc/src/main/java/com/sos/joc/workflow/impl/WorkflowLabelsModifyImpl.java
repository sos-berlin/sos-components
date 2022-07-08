package com.sos.joc.workflow.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.workflow.ModifyWorkflowLabels;
import com.sos.joc.workflow.resource.IWorkflowLabelsModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand.Response;
import js7.data.workflow.Workflow;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
import js7.data.workflow.position.Label;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;

@Path("workflow")
public class WorkflowLabelsModifyImpl extends JOCResourceImpl implements IWorkflowLabelsModify {

    private static final String API_CALL = "./workflow/";

    private enum Action {
        SKIP, UNSKIP, STOP, UNSTOP
    }
    
    private class WorkflowJobs {
        private WorkflowPath workflowPath;
        private Set<String> labels;
        
        public WorkflowJobs(ModifyWorkflowLabels workflowJob) {
            workflowPath = WorkflowPath.of(JocInventory.pathToName(workflowJob.getWorkflowPath()));
            labels = workflowJob.getLabels().stream().collect(Collectors.toSet());
        }
        
        public WorkflowPath getWorkflowPath() {
            return workflowPath;
        }
        
        public String getPath() {
            return workflowPath.string();
        }
        
        public Set<String> getLabels() {
            return labels;
        }
        
        public Map<Label, Boolean> getLabelsForSkipOrUnSkip(Boolean action) {
            Map<Label, Boolean> m = new HashMap<>(labels.size());
            labels.forEach(l -> m.put(Label.fromString(l), action));
            return m;
        }
    }
    
    @Override
    public JOCDefaultResponse skipWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.SKIP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.SKIP, modifyWorkflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse unskipWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.UNSKIP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.UNSKIP, modifyWorkflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse stopWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.STOP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.STOP, modifyWorkflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse unstopWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.UNSTOP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.UNSTOP, modifyWorkflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void postWorkflowJobsModify(Action action, ModifyWorkflowLabels modifyWorkflow) throws Exception {

        String controllerId = modifyWorkflow.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflow.getAuditLog(), controllerId, CategoryType.CONTROLLER);
        JControllerState currentState = Proxy.of(controllerId).currentState();

        WorkflowJobs wj = new WorkflowJobs(modifyWorkflow);
        checkFolderPermissions(WorkflowPaths.getPath(wj.getPath()));
        checkWorkflow(action, wj, currentState, modifyWorkflow.getLabels());
        
        command(controllerId, action, wj, dbAuditLog);
    }
    
    private void checkWorkflow(Action action, WorkflowJobs wj, JControllerState currentState, List<String> requestedLabels) {
        
        Set<String> knownLabels = new HashSet<>();
        Either<Problem, JWorkflow> workflowV = currentState.repo().pathToCheckedWorkflow(wj.getWorkflowPath());
        if (workflowV == null || workflowV.isLeft()) {
            throw new ControllerObjectNotExistException("Workflow '" + wj.getPath() + "' not found.");
        }

        // knownLabels = JavaConverters.asJava(workflowV.get().asScala().labeledInstructions()).stream().map(Labeled::labelString).filter(
        // s -> !s.isEmpty()).map(String::trim).map(s -> s.replaceFirst(":$", "")).collect(Collectors.toSet());

        Workflow wV = workflowV.get().asScala();
        requestedLabels.forEach(l -> {
            if (wV.labelToPosition(Label.fromString(l)).isRight()) {
                knownLabels.add(l);
            }
        });
        
        // check labels of older workflow versions
        WorkflowsHelper.oldJWorkflowIds(currentState).filter(wId -> wj.getPath().equals(wId.path().string())).map(wId -> currentState.repo()
                .idToCheckedWorkflow(wId)).filter(Either::isRight).map(Either::get).map(JWorkflow::asScala).forEach(w -> {
                    requestedLabels.forEach(l -> {
                        if (w.labelToPosition(Label.fromString(l)).isRight()) {
                            knownLabels.add(l);
                        }
                    });
                });

        wj.getLabels().retainAll(knownLabels);
        requestedLabels.removeAll(wj.getLabels());

        if (!requestedLabels.isEmpty()) {
            if (requestedLabels.size() == 1) {
                throw new ControllerObjectNotExistException("The label '" + requestedLabels.get(0) + "' doesn't exist in workflow '" + wj.getPath()
                        + "'.");
            }
            throw new ControllerObjectNotExistException("The labels " + requestedLabels.toString() + " don't exist in workflow '" + wj.getPath()
                    + "'.");
        }

        Optional<WorkflowPathControl> controlState = WorkflowsHelper.getWorkflowPathControl(currentState, wj.getWorkflowPath(), false);
        if (controlState.isPresent()) {
            Set<String> skippedLabels = WorkflowsHelper.getSkippedLabels(controlState, false);
            
            switch (action) {
            case SKIP:
                wj.getLabels().removeAll(skippedLabels);
                if (wj.getLabels().isEmpty()) { // TODO or maybe better always raise an exception?
                    throw new JocBadRequestException("All requested labels are already skipped.");
                }
                break;
            case UNSKIP:
                wj.getLabels().retainAll(skippedLabels);
                if (wj.getLabels().isEmpty()) { // TODO or maybe better always raise an exception?
                    throw new JocBadRequestException("None of the requested labels are skipped.");
                }
                break;
            case STOP:
            case UNSTOP:
                throw new JocNotImplementedException(API_CALL + action.name().toLowerCase() + " is not yet implemented.");
            }
        }
    }
    
    private ModifyWorkflowLabels initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, ModifyWorkflowLabels.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflowLabels.class);
    }

    private void command(String controllerId, Action action, WorkflowJobs wj, DBItemJocAuditLog dbAuditLog) {
        switch (action) {
        case SKIP:
        case UNSKIP:
            JControllerCommand commmand = JControllerCommand.controlWorkflowPath(wj.getWorkflowPath(), Optional.empty(), wj.getLabelsForSkipOrUnSkip(
                    Action.SKIP.equals(action)));
            // LOGGER.info(action.name().toLowerCase() + "-command: " + commmand.toJson());
            ControllerApi.of(controllerId).executeCommand(commmand).thenAccept(either -> thenAcceptHandler(either, controllerId, wj, dbAuditLog));
            break;
        case STOP:
        case UNSTOP:
            throw new JocNotImplementedException(API_CALL + action.name().toLowerCase() + " is not yet implemented.");
        }
    }

    private void thenAcceptHandler(Either<Problem, Response> either, String controllerId, WorkflowJobs wj, DBItemJocAuditLog dbAuditLog) {
        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
        if (either.isRight()) {
            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPath(wj.getWorkflowPath(), dbAuditLog, controllerId).thenAccept(either2 -> ProblemHelper
                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
        }
    }
    
    private boolean hasPermission(String controllerId, String accessToken) {
        return getControllerPermissions(controllerId, accessToken).getOrders().getManagePositions();
    }
}