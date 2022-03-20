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
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.ReadSubagentClusters;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.SubagentClusters;
import com.sos.schema.JsonValidator;

import js7.data.subagent.SubagentSelectionId;
import js7.data_for_java.subagent.JSubagentSelection;

@Path("agents")
public class SubAgentClusterResourceImpl extends JOCResourceImpl implements ISubAgentClusterResource {

    private static final String API_STORE = "./agents/cluster";

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_STORE, filterBytes, accessToken);
            
//            if (JocClusterService.getInstance().getCluster() == null || !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
//                throw new JocMissingLicenseException("missing license for Agent cluster");
//            }
            
            JsonValidator.validateFailFast(filterBytes, ReadSubagentClusters.class);
            ReadSubagentClusters subAgentClusterParameter = Globals.objectMapper.readValue(filterBytes, ReadSubagentClusters.class);
            
            String controllerId = subAgentClusterParameter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            boolean withSyncState = controllerId != null && !controllerId.isEmpty();
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

            connection = Globals.createSosHibernateStatelessConnection(API_STORE);
            
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, subAgentClusterParameter
                    .getAgentIds(), false, false);
            Map<String, String> agentIdControllerIdMap = dbAgents.stream().collect(Collectors.toMap(DBItemInventoryAgentInstance::getAgentId,
                    DBItemInventoryAgentInstance::getControllerId));

            InventorySubagentClustersDBLayer agentDBLayer = new InventorySubagentClustersDBLayer(connection);
            Map<DBItemInventorySubAgentCluster, List<SubAgentId>> dbSubagentClusters = agentDBLayer.getSubagentClusters(allowedControllers,
                    subAgentClusterParameter.getAgentIds(), subAgentClusterParameter.getSubagentClusterIds());
            
            Set<String> subagentSelections = getSubagentSelections(controllerId, withSyncState);

            SubagentClusters entity = new SubagentClusters();
            entity.setAuditLog(null);
            entity.setSubagentClusters(dbSubagentClusters.entrySet().stream().map(e -> {
                DBItemInventorySubAgentCluster key = e.getKey();
                SubagentCluster subagentCluster = new SubagentCluster();
                subagentCluster.setControllerId(agentIdControllerIdMap.get(key.getAgentId()));
                subagentCluster.setAgentId(key.getAgentId());
                subagentCluster.setDeployed(key.getDeployed());
                subagentCluster.setSyncState(getSyncState(subagentSelections, key, withSyncState));
                subagentCluster.setSubagentClusterId(key.getSubAgentClusterId());
                subagentCluster.setTitle(key.getTitle());
                subagentCluster.setSubagentIds(e.getValue());
                return subagentCluster;
            }).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static Set<String> getSubagentSelections(String controllerId, boolean withSyncState) {
        if (withSyncState) {
            try {
                Map<SubagentSelectionId, JSubagentSelection> s = Proxy.of(controllerId).currentState().idToSubagentSelection();
                return s.keySet().stream().map(SubagentSelectionId::string).collect(Collectors.toSet());
            } catch (Exception e1) {
                return null;
            }
        }
        return Collections.emptySet();
    }
    
    private static SyncState getSyncState(Set<String> subagentSelections, DBItemInventorySubAgentCluster dbSubAgent, boolean withSyncState) {
        if (withSyncState) {
            if (subagentSelections == null) {
                return SyncStateHelper.getState(SyncStateText.UNKNOWN);
            } else if (subagentSelections.contains(dbSubAgent.getSubAgentClusterId())) {
                return SyncStateHelper.getState(SyncStateText.IN_SYNC); 
            } else if (dbSubAgent.getDeployed()) {
                return SyncStateHelper.getState(SyncStateText.NOT_IN_SYNC); 
            } else {
                return SyncStateHelper.getState(SyncStateText.NOT_DEPLOYED);
            }
        } else {
            return null;
        }
    }
}
