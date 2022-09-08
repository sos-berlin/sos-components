package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsStandaloneResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.Agents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsStandaloneResourceImpl extends JOCResourceImpl implements IAgentsStandaloneResource {

    private static final String API_CALL = "./agents/inventory";

    @Override
    public JOCDefaultResponse post2(String accessToken, byte[] filterBytes) {
     return post(accessToken, filterBytes);   
    }
    
    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReadAgents.class);
            ReadAgents agentParameter = Globals.objectMapper.readValue(filterBytes, ReadAgents.class);
            
            String controllerId = agentParameter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getAgents().getView() || getJocPermissions(accessToken).getAdministration().getControllers()
                                .getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getAgents().getView() || getJocPermissions(accessToken)
                        .getAdministration().getControllers().getView();
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            dbLayer.setWithAgentOrdering(true);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, agentParameter
                    .getAgentIds(), false, agentParameter.getOnlyVisibleAgents());
            List<String> dbClusterAgentIds = dbLayer.getClusterAgentIds(allowedControllers, agentParameter.getOnlyVisibleAgents());
            Agents agents = new Agents();
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
                    agent.setIsClusterWatcher(dbAgent.getIsWatcher());
                    agent.setControllerId(dbAgent.getControllerId());
                    agent.setUrl(dbAgent.getUri());
                    agent.setOrdering(++position);
                    soloAgents.add(agent);
                }
                agents.setAgents(soloAgents);
            }
            agents.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(agents);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
}
