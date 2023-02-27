package com.sos.joc.workflow.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.joc.DBItemJocAuditLog;
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
import js7.data.workflow.WorkflowPath;
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
        SOSHibernateSession connection = null;
        try {
            String controllerId = workflowFilter.getControllerId();
            DBItemJocAuditLog dbAuditLog = storeAuditLog(workflowFilter.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            JControllerProxy proxy = Proxy.of(controllerId);
            JControllerState currentState = proxy.currentState();

            String versionId = workflowFilter.getWorkflowId().getVersionId();
            String workflowPath = workflowFilter.getWorkflowId().getPath();
            checkRequiredParameter("versionId", versionId);
            
            String workflowName = JocInventory.pathToName(workflowPath);
            
            JWorkflowId wId = JWorkflowId.of(workflowName, versionId);
            Either<Problem, JWorkflow> workflowE = currentState.repo().idToCheckedWorkflow(wId);
            ProblemHelper.throwProblemIfExist(workflowE);
            
            Either<Problem, JWorkflow> curWorkflowE = currentState.repo().pathToCheckedWorkflow(WorkflowPath.of(workflowName));
            ProblemHelper.throwProblemIfExist(curWorkflowE);

            JWorkflow workflow = workflowE.get();
            JWorkflow curWorkflow = curWorkflowE.get();
            if (workflow.id().versionId().string().equals(curWorkflow.id().versionId().string())) {
                throw new JocBadRequestException("The requested versionId is the current version. Use an older version.");
            }
            checkFolderPermissions(WorkflowPaths.getPath(workflow.id().path().string()));
            
            String json = String.format("{\"TYPE\":\"TransferOrders\",\"workflowId\":{\"path\": \"%s\",\"versionId\": \"%s\"}}", workflow.id().path()
                    .string(), workflow.id().versionId().string());
            LOGGER.debug("send command: " + json);
            proxy.api().executeCommandJson(json).thenAccept(either -> thenAcceptHandler(either, controllerId, workflow, dbAuditLog));

        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private ModifyWorkflow initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyWorkflow.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflow.class);
    }

    private void thenAcceptHandler(Either<Problem, String> either, String controllerId, JWorkflow workflow, DBItemJocAuditLog dbAuditLog) {
        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
        if (either.isRight()) {
            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPath(workflow.id().path(), dbAuditLog, controllerId).thenAccept(either2 -> ProblemHelper
                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
        }
    }

    private boolean hasPermission(String controllerId, String accessToken) {
        return getControllerPermissions(controllerId, accessToken).getOrders().getManagePositions();
    }
}