package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
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
import com.sos.joc.agent.impl.SubAgentStoreResourceImpl;
import com.sos.joc.agents.resource.IAgentsResourceStore;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingLicenseException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.InventoryClusterAgent;
import com.sos.joc.model.agent.StoreAgents;
import com.sos.joc.model.agent.StoreInventoryClusterAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentRef;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsResourceStoreImpl extends JOCResourceImpl implements IAgentsResourceStore {

    private static final String API_STORE = "./agents/store";
    private static final String API_CLUSTER_STORE = "./agents/cluster/store";
    private static final String API_CLUSTER_INVENTORY_STORE = "./agents/inventory/cluster/store";

    @Override
    public JOCDefaultResponse inventoryStore(String accessToken, byte[] filterBytes) {
        return store(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_STORE, filterBytes, accessToken);
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

            connection = Globals.createSosHibernateStatelessConnection(API_STORE);
            connection.setAutoCommit(false);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);

            Map<String, Agent> agentMap = agentStoreParameter.getAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function.identity()));
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(null);
            Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(agentIds.keySet());
            List<JUpdateItemOperation> agentRefs = new ArrayList<>();
            
            JControllerProxy proxy = Proxy.of(controllerId);
            JControllerState currentState = proxy.currentState();

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
                    boolean controllerUpdateRequired = true; // false;
                    boolean dbUpdateRequired = false;
                    if (dbAgent.getDisabled() != agent.getDisabled()) {
                        dbAgent.setDisabled(agent.getDisabled());
                        dbUpdateRequired = true;
                        if (agent.getDisabled()) {
                            controllerUpdateRequired = false;
                        }
                    }
                    if (!dbAgent.getAgentName().equals(agent.getAgentName())) {
                        dbAgent.setAgentName(agent.getAgentName());
                        dbUpdateRequired = true;
                    }
                    if (!dbAgent.getUri().equals(agent.getUrl())) {
                        dbAgent.setUri(agent.getUrl());
                        dbUpdateRequired = true;
                        // controllerUpdateRequired = true;
                    }
                    if (dbUpdateRequired) {
                        agentDBLayer.updateAgent(dbAgent);
                    }
                    if (controllerUpdateRequired) {
                        // TODO consider old Agents
                        Either<Problem, JAgentRef> agentE = currentState.pathToAgentRef(AgentPath.of(dbAgent.getAgentId()));
                        if (agentE.isRight() && (!agentE.get().director().isPresent() || agentE.get().directors().isEmpty())) {
                            agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createOldAgent(dbAgent)));
                        } else {
                            agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createNewAgent(dbAgent)));
                            agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createSubagentDirector(dbAgent)));
                        }
                    }

                    updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
                }
            }

            for (Agent agent : agentMap.values()) {
                boolean controllerUpdateRequired = true;
                DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                dbAgent.setId(null);
                dbAgent.setAgentId(agent.getAgentId());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setControllerId(controllerId);
                dbAgent.setDisabled(agent.getDisabled());
                if (agent.getDisabled()) {
                    controllerUpdateRequired = false;
                }
                dbAgent.setIsWatcher(false);
                dbAgent.setOsId(0L);
                dbAgent.setStartedAt(null);
                dbAgent.setUri(agent.getUrl());
                dbAgent.setVersion(null);
                dbAgent.setTitle(null); // TODO
                // dbAgent.setDeployed(null); // TODO
                agentDBLayer.saveAgent(dbAgent);

                if (controllerUpdateRequired) {
                    // TODO consider old Agents
                    Either<Problem, JAgentRef> agentE = currentState.pathToAgentRef(AgentPath.of(dbAgent.getAgentId()));
                    if (agentE.isRight() && (!agentE.get().director().isPresent() || agentE.get().directors().isEmpty())) {
                        agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createOldAgent(dbAgent)));
                    } else {
                        agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createNewAgent(dbAgent)));
                        agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createSubagentDirector(dbAgent)));
                    }
                }

                updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
            }

            Globals.commit(connection);

            if (!agentRefs.isEmpty()) {
                proxy.api().updateItems(Flux.fromIterable(agentRefs)).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e,
                        getAccessToken(), getJocError(), controllerId));
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
    
    private static JAgentRef createOldAgent(DBItemInventoryAgentInstance a) {
        return JAgentRef.of(AgentPath.of(a.getAgentId()), Uri.of(a.getUri()));
    }
    
    private static JAgentRef createNewAgent(DBItemInventoryAgentInstance a) {
        return JAgentRef.of(AgentPath.of(a.getAgentId()), SubagentId.of((a.getAgentId())));
    }
    
    private static JSubagentRef createSubagentDirector(DBItemInventoryAgentInstance a) {
        return JSubagentRef.of(SubagentId.of(a.getAgentId()), AgentPath.of(a.getAgentId()), Uri.of(a.getUri()));
    }
    
    @Override
    public JOCDefaultResponse clusterInventoryStore(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CLUSTER_INVENTORY_STORE, filterBytes, accessToken);

            if (JocClusterService.getInstance().getCluster() == null || !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
                throw new JocMissingLicenseException("missing license for Agent cluster");
            }

            JsonValidator.validateFailFast(filterBytes, StoreInventoryClusterAgents.class);
            StoreInventoryClusterAgents agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreInventoryClusterAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = agentStoreParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Map<String, Long> agentIds = agentStoreParameter.getClusterAgents().stream().collect(Collectors.groupingBy(InventoryClusterAgent::getAgentId,
                    Collectors.counting()));

            // check uniqueness of AgentId
            agentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(getUniquenessMsg("AgentId", e));
            });

            checkUniquenessOfAgentNames(agentStoreParameter.getClusterAgents());

            // check uniqueness of SubagentUrl
            agentStoreParameter.getClusterAgents().stream().map(InventoryClusterAgent::getSubagents).flatMap(List::stream).collect(Collectors.groupingBy(
                    SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(getUniquenessMsg("Subagent url", e));
                    });

            // check java name rules of AgentIds
            for (String agentId : agentIds.keySet()) {
                CheckJavaVariableName.test("Agent ID", agentId);
            }

            storeAuditLog(agentStoreParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CLUSTER_INVENTORY_STORE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);

            Map<String, InventoryClusterAgent> agentMap = agentStoreParameter.getClusterAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function
                    .identity()));
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(null);
            Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(agentIds.keySet());
            //List<JUpdateItemOperation> subAgentsToController = new ArrayList<>();

            if (dbAgents != null && !dbAgents.isEmpty()) {
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    InventoryClusterAgent agent = agentMap.remove(dbAgent.getAgentId());
                    if (agent == null) {
                        // throw something?
                        continue;
                    }
                    if (!dbAgent.getControllerId().equals(controllerId)) {
                        throw new JocBadRequestException(String.format("Agent '%s' is already assigned for Controller '%s'", dbAgent.getAgentId(),
                                dbAgent.getControllerId()));
                    }
                    boolean dbUpdateRequired = false;
                    if (dbAgent.getDisabled() != agent.getDisabled()) {
                        dbAgent.setDisabled(agent.getDisabled());
                        dbUpdateRequired = true;
                    }
                    if (!dbAgent.getAgentName().equals(agent.getAgentName())) {
                        dbAgent.setAgentName(agent.getAgentName());
                        dbUpdateRequired = true;
                    }
                    if (dbUpdateRequired) {
                        agentDBLayer.updateAgent(dbAgent);
                    }

                    List<DBItemInventorySubAgentInstance> dbSubAgents = agentDBLayer.getSubAgentInstancesByControllerIds(Collections.singleton(
                            controllerId));
//                    subAgentsToController.addAll(SubAgentStoreResourceImpl.saveOrUpdate(agentDBLayer, dbAgent, dbSubAgents, agent.getSubagents()));
                    SubAgentStoreResourceImpl.saveOrUpdate(agentDBLayer, dbAgent, dbSubAgents, agent.getSubagents());
                    
                    updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
                }
            }

            for (InventoryClusterAgent agent : agentMap.values()) {
                DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                dbAgent.setId(null);
                dbAgent.setAgentId(agent.getAgentId());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setControllerId(controllerId);
                dbAgent.setDisabled(agent.getDisabled());
                dbAgent.setIsWatcher(false);
                dbAgent.setOsId(0L);
                dbAgent.setUri(agent.getSubagents().get(0).getUrl());
                dbAgent.setStartedAt(null);
                dbAgent.setVersion(null);
                dbAgent.setTitle(null); // TODO
                // dbAgent.setDeployed(null); // TODO
                agentDBLayer.saveAgent(dbAgent);

                List<DBItemInventorySubAgentInstance> dbSubAgents = agentDBLayer.getSubAgentInstancesByControllerIds(Collections.singleton(
                        controllerId));
//                subAgentsToController.addAll(SubAgentStoreResourceImpl.saveOrUpdate(agentDBLayer, dbAgent, dbSubAgents, agent.getSubagents()));
                SubAgentStoreResourceImpl.saveOrUpdate(agentDBLayer, dbAgent, dbSubAgents, agent.getSubagents());
                
                updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
            }

            Globals.commit(connection);
            Globals.disconnect(connection);
            connection = null;

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

    public static void updateAliases(InventoryAgentInstancesDBLayer agentDBLayer, Agent agent, Collection<DBItemInventoryAgentName> dbAliases)
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
