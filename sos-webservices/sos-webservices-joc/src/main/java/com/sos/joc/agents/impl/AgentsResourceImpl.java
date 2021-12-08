package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingLicenseException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.AgentNames;
import com.sos.joc.model.agent.Agents;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.ClusterAgents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.controller.ControllerId;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsResourceImpl extends JOCResourceImpl implements IAgentsResource {

    private static String API_CALL_P = "./agents/p";
    private static String API_CALL_CLUSTER_P = "./agents/cluster/p";
    private static String API_CALL_NAMES = "./agents/names";

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_P, filterBytes, accessToken);
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
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_P);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, agentParameter
                    .getAgentIds(), false, agentParameter.getOnlyEnabledAgents());
            List<String> dbClusterAgentIds = dbLayer.getClusterAgentIds(allowedControllers, agentParameter.getOnlyEnabledAgents());
            Agents agents = new Agents();
            if (dbAgents != null) {
                Set<String> controllerIds = dbAgents.stream().map(DBItemInventoryAgentInstance::getControllerId).collect(Collectors.toSet());
                Map<String, Set<String>> allAliases = dbLayer.getAgentNamesByAgentIds(controllerIds);
                agents.setAgents(dbAgents.stream().map(a -> {
                    if (dbClusterAgentIds.contains(a.getAgentId())) {
                        return null;
                    }
                    Agent agent = new Agent();
                    agent.setAgentId(a.getAgentId());
                    agent.setAgentName(a.getAgentName());
                    agent.setAgentNameAliases(allAliases.get(a.getAgentId()));
                    agent.setDisabled(a.getDisabled());
                    agent.setIsClusterWatcher(a.getIsWatcher());
                    agent.setControllerId(a.getControllerId());
                    agent.setUrl(a.getUri());
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
    
    @Override
    public JOCDefaultResponse postCluster(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_CLUSTER_P, filterBytes, accessToken);
            
            if (JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
                ClusterAgents agents = new ClusterAgents();
                agents.setDeliveryDate(Date.from(Instant.now()));
                
                return JOCDefaultResponse.responseStatus200(agents);
            }
            
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
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_CLUSTER_P);
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
    
    private static List<SubAgent> mapDBSubAgentsToSubAgents(List<DBItemInventorySubAgentInstance> dbSubagents) {
        if (dbSubagents == null) {
            return null;
        }
        return dbSubagents.stream().map(dbSubagent -> {
            SubAgent subagent = new SubAgent();
            subagent.setSubagentId(dbSubagent.getSubAgentId());
            subagent.setUrl(dbSubagent.getUri());
            subagent.setIsDirector(dbSubagent.getDirectorAsEnum());
            subagent.setIsClusterWatcher(dbSubagent.getIsWatcher());
            return subagent;
        }).collect(Collectors.toList());
    }

    @Override
    public JOCDefaultResponse postNames(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_NAMES, null, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId agentParameter = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
            String controllerId = agentParameter.getControllerId();
                    
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getDeployments().getDeploy()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getDeployments().getDeploy();
            }        
                    
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            boolean withClusterLicense = JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster()
                    .getConfig().getClusterMode();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_NAMES);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            AgentNames agentNames = new AgentNames();
            agentNames.setAgentNames(dbLayer.getEnabledAgentNames(allowedControllers, withClusterLicense));
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
