package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsClusterResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.ClusterAgents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data_for_java.controller.JControllerState;

@Path("agents")
public class AgentsClusterResourceImpl extends JOCResourceImpl implements IAgentsClusterResource {

    private static final String API_CALL = "./agents/inventory/cluster";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsClusterResourceImpl.class);

    @Override
    public JOCDefaultResponse postCluster(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            
            JsonValidator.validateFailFast(filterBytes, ReadAgents.class);
            ReadAgents agentParameter = Globals.objectMapper.readValue(filterBytes, ReadAgents.class);
            
            String controllerId = agentParameter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean adminPermitted = getBasicJocPermissions(accessToken).getAdministration().getControllers().getView();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                if (Proxies.getControllerDbInstances().isEmpty()) {
                    permitted = getBasicControllerDefaultPermissions(accessToken).getAgents().getView() || getBasicControllerDefaultPermissions(
                            accessToken).getView() || adminPermitted;
                } else {
                    allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                            availableController -> getBasicControllerPermissions(availableController, accessToken).getAgents().getView()
                                    || getBasicControllerPermissions(availableController, accessToken).getView() || adminPermitted).collect(Collectors
                                            .toSet());
                    permitted = !allowedControllers.isEmpty();
                    if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                        allowedControllers = Collections.emptySet();
                    }
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getBasicControllerPermissions(controllerId, accessToken).getAgents().getView() || getBasicControllerPermissions(
                        controllerId, accessToken).getView() || adminPermitted;
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            ClusterAgents agents = new ClusterAgents();
            if (Proxies.getControllerDbInstances().isEmpty()) {
                agents.setAgents(Collections.emptyList());
                agents.setDeliveryDate(Date.from(Instant.now()));
                JocError jocError = getJocError();
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.warn(InventoryInstancesDBLayer.noRegisteredControllers());
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(agents));
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            dbLayer.setWithAgentOrdering(true);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, agentParameter
                    .getAgentIds(), false);
            
            Map<String, List<DBItemInventorySubAgentInstance>> subAgents = dbLayer.getSubAgentInstancesByControllerIds(allowedControllers, false);
            if (dbAgents != null) {
                Set<String> controllerIds = dbAgents.stream().map(DBItemInventoryAgentInstance::getControllerId).filter(Objects::nonNull).collect(
                        Collectors.toSet());
                Map<String, JControllerState> currentStates = AgentHelper.getCurrentStates(controllerIds);
                Map<String, Set<String>> agentsOnController = AgentHelper.getAgents(controllerIds, currentStates);
                Map<String, Set<String>> subagentsOnController = AgentHelper.getSubagents(controllerIds, currentStates);
                Map<String, Set<String>> allAliases = dbLayer.getAgentNamesByAgentIds(controllerIds);
                List<ClusterAgent> clusterAgents = new ArrayList<>();
                int position = 0;
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    if (!subAgents.containsKey(dbAgent.getAgentId())) { // solo agent
                        continue;
                    }
                    ClusterAgent agent = new ClusterAgent();
                    agent.setAgentId(dbAgent.getAgentId());
                    agent.setAgentName(dbAgent.getAgentName());
                    agent.setAgentNameAliases(allAliases.get(dbAgent.getAgentId()));
                    agent.setProcessLimit(dbAgent.getProcessLimit());
                    //agent.setDisabled(dbAgent.getDisabled());
                    agent.setControllerId(dbAgent.getControllerId());
                    agent.setUrl(dbAgent.getUri());
                    agent.setVersion(dbAgent.getVersion());
                    agent.setDeployed(null); // deployed is obsolete, now part of syncState
                    agent.setSyncState(AgentHelper.getSyncState(agentsOnController.get(dbAgent.getControllerId()), dbAgent));
                    agent.setTitle(dbAgent.getTitle());
                    agent.setOrdering(++position);
                    agent.setSubagents(mapDBSubAgentsToSubAgents(subAgents.get(dbAgent.getAgentId()), subagentsOnController.get(dbAgent.getControllerId())));
                    clusterAgents.add(agent);
                }
                agents.setAgents(clusterAgents);
            }
            agents.setDeliveryDate(Date.from(Instant.now()));
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(agents));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    // old api address
    @Override
    public JOCDefaultResponse postClusterP(String accessToken, byte[] filterBytes) {
        return postCluster(accessToken, filterBytes);
    }
    
    private static List<SubAgent> mapDBSubAgentsToSubAgents(List<DBItemInventorySubAgentInstance> dbSubagents, Set<String> subagentsOnController) {
        if (dbSubagents == null) {
            return null;
        }
        dbSubagents.sort(Comparator.comparingInt(DBItemInventorySubAgentInstance::getOrdering));
        int index = 0;
        for (DBItemInventorySubAgentInstance item : dbSubagents) {
            item.setOrdering(++index);
        }
        return dbSubagents.stream().map(dbSubagent -> toSubAgentMapper(dbSubagent, subagentsOnController)).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    private static SubAgent toSubAgentMapper(DBItemInventorySubAgentInstance dbSubagent, Set<String> subagentsOnController) {
        if (dbSubagent == null) {
            return null;
        }
        SubAgent subagent = new SubAgent();
        subagent.setSubagentId(dbSubagent.getSubAgentId());
        subagent.setUrl(dbSubagent.getUri());
        subagent.setVersion(dbSubagent.getVersion());
        subagent.setIsDirector(dbSubagent.getDirectorAsEnum());
        subagent.setOrdering(dbSubagent.getOrdering());
        subagent.setDisabled(dbSubagent.getDisabled());
        subagent.setTitle(dbSubagent.getTitle());
        subagent.setDeployed(null); // deployed is obsolete, now part of syncState
        subagent.setSyncState(AgentHelper.getSyncState(subagentsOnController, dbSubagent));
        subagent.setWithGenerateSubagentCluster(null);
        return subagent;
    }
    
}
