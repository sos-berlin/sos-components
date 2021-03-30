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
import com.sos.joc.agents.resource.IAgentsResourceStore;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.ModifyAgentClusterAudit;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.StoreAgents;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.agent.AgentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsResourceStoreImpl extends JOCResourceImpl implements IAgentsResourceStore {

    private static String API_CALL = "./agents/store";

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, StoreAgents.class);
            StoreAgents agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = agentStoreParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Map<String, Long> agentIds = agentStoreParameter.getAgents().stream().collect(Collectors.groupingBy(Agent::getAgentId, Collectors.counting()));
            
            // check uniqueness of AgentId
            agentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(getUniquenessMsg("AgentId", e));
            });
            
            
            // check uniqueness of AgentName/-aliases
            agentStoreParameter.getAgents().stream().map(a -> {
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
            
            // check uniqueness of AgentUrl
            agentStoreParameter.getAgents().stream().collect(Collectors.groupingBy(Agent::getUrl, Collectors.counting())).entrySet().stream().filter(
                    e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(getUniquenessMsg("Agent url", e));
                    });

            // check java name rules of AgentIds
            for (String agentId : agentIds.keySet()) {
                CheckJavaVariableName.test("Agent ID", agentId);
            }

            checkRequiredComment(agentStoreParameter.getAuditLog());

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);

            Map<String, Agent> agentMap = agentStoreParameter.getAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function.identity()));
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(null);
            Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(agentIds.keySet());
            List<JAgentRef> agentRefs = new ArrayList<>();

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
                    boolean controllerUpdateRequired = false;
                    boolean dbUpdateRequired = false;
                    if (dbAgent.getDisabled() != agent.getDisabled()) {
                        dbAgent.setDisabled(agent.getDisabled());
                        dbUpdateRequired = true;
                        if (!agent.getDisabled()) {
                            controllerUpdateRequired = true;
                        }
                    }
//                    if (dbAgent.getIsWatcher() != agent.getIsClusterWatcher()) {
//                        dbAgent.setIsWatcher(agent.getIsClusterWatcher());
//                        dbUpdateRequired = true;
//                    }
                    if (!dbAgent.getAgentName().equals(agent.getAgentName())) {
                        dbAgent.setAgentName(agent.getAgentName());
                        dbUpdateRequired = true;
                    }
                    if (!dbAgent.getUri().equals(agent.getUrl())) {
                        dbAgent.setUri(agent.getUrl());
                        dbUpdateRequired = true;
                        controllerUpdateRequired = true;
                    }
                    if (dbUpdateRequired) {
                        agentDBLayer.updateAgent(dbAgent);
                    }
                    if (controllerUpdateRequired) {
                        agentRefs.add(JAgentRef.of(AgentId.of(dbAgent.getAgentId()), Uri.of(dbAgent.getUri())));
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
                //dbAgent.setIsWatcher(agent.getIsClusterWatcher());
                dbAgent.setIsWatcher(false);
                dbAgent.setOsId(0L);
                dbAgent.setStartedAt(null);
                dbAgent.setUri(agent.getUrl());
                dbAgent.setVersion(null);
                agentDBLayer.saveAgent(dbAgent);

                if (controllerUpdateRequired) {
                    agentRefs.add(JAgentRef.of(AgentId.of(dbAgent.getAgentId()), Uri.of(dbAgent.getUri())));
                }

                updateAliases(agentDBLayer, agent, allAliases.get(agent.getAgentId()));
            }
            
            ModifyAgentClusterAudit jobschedulerAudit = new ModifyAgentClusterAudit(agentStoreParameter);
            logAuditMessage(jobschedulerAudit);
            storeAuditLogEntry(jobschedulerAudit, connection);
            
            Globals.commit(connection);

            // List<JAgentRef> agentRefs = Proxies.getAgents(controllerId, agentDBLayer);
            if (!agentRefs.isEmpty()) {
                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(agentRefs).map(JUpdateItemOperation::addOrChange)).thenAccept(
                        e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId));
            }

            // ask for cluster
            // List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
            // if (watcherUpdateRequired && (controllerInstances == null || controllerInstances.size() == 2)) { // is cluster
            // JobSchedulerResourceModifyJobSchedulerClusterImpl.appointNodes(agentStoreParameter.getControllerId(), agentDBLayer, getJocError());
            // }

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
    
    private static String getUniquenessMsg(String key, Map.Entry<String, Long> e) {
        return key + " has to be unique: " + e.getKey() + " is used " + (e.getValue() == 2L ? "twice" : e.getValue() + " times");
    }
}
