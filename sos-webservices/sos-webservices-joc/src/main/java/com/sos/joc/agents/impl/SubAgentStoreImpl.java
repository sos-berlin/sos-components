package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentStore;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.StoreSubAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

@Path("agents")
public class SubAgentStoreImpl extends JOCResourceImpl implements ISubAgentStore {

    private static final String API_CALL = "./agents/inventory/cluster/subagents/store";
    
    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);

            AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, StoreSubAgents.class);
            StoreSubAgents subAgentsParam = Globals.objectMapper.readValue(filterBytes, StoreSubAgents.class);

            String controllerId = subAgentsParam.getControllerId();
            String agentId = subAgentsParam.getAgentId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(subAgentsParam.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            
            DBItemInventoryAgentInstance dbAgent = dbLayer.getAgentInstance(agentId);
            if (dbAgent == null) {
                throw new JocBadRequestException("Cluster Agent '" + agentId + "' doesn't exist");
            }
            
            // check uniqueness of SubagentUrl in request
            subAgentsParam.getSubagents().stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(e -> e
                    .getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(getUniquenessMsg("Subagent url", e));
                    });
            
            Set<String> requestedSubagentIds = subAgentsParam.getSubagents().stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());

            // check java name rules of SubagentIds
            for (String subagentId : requestedSubagentIds) {
                CheckJavaVariableName.test("Subagent ID", subagentId);
            }
            
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIds(null);
            List<DBItemInventorySubAgentInstance> dbSubAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(controllerId));
            
            // check uniqueness of SubagentUrl with DB
            Set<String> requestedSubagentUrls = subAgentsParam.getSubagents().stream().map(SubAgent::getUrl).collect(Collectors.toSet());
            dbSubAgents.stream().filter(s -> !requestedSubagentIds.contains(s.getSubAgentId())).filter(s -> requestedSubagentUrls.contains(s
                    .getUri())).findAny().ifPresent(s -> {
                        throw new JocBadRequestException(String.format("Subagent url %s is already used by Subagent %s", s.getUri(), s.getSubAgentId()));
                    });
            dbAgents.stream().filter(a -> a.getUri() != null && !a.getUri().isEmpty() && !a.getAgentId().equals(agentId)).filter(
                    a -> requestedSubagentUrls.contains(a.getUri())).findAny().ifPresent(a -> {
                        throw new JocBadRequestException(String.format("Subagent url %s is already used by Agent %s", a.getUri(), a.getAgentId()));
                    });
            
            saveOrUpdate(dbLayer, new InventorySubagentClustersDBLayer(connection), dbAgent, dbSubAgents, subAgentsParam.getSubagents());
            Globals.commit(connection);
            Globals.disconnect(connection);
            connection = null;
            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, agentId));
            
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
    
    public static void saveOrUpdate(InventoryAgentInstancesDBLayer dbLayer, InventorySubagentClustersDBLayer clusterDbLayer,
            DBItemInventoryAgentInstance dbAgent, Collection<DBItemInventorySubAgentInstance> dbSubAgents, Collection<SubAgent> subAgents)
            throws SOSHibernateException {

        subAgents.stream().collect(Collectors.groupingBy(SubAgent::getSubagentId, Collectors.counting())).entrySet().stream().filter(e -> e
                .getValue() > 1L).map(Map.Entry::getKey).findAny().ifPresent(sId -> {
                    throw new JocBadRequestException("Subagent ID '" + sId + "' must be unique per contoller");
                });

        // TODO URL has to be unique?
        // subAgents.stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(
        // e -> e.getValue() > 1L).map(Map.Entry::getKey).findAny().ifPresent(sUrl -> {
        // throw new JocBadRequestException("URL '" + sUrl + "' must be unique");
        // });

        String agentId = dbAgent.getAgentId();

        Map<Boolean, List<DBItemInventorySubAgentInstance>> mapOfAgentIds = dbSubAgents.stream().collect(Collectors.groupingBy(s -> s.getAgentId()
                .equals(agentId)));
        dbSubAgents = null;

        List<String> subAgentIds = subAgents.stream().map(SubAgent::getSubagentId).distinct().collect(Collectors.toList());

        // checks if subagentId from request is used in other agentIds
        mapOfAgentIds.getOrDefault(false, Collections.emptyList()).parallelStream().filter(s -> subAgentIds.contains(s.getSubAgentId())).findAny()
                .ifPresent(s -> {
                    throw new JocBadRequestException("Subagent Id has to be unique per Controller: '" + s.getSubAgentId()
                            + "' is already used in Agent '" + s.getAgentId() + "'");
                });

        // checks java name rules of SubagentIds
        subAgentIds.forEach(id -> {
            CheckJavaVariableName.test("Subagent ID", id);
        });
        
        Set<String> existingSubagentClusters = clusterDbLayer.getSubagentClusterMembers(subAgentIds).stream().map(
                DBItemInventorySubAgentClusterMember::getSubAgentClusterId).collect(Collectors.toSet());

        // checks that director and standby director can only exist once
        List<SubagentDirectorType> direcs = Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR, SubagentDirectorType.SECONDARY_DIRECTOR);
        Predicate<SubAgent> isDirector = s -> direcs.contains(s.getIsDirector());
        subAgents.stream().filter(isDirector).collect(Collectors.groupingBy(SubAgent::getIsDirector, Collectors.counting())).forEach((k, v) -> {
            String directorType = k.equals(SubagentDirectorType.PRIMARY_DIRECTOR) ? "primary" : "standby";
            if (v > 1L) {
                throw new JocBadRequestException("At most one SubAgent can be a " + directorType + " director");
            }
        });

        // TODO check URL uniqueness?

        DBItemInventorySubAgentInstance primaryDirector = dbLayer.getDirectorInstance(agentId, SubagentDirectorType.PRIMARY_DIRECTOR.intValue());
        DBItemInventorySubAgentInstance standbyDirector = dbLayer.getDirectorInstance(agentId, SubagentDirectorType.SECONDARY_DIRECTOR.intValue());

        boolean primaryDirectorIsChanged = false;
        boolean standbyDirectorIsChanged = false;
        Date now = Date.from(Instant.now());

        Map<String, DBItemInventorySubAgentInstance> subAgentsOfCurAgent = mapOfAgentIds.getOrDefault(true, Collections.emptyList()).stream().collect(
                Collectors.toMap(DBItemInventorySubAgentInstance::getSubAgentId, Function.identity()));

        Set<DBItemInventorySubAgentInstance> curDbSubAgents = new HashSet<>();
        mapOfAgentIds = null;
        int position = -1;
        int clusterPosition = -1;
        if (!subAgents.isEmpty()) {
            position = dbLayer.getSubagentMaxOrdering();
            if (subAgents.stream().anyMatch(SubAgent::getWithGenerateSubagentCluster)) {
                InventorySubagentClustersDBLayer dbClusterLayer = new InventorySubagentClustersDBLayer(dbLayer.getSession());
                clusterPosition = dbClusterLayer.getMaxOrdering();
            }
        }

        for (SubAgent subAgent : subAgents) {
            DBItemInventorySubAgentInstance dbSubAgent = subAgentsOfCurAgent.remove(subAgent.getSubagentId());
            if (dbSubAgent == null) {
                // save
                if (primaryDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.PRIMARY_DIRECTOR)) {
                    primaryDirectorIsChanged = true;
                }
                if (standbyDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.SECONDARY_DIRECTOR)) {
                    standbyDirectorIsChanged = true;
                }
                DBItemInventorySubAgentInstance newDbSubAgent = new DBItemInventorySubAgentInstance();
                newDbSubAgent.setId(null);
                newDbSubAgent.setAgentId(agentId);
                newDbSubAgent.setSubAgentId(subAgent.getSubagentId());
                newDbSubAgent.setIsDirector(subAgent.getIsDirector());
                newDbSubAgent.setOrdering(++position);
                newDbSubAgent.setUri(subAgent.getUrl());
                newDbSubAgent.setIsWatcher(false);
                newDbSubAgent.setOsId(0L);
                newDbSubAgent.setTitle(subAgent.getTitle());
                newDbSubAgent.setDisabled(false);
                newDbSubAgent.setDeployed(false);
                newDbSubAgent.setTransaction("save");
                curDbSubAgents.add(newDbSubAgent);

            } else {
                // update (if subagent and dbSubangent unequal)
                if (dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector()) && dbSubAgent.getUri().equals(subAgent.getUrl())) {
                    if (subAgent.getWithGenerateSubagentCluster() && !existingSubagentClusters.contains(subAgent.getSubagentId())) {
                        saveNewSubAgentCluster(subAgent, agentId, dbLayer.getSession(), ++clusterPosition, now);
                    }
                    continue;
                }
                if (primaryDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.PRIMARY_DIRECTOR) && !subAgent.getSubagentId()
                        .equals(primaryDirector.getSubAgentId())) {
                    if (primaryDirectorIsChanged) {
                        throw new JocBadRequestException("At most one SubAgent can be a director");
                    } else if (!dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector())) {
                        primaryDirectorIsChanged = true;
                    }
                }
                if (standbyDirector != null && subAgent.getIsDirector().equals(SubagentDirectorType.SECONDARY_DIRECTOR) && !subAgent.getSubagentId()
                        .equals(standbyDirector.getSubAgentId())) {
                    if (standbyDirectorIsChanged) {
                        throw new JocBadRequestException("At most one SubAgent can be a standby director");
                    } else if (!dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector())) {
                        standbyDirectorIsChanged = true;
                    }
                }
                
                dbSubAgent.setIsDirector(subAgent.getIsDirector());
                dbSubAgent.setUri(subAgent.getUrl());
                dbSubAgent.setTitle(subAgent.getTitle());
                dbSubAgent.setDeployed(false);
                dbSubAgent.setTransaction("update");
                curDbSubAgents.add(dbSubAgent);
            }
            if (subAgent.getWithGenerateSubagentCluster() && !existingSubagentClusters.contains(subAgent.getSubagentId())) {
                saveNewSubAgentCluster(subAgent, agentId, dbLayer.getSession(), ++clusterPosition, now);
            }
        }

        if (primaryDirectorIsChanged || standbyDirectorIsChanged) {
            if (primaryDirectorIsChanged && primaryDirector != null) {
                primaryDirector.setIsDirector(SubagentDirectorType.NO_DIRECTOR.intValue());
                primaryDirector.setTransaction("update");
                primaryDirector.setDeployed(false);
                curDbSubAgents.add(primaryDirector);
            }
            if (standbyDirectorIsChanged && standbyDirector != null) {
                standbyDirector.setIsDirector(SubagentDirectorType.NO_DIRECTOR.intValue());
                standbyDirector.setTransaction("update");
                standbyDirector.setDeployed(false);
                curDbSubAgents.add(standbyDirector);
            }
        }

        String directorUrl = null;
        for (DBItemInventorySubAgentInstance item : curDbSubAgents) {
            if (item.getIsDirector() == SubagentDirectorType.PRIMARY_DIRECTOR.intValue()) {
                directorUrl = item.getUri();
            }
            item.setModified(now);
            if ("save".equals(item.getTransaction())) {
                dbLayer.getSession().save(item);
            } else if ("update".equals(item.getTransaction())) {
                dbLayer.getSession().update(item);
            }
        }
        if (directorUrl != null && !dbAgent.getUri().equals(directorUrl)) {
            dbAgent.setUri(directorUrl);
            dbLayer.updateAgent(dbAgent);
        }

    }
    
    private static void saveNewSubAgentCluster(SubAgent subAgent, String agentId, SOSHibernateSession connection, int position, Date now)
            throws SOSHibernateException {
            DBItemInventorySubAgentCluster dbSubagentCluster = new DBItemInventorySubAgentCluster();
            dbSubagentCluster.setId(null);
            dbSubagentCluster.setDeployed(false);
            dbSubagentCluster.setModified(now);
            dbSubagentCluster.setAgentId(agentId);
            dbSubagentCluster.setSubAgentClusterId(subAgent.getSubagentId());
            dbSubagentCluster.setTitle(subAgent.getTitle());
            dbSubagentCluster.setOrdering(position);

            DBItemInventorySubAgentClusterMember dbSubagentClusterMember = new DBItemInventorySubAgentClusterMember();
            dbSubagentClusterMember.setId(null);
            dbSubagentClusterMember.setModified(now);
            dbSubagentClusterMember.setPriority(0);
            dbSubagentClusterMember.setSubAgentClusterId(subAgent.getSubagentId());
            dbSubagentClusterMember.setSubAgentId(subAgent.getSubagentId());

            connection.save(dbSubagentCluster);
            connection.save(dbSubagentClusterMember);
    }
    
    private static String getUniquenessMsg(String key, Map.Entry<String, Long> e) {
        return key + " has to be unique: " + e.getKey() + " is used " + (e.getValue() == 2L ? "twice" : e.getValue() + " times");
    }
};