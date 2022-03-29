package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
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
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.AgentNames;
import com.sos.joc.model.agent.Agents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.joc.model.agent.SelectionIdsPerAgentName;
import com.sos.joc.model.controller.ControllerId;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsResourceImpl extends JOCResourceImpl implements IAgentsResource {

    private static final String API_CALL_P = "./agents/inventory";
    private static final String API_CALL_NAMES = "./agents/names";

    @Override
    public JOCDefaultResponse post2(String accessToken, byte[] filterBytes) {
     return post(accessToken, filterBytes);   
    }
    
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
                    agent.setTitle(a.getTitle());
                    agent.setDisabled(a.getDisabled());
                    agent.setDeployed(a.getDeployed());
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
            
            boolean withClusterLicense = true; //JocClusterService.getInstance().getCluster() != null && JocClusterService.getInstance().getCluster()
                   // .getConfig().getClusterMode();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_NAMES);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            InventorySubagentClustersDBLayer dbLayerCluster = new InventorySubagentClustersDBLayer(connection);
            AgentNames agentNames = new AgentNames();
            //agentNames.setAgentNames(dbLayer.getEnabledAgentNames(allowedControllers, withClusterLicense));
            List<DBItemInventorySubAgentInstance> subagents = dbLayer.getSubAgentInstancesByControllerIds(allowedControllers);
            Map<String, List<String>> subagentIdsPerAgentId = subagents.stream().collect(Collectors.groupingBy(DBItemInventorySubAgentInstance::getAgentId, Collectors.mapping(DBItemInventorySubAgentInstance::getSubAgentId, Collectors.toList())));
            Comparator<String> comparator = Comparator.comparing(String::toLowerCase);
            if (withClusterLicense) {
                List<DBItemInventorySubAgentCluster> subagentClusters = dbLayerCluster.getSubagentClusters(allowedControllers, null);
                Map<String, List<String>> subagentClusterIdsPerAgentId = subagentClusters.stream().collect(Collectors.groupingBy(
                        DBItemInventorySubAgentCluster::getAgentId, Collectors.mapping(DBItemInventorySubAgentCluster::getSubAgentClusterId,
                                Collectors.toList())));
                subagentIdsPerAgentId.forEach((agentId, subagentIds) -> {
                    List<String> subagentClusterIds = subagentClusterIdsPerAgentId.get(agentId);
                    if (subagentClusterIds != null) {
                        subagentIds.addAll(subagentClusterIds);
                        subagentIds.sort(comparator);
                    }
                });
            }
            
            Set<String> clusterAgentIds = subagentIdsPerAgentId.keySet();
            Map<String, List<String>> agentNamesPerAgentId = dbLayer.getAgentNamesPerAgentId(allowedControllers, true);
            
            //Set<String> clusterAgentNames = agentNamesPerAgentId.entrySet().stream().filter(e -> clusterAgentIds.contains(e.getKey())).map(Map.Entry::getValue).flatMap(List::stream).collect(Collectors.toSet());
            //agentNames.setClusterAgentNames(clusterAgentNames);
            //agentNames.setAgentNames(agentNamesPerAgentId.entrySet().stream().filter(e -> !clusterAgentIds.contains(e.getKey())).map(Map.Entry::getValue).flatMap(List::stream).collect(Collectors.toSet()));
            SelectionIdsPerAgentName s = new SelectionIdsPerAgentName();
            List<String> standaloneAgentNames = new ArrayList<>();
            List<String> clusterAgentNames = new ArrayList<>();
            
            for (Map.Entry<String, List<String>> entry : agentNamesPerAgentId.entrySet()) {
                String agentId = entry.getKey();
                if (clusterAgentIds.contains(agentId)) {
                    if (withClusterLicense) {
                        clusterAgentNames.addAll(entry.getValue());
                        for (String agentName : entry.getValue()) {
                            s.setAdditionalProperty(agentName, subagentIdsPerAgentId.get(agentId));
                        }
                    }
                } else {
                    standaloneAgentNames.addAll(entry.getValue());
                }
            }
            agentNames.setAgentNames(standaloneAgentNames.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new)));
            if (withClusterLicense) {
                agentNames.setClusterAgentNames(clusterAgentNames.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new)));
                agentNames.setSubagentClusterIds(s);
            }
            agentNames.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(agentNames));
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
