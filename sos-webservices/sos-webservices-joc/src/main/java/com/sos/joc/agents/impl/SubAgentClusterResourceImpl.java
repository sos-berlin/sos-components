package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.agent.ReadSubagentClusters;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.SubagentClusters;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class SubAgentClusterResourceImpl extends JOCResourceImpl implements ISubAgentClusterResource {

    private static final String API_CALL = "./agents/cluster";
    private static final Logger LOGGER = LoggerFactory.getLogger(SubAgentClusterResourceImpl.class);

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            
            //AgentHelper.throwJocMissingLicenseException();
            
            JsonValidator.validateFailFast(filterBytes, ReadSubagentClusters.class);
            ReadSubagentClusters subAgentClusterParameter = Globals.objectMapper.readValue(filterBytes, ReadSubagentClusters.class);
            
            String controllerId = subAgentClusterParameter.getControllerId();
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
            
            SubagentClusters entity = new SubagentClusters();
            entity.setAuditLog(null);
            if (Proxies.getControllerDbInstances().isEmpty()) {
                entity.setSubagentClusters(Collections.emptyList());
                entity.setDeliveryDate(Date.from(Instant.now()));
                JocError jocError = getJocError();
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.warn(InventoryInstancesDBLayer.noRegisteredControllers());
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, subAgentClusterParameter
                    .getAgentIds(), false);
            Map<String, String> agentIdControllerIdMap = dbAgents.stream().collect(Collectors.toMap(DBItemInventoryAgentInstance::getAgentId,
                    DBItemInventoryAgentInstance::getControllerId));
            
            InventorySubagentClustersDBLayer agentDBLayer = new InventorySubagentClustersDBLayer(connection);
            Map<DBItemInventorySubAgentCluster, List<SubAgentId>> dbSubagentClusters = agentDBLayer.getSubagentClusters(allowedControllers,
                    subAgentClusterParameter.getAgentIds(), subAgentClusterParameter.getSubagentClusterIds());

            Map<String, Set<String>> subagentSelectionsOnController = AgentHelper.getSubagentSelections(agentIdControllerIdMap.values(), AgentHelper
                    .getCurrentStates(agentIdControllerIdMap.values()));

            int position = 0;
            List<SubagentCluster> subagentClusters = new ArrayList<>();
            Comparator<DBItemInventorySubAgentCluster> comp = Comparator.comparingInt(DBItemInventorySubAgentCluster::getOrdering);
            for (DBItemInventorySubAgentCluster key : dbSubagentClusters.keySet().stream().sorted(comp).collect(Collectors.toList())) {
                SubagentCluster subagentCluster = new SubagentCluster();
                subagentCluster.setControllerId(agentIdControllerIdMap.get(key.getAgentId()));
                subagentCluster.setAgentId(key.getAgentId());
                subagentCluster.setDeployed(null); // deployed is obsolete, now part of syncState
                subagentCluster.setSyncState(AgentHelper.getSyncState(subagentSelectionsOnController.get(subagentCluster.getControllerId()), key));
                subagentCluster.setSubagentClusterId(key.getSubAgentClusterId());
                subagentCluster.setTitle(key.getTitle());
                subagentCluster.setSubagentIds(dbSubagentClusters.get(key));
                subagentCluster.setOrdering(++position);
                subagentClusters.add(subagentCluster);
            }
            entity.setSubagentClusters(subagentClusters);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
}
