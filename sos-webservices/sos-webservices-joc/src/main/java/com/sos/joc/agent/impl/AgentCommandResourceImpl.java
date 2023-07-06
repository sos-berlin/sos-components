package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.cluster.ClusterState;
import com.sos.controller.model.cluster.ClusterType;
import com.sos.joc.Globals;
import com.sos.joc.agent.resource.IAgentCommandResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentClusterWatch;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentCommand;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.data.agent.AgentPath;
import js7.data.controller.ControllerCommand;
import js7.data.node.NodeId;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.agent.JAgentRefState;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agent")
public class AgentCommandResourceImpl extends JOCResourceImpl implements IAgentCommandResource {

    private static final String API_CALL_RESET = "./agent/reset";
    private static final String API_CALL_REMOVE = "./agent/delete";
    private static final String API_CALL_SWITCHOVER = "./agent/cluster/switchover";
    private static final String API_CALL_CONFIRMNODELOSS = "./agent/cluster/confirm_node_loss";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentCommandResourceImpl.class);

    @Override
    public JOCDefaultResponse reset(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RESET, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AgentCommand.class);
            AgentCommand agentCommand = Globals.objectMapper.readValue(filterBytes, AgentCommand.class);
            
            String controllerId = agentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(agentCommand.getAuditLog(), agentCommand.getControllerId(), CategoryType.CONTROLLER);
            JControllerCommand resetAgentCommand = JControllerCommand.apply(getResetCommand(agentCommand.getAgentId(), agentCommand
                    .getForce() == Boolean.TRUE));
            LOGGER.debug("Reset Agent: " + resetAgentCommand.toJson());
            ControllerApi.of(controllerId).executeCommand(resetAgentCommand).thenAccept(e -> {
                ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
            });

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    public static ControllerCommand.ResetAgent getResetCommand(String agentId, boolean force) {
        return new ControllerCommand.ResetAgent(AgentPath.of(agentId), force);
    }
    
    @Override
    public JOCDefaultResponse delete(String accessToken, byte[] filterBytes) {
        return remove(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse remove(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_REMOVE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AgentCommand.class);
            AgentCommand agentCommand = Globals.objectMapper.readValue(filterBytes, AgentCommand.class);
            
            String controllerId = agentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(agentCommand.getAuditLog(), agentCommand.getControllerId(), CategoryType.CONTROLLER);
            
            String agentId = agentCommand.getAgentId();
            JControllerProxy proxy = Proxy.of(controllerId);
            JControllerState currentState = proxy.currentState();
            
            Map<SubagentId, JSubagentItem> subAgentsOnController = currentState.idToSubagentItem();

            Set<JUpdateItemOperation> subAgentIdsOnController = subAgentsOnController.values().stream().filter(s -> s.agentPath().string().equals(
                    agentId)).map(JSubagentItem::id).map(JUpdateItemOperation::deleteSimple).collect(Collectors.toSet());

            // add agent
            JAgentRef agentOnController = currentState.pathToAgentRef().get(AgentPath.of(agentId));
            if (agentOnController != null) {
                subAgentIdsOnController.add(JUpdateItemOperation.deleteSimple(agentOnController.path()));
            }
            // TODO consider to delete selection
            if (!subAgentIdsOnController.isEmpty()) {
                proxy.api().updateItems(Flux.fromIterable(subAgentIdsOnController)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        deleteAgentInstance(agentId, accessToken, getJocError(), controllerId);
                    }
                });
            } else {
                deleteAgentInstance(agentId, accessToken, getJocError(), controllerId);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse switchover(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_SWITCHOVER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AgentCommand.class);
            AgentCommand agentCommand = Globals.objectMapper.readValue(filterBytes, AgentCommand.class);
            
            String controllerId = agentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerId, accessToken).getSwitchOver());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(agentCommand.getAuditLog(), agentCommand.getControllerId(), CategoryType.CONTROLLER);
            
            String agentId = agentCommand.getAgentId();
            JAgentRefState agentState = Proxy.of(controllerId).currentState().pathToAgentRefState().get(AgentPath.of(agentId));
            
            if (agentState != null) {
                ClusterState clusterState = Globals.objectMapper.readValue(agentState.clusterState().toJson(), ClusterState.class);
                if (clusterState == null || ClusterType.EMPTY.equals(clusterState.getTYPE())) {
                    throw new JocBadRequestException("There is no Agent director cluster with the Id: " + agentId);
                } else if (!ClusterType.COUPLED.equals(clusterState.getTYPE())) {
                    throw new JocBadRequestException("Switchover is not available because the Agent director cluster is not coupled");
                }
                ControllerApi.of(controllerId).executeCommand(JControllerCommand.clusterSwitchover(Optional.of(AgentPath.of(agentId)))).thenAccept(
                        e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId));
            } else {
                throw new JocBadRequestException("An Agent '" + agentId + "' is not deployed at the Controller '" + controllerId + "'");
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse confirmNodeLoss(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_CONFIRMNODELOSS, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AgentCommand.class);
            AgentCommand agentCommand = Globals.objectMapper.readValue(filterBytes, AgentCommand.class);
            
            String controllerId = agentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerId, accessToken).getSwitchOver());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            //checkRequiredParameter("lostNodeId", agentCommand.getLostDirector());
            //String lossNodeIdFromRequest = AgentCommand.LostDirector.PRIMARY_DIRECTOR.equals(agentCommand.getLostDirector()) ? "Primary" : "Backup";
            
            storeAuditLog(agentCommand.getAuditLog(), agentCommand.getControllerId(), CategoryType.CONTROLLER);
            
            String agentId = agentCommand.getAgentId();
            JControllerProxy proxy = Proxy.of(controllerId);
            AgentPath agentPath = AgentPath.of(agentId);
            JAgentRefState agentState = proxy.currentState().pathToAgentRefState().get(agentPath);
            
            if (agentState != null) {
                ClusterState clusterState = Globals.objectMapper.readValue(agentState.clusterState().toJson(), ClusterState.class);
                if (clusterState == null || ClusterType.EMPTY.equals(clusterState.getTYPE())) {
                    throw new JocBadRequestException("There is no Agent director cluster with the Id: " + agentId);
                }
                NodeId nodeId = AgentClusterWatch.getLostNodeId(controllerId, agentPath, agentState).orElseThrow(() -> new JocBadRequestException(
                        "The Agent cluster '" + agentId + "' doesn't lost a director node"));
                String user = null;
                try {
                    user = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
                } catch (Exception e) {
                    user = Globals.getJocId();
                }
                proxy.api().executeCommand(JControllerCommand.confirmClusterNodeLoss(AgentPath.of(agentId), nodeId,
                        user)).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId));
            } else {
                throw new JocBadRequestException("An Agent '" + agentId + "' is not deployed at the Controller '" + controllerId + "'");
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static void deleteAgentInstance(String agentId, String accessToken, JocError jocError, String controllerId) {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            dbLayer.deleteInstance(dbLayer.getAgentInstance(agentId));
            Globals.commit(connection);
            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, agentId));
        } catch (Exception e1) {
            Globals.rollback(connection);
            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, jocError, null);
        } finally {
            Globals.disconnect(connection);
        }
    }
}
