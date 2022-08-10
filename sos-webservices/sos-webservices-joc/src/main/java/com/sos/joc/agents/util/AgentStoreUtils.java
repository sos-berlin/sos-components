package com.sos.joc.agents.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.SubagentDirectorType;

public class AgentStoreUtils {

    public static void storeStandaloneAgent(Agent agent, String controllerId, boolean overwrite, InventoryAgentInstancesDBLayer dbLayer)
            throws SOSHibernateException {
        Map<String, Agent> agentMap = new HashMap<String, Agent>(1);
        agentMap.put(agent.getAgentId(), agent);
        storeStandaloneAgent(agentMap, controllerId, overwrite, dbLayer);
    }
    
    public static void storeStandaloneAgent(Map<String, Agent> agentMap, String controllerId, boolean overwrite,
            InventoryAgentInstancesDBLayer dbLayer) throws SOSHibernateException {
        List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAllAgents();
        Map<String, Set<DBItemInventoryAgentName>> allAliases = dbLayer.getAgentNameAliases(agentMap.keySet());
        int position = -1;
        if (dbAgents != null && !dbAgents.isEmpty()) {
            if (overwrite) {
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    if (position < dbAgent.getOrdering()) {
                        position = dbAgent.getOrdering();
                    }
                    Agent agentFound = agentMap.remove(dbAgent.getAgentId());
                    if (agentFound == null) {
                        continue;
                    }
                    if (!dbAgent.getControllerId().equals(controllerId)) {
                        throw new JocBadRequestException(String.format("Agent '%s' is already assigned for Controller '%s'", dbAgent.getAgentId(),
                                dbAgent.getControllerId()));
                    }
                    dbAgent.setHidden(agentFound.getHidden());
                    dbAgent.setAgentName(agentFound.getAgentName());
                    dbAgent.setTitle(agentFound.getTitle());
                    if (!dbAgent.getUri().equals(agentFound.getUrl())) {
                        dbAgent.setDeployed(false);
                    }
                    dbAgent.setUri(agentFound.getUrl());
                    dbLayer.updateAgent(dbAgent);
                    updateAliases(dbLayer, agentFound, allAliases.get(agentFound.getAgentId()));
                }
            }
        }
        for (Agent newAgent : agentMap.values()) {
            DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
            dbAgent.setId(null);
            dbAgent.setAgentId(newAgent.getAgentId());
            dbAgent.setAgentName(newAgent.getAgentName());
            dbAgent.setControllerId(controllerId);
            dbAgent.setHidden(newAgent.getHidden());
            dbAgent.setDisabled(false);
            dbAgent.setIsWatcher(false);
            dbAgent.setOsId(0L);
            dbAgent.setStartedAt(null);
            dbAgent.setUri(newAgent.getUrl());
            dbAgent.setVersion(null);
            dbAgent.setTitle(newAgent.getTitle());
            dbAgent.setDeployed(false);
            dbAgent.setOrdering(++position);
            dbLayer.saveAgent(dbAgent);
            updateAliases(dbLayer, newAgent, allAliases.get(newAgent.getAgentId()));
        }
    }
    
    public static void storeClusterAgent(ClusterAgent clusterAgent, String controllerId, boolean overwrite,
            InventoryAgentInstancesDBLayer agentDbLayer, InventorySubagentClustersDBLayer subagentDbLayer) throws SOSHibernateException {
        Map<String, ClusterAgent> clusterAgentMap = new HashMap<String, ClusterAgent>(1);
        clusterAgentMap.put(clusterAgent.getAgentId(), clusterAgent);
        Set<SubAgent> requestedSubagents = clusterAgent.getSubagents().stream().collect(Collectors.toSet());
        Set<String> requestedSubagentIds = requestedSubagents.stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());
        storeClusterAgent(clusterAgentMap, requestedSubagents, requestedSubagentIds, controllerId, overwrite, agentDbLayer, subagentDbLayer);
    }
    
    public static void storeClusterAgent(Map<String, ClusterAgent> clusterAgentMap, Set<SubAgent> requestedSubagents,
            Set<String> requestedSubagentIds, String controllerId, boolean overwrite, InventoryAgentInstancesDBLayer agentDbLayer, 
            InventorySubagentClustersDBLayer subagentDbLayer) throws SOSHibernateException {
        List<DBItemInventoryAgentInstance> dbAgents = agentDbLayer.getAllAgents();
        Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDbLayer.getAgentNameAliases(clusterAgentMap.keySet());
        List<DBItemInventorySubAgentInstance> dbSubAgents = agentDbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(
                controllerId));
        // check uniqueness of SubagentUrl with DB
        Set<String> requestedSubagentUrls = requestedSubagents.stream().map(SubAgent::getUrl).collect(Collectors.toSet());
        dbSubAgents.stream().filter(s -> !requestedSubagentIds.contains(s.getSubAgentId())).filter(s -> requestedSubagentUrls.contains(s
                .getUri())).findAny().ifPresent(s -> {
                    throw new JocBadRequestException(
                            String.format("Subagent url %s is already used by Subagent %s", s.getUri(), s.getSubAgentId()));
                });
        dbAgents.stream().filter(a -> a.getUri() != null && !a.getUri().isEmpty()).filter(a -> requestedSubagentUrls.contains(a.getUri()))
                .filter(a -> clusterAgentMap.get(a.getAgentId()) == null || !clusterAgentMap.get(a.getAgentId()).getSubagents().stream()
                .map(SubAgent::getUrl).collect(Collectors.toSet()).contains(a.getUri())).findAny().ifPresent(s -> {
                            throw new JocBadRequestException(
                                    String.format("Subagent url %s is already used by Agent %s", s.getUri(), s.getAgentId()));
                        });
        int position = -1;
        if (dbAgents != null && !dbAgents.isEmpty()) {
            for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                if (position < dbAgent.getOrdering()) {
                    position = dbAgent.getOrdering();
                }
                ClusterAgent agent = clusterAgentMap.remove(dbAgent.getAgentId());
                if (agent == null) {
                    continue;
                }
                if (!dbAgent.getControllerId().equals(controllerId)) {
                    throw new JocBadRequestException(String.format("Agent '%s' is already assigned for Controller '%s'", dbAgent.getAgentId(),
                            dbAgent.getControllerId()));
                }
                dbAgent.setHidden(agent.getHidden());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setTitle(agent.getTitle());
                agentDbLayer.updateAgent(dbAgent);
                
                AgentStoreUtils.saveOrUpdate(agentDbLayer, subagentDbLayer, dbAgent, dbSubAgents, agent.getSubagents(), overwrite);
                updateAliases(agentDbLayer, agent, allAliases.get(agent.getAgentId()));
            }
        }
        for (ClusterAgent agent : clusterAgentMap.values()) {
            DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
            dbAgent.setId(null);
            dbAgent.setAgentId(agent.getAgentId());
            dbAgent.setAgentName(agent.getAgentName());
            dbAgent.setControllerId(controllerId);
            dbAgent.setHidden(agent.getHidden());
            dbAgent.setIsWatcher(false);
            dbAgent.setOsId(0L);
            dbAgent.setUri(agent.getSubagents().get(0).getUrl());
            dbAgent.setStartedAt(null);
            dbAgent.setVersion(null);
            dbAgent.setTitle(agent.getTitle());
            dbAgent.setDeployed(false);
            dbAgent.setDisabled(false);
            dbAgent.setOrdering(++position);
            agentDbLayer.saveAgent(dbAgent);
            AgentStoreUtils.saveOrUpdate(agentDbLayer, subagentDbLayer, dbAgent, dbSubAgents, agent.getSubagents(), overwrite);
            updateAliases(agentDbLayer, agent, allAliases.get(agent.getAgentId()));
        }
    }
    
    public static List<DBItemInventorySubAgentCluster> storeSubagentCluster (SubagentCluster subagentCluster,
            InventorySubagentClustersDBLayer agentClusterDBLayer, Date modified) throws SOSHibernateException {
        Map<String, SubagentCluster> subagentClusterMap = new HashMap<String, SubagentCluster>(1);
        subagentClusterMap.put(subagentCluster.getSubagentClusterId(), subagentCluster);
        return storeSubagentCluster(subagentClusterMap, agentClusterDBLayer, modified);
    }
    
    public static List<DBItemInventorySubAgentCluster> storeSubagentCluster (Map<String, SubagentCluster> subagentClusterMap, 
            InventorySubagentClustersDBLayer agentClusterDBLayer, Date modified) throws SOSHibernateException {
        List<String> subagentClusterIds = subagentClusterMap.keySet().stream().collect(Collectors.toList());
        List<DBItemInventorySubAgentCluster> dbsubagentClusters = agentClusterDBLayer.getSubagentClusters(subagentClusterIds);
        List<DBItemInventorySubAgentClusterMember> dbsubagentClusterMembers = 
                agentClusterDBLayer.getSubagentClusterMembers(subagentClusterIds);
        
        Map<String, List<DBItemInventorySubAgentClusterMember>> dbsubagentClusterMembersMap = Collections.emptyMap();
        if (dbsubagentClusterMembers != null) {
            dbsubagentClusterMembersMap = dbsubagentClusterMembers.stream().collect(Collectors.groupingBy(
                    DBItemInventorySubAgentClusterMember::getSubAgentClusterId));
        }
        
        // update
        if (dbsubagentClusters != null) {
            for (DBItemInventorySubAgentCluster dbsubagentCluster : dbsubagentClusters) {
                SubagentCluster s = subagentClusterMap.remove(dbsubagentCluster.getSubAgentClusterId());
                if (!dbsubagentCluster.getAgentId().equals(s.getAgentId())) {
                    throw new JocBadRequestException(String.format("Subagent Cluster ID '%s' is already used for Agent '%s'",
                            dbsubagentCluster.getSubAgentClusterId(), dbsubagentCluster.getAgentId()));
                }
                dbsubagentCluster.setDeployed(false);
                dbsubagentCluster.setModified(modified);
                dbsubagentCluster.setTitle(s.getTitle());
                agentClusterDBLayer.getSession().update(dbsubagentCluster);
                updateMembers(agentClusterDBLayer.getSession(), dbsubagentClusterMembersMap, s.getSubagentIds(), s.getSubagentClusterId(),
                        modified);
            }
        }
        // insert
        int position = agentClusterDBLayer.getMaxOrdering();
        for (SubagentCluster s : subagentClusterMap.values()) {
            if (s.getSubagentIds().isEmpty()) { //don't store a new subagent cluster with an empty cluster
                continue;
            }
            DBItemInventorySubAgentCluster dbsubagentCluster = new DBItemInventorySubAgentCluster();
            dbsubagentCluster.setId(null);
            dbsubagentCluster.setDeployed(false);
            dbsubagentCluster.setModified(modified);
            dbsubagentCluster.setAgentId(s.getAgentId());
            dbsubagentCluster.setTitle(s.getTitle());
            dbsubagentCluster.setSubAgentClusterId(s.getSubagentClusterId());
            dbsubagentCluster.setOrdering(++position);
            agentClusterDBLayer.getSession().save(dbsubagentCluster);

            updateMembers(agentClusterDBLayer.getSession(), dbsubagentClusterMembersMap, s.getSubagentIds(), s.getSubagentClusterId(), modified);
        }
        return dbsubagentClusters;
    }
    
    public static void updateAliases(InventoryAgentInstancesDBLayer agentDBLayer, Agent agent, Collection<DBItemInventoryAgentName> dbAliases)
            throws SOSHibernateException {
        if (dbAliases != null) {
            for (DBItemInventoryAgentName dbAlias : dbAliases) {
                agentDBLayer.getSession().delete(dbAlias);
            }
        }
        // TODO read aliases to provide that per controller aliases should map unique to agentId
        // Collection<String> a = agentDBLayer.getAgentNamesByAgentIds(Collections.singleton(agent.getAgentId())).values();
        Set<String> aliases = agent.getAgentNameAliases();
        if (aliases != null && !aliases.isEmpty()) {
            aliases.remove(agent.getAgentName());
            for (String name : aliases) {
                DBItemInventoryAgentName a = new DBItemInventoryAgentName();
                a.setAgentId(agent.getAgentId());
                a.setAgentName(name);
                agentDBLayer.getSession().save(a);
            }
        }
    }
    
    public static String getUniquenessMsg(String key, Map.Entry<String, Long> e) {
        return key + " has to be unique: " + e.getKey() + " is used " + (e.getValue() == 2L ? "twice" : e.getValue() + " times");
    }
    
    // check uniqueness of AgentName/-aliases
    public static void checkUniquenessOfAgentNames(List<? extends Agent> agents) throws JocBadRequestException {
        agents.stream().map(a -> {
            if (a.getAgentNameAliases() == null) {
                a.setAgentNameAliases(Collections.singleton(a.getAgentName()));
            } else {
                a.getAgentNameAliases().add(a.getAgentName());
            }
            return a.getAgentNameAliases();
        }).flatMap(Set::stream).collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1L)
                .findAny().ifPresent(e -> {
                    throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("AgentName/-aliase", e));
                });
    }
    
    public static void saveOrUpdate(InventoryAgentInstancesDBLayer dbLayer, InventorySubagentClustersDBLayer clusterDbLayer,
            DBItemInventoryAgentInstance dbAgent, Collection<DBItemInventorySubAgentInstance> dbSubAgents, Collection<SubAgent> subAgents,
            boolean overwrite) throws SOSHibernateException {
        subAgents.stream().collect(Collectors.groupingBy(SubAgent::getSubagentId, Collectors.counting())).entrySet().stream()
                .filter(e -> e.getValue() > 1L).map(Map.Entry::getKey).findAny().ifPresent(sId -> {
                    throw new JocBadRequestException("Subagent ID '" + sId + "' must be unique per contoller");
                });
        // TODO URL has to be unique?
        // subAgents.stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(
        // e -> e.getValue() > 1L).map(Map.Entry::getKey).findAny().ifPresent(sUrl -> {
        // throw new JocBadRequestException("URL '" + sUrl + "' must be unique");
        // });
        String agentId = dbAgent.getAgentId();
        Map<Boolean, List<DBItemInventorySubAgentInstance>> mapOfAgentIds = dbSubAgents.stream().collect(
                Collectors.groupingBy(s -> s.getAgentId().equals(agentId)));
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
            SOSCheckJavaVariableName.test("Subagent ID", id);
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
                if (overwrite) {
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
    
    private static void updateMembers(SOSHibernateSession connection, 
            Map<String, List<DBItemInventorySubAgentClusterMember>> dbsubagentClusterMembersMap,
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
