package com.sos.joc.workflow.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.workflow.ModifyWorkflowPositions;
import com.sos.joc.workflow.resource.IWorkflowPositionsModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand.Response;
import js7.data.workflow.Workflow;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowControl;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;

@Path("workflow")
public class WorkflowPositionsModifyImpl extends JOCResourceImpl implements IWorkflowPositionsModify {

    private static final String API_CALL = "./workflow/";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowPositionsModifyImpl.class);

    private enum Action {
        STOP, UNSTOP
    }

    @Override
    public JOCDefaultResponse stopWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowPositions modifyWorkflow = initRequest(Action.STOP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowInstructionsModify(Action.STOP, modifyWorkflow);
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
            ModifyWorkflowPositions modifyWorkflow = initRequest(Action.UNSTOP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowInstructionsModify(Action.UNSTOP, modifyWorkflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void postWorkflowInstructionsModify(Action action, ModifyWorkflowPositions modifyWorkflow) throws Exception {

        String controllerId = modifyWorkflow.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflow.getAuditLog(), controllerId, CategoryType.CONTROLLER);
        JControllerState currentState = Proxy.of(controllerId).currentState();

        String versionId = modifyWorkflow.getWorkflowId().getVersionId();
        String workflowPath = modifyWorkflow.getWorkflowId().getPath();
        Either<Problem, JWorkflow> workflowE = null;
        if (versionId != null && !versionId.isEmpty()) {
            JWorkflowId wId = JWorkflowId.of(JocInventory.pathToName(workflowPath), versionId);
            workflowE = currentState.repo().idToCheckedWorkflow(wId);
            ProblemHelper.throwProblemIfExist(workflowE);
        } else {
            workflowE = currentState.repo().pathToCheckedWorkflow(WorkflowPath.of(workflowPath));
            ProblemHelper.throwProblemIfExist(workflowE);
        }

        JWorkflow workflow = workflowE.get();
        checkFolderPermissions(WorkflowPaths.getPath(workflow.id().path().string()));
        modifyWorkflow.getPositions().stream().map(pos -> JPosition.fromList(pos)).filter(Either::isLeft).findAny().ifPresent(e -> ProblemHelper
                .throwProblemIfExist(e));
        Set<JPosition> positions = modifyWorkflow.getPositions().stream().map(pos -> JPosition.fromList(pos)).filter(Either::isRight).map(Either::get)
                .collect(Collectors.toSet());
        checkWorkflow(action, workflow, positions, currentState, new ArrayList<>(positions));

        command(controllerId, action, workflow, positions, dbAuditLog);
    }

    private void checkWorkflow(Action action, JWorkflow workflow, Set<JPosition> positions, JControllerState currentState,
            List<JPosition> requestedPositions) {

        Set<JPosition> knownPositions = new HashSet<>();

        Workflow wV = workflow.asScala();
        requestedPositions.forEach(l -> {
            if (wV.checkedPosition(l.asScala()).isRight()) {
                knownPositions.add(l);
            }
        });

        positions.retainAll(knownPositions);
        requestedPositions.removeAll(positions);

        if (!requestedPositions.isEmpty()) {
            if (requestedPositions.size() == 1) {
                throw new ControllerObjectNotExistException("The position '" + requestedPositions.get(0) + "' doesn't exist in workflow '" + workflow
                        .id().path().string() + "'.");
            }
            throw new ControllerObjectNotExistException("The positions " + requestedPositions.toString() + " don't exist in workflow '" + workflow
                    .id().path().string() + "'.");
        }
        
        Optional<JWorkflowControl> controlState = WorkflowsHelper.getWorkflowControl(currentState, workflow.id(), false);
        if (controlState.isPresent()) {
            Set<JPosition> stoppedPositions = WorkflowsHelper.getStoppedPositions(controlState, false);
            
            switch (action) {
            case STOP:
                positions.removeAll(stoppedPositions);
                if (positions.isEmpty()) { // TODO or maybe better always raise an exception?
                    throw new JocBadRequestException("All requested positions are already stopped.");
                }
                break;
            case UNSTOP:
                positions.retainAll(stoppedPositions);
                if (positions.isEmpty()) { // TODO or maybe better always raise an exception?
                    throw new JocBadRequestException("None of the requested positions are stopped.");
                }
                break;
            }
        }
    }

    private ModifyWorkflowPositions initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyWorkflowPositions.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflowPositions.class);
    }

    private void command(String controllerId, Action action, JWorkflow workflow, Set<JPosition> positions, DBItemJocAuditLog dbAuditLog) {
        boolean stop = Action.STOP.equals(action);
        Map<JPosition, Boolean> m = new HashMap<>(positions.size());
        positions.forEach(l -> m.put(l, stop));
        JControllerCommand command = JControllerCommand.controlWorkflow(workflow.id(), m);
        LOGGER.debug("send command: " + command.toJson());
        ControllerApi.of(controllerId).executeCommand(command).thenAccept(either -> thenAcceptHandler(either, controllerId, workflow, dbAuditLog));
    }

    private void thenAcceptHandler(Either<Problem, Response> either, String controllerId, JWorkflow workflow, DBItemJocAuditLog dbAuditLog) {
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