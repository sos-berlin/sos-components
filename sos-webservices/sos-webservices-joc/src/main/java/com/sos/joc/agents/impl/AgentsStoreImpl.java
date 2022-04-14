package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsStore;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.StoreAgents;
import com.sos.joc.model.agent.StoreClusterAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsStoreImpl extends JOCResourceImpl implements IAgentsStore {

    private static final String API_INVENTORY_STORE = "./agents/inventory/store";
    private static final String API_CLUSTER_INVENTORY_STORE = "./agents/inventory/cluster/store";

    @Override
    public JOCDefaultResponse inventoryStore(String accessToken, byte[] filterBytes) {
        return store(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_INVENTORY_STORE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, StoreAgents.class);
            StoreAgents agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = agentStoreParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Map<String, Long> agentIds = agentStoreParameter.getAgents().stream().collect(Collectors.groupingBy(Agent::getAgentId, Collectors
                    .counting()));

            // check uniqueness of AgentId
            agentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(getUniquenessMsg("AgentId", e));
            });

            checkUniquenessOfAgentNames(agentStoreParameter.getAgents());

            // check uniqueness of AgentUrl
            agentStoreParameter.getAgents().stream().collect(Collectors.groupingBy(Agent::getUrl, Collectors.counting())).entrySet().stream().filter(
                    e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(getUniquenessMsg("Agent url", e));
                    });

            // check java name rules of AgentIds
            for (String agentId : agentIds.keySet()) {
                CheckJavaVariableName.test("Agent ID", agentId);
            }

            storeAuditLog(agentStoreParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_INVENTORY_STORE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);

            Map<String, Agent> agentMap = agentStoreParameter.getAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function.identity()));
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(null);
            Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(agentIds.keySet());
            
            if (dbAgents != null && !dbAgents.isEmpty()) {
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    Agent agent = agentMap.remove(dbAgent.getAgentId());
                    if (agent == null) {
                        // throw something?
                        continue;
                    }
                    if (!dbAgent.getControllerId().equals(controllerId)) {
                        throw new JocBadRequestException(String.format("Agent '%s' is already assigned for Controller '%s'", dbAgent.getAgentId(),
                                dbAgent.getControllerId()));
                    }
                    dbAgent.setHidden(agent.getHidden());
                    dbAgent.setAgentName(agent.getAgentName());
                    dbAgent.setTitle(agent.getTitle());
                    
                    if (!dbAgent.getUri().equals(agent.getUrl())) {
                        dbAgent.setDeployed(false);
                    }
                    dbAgent.setUri(agent.getUrl());
                    agentDBLayer.updateAgent(dbAgent);

                    updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
                }
            }

            for (Agent agent : agentMap.values()) {
                DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                dbAgent.setId(null);
                dbAgent.setAgentId(agent.getAgentId());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setControllerId(controllerId);
                dbAgent.setHidden(agent.getHidden());
                dbAgent.setIsWatcher(false);
                dbAgent.setOsId(0L);
                dbAgent.setStartedAt(null);
                dbAgent.setUri(agent.getUrl());
                dbAgent.setVersion(null);
                dbAgent.setTitle(agent.getTitle());
                dbAgent.setDeployed(false);
                agentDBLayer.saveAgent(dbAgent);
                
                updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
            }

            Globals.commit(connection);
            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, agentIds.keySet()));

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
    public JOCDefaultResponse clusterInventoryStore(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CLUSTER_INVENTORY_STORE, filterBytes, accessToken);

            AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, StoreClusterAgents.class);
            StoreClusterAgents agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreClusterAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = agentStoreParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Map<String, Long> agentIds = agentStoreParameter.getClusterAgents().stream().collect(Collectors.groupingBy(ClusterAgent::getAgentId,
                    Collectors.counting()));

            // check uniqueness of AgentId in request
            agentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(getUniquenessMsg("AgentId", e));
            });

            checkUniquenessOfAgentNames(agentStoreParameter.getClusterAgents());

            // check uniqueness of SubagentUrl in request
            Set<SubAgent> requestedSubagents = agentStoreParameter.getClusterAgents().stream().map(ClusterAgent::getSubagents).flatMap(List::stream)
                    .collect(Collectors.toSet());
            requestedSubagents.stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(e -> e
                    .getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(getUniquenessMsg("Subagent url", e));
                    });
            
            Set<String> requestedSubagentIds = requestedSubagents.stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());

            // check java name rules of AgentIds
            for (String agentId : agentIds.keySet()) {
                CheckJavaVariableName.test("Agent ID", agentId);
            }
            for (String subagentId : requestedSubagentIds) {
                CheckJavaVariableName.test("Subagent ID", subagentId);
            }

            storeAuditLog(agentStoreParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CLUSTER_INVENTORY_STORE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            InventorySubagentClustersDBLayer subagentClusterDBLayer = new InventorySubagentClustersDBLayer(connection);

            Map<String, ClusterAgent> agentMap = agentStoreParameter.getClusterAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function
                    .identity()));
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(null);
            Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(agentIds.keySet());
            
            List<DBItemInventorySubAgentInstance> dbSubAgents = agentDBLayer.getSubAgentInstancesByControllerIds(Collections.singleton(
                    controllerId));
            
            // check uniqueness of SubagentUrl with DB
            Set<String> requestedSubagentUrls = requestedSubagents.stream().map(SubAgent::getUrl).collect(Collectors.toSet());
            dbSubAgents.stream().filter(s -> !requestedSubagentIds.contains(s.getSubAgentId())).filter(s -> requestedSubagentUrls.contains(s
                    .getUri())).findAny().ifPresent(s -> {
                        throw new JocBadRequestException(String.format("Subagent url %s is already used by Subagent %s", s.getUri(), s.getSubAgentId()));
                    });
            dbAgents.stream().filter(a -> a.getUri() != null && !a.getUri().isEmpty()).filter(a -> requestedSubagentUrls.contains(a
                  .getUri())).findAny().ifPresent(s -> {
                      throw new JocBadRequestException(String.format("Subagent url %s is already used by Agent %s", s.getUri(), s.getAgentId()));
                  });

            if (dbAgents != null && !dbAgents.isEmpty()) {
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    ClusterAgent agent = agentMap.remove(dbAgent.getAgentId());
                    if (agent == null) {
                        // throw something?
                        continue;
                    }
                    if (!dbAgent.getControllerId().equals(controllerId)) {
                        throw new JocBadRequestException(String.format("Agent '%s' is already assigned for Controller '%s'", dbAgent.getAgentId(),
                                dbAgent.getControllerId()));
                    }
                    dbAgent.setHidden(agent.getHidden());
                    dbAgent.setAgentName(agent.getAgentName());
                    dbAgent.setTitle(agent.getTitle());
                    agentDBLayer.updateAgent(dbAgent);

                    SubAgentStoreImpl.saveOrUpdate(agentDBLayer, subagentClusterDBLayer, dbAgent, dbSubAgents, agent.getSubagents());

                    updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
                }
            }

            for (ClusterAgent agent : agentMap.values()) {
                DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                dbAgent.setId(null);
                dbAgent.setAgentId(agent.getAgentId());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setControllerId(controllerId);
                dbAgent.setHidden(agent.getHidden());
                dbAgent.setIsWatcher(false);
                dbAgent.setOsId(0L);
                dbAgent.setUri(agent.getSubagents().get(0).getUrl());
                dbAgent.setStartedAt(null);
                dbAgent.setVersion(null);
                dbAgent.setTitle(agent.getTitle());
                dbAgent.setDeployed(false);
                dbAgent.setHidden(false);
                agentDBLayer.saveAgent(dbAgent);

                SubAgentStoreImpl.saveOrUpdate(agentDBLayer, subagentClusterDBLayer, dbAgent, dbSubAgents, agent.getSubagents());
                
                updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
            }

            Globals.commit(connection);
            Globals.disconnect(connection);
            connection = null;
            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, agentIds.keySet()));

//            if (!subAgentsToController.isEmpty()) {
//                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(subAgentsToController)).thenAccept(e -> ProblemHelper
//                        .postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
//            }

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

    private static void updateAliases(InventoryAgentInstancesDBLayer agentDBLayer, Agent agent, Collection<DBItemInventoryAgentName> dbAliases)
            throws SOSHibernateException {
        if (dbAliases != null) {
            for (DBItemInventoryAgentName dbAlias : dbAliases) {
                agentDBLayer.getSession().delete(dbAlias);
            }
        }
        // TODO read aliases to provide that per controller aliases hould map unique to agentId
        // Collection<String> a = agentDBLayer.getAgentNamesByAgentIds(Collections.singleton(agent.getAgentId())).values();
        Set<String> aliases = agent.getAgentNameAliases();
        if (aliases != null && !aliases.isEmpty()) {
            aliases.remove(agent.getAgentName());
            for (String name : aliases) {
                DBItemInventoryAgentName a = new DBItemInventoryAgentName();
                a.setAgentId(agent.getAgentId());
                a.setAgentName(name);
                agentDBLayer.getSession().save(a);
            }
        }
    }

    // check uniqueness of AgentName/-aliases
    private static void checkUniquenessOfAgentNames(List<? extends Agent> agents) throws JocBadRequestException {
        agents.stream().map(a -> {
            if (a.getAgentNameAliases() == null) {
                a.setAgentNameAliases(Collections.singleton(a.getAgentName()));
            } else {
                a.getAgentNameAliases().add(a.getAgentName());
            }
            return a.getAgentNameAliases();
        }).flatMap(Set::stream).collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1L)
                .findAny().ifPresent(e -> {
                    throw new JocBadRequestException(getUniquenessMsg("AgentName/-aliase", e));
                });
    }

    private static String getUniquenessMsg(String key, Map.Entry<String, Long> e) {
        return key + " has to be unique: " + e.getKey() + " is used " + (e.getValue() == 2L ? "twice" : e.getValue() + " times");
    }
}
