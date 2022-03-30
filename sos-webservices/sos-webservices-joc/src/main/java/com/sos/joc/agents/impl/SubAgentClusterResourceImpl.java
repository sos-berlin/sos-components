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
import com.sos.joc.agents.resource.ISubAgentClusterResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxies;
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

@Path("agents")
public class SubAgentClusterResourceImpl extends JOCResourceImpl implements ISubAgentClusterResource {

    private static final String API_CALL = "./agents/cluster";

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            
            //AgentHelper.throwJocMissingLicenseException();
            
            JsonValidator.validateFailFast(filterBytes, ReadSubagentClusters.class);
            ReadSubagentClusters subAgentClusterParameter = Globals.objectMapper.readValue(filterBytes, ReadSubagentClusters.class);
            
            String controllerId = subAgentClusterParameter.getControllerId();
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
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, subAgentClusterParameter
                    .getAgentIds(), false, false);
            Map<String, String> agentIdControllerIdMap = dbAgents.stream().collect(Collectors.toMap(DBItemInventoryAgentInstance::getAgentId,
                    DBItemInventoryAgentInstance::getControllerId));
            
            InventorySubagentClustersDBLayer agentDBLayer = new InventorySubagentClustersDBLayer(connection);
            Map<DBItemInventorySubAgentCluster, List<SubAgentId>> dbSubagentClusters = agentDBLayer.getSubagentClusters(allowedControllers,
                    subAgentClusterParameter.getAgentIds(), subAgentClusterParameter.getSubagentClusterIds());

            Map<String, Set<String>> subagentSelectionsOnController = AgentHelper.getSubagentSelections(agentIdControllerIdMap.values(), AgentHelper
                    .getCurrentStates(agentIdControllerIdMap.values()));

            SubagentClusters entity = new SubagentClusters();
            entity.setAuditLog(null);
            entity.setSubagentClusters(dbSubagentClusters.entrySet().stream().map(e -> {
                DBItemInventorySubAgentCluster key = e.getKey();
                SubagentCluster subagentCluster = new SubagentCluster();
                subagentCluster.setControllerId(agentIdControllerIdMap.get(key.getAgentId()));
                subagentCluster.setAgentId(key.getAgentId());
                subagentCluster.setDeployed(null); // deployed is obsolete, now part of syncState
                subagentCluster.setSyncState(AgentHelper.getSyncState(subagentSelectionsOnController.get(subagentCluster.getControllerId()), key));
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
    
}
