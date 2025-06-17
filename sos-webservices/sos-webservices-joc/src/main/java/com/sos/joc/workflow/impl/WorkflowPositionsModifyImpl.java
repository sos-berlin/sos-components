package com.sos.joc.workflow.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.workflow.ModifyWorkflowPositions;
import com.sos.joc.workflow.resource.IWorkflowPositionsModify;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
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
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), hasPermission(modifyWorkflow.getControllerId(),
                    accessToken));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowInstructionsModify(Action.STOP, modifyWorkflow);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse unstopWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowPositions modifyWorkflow = initRequest(Action.UNSTOP, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), hasPermission(modifyWorkflow.getControllerId(),
                    accessToken));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowInstructionsModify(Action.UNSTOP, modifyWorkflow);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private void postWorkflowInstructionsModify(Action action, ModifyWorkflowPositions modifyWorkflow) throws Exception {
        SOSHibernateSession connection = null;
        try {
            String controllerId = modifyWorkflow.getControllerId();
            DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflow.getAuditLog(), controllerId);
            JControllerState currentState = Proxy.of(controllerId).currentState();

            String versionId = modifyWorkflow.getWorkflowId().getVersionId();
            String workflowPath = modifyWorkflow.getWorkflowId().getPath();
            Either<Problem, JWorkflow> workflowE = null;
            if (versionId != null && !versionId.isEmpty()) {
                JWorkflowId wId = JWorkflowId.of(JocInventory.pathToName(workflowPath), versionId);
                workflowE = currentState.repo().idToCheckedWorkflow(wId);
                ProblemHelper.throwProblemIfExist(workflowE);
            } else {
                workflowE = currentState.repo().pathToCheckedWorkflow(WorkflowPath.of(JocInventory.pathToName(workflowPath)));
                ProblemHelper.throwProblemIfExist(workflowE);
            }

            JWorkflow workflow = workflowE.get();
            checkFolderPermissions(WorkflowPaths.getPath(workflow.id().path().string()));

            // TODO JOC-1453 consider labels
            if (modifyWorkflow.getPositions() == null) {
                modifyWorkflow.setPositions(Collections.emptyList());
            }
            Map<String, List<Object>> labelMap = Collections.emptyMap();
            if (modifyWorkflow.getPositions().stream().anyMatch(pos -> pos instanceof String)) {
                // throw new JocNotImplementedException("The use of labels as positions is not yet implemented");

                connection = Globals.createSosHibernateStatelessConnection(API_CALL + action.name().toLowerCase());
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                DeployedContent dbWorkflow = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowPath);
                Globals.disconnect(connection);
                connection = null;
                if (dbWorkflow != null) {
                    com.sos.inventory.model.workflow.Workflow w = JocInventory.workflowContent2Workflow(dbWorkflow.getContent());
                    if (w != null) {
                        labelMap = WorkflowsHelper.getLabelToPositionsMap(w);
                    }
                }
            }

            Set<Either<Problem, JPosition>> jPosEithers = getJPositions(modifyWorkflow.getPositions(), labelMap);

            jPosEithers.stream().filter(Either::isLeft).findAny().ifPresent(e -> ProblemHelper.throwProblemIfExist(e));
            Set<JPosition> positions = jPosEithers.stream().filter(Either::isRight).map(Either::get).collect(Collectors.toSet());
            checkWorkflow(action, workflow, positions, currentState, new ArrayList<>(positions));

            command(controllerId, action, workflow, positions, dbAuditLog);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private Set<Either<Problem, JPosition>> getJPositions(List<Object> poss, Map<String, List<Object>> labelMap) {
        return poss.stream().map(ep -> OrdersHelper.getPosition(ep, labelMap)).filter(Objects::nonNull).map(pos -> JPosition.fromList(pos)).collect(
                Collectors.toSet());
    }

    private void checkWorkflow(Action action, JWorkflow workflow, Set<JPosition> positions, JControllerState currentState,
            List<JPosition> requestedPositions) {

        Set<JPosition> knownPositions = new HashSet<>();

        Workflow wV = workflow.asScala();
        requestedPositions.forEach(l -> {
            if (wV.checkPosition(l.asScala()).isRight()) {
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

    private ModifyWorkflowPositions initRequest(Action action, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken, CategoryType.CONTROLLER);
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

    private Stream<Boolean> hasPermission(String controllerId, String accessToken) {
        return getControllerPermissions(controllerId, accessToken).map(p -> p.getOrders().getManagePositions());
    }
}