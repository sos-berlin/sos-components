package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
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
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.StoreAgents;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.agent.AgentName;
import js7.data.agent.AgentRef;
import js7.proxy.javaapi.data.agent.JAgentRef;

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
            // TODO permissions
            boolean permission = true; //getPermissonsJocCockpit(agentStoreParameter.getControllerId(), accessToken).getJS7Controller().getExecute().isContinue();
            String controllerId = agentStoreParameter.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            Set<String> agentIds = agentStoreParameter.getAgents().stream().map(Agent::getAgentId).collect(Collectors.toSet());
            
            for (String agentId : agentIds) {
                CheckJavaVariableName.test("Agent ID", agentId);
            }

            checkRequiredComment(agentStoreParameter.getAuditLog());
            ModifyAgentClusterAudit jobschedulerAudit = new ModifyAgentClusterAudit(agentStoreParameter);
            logAuditMessage(jobschedulerAudit);
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            
            Map<String, Agent> agentMap = agentStoreParameter.getAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function.identity()));
            // TODO check unique URLs
            // Set<String> urls = agentStoreParameter.getAgents().stream().map(Agent::getUrl).map(String::toLowerCase).collect(Collectors.toSet());
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(Arrays.asList(controllerId));
            Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(agentIds);
            
            boolean watcherUpdateRequired = false;
            if (dbAgents != null && !dbAgents.isEmpty()) {
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    Agent agent = agentMap.remove(dbAgent.getAgentId());
                    if (agent == null) {
                        // throw something?
                        continue;
                    }
                    boolean controllerUpdateRequired = false;
                    boolean dbUpdateRequired = false;
                    if (dbAgent.getDisabled() != agent.getDisabled()) {
                        dbAgent.setDisabled(agent.getDisabled());
                        dbUpdateRequired = true;
                    }
                    if (dbAgent.getIsWatcher() != agent.getIsClusterWatcher()) {
                        dbAgent.setIsWatcher(agent.getIsClusterWatcher());
                        dbUpdateRequired = true;
                        watcherUpdateRequired = true;
                    }
                    if (!dbAgent.getAgentName().equals(agent.getAgentName())) {
                        dbAgent.setIsWatcher(agent.getIsClusterWatcher());
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
                    
                    updateAliases(connection, agent, allAliases.get(agent.getAgentId()));
                }
            }
            
            for (Agent agent : agentMap.values()) {
                DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                dbAgent.setId(null);
                dbAgent.setAgentId(agent.getAgentId());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setControllerId(controllerId);
                dbAgent.setDisabled(agent.getDisabled());
                dbAgent.setIsWatcher(agent.getIsClusterWatcher());
                if (agent.getIsClusterWatcher()) {
                    watcherUpdateRequired = true;
                }
                dbAgent.setOsId(0L);
                dbAgent.setStartedAt(null);
                dbAgent.setUri(agent.getUrl());
                dbAgent.setVersion(null);
                agentDBLayer.saveAgent(dbAgent);
                
                updateAliases(connection, agent, allAliases.get(agent.getAgentId()));
            }
            
            List<JAgentRef> agentRefs = Proxies.getAgents(controllerId, agentDBLayer);
            if (!agentRefs.isEmpty()) {
                ControllerApi.of(controllerId).updateAgentRefs(agentRefs).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getJocError(),
                        controllerId));
            }
            
            
            // ask for cluster
//            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
//            if (watcherUpdateRequired && (controllerInstances == null || controllerInstances.size() == 2)) { // is cluster
//                JobSchedulerResourceModifyJobSchedulerClusterImpl.appointNodes(agentStoreParameter.getControllerId(), agentDBLayer, getJocError());
//            }
            
            storeAuditLogEntry(jobschedulerAudit);

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
    
    public static void updateAliases(SOSHibernateSession connection, Agent agent, Collection<DBItemInventoryAgentName> dbAliases)
            throws SOSHibernateException {
        if (dbAliases != null) {
            for (DBItemInventoryAgentName dbAlias : dbAliases) {
                connection.delete(dbAlias);
            }
        }
        Set<String> aliases = agent.getAgentNameAliases();
        if (aliases != null && !aliases.isEmpty()) {
            aliases.remove(agent.getAgentName());
            for (String name : aliases) {
                DBItemInventoryAgentName a = new DBItemInventoryAgentName();
                a.setAgentId(agent.getAgentId());
                a.setAgentName(name);
                connection.save(a);
            }
        }
    }
}
