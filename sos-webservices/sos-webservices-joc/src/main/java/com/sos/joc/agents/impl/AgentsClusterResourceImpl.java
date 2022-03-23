package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsClusterResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.ClusterAgents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsClusterResourceImpl extends JOCResourceImpl implements IAgentsClusterResource {

    private static final String API_CALL = "./agents/inventory/cluster";

    @Override
    public JOCDefaultResponse postCluster(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            
//            if (JocClusterService.getInstance().getCluster() == null || !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
//                ClusterAgents agents = new ClusterAgents();
//                agents.setDeliveryDate(Date.from(Instant.now()));
//                
//                return JOCDefaultResponse.responseStatus200(agents);
//            }
            
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
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, agentParameter
                    .getAgentIds(), false, agentParameter.getOnlyEnabledAgents());
            Map<String, List<DBItemInventorySubAgentInstance>> subAgents = dbLayer.getSubAgentInstancesByControllerIds(allowedControllers, false,
                    agentParameter.getOnlyEnabledAgents());
            ClusterAgents agents = new ClusterAgents();
            if (dbAgents != null) {
                Set<String> controllerIds = dbAgents.stream().map(DBItemInventoryAgentInstance::getControllerId).collect(Collectors.toSet());
                Map<String, Set<String>> allAliases = dbLayer.getAgentNamesByAgentIds(controllerIds);
                agents.setAgents(dbAgents.stream().map(a -> {
                    if (!subAgents.containsKey(a.getAgentId())) { // solo agent
                        return null;
                    }
                    ClusterAgent agent = new ClusterAgent();
                    agent.setAgentId(a.getAgentId());
                    agent.setAgentName(a.getAgentName());
                    agent.setAgentNameAliases(allAliases.get(a.getAgentId()));
                    agent.setDisabled(a.getDisabled());
                    agent.setControllerId(a.getControllerId());
                    agent.setUrl(a.getUri());
                    agent.setDeployed(a.getDeployed());
                    agent.setTitle(a.getTitle());
                    agent.setSubagents(mapDBSubAgentsToSubAgents(subAgents.get(a.getAgentId())));
                    return agent;
                }).filter(Objects::nonNull).collect(Collectors.toList()));
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
    
    // old api address
    @Override
    public JOCDefaultResponse postClusterP(String accessToken, byte[] filterBytes) {
        return postCluster(accessToken, filterBytes);
    }
    
    private static List<SubAgent> mapDBSubAgentsToSubAgents(List<DBItemInventorySubAgentInstance> dbSubagents) {
        if (dbSubagents == null) {
            return null;
        }
        dbSubagents.sort(Comparator.comparingInt(DBItemInventorySubAgentInstance::getOrdering));
        int index = 0;
        for (DBItemInventorySubAgentInstance item : dbSubagents) {
            item.setOrdering(++index);
        }
        return dbSubagents.stream().map(dbSubagent -> toSubAgentMapper(dbSubagent)).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    private static SubAgent toSubAgentMapper(DBItemInventorySubAgentInstance dbSubagent) {
        if (dbSubagent == null) {
            return null;
        }
        SubAgent subagent = new SubAgent();
        subagent.setSubagentId(dbSubagent.getSubAgentId());
        subagent.setUrl(dbSubagent.getUri());
        subagent.setIsDirector(dbSubagent.getDirectorAsEnum());
        subagent.setIsClusterWatcher(dbSubagent.getIsWatcher());
        subagent.setOrdering(dbSubagent.getOrdering());
        subagent.setDisabled(dbSubagent.getDisabled());
        subagent.setTitle(dbSubagent.getTitle());
        subagent.setDeployed(dbSubagent.getDeployed());
        return subagent;
    }
    
}
