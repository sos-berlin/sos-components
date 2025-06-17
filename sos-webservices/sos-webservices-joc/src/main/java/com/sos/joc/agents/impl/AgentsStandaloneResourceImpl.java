package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsStandaloneResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.Agents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class AgentsStandaloneResourceImpl extends JOCResourceImpl implements IAgentsStandaloneResource {

    private static final String API_CALL = "./agents/inventory";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsStandaloneResourceImpl.class);

    @Override
    public JOCDefaultResponse post2(String accessToken, byte[] filterBytes) {
     return post(accessToken, filterBytes);   
    }
    
    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
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
            
            Agents agents = new Agents();
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
                    .getAgentIds(), agentParameter.getOnlyVisibleAgents());
            List<String> dbClusterAgentIds = dbLayer.getClusterAgentIds(allowedControllers, agentParameter.getOnlyVisibleAgents());
            if (dbAgents != null) {
                Set<String> controllerIds = dbAgents.stream().map(DBItemInventoryAgentInstance::getControllerId).collect(Collectors.toSet());
                Map<String, Set<String>> allAliases = dbLayer.getAgentNamesByAgentIds(controllerIds);
                Map<String, Set<String>> agentsOnController = AgentHelper.getAgents(controllerIds, AgentHelper.getCurrentStates(controllerIds));
                List<Agent> soloAgents = new ArrayList<>();
                int position = 0;
                
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    if (dbClusterAgentIds.contains(dbAgent.getAgentId())) {
                        continue;
                    }
                    Agent agent = new Agent();
                    agent.setAgentId(dbAgent.getAgentId());
                    agent.setAgentName(dbAgent.getAgentName());
                    agent.setAgentNameAliases(allAliases.get(dbAgent.getAgentId()));
                    agent.setTitle(dbAgent.getTitle());
                    agent.setHidden(dbAgent.getHidden());
                    agent.setDisabled(dbAgent.getDisabled());
                    agent.setDeployed(null); // deployed is obsolete, now part of syncState
                    agent.setSyncState(AgentHelper.getSyncState(agentsOnController.get(dbAgent.getControllerId()), dbAgent));
                    agent.setControllerId(dbAgent.getControllerId());
                    agent.setUrl(dbAgent.getUri());
                    agent.setVersion(dbAgent.getVersion());
                    agent.setProcessLimit(dbAgent.getProcessLimit());
                    agent.setOrdering(++position);
                    soloAgents.add(agent);
                }
                agents.setAgents(soloAgents);
            }
            agents.setDeliveryDate(Date.from(Instant.now()));
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(agents));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
}
