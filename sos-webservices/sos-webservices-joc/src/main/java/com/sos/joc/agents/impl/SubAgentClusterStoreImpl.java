package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterStore;
import com.sos.joc.agents.util.AgentStoreUtils;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.StoreSubagentClusters;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

@Path("agents")
public class SubAgentClusterStoreImpl extends JOCResourceImpl implements ISubAgentClusterStore {

    private static final String API_STORE = "./agents/cluster/store";

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_STORE, filterBytes, accessToken);
            
            AgentHelper.throwJocMissingLicenseException();
            
            JsonValidator.validateFailFast(filterBytes, StoreSubagentClusters.class);
            StoreSubagentClusters agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreSubagentClusters.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Map<String, Long> subagentClusterIds = agentStoreParameter.getSubagentClusters().stream().collect(Collectors.groupingBy(
                    SubagentCluster::getSubagentClusterId, Collectors.counting()));

            // check uniqueness of SubagentClusterIds
            subagentClusterIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("SubagentClusterId", e));
            });

            // check java name rules of SubagentClusterIds
            for (String subagentClusterId : subagentClusterIds.keySet()) {
                SOSCheckJavaVariableName.test("Subagent Cluster ID", subagentClusterId);
            }
            
            storeAuditLog(agentStoreParameter.getAuditLog(), CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_STORE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventorySubagentClustersDBLayer agentClusterDBLayer = new InventorySubagentClustersDBLayer(connection);

            // Check if all subagents in the inventory
            List<String> subagentIds = agentStoreParameter.getSubagentClusters().stream().map(SubagentCluster::getSubagentIds).flatMap(List::stream).map(
                    SubAgentId::getSubagentId).distinct().collect(Collectors.toList());
            String missingSubagentId = agentClusterDBLayer.getFirstSubagentIdThatNotExists(subagentIds);
            if (!missingSubagentId.isEmpty()) {
                throw new JocBadRequestException(String.format("At least one Subagent doesn't exist: '%s'", missingSubagentId));
            }
            
            Map<String, SubagentCluster> subagentMap = agentStoreParameter.getSubagentClusters().stream().collect(Collectors.toMap(
                    SubagentCluster::getSubagentClusterId, Function.identity()));
            Date now = Date.from(Instant.now());
            List<DBItemInventorySubAgentCluster> dbsubagentClusters = AgentStoreUtils
                    .storeSubagentCluster(subagentMap, agentClusterDBLayer, now);
            List<String> controllerIds = agentClusterDBLayer.getControllerIds(dbsubagentClusters.stream().map(
                  DBItemInventorySubAgentCluster::getAgentId).distinct().collect(Collectors.toList()));
            
            Globals.commit(connection);
            
            for (String controllerId : controllerIds) {
                EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
            }

            return JOCDefaultResponse.responseStatusJSOk(now);
        } catch (JocException e) {
            Globals.rollback(connection);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(connection);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}
