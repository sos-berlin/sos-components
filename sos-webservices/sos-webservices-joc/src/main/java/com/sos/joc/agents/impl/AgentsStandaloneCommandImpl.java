package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsStandaloneCommand;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.DeployAgents;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsStandaloneCommandImpl extends JOCResourceImpl implements IAgentsStandaloneCommand {

    private static final String API_CALL_REVOKE = "./agents/inventory/revoke";
    private static final String API_CALL_ENABLE = "./agents/inventory/enable";
    private static final String API_CALL_DISABLE = "./agents/inventory/disable";

    @Override
    public JOCDefaultResponse postRevoke(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REVOKE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeployAgents.class);
            DeployAgents agentDeployParameter = Globals.objectMapper.readValue(filterBytes, DeployAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String controllerId = agentDeployParameter.getControllerId();
            List<String> agentIds = agentDeployParameter.getAgentIds();

            storeAuditLog(agentDeployParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REVOKE);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIdAndAgentIds(Collections.singleton(controllerId),
                    agentIds, false, false);
            
            if (dbAgents != null) {
                agentIds.removeAll(dbAgents.stream().map(DBItemInventoryAgentInstance::getAgentId).collect(Collectors.toList()));
            }
            if (agentIds.size() > 0) {
                throw new JocBadRequestException(String.format("The Agents %s are not assigned to Controller '%s'", agentIds.toString(),
                        controllerId));
            }
            
            List<JUpdateItemOperation> agentRefs = new ArrayList<>();
            List<String> updateAgentIds = new ArrayList<>();
            
            JControllerProxy proxy = Proxy.of(controllerId);
            JControllerState currentState = proxy.currentState();

            if (dbAgents != null) {
                Map<AgentPath, JAgentRef> knownAgents = currentState.pathToAgentRef();
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    JAgentRef agentRef = knownAgents.get(AgentPath.of(dbAgent.getAgentId()));
                    if (agentRef != null) {
                        agentRefs.add(JUpdateItemOperation.deleteSimple(AgentPath.of(dbAgent.getAgentId())));
                        if (agentRef.director().isPresent()) {
                            agentRefs.add(JUpdateItemOperation.deleteSimple(agentRef.director().get()));
                        }
                        updateAgentIds.add(dbAgent.getAgentId());
                    } else {
                        if (dbAgent.getDeployed()) {
                            dbAgent.setDeployed(false);
                            agentDBLayer.updateAgent(dbAgent);
                        }
                    }
                }
            }

            if (!agentRefs.isEmpty()) {
                proxy.api().updateItems(Flux.fromIterable(agentRefs)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL_REVOKE);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setAgentsDeployed(updateAgentIds, false);
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, updateAgentIds));
                        } catch (Exception e1) {
                            Globals.rollback(connection1);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), controllerId);
                        } finally {
                            Globals.disconnect(connection1);
                        }
                    }
                });
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(connection);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(connection);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    @Override
    public JOCDefaultResponse postEnable(String accessToken, byte[] filterBytes) {
        return disOrEnable(accessToken, filterBytes, false);
    }
    
    @Override
    public JOCDefaultResponse postDisable(String accessToken, byte[] filterBytes) {
        return disOrEnable(accessToken, filterBytes, true);
    }
    
    public JOCDefaultResponse disOrEnable(String accessToken, byte[] filterBytes, boolean disabled) {
        SOSHibernateSession connection = null;
        try {
            String apiCall = disabled ? API_CALL_DISABLE : API_CALL_ENABLE;
            initLogging(apiCall, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeployAgents.class);
            
            DeployAgents agentParameter = Globals.objectMapper.readValue(filterBytes, DeployAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String controllerId = agentParameter.getControllerId();
            storeAuditLog(agentParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(apiCall);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(Collections.singleton(controllerId),
                    agentParameter.getAgentIds(), false, false);
            
            checkAgentsBelongToController(agentParameter, dbAgents);
            
            List<String> dbClusterAgentIds = dbLayer.getClusterAgentIds(Collections.singleton(controllerId), false);
            dbClusterAgentIds.retainAll(agentParameter.getAgentIds());
            if (!dbClusterAgentIds.isEmpty()) {
                throw new JocBadRequestException(String.format("The Agents %s are not standalone", dbClusterAgentIds.toString()));
            }
            
            JControllerProxy proxy = Proxy.of(controllerId);
            Map<AgentPath, JAgentRef> agentsOnController = proxy.currentState().pathToAgentRef();
            Map<SubagentId, JSubagentItem> subagentsOnController = proxy.currentState().idToSubagentItem();
            Map<String, SubagentId> directors = new HashMap<>();
            Set<String> unknownAgents = new HashSet<>();
            for (String agentId : agentParameter.getAgentIds()) {
                JAgentRef aRef = agentsOnController.get(AgentPath.of(agentId));
                if (aRef != null && aRef.director().isPresent()) {
                    directors.put(agentId, aRef.director().get());
                } else {
                    unknownAgents.add(agentId);
                }
            }

            if (!unknownAgents.isEmpty()) {
                boolean isBulk = agentParameter.getAgentIds().stream().distinct().count() > 1L;
                if (isBulk) {
                    ProblemHelper.postExceptionEventAsHintIfExist(Either.left(new ControllerObjectNotExistException("Agents " + unknownAgents
                            .toString() + "not exist or don't have an implicit subagent.")), accessToken, getJocError(), null);
                } else {
                    throw new ControllerObjectNotExistException("Agents " + unknownAgents.toString()
                            + "not exist or don't have an implicit subagent.");
                }
            }
            if (!directors.isEmpty()) {

                final Stream<JUpdateItemOperation> subAgents = directors.values().stream().map(s -> subagentsOnController.get(s)).filter(
                        Objects::nonNull).map(s -> JSubagentItem.of(s.id(), s.agentPath(), s.uri(), disabled)).map(
                                JUpdateItemOperation::addOrChangeSimple);

                proxy.api().updateItems(Flux.fromStream(subAgents)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(apiCall);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setStandaloneAgentsDisabled(directors.keySet().stream().collect(Collectors.toList()), disabled);
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
                        } catch (Exception e1) {
                            Globals.rollback(connection1);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), controllerId);
                        } finally {
                            Globals.disconnect(connection1);
                        }
                    }
                });
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static void checkAgentsBelongToController(DeployAgents agentCommand, List<DBItemInventoryAgentInstance> dbAgents) {
        Set<String> knownDbAgents = dbAgents.stream().map(DBItemInventoryAgentInstance::getAgentId).collect(Collectors.toSet());

        Set<String> unknownAgents = agentCommand.getAgentIds().stream().filter(s -> !knownDbAgents.contains(s)).collect(Collectors.toSet());
        if (!unknownAgents.isEmpty()) {
            throw new JocBadRequestException(String.format("The Agents %s don't belong to the Controller '%s'", unknownAgents.toString(), agentCommand
                    .getControllerId()));
        }
    }
}
