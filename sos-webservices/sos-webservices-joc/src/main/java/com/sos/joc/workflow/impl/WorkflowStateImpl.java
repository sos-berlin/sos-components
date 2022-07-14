package com.sos.joc.workflow.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.model.workflow.WorkflowState;
import com.sos.joc.workflow.resource.IWorkflowState;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.agent.AgentPath;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import scala.collection.JavaConverters;

@Path("workflow")
public class WorkflowStateImpl extends JOCResourceImpl implements IWorkflowState {

    private static final String API_CALL = "./workflow/status";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowStateImpl.class);
    
    @Override
    public JOCDefaultResponse postStatus(String accessToken, byte[] filterBytes) {
        return postState(accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postState(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            String controllerId = workflowFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getWorkflows()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String workflowPath = workflowFilter.getWorkflowId().getPath();
            String versionId = workflowFilter.getWorkflowId().getVersionId();
            
            WorkflowState entity = new WorkflowState();
            entity.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = getCurrentState(controllerId);
            SyncStateText stateText = SyncStateText.UNKNOWN;
            if (currentstate != null) {
                entity.setSurveyDate(Date.from(currentstate.instant()));
                
                Either<Problem, JWorkflow> workflowE = null;
                WorkflowPath wPath = null;
                if (versionId != null && !versionId.isEmpty()) {
                    JWorkflowId wId = JWorkflowId.of(JocInventory.pathToName(workflowPath), versionId);
                    wPath = wId.path();
                    workflowE = currentstate.repo().idToCheckedWorkflow(wId);
                } else {
                    wPath = WorkflowPath.of(JocInventory.pathToName(workflowPath));
                    workflowE = currentstate.repo().pathToCheckedWorkflow(wPath); 
                }
                
                stateText = SyncStateText.NOT_IN_SYNC;
                if (workflowE != null && workflowE.isRight()) {
                    
                    Optional<WorkflowPathControl> controlState = WorkflowsHelper.getWorkflowPathControl(currentstate, wPath, false);
                    stateText = SyncStateText.IN_SYNC;
                    if (controlState.isPresent()) {
                        Set<AgentPath> agentsThatIgnoreCommand = currentstate.workflowPathControlToIgnorantAgent().getOrDefault(wPath, Collections
                                .emptySet());
                        Set<AgentPath> allAgents = JavaConverters.asJava(workflowE.get().asScala().referencedAgentPaths());
                        
                        if (!agentsThatIgnoreCommand.isEmpty()) {
                            stateText = SyncStateText.OUTSTANDING;
                        } else if (controlState.get().suspended()) {
                            stateText = SyncStateText.SUSPENDED;
                        }
                        
                        agentsThatIgnoreCommand.forEach(a -> allAgents.remove(a));
                        Map<String, String> idNameMap = getAgentIdNameMap(controllerId);

                        entity.setConfirmedAgentNames(allAgents.stream().map(AgentPath::string).map(a -> idNameMap.getOrDefault(a, a)).collect(
                                Collectors.toSet()));
                        entity.setNotConfirmedAgentNames(agentsThatIgnoreCommand.stream().map(AgentPath::string).map(a -> idNameMap
                                .getOrDefault(a, a)).collect(Collectors.toSet()));
                    }
                }
            }
            
            
            entity.setState(SyncStateHelper.getState(stateText));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static Map<String, String> getAgentIdNameMap(String controllerId) {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            return dbLayer.getAgentIdNameMap(controllerId);
        } catch (Exception e) {
            LOGGER.warn(e.toString());
            return Collections.emptyMap();
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }
}
