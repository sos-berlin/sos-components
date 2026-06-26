package com.sos.joc.workflow.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.workflow.ModifyWorkflowLabels;
import com.sos.joc.workflow.resource.IWorkflowLabelsModify;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand.Response;
import js7.data.workflow.Workflow;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
import js7.data.workflow.position.Label;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.proxy.javaapi.JControllerProxy;

@Path("workflow")
public class WorkflowLabelsModifyImpl extends AWorkflowModify implements IWorkflowLabelsModify {

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
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.SKIP, accessToken, filterBytes, ModifyWorkflowLabels.class);
            JOCDefaultResponse jocDefaultResponse = initPermission(modifyWorkflow.getControllerId(), modifyWorkflow.getWorkflowPath(), accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.SKIP, modifyWorkflow);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse unskipWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.UNSKIP, accessToken, filterBytes, ModifyWorkflowLabels.class);
            JOCDefaultResponse jocDefaultResponse = initPermission(modifyWorkflow.getControllerId(), modifyWorkflow.getWorkflowPath(), accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.UNSKIP, modifyWorkflow);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private void postWorkflowJobsModify(Action action, ModifyWorkflowLabels modifyWorkflow) throws Exception {

        String controllerId = modifyWorkflow.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflow.getAuditLog(), controllerId);
        JControllerProxy proxy = Proxy.of(controllerId);
        JControllerState currentState = proxy.currentState();

        WorkflowJobs wj = new WorkflowJobs(modifyWorkflow);
        checkFolderPermissions(WorkflowPaths.getPath(wj.getPath()));
        
        Either<Problem, JWorkflow> workflowV = currentState.repo().pathToCheckedWorkflow(wj.getWorkflowPath());
        if (workflowV == null || workflowV.isLeft()) {
            throw new ControllerObjectNotExistException("Couldn't find the the Workflow '" + wj.getPath() + "'.");
        }

        JWorkflow jWorkflow = workflowV.get();
        
        checkWorkflow(action, controllerId, wj, currentState, jWorkflow, new ArrayList<>(modifyWorkflow.getLabels()));
        command(controllerId, action, wj, dbAuditLog);
    }
    
    private static Optional<NamedJob> checkWorkflow(Action action, String controllerId, WorkflowJobs wj, JControllerState currentState,
            JWorkflow jWorkflow, List<String> requestedLabels) throws IOException {

        Set<String> knownLabels = new HashSet<>();
        Workflow wV = jWorkflow.asScala();
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
            // get Workflow from database and check if label is known in the inventory
            // if yes then error message with re-deploy info
            Set<String> labelsFromInv = getLabelsFromInventory(action, controllerId, wj.getPath());
            labelsFromInv.retainAll(requestedLabels);
            if (!labelsFromInv.isEmpty()) {
                if (labelsFromInv.size() == 1) {
                    throw new ControllerObjectNotExistException("The label '" + labelsFromInv.iterator().next()
                            + "' is known in the inventory of the workflow '" + wj.getPath()
                            + "' but couldn't find at the Controller. Please re-deploy the workflow!");
                }
                throw new ControllerObjectNotExistException("The labels " + labelsFromInv.toString() + " are known in the inventory of the workflow '"
                        + wj.getPath() + "' but couldn't find at the Controller. Please re-deploy the workflow!");
            }
            if (requestedLabels.size() == 1) {
                throw new ControllerObjectNotExistException("Couldn't find an instruction with the label '" + requestedLabels.get(0)
                        + "' in the Workflow '" + wj.getPath() + "'.");
            }
            throw new ControllerObjectNotExistException("Couldn't find instructions with the labels " + requestedLabels.toString()
                    + " in the workflow '" + wj.getPath() + "'.");
        }

        Optional<NamedJob> nj = Optional.empty();
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
//                JsonNode node = Globals.objectMapper.readTree(jWorkflow.toJson());
//                Instruction firstInstruction = Globals.objectMapper.reader().forType(new TypeReference<Instruction>() {}).readValue(node.get("instructions").get(0));
//                nj = WorkflowsHelper.getFirstJob(firstInstruction, skippedLabels);
                break;
            default:
                break;
            }
        }
        
        return nj;
    }
    
    private static Set<String> getLabelsFromInventory(Action action, String controllerId, String workflowName) throws JsonMappingException,
            JsonProcessingException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(getApiCall(action));
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            DeployedContent lastContent = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowName);
            if (lastContent != null && lastContent.getContent() != null && !lastContent.getContent().isEmpty()) {
                com.sos.inventory.model.workflow.Workflow workflow = Globals.objectMapper.readValue(lastContent.getContent(),
                        com.sos.inventory.model.workflow.Workflow.class);
                return WorkflowsHelper.getLabelToPositionsMap(workflow).keySet();
            }
        } finally {
            Globals.disconnect(connection);
        }
        return Collections.emptySet();
    }

    private CompletableFuture<Boolean> command(String controllerId, Action action, WorkflowJobs wj, DBItemJocAuditLog dbAuditLog) {
        JControllerCommand commmand = JControllerCommand.controlWorkflowPath(wj.getWorkflowPath(), Optional.empty(), wj.getLabelsForSkipOrUnSkip(
                Action.SKIP.equals(action)));
        // LOGGER.info(action.name().toLowerCase() + "-command: " + commmand.toJson());
        return ControllerApi.of(controllerId).executeCommand(commmand).thenApply(either -> thenAcceptHandler(either, controllerId, wj, dbAuditLog));
    }

    private boolean thenAcceptHandler(Either<Problem, Response> either, String controllerId, WorkflowJobs wj, DBItemJocAuditLog dbAuditLog) {
        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
        if (either.isRight()) {
            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPaths(Collections.singleton(wj.getWorkflowPath()), dbAuditLog.getId(), controllerId)
                    .thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
            return true;
        }
        return false;
    }
}