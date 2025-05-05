package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsNames;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentNames;
import com.sos.joc.model.agent.SelectionIdsPerAgentName;
import com.sos.joc.model.controller.ControllerId;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class AgentsNamesImpl extends JOCResourceImpl implements IAgentsNames {

    private static final String API_CALL = "./agents/names";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsNamesImpl.class);

    @Override
    public JOCDefaultResponse postNames(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, null, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId agentParameter = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
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
            
            AgentNames agentNames = new AgentNames();
            boolean withClusterLicense = AgentHelper.hasClusterLicense();
            
            if (Proxies.getControllerDbInstances().isEmpty()) {
                agentNames.setAgentNames(Collections.emptySet());
                if (withClusterLicense) {
                    agentNames.setClusterAgentNames(Collections.emptySet());
                    agentNames.setSubagentClusterIds(new SelectionIdsPerAgentName());
                }
                agentNames.setDeliveryDate(Date.from(Instant.now()));
                JocError jocError = getJocError();
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.warn(InventoryInstancesDBLayer.noRegisteredControllers());
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(agentNames));
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            InventorySubagentClustersDBLayer dbLayerCluster = new InventorySubagentClustersDBLayer(connection);
            Map<String, List<String>> subagentClusterIdsPerAgentId = Collections.emptyMap();
            Comparator<String> comparator = Comparator.comparing(String::toLowerCase);
            if (withClusterLicense) {
                List<DBItemInventorySubAgentCluster> subagentClusters = dbLayerCluster.getSubagentClusters(allowedControllers, null);
                subagentClusterIdsPerAgentId = subagentClusters.stream().sorted(Comparator.comparing(
                        DBItemInventorySubAgentCluster::getSubAgentClusterId)).collect(Collectors.groupingBy(
                                DBItemInventorySubAgentCluster::getAgentId, Collectors.mapping(DBItemInventorySubAgentCluster::getSubAgentClusterId,
                                        Collectors.toList())));
            }
            
            List<String> clusterAgentIds = dbLayer.getClusterAgentIds(allowedControllers, false);
            Map<String, List<String>> agentNamesPerAgentId = dbLayer.getAgentNamesPerAgentId(allowedControllers, true);
            
            SelectionIdsPerAgentName s = new SelectionIdsPerAgentName();
            List<String> standaloneAgentNames = new ArrayList<>();
            List<String> clusterAgentNames = new ArrayList<>();
            
            for (Map.Entry<String, List<String>> entry : agentNamesPerAgentId.entrySet()) {
                String agentId = entry.getKey();
                if (clusterAgentIds.contains(agentId)) {
                    if (withClusterLicense) {
                        clusterAgentNames.addAll(entry.getValue());
                        for (String agentName : entry.getValue()) {
                            s.setAdditionalProperty(agentName, subagentClusterIdsPerAgentId.get(agentId));
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
