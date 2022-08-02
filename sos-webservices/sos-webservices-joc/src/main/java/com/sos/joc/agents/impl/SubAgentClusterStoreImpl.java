package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterStore;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
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
                throw new JocBadRequestException(getUniquenessMsg("SubagentClusterId", e));
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
            List<String> subagentClusterIds2 = subagentMap.keySet().stream().collect(Collectors.toList());
            List<DBItemInventorySubAgentCluster> dbsubagentClusters = agentClusterDBLayer.getSubagentClusters(subagentClusterIds2);
            List<DBItemInventorySubAgentClusterMember> dbsubagentClusterMembers = agentClusterDBLayer.getSubagentClusterMembers(subagentClusterIds2);
            
            List<String> controllerIds = agentClusterDBLayer.getControllerIds(dbsubagentClusters.stream().map(
                    DBItemInventorySubAgentCluster::getAgentId).distinct().collect(Collectors.toList()));

            Map<String, List<DBItemInventorySubAgentClusterMember>> dbsubagentClusterMembersMap = Collections.emptyMap();
            if (dbsubagentClusterMembers != null) {
                dbsubagentClusterMembersMap = dbsubagentClusterMembers.stream().collect(Collectors.groupingBy(
                        DBItemInventorySubAgentClusterMember::getSubAgentClusterId));
            }
            
            Date now = Date.from(Instant.now());
            // update
            if (dbsubagentClusters != null) {
                for (DBItemInventorySubAgentCluster dbsubagentCluster : dbsubagentClusters) {
                    SubagentCluster s = subagentMap.remove(dbsubagentCluster.getSubAgentClusterId());
                    if (!dbsubagentCluster.getAgentId().equals(s.getAgentId())) {
                        throw new JocBadRequestException(String.format("Subagent Cluster ID '%s' is already used for Agent '%s'", dbsubagentCluster
                                .getSubAgentClusterId(), dbsubagentCluster.getAgentId()));
                    }
                    dbsubagentCluster.setDeployed(false);
                    dbsubagentCluster.setModified(now);
                    dbsubagentCluster.setTitle(s.getTitle());
                    connection.update(dbsubagentCluster);

                    updateMembers(connection, dbsubagentClusterMembersMap, s.getSubagentIds(), s.getSubagentClusterId(), now);
                }
            }
            // insert
            int position = agentClusterDBLayer.getMaxOrdering();
            for (SubagentCluster s : subagentMap.values()) {
                if (s.getSubagentIds().isEmpty()) { //don't store a new subagent cluster with an empty cluster
                    continue;
                }
                DBItemInventorySubAgentCluster dbsubagentCluster = new DBItemInventorySubAgentCluster();
                dbsubagentCluster.setId(null);
                dbsubagentCluster.setDeployed(false);
                dbsubagentCluster.setModified(now);
                dbsubagentCluster.setAgentId(s.getAgentId());
                dbsubagentCluster.setTitle(s.getTitle());
                dbsubagentCluster.setSubAgentClusterId(s.getSubagentClusterId());
                dbsubagentCluster.setOrdering(++position);
                connection.save(dbsubagentCluster);

                updateMembers(connection, dbsubagentClusterMembersMap, s.getSubagentIds(), s.getSubagentClusterId(), now);
            }
            
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

    private static String getUniquenessMsg(String key, Map.Entry<String, Long> e) {
        return key + " has to be unique: " + e.getKey() + " is used " + (e.getValue() == 2L ? "twice" : e.getValue() + " times");
    }
    
    private static void updateMembers(SOSHibernateSession connection, Map<String, List<DBItemInventorySubAgentClusterMember>> dbsubagentClusterMembersMap,
            List<SubAgentId> subAgents, String subagentClusterId, Date now) throws SOSHibernateException {
        
        // TODO check if subagentId in inventory
        List<DBItemInventorySubAgentClusterMember> members = dbsubagentClusterMembersMap.remove(subagentClusterId);
        if (subAgents == null) {
            subAgents = Collections.emptyList();
        }
        if (members == null) {
            members = Collections.emptyList();
        }
        
        Map<String, Long> subagentIds = subAgents.stream().collect(Collectors.groupingBy(SubAgentId::getSubagentId, Collectors.counting()));
        // check uniqueness of SubagentIds per Subagent Cluster
        subagentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
            throw new JocBadRequestException("Subagent ID has to be unique per Subagent Cluster: " + e.getKey() + " is used " + (e.getValue() == 2L
                    ? "twice" : e.getValue() + " times"));
        });
        
        Map<String, SubAgentId> subagentMap = subAgents.stream().collect(Collectors.toMap(SubAgentId::getSubagentId, Function.identity()));
        
        // update member
        for (DBItemInventorySubAgentClusterMember member : members) {
            if (subagentMap.isEmpty()) {
                connection.delete(member);
            } else {
                SubAgentId subAgent = subagentMap.remove(member.getSubAgentId());
                if (subAgent == null) {
                    connection.delete(member);
                } else {
                    member.setModified(now);
                    member.setPriority(subAgent.getPriority());
                    connection.update(member);
                }
            }
        }
        // insert member
        for (SubAgentId subAgent : subagentMap.values()) {
            DBItemInventorySubAgentClusterMember member = new DBItemInventorySubAgentClusterMember();
            member.setId(null);
            member.setModified(now);
            member.setPriority(subAgent.getPriority());
            member.setSubAgentId(subAgent.getSubagentId());
            member.setSubAgentClusterId(subagentClusterId);
            connection.save(member);
        }
    }
}
