package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterStore;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.agent.AgentStoreUtils;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
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

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.data_for_java.value.JExpression;

@Path("agents")
public class SubAgentClusterStoreImpl extends JOCResourceImpl implements ISubAgentClusterStore {

    private static final String API_STORE = "./agents/cluster/store";

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_STORE, filterBytes, accessToken);
            
            AgentHelper.throwJocMissingLicenseException();
            
            JsonValidator.validate(filterBytes, StoreSubagentClusters.class);
            StoreSubagentClusters agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreSubagentClusters.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(agentStoreParameter.getAuditLog(), CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_STORE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventorySubagentClustersDBLayer agentClusterDBLayer = new InventorySubagentClustersDBLayer(connection);
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(connection);
            Map<String, String> agentToControllerMap = agentDbLayer.getAllAgents().stream().collect(Collectors.toMap(
                    DBItemInventoryAgentInstance::getAgentId, DBItemInventoryAgentInstance::getControllerId));

            Map<String, List<SubagentCluster>> subagentClusterPerController = agentStoreParameter.getSubagentClusters().stream().peek(subA -> subA
                    .setControllerId(agentToControllerMap.get(subA.getAgentId()))).collect(Collectors.groupingBy(SubagentCluster::getControllerId));
            Date now = Date.from(Instant.now());
            
            for (Map.Entry<String, List<SubagentCluster>> subagentCluster : subagentClusterPerController.entrySet()) {

                Map<String, Long> subagentClusterIds = subagentCluster.getValue().stream().collect(Collectors.groupingBy(
                        SubagentCluster::getSubagentClusterId, Collectors.counting()));

                // check uniqueness of SubagentClusterIds per Controller
                subagentClusterIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                    throw new JocBadRequestException(String.format("SubagentClusterId '%s' has to be unique per Controller", e.getKey()));
                });
                
                // check java name rules of SubagentClusterIds
                subagentClusterIds.keySet().forEach(sId -> SOSCheckJavaVariableName.test("Subagent Cluster ID", sId));
                
                // Check if all subagents in the inventory
                List<String> subagentIds = subagentCluster.getValue().stream().map(SubagentCluster::getSubagentIds).flatMap(List::stream).map(
                        SubAgentId::getSubagentId).distinct().collect(Collectors.toList());
                String missingSubagentId = agentClusterDBLayer.getFirstSubagentIdThatNotExists(subagentIds);
                if (!missingSubagentId.isEmpty()) {
                    throw new JocBadRequestException(String.format("At least one Subagent doesn't exist: '%s'", missingSubagentId));
                }

                // Check priority expressions
                subagentCluster.getValue().stream().map(SubagentCluster::getSubagentIds).flatMap(List::stream).map(SubAgentId::getPriority).map(
                        JExpression::parse).filter(Either::isLeft).findAny().ifPresent(ProblemHelper::throwProblemIfExist);
                
                Map<String, SubagentCluster> subagentMap = agentStoreParameter.getSubagentClusters().stream().collect(Collectors.toMap(
                        SubagentCluster::getSubagentClusterId, Function.identity()));
                
                AgentStoreUtils.storeSubagentCluster(subagentCluster.getKey(), subagentMap, agentClusterDBLayer, now);
            }

            Globals.commit(connection);
            
            subagentClusterPerController.keySet().forEach(controllerId -> EventBus.getInstance().post(new AgentInventoryEvent(controllerId)));
            
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
