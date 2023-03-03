package com.sos.joc.workflow.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.workflow.ModifyWorkflow;
import com.sos.joc.workflow.resource.IWorkflowModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.proxy.javaapi.JControllerProxy;

@Path("workflow")
public class WorkflowModifyImpl extends JOCResourceImpl implements IWorkflowModify {

    private static final String API_CALL = "./workflow/";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowModifyImpl.class);

    private enum Action {
        TRANSITION, TRANFER
    }

    @Override
    public JOCDefaultResponse transition(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflow workflow = initRequest(Action.TRANSITION, accessToken, filterBytes);
            boolean perm = hasPermission(workflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowModify(Action.TRANSITION, workflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse transfer(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflow workflow = initRequest(Action.TRANFER, accessToken, filterBytes);
            boolean perm = hasPermission(workflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowModify(Action.TRANFER, workflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void postWorkflowModify(Action action, ModifyWorkflow workflowFilter) throws Exception {
        String controllerId = workflowFilter.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(workflowFilter.getAuditLog(), controllerId, CategoryType.CONTROLLER);
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

        String json = getCommandJson(workflowId);
        LOGGER.debug("send command: " + json);
        proxy.api().executeCommandJson(json).thenAccept(either -> thenAcceptHandler(either, proxy, controllerId, workflow.id(), dbAuditLog));
    }
    
    private static String getCommandJson(JWorkflowId workflowId) {
        return String.format("{\"TYPE\":\"TransferOrders\",\"workflowId\":{\"path\":\"%s\",\"versionId\":\"%s\"}}", workflowId.path()
                .string(), workflowId.versionId().string());
    }
    
    private ModifyWorkflow initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyWorkflow.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflow.class);
    }

    private void thenAcceptHandler(Either<Problem, String> either, JControllerProxy proxy, String controllerId, JWorkflowId workflowId,
            DBItemJocAuditLog dbAuditLog) {
        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
        if (either.isRight()) {
            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPath(workflowId.path(), dbAuditLog, controllerId).thenAccept(either2 -> ProblemHelper
                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
        }
    }

    private boolean hasPermission(String controllerId, String accessToken) {
        return getControllerPermissions(controllerId, accessToken).getOrders().getManagePositions();
    }
}