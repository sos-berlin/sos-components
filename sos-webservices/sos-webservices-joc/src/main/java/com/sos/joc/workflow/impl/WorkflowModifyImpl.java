package com.sos.joc.workflow.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.workflow.ModifyWorkflow;
import com.sos.joc.workflow.resource.IWorkflowModify;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.proxy.javaapi.JControllerProxy;

@Path("workflow")
public class WorkflowModifyImpl extends AWorkflowModify implements IWorkflowModify {

    @Override
    public JOCDefaultResponse transition(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflow workflow = initRequest(Action.TRANSITION, accessToken, filterBytes, ModifyWorkflow.class);
            JOCDefaultResponse jocDefaultResponse = initPermission(workflow, accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowModify(Action.TRANSITION, workflow);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse transfer(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflow workflow = initRequest(Action.TRANSFER, accessToken, filterBytes, ModifyWorkflow.class);
            JOCDefaultResponse jocDefaultResponse = initPermission(workflow, accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowModify(Action.TRANSFER, workflow);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private void postWorkflowModify(Action action, ModifyWorkflow workflowFilter) throws Exception {
        String controllerId = workflowFilter.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(workflowFilter.getAuditLog(), controllerId);
        JControllerProxy proxy = Proxy.of(controllerId);
        JControllerState currentState = proxy.currentState();

        String versionId = workflowFilter.getWorkflowId().getVersionId();
        String workflowPath = workflowFilter.getWorkflowId().getPath();
        checkRequiredParameter("versionId", versionId);

        JWorkflowId workflowId = JWorkflowId.of(JocInventory.pathToName(workflowPath), versionId);
        
        Either<Problem, JWorkflow> workflowE = currentState.repo().idToCheckedWorkflow(workflowId);
        ProblemHelper.throwProblemIfExist(workflowE);

        Either<Problem, JWorkflow> curWorkflowE = currentState.repo().pathToCheckedWorkflow(workflowId.path());
        ProblemHelper.throwProblemIfExist(curWorkflowE);

        JWorkflow workflow = workflowE.get();
        if (versionId.equals(curWorkflowE.get().id().versionId().string())) {
            throw new JocBadRequestException("The requested versionId is the current version. Use an older version.");
        }
        checkFolderPermissions(WorkflowPaths.getPath(workflowId.path().string()));
        
        proxy.api().executeCommand(JControllerCommand.transferOrders(workflowId)).thenAccept(either -> thenAcceptHandler(either, controllerId,
                workflow.id(), dbAuditLog));
    }
}