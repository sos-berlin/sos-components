package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.AgentNames;
import com.sos.joc.model.agent.Agents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsResourceImpl extends JOCResourceImpl implements IAgentsResource {

    private static String API_CALL_P = "./agents/p";
    private static String API_CALL_NAMES = "./agents/names";

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_P, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReadAgents.class);
            ReadAgents agentParameter = Globals.objectMapper.readValue(filterBytes, ReadAgents.class);
            boolean permission = getControllerPermissions(agentParameter.getControllerId(), accessToken).getAgents().getView() || getJocPermissions(
                    accessToken).getAdministration().getControllers().getView();

            JOCDefaultResponse jocDefaultResponse = initPermissions(agentParameter.getControllerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_P);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<String> controllerIds = agentParameter.getControllerId().isEmpty() ? null : Collections.singletonList(agentParameter.getControllerId());
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIds(controllerIds, false, agentParameter
                    .getOnlyEnabledAgents());
            Agents agents = new Agents();
            if (dbAgents != null) {
                Map<String, Set<String>> allAliases = dbLayer.getAgentNamesByAgentIds(dbAgents.stream().map(DBItemInventoryAgentInstance::getAgentId)
                        .collect(Collectors.toSet()));
                agents.setAgents(dbAgents.stream().map(a -> {
                    Agent agent = new Agent();
                    agent.setAgentId(a.getAgentId());
                    agent.setAgentName(a.getAgentName());
                    agent.setAgentNameAliases(allAliases.get(a.getAgentId()));
                    agent.setDisabled(a.getDisabled());
                    agent.setIsClusterWatcher(a.getIsWatcher());
                    agent.setUrl(a.getUri());
                    agent.setControllerId(a.getControllerId());
                    return agent;
                }).collect(Collectors.toList()));
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

    @Override
    public JOCDefaultResponse postNames(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_NAMES, null, accessToken);
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_NAMES);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            AgentNames agentNames = new AgentNames();
            agentNames.setAgentNames(dbLayer.getEnabledAgentNames());
            agentNames.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(agentNames);
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
