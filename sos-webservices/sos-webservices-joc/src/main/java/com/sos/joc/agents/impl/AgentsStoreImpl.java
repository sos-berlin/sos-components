package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsStore;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.agent.AgentStoreUtils;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.StoreAgents;
import com.sos.joc.model.agent.StoreClusterAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsStoreImpl extends JOCResourceImpl implements IAgentsStore {

    private static final String API_INVENTORY_STORE = "./agents/inventory/store";
    private static final String API_CLUSTER_INVENTORY_STORE = "./agents/inventory/cluster/store";

    @Override
    public JOCDefaultResponse inventoryStore(String accessToken, byte[] filterBytes) {
        return store(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_INVENTORY_STORE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, StoreAgents.class);
            StoreAgents agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = agentStoreParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Map<String, Long> agentIds = agentStoreParameter.getAgents().stream().collect(Collectors.groupingBy(Agent::getAgentId, Collectors
                    .counting()));

            // check uniqueness of AgentId
            agentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("AgentId", e));
            });

            AgentStoreUtils.checkUniquenessOfAgentNames(agentStoreParameter.getAgents());

            // check uniqueness of AgentUrl
            agentStoreParameter.getAgents().stream().collect(Collectors.groupingBy(Agent::getUrl, Collectors.counting())).entrySet().stream().filter(
                    e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("Agent url", e));
                    });

            // check java name rules of AgentIds
            for (String agentId : agentIds.keySet()) {
                SOSCheckJavaVariableName.test("Agent ID", agentId);
            }

            storeAuditLog(agentStoreParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_INVENTORY_STORE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);

            Map<String, Agent> agentMap = agentStoreParameter.getAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function.identity()));
            AgentStoreUtils.storeStandaloneAgent(agentMap, controllerId, true, agentDBLayer);

            Globals.commit(connection);
            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, agentIds.keySet()));

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
    
    @Override
    public JOCDefaultResponse clusterInventoryStore(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CLUSTER_INVENTORY_STORE, filterBytes, accessToken);

            AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, StoreClusterAgents.class);
            StoreClusterAgents agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreClusterAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = agentStoreParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Map<String, Long> agentIds = agentStoreParameter.getClusterAgents().stream().collect(Collectors.groupingBy(ClusterAgent::getAgentId,
                    Collectors.counting()));

            // check uniqueness of AgentId in request
            agentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("AgentId", e));
            });

            AgentStoreUtils.checkUniquenessOfAgentNames(agentStoreParameter.getClusterAgents());

            // check uniqueness of SubagentUrl in request
            Set<SubAgent> requestedSubagents = agentStoreParameter.getClusterAgents().stream().map(ClusterAgent::getSubagents).flatMap(List::stream)
                    .collect(Collectors.toSet());
            requestedSubagents.stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(e -> e
                    .getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("Subagent url", e));
                    });
            
            Set<String> requestedSubagentIds = requestedSubagents.stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());

            // check java name rules of AgentIds
            for (String agentId : agentIds.keySet()) {
                SOSCheckJavaVariableName.test("Agent ID", agentId);
            }
            for (String subagentId : requestedSubagentIds) {
                SOSCheckJavaVariableName.test("Subagent ID", subagentId);
            }

            storeAuditLog(agentStoreParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CLUSTER_INVENTORY_STORE);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            InventorySubagentClustersDBLayer subagentClusterDBLayer = new InventorySubagentClustersDBLayer(connection);

            Map<String, ClusterAgent> agentMap = agentStoreParameter.getClusterAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function
                    .identity()));
            AgentStoreUtils.storeClusterAgent(agentMap, requestedSubagents, requestedSubagentIds, controllerId, true, agentDBLayer, subagentClusterDBLayer);

            Globals.commit(connection);
            Globals.disconnect(connection);
            connection = null;
            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, agentIds.keySet()));

//            if (!subAgentsToController.isEmpty()) {
//                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(subAgentsToController)).thenAccept(e -> ProblemHelper
//                        .postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
//            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
