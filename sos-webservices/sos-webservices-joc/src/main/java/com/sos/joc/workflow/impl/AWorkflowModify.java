package com.sos.joc.workflow.impl;

import java.util.Collections;
import java.util.stream.Stream;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.workflow.ModifyWorkflow;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand;
import js7.data_for_java.workflow.JWorkflowId;

public abstract class AWorkflowModify extends JOCResourceImpl {
    
    private static final String API_CALL = "./workflow/";
    
    protected enum Action {
        TRANSITION, TRANSFER, STOP, UNSTOP, SKIP, UNSKIP
    }

    protected <T> T initRequest(Action action, String accessToken, byte[] filterBytes, Class<T> clazz) throws Exception {
        filterBytes = initLogging(getApiCall(action), filterBytes, accessToken, CategoryType.CONTROLLER);
        JsonValidator.validate(filterBytes, clazz);
        return Globals.objectMapper.readValue(filterBytes, clazz);
    }
    
    protected JOCDefaultResponse initPermission(ModifyWorkflow workflow, String accessToken) {
        return initPermission(workflow.getControllerId(), workflow.getWorkflowId().getPath(), accessToken);
    }

    protected JOCDefaultResponse initPermission(String controllerId, String workflow, String accessToken) {
        return initWorkflowPermissions(controllerId, hasPermission(controllerId, accessToken), Collections.singleton(JocInventory.pathToName(
                workflow)));
    }

    private Stream<Boolean> hasPermission(String controllerId, String accessToken) {
        return getControllerPermissions(controllerId, accessToken).map(p -> p.getOrders().getManagePositions());
    }
    
    protected void thenAcceptHandler(Either<Problem, ControllerCommand.Response> either, String controllerId,
            JWorkflowId workflowId, DBItemJocAuditLog dbAuditLog) {
        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
        if (either.isRight()) {
            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPath(workflowId.path(), dbAuditLog, controllerId).thenAccept(either2 -> ProblemHelper
                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
        }
    }
    
    protected static String getApiCall(Action action) {
        return API_CALL + action.name().toLowerCase();
    }
}
