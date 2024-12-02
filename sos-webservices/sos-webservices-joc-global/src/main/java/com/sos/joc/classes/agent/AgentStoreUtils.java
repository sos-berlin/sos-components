package com.sos.joc.classes.agent;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.agent.AgentVersionUpdatedEvent;
import com.sos.joc.event.bean.agent.SubagentVersionUpdatedEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.SubagentDirectorType;

import io.vavr.control.Either;
import js7.data.platform.PlatformInfo;
import js7.data_for_java.value.JExpression;

public class AgentStoreUtils {
    
    private static AgentStoreUtils instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStoreUtils.class);

    private AgentStoreUtils() {
        EventBus.getInstance().register(this);
        LOGGER.info("AgentStoreUtils has been registered for the EventBus.");
    }

    public static AgentStoreUtils getInstance() {
        if (instance == null) {
            instance = new AgentStoreUtils();
        }
        return instance;
    }
    
    public static void storeStandaloneAgent(Map<String, Agent> agentMap, String controllerId, boolean overwrite, boolean onlyAdd, boolean onlyEdit,
            InventoryAgentInstancesDBLayer dbLayer) throws SOSHibernateException {
        List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAllAgents();
        Set<String> agentNamesAndAliases = new HashSet<>();
        Map<String, String> allNames = dbAgents != null ? dbAgents.stream().filter(a -> a.getControllerId().equals(controllerId)).collect(Collectors
                .toMap(DBItemInventoryAgentInstance::getAgentId, DBItemInventoryAgentInstance::getAgentName)) : Collections.emptyMap();
        Map<String, Set<DBItemInventoryAgentName>> allAliases = !allNames.isEmpty() ? dbLayer.getAgentNameAliases(allNames.keySet()) : Collections
                .emptyMap();
        Map<String, Set<DBItemInventoryAgentName>> newAliases = new HashMap<>();
        Set<String> missedAgentNames = new HashSet<>();
        
        Map<DBItemInventoryAgentInstance, Agent> agentsForUpdate = new HashMap<>();
                
        int position = -1;
        if (dbAgents != null && !dbAgents.isEmpty()) {
            //if (overwrite) { //TODO that's wrong!!! used by import. If it is false -> all agent from agentMap will be inserted
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    if (position < dbAgent.getOrdering()) {
                        position = dbAgent.getOrdering();
                    }
                    Agent agentFound = agentMap.remove(dbAgent.getAgentId());
                    if (agentFound == null) {
                        continue;
                    }
                    
                    if (!overwrite) {
                        continue;
                    }
                    
                    if (!dbAgent.getControllerId().equals(controllerId)) {
                        throw new JocBadRequestException(String.format("Agent '%s' is already assigned to Controller '%s'", dbAgent.getAgentId(),
                                dbAgent.getControllerId()));
                    }
                    
                    missedAgentNames.addAll(checkUniquenessOfAgentNames(agentFound, allAliases, allNames));
                    
                    dbAgent.setHidden(agentFound.getHidden());
                    if (!dbAgent.getAgentName().equals(agentFound.getAgentName())) {
                        dbAgent.setAgentName(agentFound.getAgentName());
                        agentNamesAndAliases.add(agentFound.getAgentName());
                    }
                    dbAgent.setTitle(agentFound.getTitle());
                    if (!dbAgent.getUri().equals(agentFound.getUrl())) {
                        dbAgent.setDeployed(false);
                    }
                    if (!Optional.ofNullable(dbAgent.getProcessLimit()).equals(Optional.ofNullable(agentFound.getProcessLimit()))) {
                        dbAgent.setDeployed(false);
                    }
                    dbAgent.setUri(agentFound.getUrl());
                    dbAgent.setProcessLimit(agentFound.getProcessLimit());
                    agentsForUpdate.put(dbAgent, agentFound);
//                    dbLayer.updateAgent(dbAgent);
//                    newAliases.put(agentFound.getAgentId(), updateAliases(dbLayer, agentFound, allAliases.get(agentFound.getAgentId())));
                }
            //}
        }
        
        // TODO check if Agent is already stored as cluster agent???
        
        if (onlyAdd && !agentsForUpdate.isEmpty()) {
            if (agentsForUpdate.size() == 1) {
                throw new JocBadRequestException("Agent with ID '" + agentsForUpdate.keySet().iterator().next().getAgentId() + "' already exists.");
            } else {
                String s = agentsForUpdate.values().stream().map(Agent::getAgentId).collect(Collectors.joining("', '", "'", "'"));
                throw new JocBadRequestException("Agents with IDs " + s + " already exists.");
            }
        }
        
        if (onlyEdit && !agentMap.isEmpty()) {
            if (agentMap.size() == 1) {
                throw new JocBadRequestException("Agent with ID '" + agentMap.keySet().iterator().next() + "' doesn't exist.");
            } else {
                String s = agentMap.keySet().stream().collect(Collectors.joining("', '", "'", "'"));
                throw new JocBadRequestException("Agents with IDs " + s + " don't exist.");
            }
        }
        
        for (Map.Entry<DBItemInventoryAgentInstance, Agent> agentForUpdate : agentsForUpdate.entrySet()) {
            dbLayer.updateAgent(agentForUpdate.getKey());
            newAliases.put(agentForUpdate.getKey().getAgentId(), updateAliases(dbLayer, agentForUpdate.getValue(), allAliases.get(agentForUpdate
                    .getKey().getAgentId())));
        }
        
        for (Agent newAgent : agentMap.values()) {
            checkUniquenessOfAgentNames(newAgent, allAliases, allNames);
            
            DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
            dbAgent.setId(null);
            dbAgent.setAgentId(newAgent.getAgentId());
            dbAgent.setAgentName(newAgent.getAgentName());
            dbAgent.setControllerId(controllerId);
            dbAgent.setHidden(newAgent.getHidden());
            dbAgent.setDisabled(false);
            dbAgent.setIsWatcher(false);
            dbAgent.setOsId(0L);
            dbAgent.setProcessLimit(newAgent.getProcessLimit());
            dbAgent.setStartedAt(null);
            dbAgent.setUri(newAgent.getUrl());
            dbAgent.setVersion(null);
            dbAgent.setTitle(newAgent.getTitle());
            dbAgent.setDeployed(false);
            dbAgent.setOrdering(++position);
            dbLayer.saveAgent(dbAgent);
            newAliases.put(newAgent.getAgentId(), updateAliases(dbLayer, newAgent, allAliases.get(newAgent.getAgentId())));
            agentNamesAndAliases.add(newAgent.getAgentName());
        }
        
        agentNamesAndAliases = Stream.concat(agentNamesAndAliases.stream(), newAliases.values().stream().flatMap(s -> s.stream().map(
                DBItemInventoryAgentName::getAgentName))).collect(Collectors.toSet());
        AgentHelper.validateWorkflowsByAgentNames(dbLayer, agentNamesAndAliases, missedAgentNames);
    }
    
    public static void storeClusterAgent(Map<String, ClusterAgent> clusterAgentMap, Set<SubAgent> requestedSubagents,
            Set<String> requestedSubagentIds, String controllerId, boolean overwrite, boolean onlyAdd, boolean onlyEdit,
            InventoryAgentInstancesDBLayer agentDbLayer, InventorySubagentClustersDBLayer subagentDbLayer) throws SOSHibernateException {
        List<DBItemInventoryAgentInstance> dbAgents = agentDbLayer.getAllAgents();
        
        Map<String, String> allNames = dbAgents != null ? dbAgents.stream().filter(a -> a.getControllerId().equals(controllerId)).collect(Collectors
                .toMap(DBItemInventoryAgentInstance::getAgentId, DBItemInventoryAgentInstance::getAgentName)) : Collections.emptyMap();
        Map<String, Set<DBItemInventoryAgentName>> allAliases = !allNames.isEmpty() ? agentDbLayer.getAgentNameAliases(allNames.keySet())
                : Collections.emptyMap();

        Map<String, Set<DBItemInventoryAgentName>> newAliases = new HashMap<>();
        Set<String> missedAgentNames = new HashSet<>();
        Set<String> agentNamesAndAliases = new HashSet<>();
//        List<DBItemInventorySubAgentInstance> dbSubAgents = agentDbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(
//                controllerId));
        List<DBItemInventorySubAgentInstance> dbSubAgents = agentDbLayer.getSubAgentInstancesByControllerIds(null);
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
        
        // check uniqueness of (Sub-)AgentId with DB per controller
        dbAgents.stream().filter(a -> a.getControllerId().equals(controllerId)).map(DBItemInventoryAgentInstance::getAgentId).filter(
                aId -> requestedSubagentIds.contains(aId)).findAny().ifPresent(aId -> {
                    throw new JocBadRequestException(String.format("Subagent id '%s' is already used by an Agent", aId));
                });
        
        Map<DBItemInventoryAgentInstance, ClusterAgent> agentsForUpdate = new HashMap<>();
        
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
                
                if (!overwrite) {
                    continue; 
                }
                
                if (!dbAgent.getControllerId().equals(controllerId)) {
                    throw new JocBadRequestException(String.format("Agent '%s' is already assigned to Controller '%s'", dbAgent.getAgentId(),
                            dbAgent.getControllerId()));
                }
                
                missedAgentNames.addAll(checkUniquenessOfAgentNames(agent, allAliases, allNames));
                
                dbAgent.setHidden(agent.getHidden());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setTitle(agent.getTitle());
                if (!Optional.ofNullable(dbAgent.getProcessLimit()).equals(Optional.ofNullable(agent.getProcessLimit()))) {
                    dbAgent.setDeployed(false);
                }
                dbAgent.setProcessLimit(agent.getProcessLimit());
                agentNamesAndAliases.add(dbAgent.getAgentName());
                agentsForUpdate.put(dbAgent, agent);
//                agentDbLayer.updateAgent(dbAgent);
//                
//                saveOrUpdate(agentDbLayer, subagentDbLayer, controllerId, dbAgent, dbSubAgents, agent.getSubagents(), overwrite);
//                newAliases.put(agent.getAgentId(), updateAliases(agentDbLayer, agent, allAliases.get(agent.getAgentId())));
            }
        }
        
        // TODO check if Agent is already stored as standalone agent???
        if (onlyAdd && !agentsForUpdate.isEmpty()) {
            if (agentsForUpdate.size() == 1) {
                throw new JocBadRequestException("Agent with ID '" + agentsForUpdate.keySet().iterator().next().getAgentId() + "' already exists.");
            } else {
                String s = agentsForUpdate.values().stream().map(Agent::getAgentId).collect(Collectors.joining("', '", "'", "'"));
                throw new JocBadRequestException("Agents with IDs " + s + " already exists.");
            }
        }
        
        if (onlyEdit && !clusterAgentMap.isEmpty()) {
            if (clusterAgentMap.size() == 1) {
                throw new JocBadRequestException("Agent with ID '" + clusterAgentMap.keySet().iterator().next() + "' doesn't exist.");
            } else {
                String s = clusterAgentMap.keySet().stream().collect(Collectors.joining("', '", "'", "'"));
                throw new JocBadRequestException("Agents with IDs " + s + " don't exist.");
            }
        }
        
        for (Map.Entry<DBItemInventoryAgentInstance, ClusterAgent> agentForUpdate : agentsForUpdate.entrySet()) {
            agentDbLayer.updateAgent(agentForUpdate.getKey());
            saveOrUpdate(agentDbLayer, subagentDbLayer, controllerId, agentForUpdate.getKey(), dbSubAgents, agentForUpdate.getValue().getSubagents(),
                    overwrite, false, false);
            newAliases.put(agentForUpdate.getKey().getAgentId(), updateAliases(agentDbLayer, agentForUpdate.getValue(), allAliases.get(agentForUpdate
                    .getKey().getAgentId())));
        }
        
        for (ClusterAgent agent : clusterAgentMap.values()) {
            checkUniquenessOfAgentNames(agent, allAliases, allNames);
            
            DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
            dbAgent.setId(null);
            dbAgent.setAgentId(agent.getAgentId());
            dbAgent.setAgentName(agent.getAgentName());
            dbAgent.setControllerId(controllerId);
            dbAgent.setHidden(agent.getHidden());
            dbAgent.setIsWatcher(false);
            dbAgent.setOsId(0L);
            dbAgent.setProcessLimit(agent.getProcessLimit());
            dbAgent.setUri(agent.getSubagents().get(0).getUrl());
            dbAgent.setStartedAt(null);
            dbAgent.setVersion(null);
            dbAgent.setTitle(agent.getTitle());
            dbAgent.setDeployed(false);
            dbAgent.setDisabled(false);
            dbAgent.setOrdering(++position);
            agentDbLayer.saveAgent(dbAgent);
            agentNamesAndAliases.add(dbAgent.getAgentName());
            saveOrUpdate(agentDbLayer, subagentDbLayer, controllerId, dbAgent, dbSubAgents, agent.getSubagents(), overwrite, false, false);
            newAliases.put(agent.getAgentId(), updateAliases(agentDbLayer, agent, allAliases.get(agent.getAgentId())));
        }
        
        agentNamesAndAliases = Stream.concat(agentNamesAndAliases.stream(), newAliases.values().stream().flatMap(s -> s.stream().map(
                DBItemInventoryAgentName::getAgentName))).collect(Collectors.toSet());
        AgentHelper.validateWorkflowsByAgentNames(agentDbLayer, agentNamesAndAliases, missedAgentNames);
    }
    
    public static Set<String> storeSubagentCluster(List<SubagentCluster> subagentCluster, InventoryAgentInstancesDBLayer agentDbLayer,
            InventorySubagentClustersDBLayer agentClusterDBLayer, boolean overwrite, boolean onlyAdd, boolean onlyEdit) throws SOSHibernateException {
        
        Map<String, String> agentToControllerMap = agentDbLayer.getAllAgents().stream().collect(Collectors.toMap(
                DBItemInventoryAgentInstance::getAgentId, DBItemInventoryAgentInstance::getControllerId));

        Map<String, List<SubagentCluster>> subagentClustersPerController = subagentCluster.stream().peek(subA -> subA.setControllerId(
                agentToControllerMap.get(subA.getAgentId()))).collect(Collectors.groupingBy(SubagentCluster::getControllerId));
        Date now = Date.from(Instant.now());

        for (Map.Entry<String, List<SubagentCluster>> subagentClusterPerController : subagentClustersPerController.entrySet()) {

            checkSubagentCluster(subagentClusterPerController.getValue(), agentClusterDBLayer);

            Map<String, SubagentCluster> subagentMap = subagentCluster.stream().collect(Collectors.toMap(SubagentCluster::getSubagentClusterId,
                    Function.identity()));

            storeSubagentCluster(subagentClusterPerController.getKey(), subagentMap, agentClusterDBLayer, now, overwrite, onlyAdd, onlyEdit);
        }
        
        return subagentClustersPerController.keySet();
    }
    
    private static void checkSubagentCluster(List<SubagentCluster> subagentCluster, InventorySubagentClustersDBLayer agentClusterDBLayer) {
        Map<String, Long> subagentClusterIds = subagentCluster.stream().collect(Collectors.groupingBy(
                SubagentCluster::getSubagentClusterId, Collectors.counting()));

        // check uniqueness of SubagentClusterIds per Controller
        subagentClusterIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
            throw new JocBadRequestException(String.format("SubagentClusterId '%s' has to be unique per Controller", e.getKey()));
        });
        
        // check java name rules of SubagentClusterIds
        subagentClusterIds.keySet().forEach(sId -> SOSCheckJavaVariableName.test("Subagent Cluster ID", sId));
        
        // Check if all subagents in the inventory
        List<String> subagentIds = subagentCluster.stream().map(SubagentCluster::getSubagentIds).flatMap(List::stream).map(
                SubAgentId::getSubagentId).distinct().collect(Collectors.toList());
        String missingSubagentId = agentClusterDBLayer.getFirstSubagentIdThatNotExists(subagentIds);
        if (!missingSubagentId.isEmpty()) {
            throw new JocBadRequestException(String.format("At least one Subagent doesn't exist: '%s'", missingSubagentId));
        }

        // Check priority expressions
        subagentCluster.stream().map(SubagentCluster::getSubagentIds).flatMap(List::stream).map(SubAgentId::getPriority).map(
                JExpression::parse).filter(Either::isLeft).findAny().ifPresent(ProblemHelper::throwProblemIfExist);
    }
    
    private static List<DBItemInventorySubAgentCluster> storeSubagentCluster(String controllerId, Map<String, SubagentCluster> subagentClusterMap,
            InventorySubagentClustersDBLayer agentClusterDBLayer, Date modified, boolean overwrite, boolean onlyAdd, boolean onlyEdit)
            throws SOSHibernateException {
        List<String> subagentClusterIds = subagentClusterMap.keySet().stream().collect(Collectors.toList());
        List<DBItemInventorySubAgentCluster> dbsubagentClusters = agentClusterDBLayer.getSubagentClusters(controllerId, subagentClusterIds);
        List<DBItemInventorySubAgentClusterMember> dbsubagentClusterMembers = 
                agentClusterDBLayer.getSubagentClusterMembers(subagentClusterIds, controllerId);
        
        Map<String, List<DBItemInventorySubAgentClusterMember>> dbsubagentClusterMembersMap = Collections.emptyMap();
        if (dbsubagentClusterMembers != null) {
            dbsubagentClusterMembersMap = dbsubagentClusterMembers.stream().collect(Collectors.groupingBy(
                    DBItemInventorySubAgentClusterMember::getSubAgentClusterId));
        }
        
        // update
        if (dbsubagentClusters != null) {
            for (DBItemInventorySubAgentCluster dbsubagentCluster : dbsubagentClusters) {
                SubagentCluster s = subagentClusterMap.remove(dbsubagentCluster.getSubAgentClusterId());
                if (s == null) {
                    continue;
                }
                
                if (!overwrite) {
                    continue;
                }
                
                if (!dbsubagentCluster.getAgentId().equals(s.getAgentId())) {
                    throw new JocBadRequestException(String.format("Subagent Cluster ID '%s' is already used for Agent '%s'",
                            dbsubagentCluster.getSubAgentClusterId(), dbsubagentCluster.getAgentId()));
                }
                if (onlyAdd) {
                    throw new JocBadRequestException("Subagent Cluster ID '" + dbsubagentCluster.getSubAgentClusterId() + "' already exists.");
                }
                dbsubagentCluster.setControllerId(controllerId);
                dbsubagentCluster.setDeployed(false);
                dbsubagentCluster.setModified(modified);
                dbsubagentCluster.setTitle(s.getTitle());
                agentClusterDBLayer.getSession().update(dbsubagentCluster);
                updateMembers(agentClusterDBLayer.getSession(), controllerId, dbsubagentClusterMembersMap, s.getSubagentIds(), s.getSubagentClusterId(),
                        modified);
            }
        }
        // insert
        int position = agentClusterDBLayer.getMaxOrdering();
        for (SubagentCluster s : subagentClusterMap.values()) {
            if (s.getSubagentIds().isEmpty()) { //don't store a new subagent cluster with an empty cluster
                continue;
            }
            if (onlyEdit) {
                throw new JocBadRequestException("Subagent Cluster ID '" + s.getSubagentClusterId() + "' doesn't exist.");
            }
            DBItemInventorySubAgentCluster dbsubagentCluster = new DBItemInventorySubAgentCluster();
            dbsubagentCluster.setId(null);
            dbsubagentCluster.setControllerId(controllerId);
            dbsubagentCluster.setDeployed(false);
            dbsubagentCluster.setModified(modified);
            dbsubagentCluster.setAgentId(s.getAgentId());
            dbsubagentCluster.setTitle(s.getTitle());
            dbsubagentCluster.setSubAgentClusterId(s.getSubagentClusterId());
            dbsubagentCluster.setOrdering(++position);
            agentClusterDBLayer.getSession().save(dbsubagentCluster);

            updateMembers(agentClusterDBLayer.getSession(), controllerId, dbsubagentClusterMembersMap, s.getSubagentIds(), s.getSubagentClusterId(), modified);
        }
        return dbsubagentClusters;
    }
    
    public static Set<DBItemInventoryAgentName> updateAliases(InventoryAgentInstancesDBLayer agentDBLayer, Agent agent,
            Collection<DBItemInventoryAgentName> dbAliases) throws SOSHibernateException {
        if (dbAliases != null) {
            for (DBItemInventoryAgentName dbAlias : dbAliases) {
                agentDBLayer.getSession().delete(dbAlias);
            }
        }
        Set<DBItemInventoryAgentName> newAliases = new HashSet<>();
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
                newAliases.add(a);
            }
        }
        return newAliases;
    }
    
    public static String getUniquenessMsg(String key, Map.Entry<String, Long> e) {
        return key + " has to be unique: " + e.getKey() + " is used " + (e.getValue() == 2L ? "twice" : e.getValue() + " times");
    }
    
    // check uniqueness of AgentName/-aliases
    public static void checkUniquenessOfAgentNames(List<? extends Agent> agents) throws JocBadRequestException {
        agents.stream().map(a -> {
            Set<String> aliases = new HashSet<>();
            aliases.add(a.getAgentName());
            if (a.getAgentNameAliases() != null) {
                aliases.addAll(a.getAgentNameAliases());
            }
            return aliases;
        }).flatMap(Set::stream).collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream().filter(e -> e.getValue() > 1L)
                .findAny().ifPresent(e -> {
                    throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("AgentName/-aliase", e));
                });
    }
    
    public static void saveOrUpdate(InventoryAgentInstancesDBLayer dbLayer, InventorySubagentClustersDBLayer clusterDbLayer, String controllerId,
            DBItemInventoryAgentInstance dbAgent, Collection<DBItemInventorySubAgentInstance> dbSubAgents, Collection<SubAgent> subAgents,
            boolean overwrite, boolean onlyAdd, boolean onlyEdit) throws SOSHibernateException {
        subAgents.stream().collect(Collectors.groupingBy(SubAgent::getSubagentId, Collectors.counting())).entrySet().stream()
                .filter(e -> e.getValue() > 1L).map(Map.Entry::getKey).findAny().ifPresent(sId -> {
                    throw new JocBadRequestException("Subagent ID '" + sId + "' must be unique");
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
                    throw new JocBadRequestException("Subagent Id has to be unique: '" + s.getSubAgentId()
                            + "' is already used in Agent '" + s.getAgentId() + "'");
                });
        // checks java name rules of SubagentIds
        subAgentIds.forEach(id -> SOSCheckJavaVariableName.test("Subagent ID", id));
        
        Set<String> existingSubagentClusters = clusterDbLayer.getSubagentClusterMembers(subAgentIds, controllerId).stream().map(
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
                
                if (onlyEdit) {
                    throw new JocBadRequestException("Subagent with ID '" + subAgent.getSubagentId() + "' doesn't exist.");
                }
                
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
//                    // update (if subagent and dbSubangent unequal)
//                    if (dbSubAgent.getDirectorAsEnum().equals(subAgent.getIsDirector()) && dbSubAgent.getUri().equals(subAgent.getUrl())) {
//                        if (subAgent.getWithGenerateSubagentCluster() && !existingSubagentClusters.contains(subAgent.getSubagentId())) {
//                            saveNewSubAgentCluster(subAgent, agentId, dbLayer.getSession(), ++clusterPosition, now);
//                        }
//                        continue;
//                    }
                    
                    if (onlyAdd) {
                        throw new JocBadRequestException("Subagent with ID '" + subAgent.getSubagentId() + "' already exists.");
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
                saveNewSubAgentCluster(subAgent, controllerId, agentId, dbLayer.getSession(), ++clusterPosition, now);
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
    
    private static <T extends Agent> Set<String> checkUniquenessOfAgentNames(T agent, Map<String, Set<DBItemInventoryAgentName>> allAliases,
            Map<String, String> allNames) {
        
        Set<String> missingAliases = Collections.emptySet();
        
        for (Map.Entry<String, String> name : allNames.entrySet()) {
            Set<String> aliase = allAliases.getOrDefault(name.getKey(), Collections.emptySet()).stream().map(DBItemInventoryAgentName::getAgentName)
                    .collect(Collectors.toSet());
            aliase.add(name.getValue());

            if (name.getKey().equals(agent.getAgentId())) {
                if (agent.getAgentNameAliases() !=  null && !agent.getAgentNameAliases().isEmpty()) {
                    aliase.removeAll(agent.getAgentNameAliases());
                }
                aliase.remove(agent.getAgentName());
                missingAliases = aliase;
                continue;
            }

            if (aliase.contains(agent.getAgentName())) {
                throw new JocBadRequestException(String.format("Agent name '%s' is already used as name or alias by the agent '%s'", agent
                        .getAgentName(), name.getKey()));
            }
            if (agent.getAgentNameAliases() != null && !agent.getAgentNameAliases().isEmpty()) {
                aliase.retainAll(agent.getAgentNameAliases());
                if (!aliase.isEmpty()) {
                    if (aliase.size() == 1) {
                        throw new JocBadRequestException(String.format("Agent '%s' has already the alias '%s' as name or alias", name.getKey(), aliase
                                .iterator().next()));
                    } else {
                        throw new JocBadRequestException(String.format("Agent '%s' has already the aliases '%s' as name or aliases", name.getKey(),
                                aliase.toString()));
                    }
                }
            }
        }
        
        return missingAliases;
    }
    
    private static void saveNewSubAgentCluster(SubAgent subAgent, String controllerId, String agentId, SOSHibernateSession connection, int position, Date now)
            throws SOSHibernateException {
            DBItemInventorySubAgentCluster dbSubagentCluster = new DBItemInventorySubAgentCluster();
            dbSubagentCluster.setId(null);
            dbSubagentCluster.setControllerId(controllerId);
            dbSubagentCluster.setDeployed(false);
            dbSubagentCluster.setModified(now);
            dbSubagentCluster.setAgentId(agentId);
            dbSubagentCluster.setSubAgentClusterId(subAgent.getSubagentId());
            dbSubagentCluster.setTitle(subAgent.getTitle());
            dbSubagentCluster.setOrdering(position);
            DBItemInventorySubAgentClusterMember dbSubagentClusterMember = new DBItemInventorySubAgentClusterMember();
            dbSubagentClusterMember.setId(null);
            dbSubagentClusterMember.setControllerId(controllerId);
            dbSubagentClusterMember.setModified(now);
            dbSubagentClusterMember.setPriority("0");
            dbSubagentClusterMember.setSubAgentClusterId(subAgent.getSubagentId());
            dbSubagentClusterMember.setSubAgentId(subAgent.getSubagentId());
            connection.save(dbSubagentCluster);
            connection.save(dbSubagentClusterMember);
    }
    
    private static void updateMembers(SOSHibernateSession connection, String controllerId,
            Map<String, List<DBItemInventorySubAgentClusterMember>> dbsubagentClusterMembersMap, List<SubAgentId> subAgents, String subagentClusterId,
            Date now) throws SOSHibernateException {

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
                    if (member.getControllerId() == null || member.getControllerId().isEmpty()) {
                        member.setControllerId(controllerId);
                    }
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
            member.setControllerId(controllerId);
            member.setModified(now);
            member.setPriority(subAgent.getPriority());
            member.setSubAgentId(subAgent.getSubagentId());
            member.setSubAgentClusterId(subagentClusterId);
            connection.save(member);
        }
    }
    
    @Subscribe({ AgentVersionUpdatedEvent.class })
    public static void updateAgentVersion(AgentVersionUpdatedEvent event) {
        SOSHibernateSession connection = null;
        LOGGER.trace("AgentReadyEvent received -> update version of agent instance if neccessary.");
        try {
            connection = initDBConnection();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            hql.append(" where agentId=:agentId ");
            Query<DBItemInventoryAgentInstance> query = connection.createQuery(hql.toString());
            query.setParameter("agentId", event.getAgentId());
            query.setMaxResults(1);
            DBItemInventoryAgentInstance result = connection.getSingleResult(query);
            boolean updated = false;
            if(result != null) {
                LOGGER.trace("result.getVersion() : " + result.getVersion());
                LOGGER.trace("event.getVersion() : " + event.getVersion());
                if(result.getVersion() == null || !result.getVersion().equals(event.getVersion())) {
                    result.setVersion(event.getVersion());
                    updated = true;
                }
                LOGGER.trace("result.getJavaVersion() : " + result.getJavaVersion());
                LOGGER.trace("event.getJavaVersion() : " + event.getJavaVersion());
                if(result.getJavaVersion() == null || !result.getJavaVersion().equals(event.getJavaVersion())) {
                    result.setJavaVersion(event.getJavaVersion());
                    updated = true;
                }
                if(updated) {
                    connection.update(result);
                    LOGGER.trace("agent version updated to version " + event.getVersion());
                }
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } finally {
            closeDBConnection(connection);
        }
    }
    
    public static void updateAgentVersion(String agentId, Optional<PlatformInfo> pOpt) {
        pOpt.ifPresent(p -> updateAgentVersion(new AgentVersionUpdatedEvent(agentId, p.js7Version().string(), p.java().version())));
    }

    @Subscribe({ SubagentVersionUpdatedEvent.class })
    public static void updateSubagentVersion(SubagentVersionUpdatedEvent event) {
        SOSHibernateSession connection = null;
        LOGGER.trace("AgentReadyEvent received -> update version of agent instance if neccessary.");
        try {
            connection = initDBConnection();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            hql.append(" where subAgentId=:subagentId");
            Query<DBItemInventorySubAgentInstance> query = connection.createQuery(hql.toString());
            query.setParameter("subagentId", event.getSubagentId());
            query.setMaxResults(1);
            DBItemInventorySubAgentInstance result = connection.getSingleResult(query);
            boolean updated = false;
            if(result != null) {
                LOGGER.trace("result.getVersion() : " + result.getVersion());
                LOGGER.trace("event.getVersion() : " + event.getVersion());
                if(result.getVersion() == null || !result.getVersion().equals(event.getVersion())) {
                    result.setVersion(event.getVersion());
                    updated = true;
                }
                LOGGER.trace("result.getJavaVersion() : " + result.getJavaVersion());
                LOGGER.trace("event.getJavaVersion() : " + event.getJavaVersion());
                if(result.getJavaVersion() == null || !result.getJavaVersion().equals(event.getJavaVersion())) {
                    result.setJavaVersion(event.getJavaVersion());
                    updated = true;
                }
                if(updated) {
                    connection.update(result);
                    LOGGER.trace("agent version updated to version " + event.getVersion());
                }
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } finally {
            closeDBConnection(connection);
        }
    }
    
    public static void updateSubagentVersion(String agentId, String subagentId, Optional<PlatformInfo> pOpt) {
        pOpt.ifPresent(p -> updateSubagentVersion(new SubagentVersionUpdatedEvent(agentId, subagentId, p.js7Version().string(), p.java().version())));
    }

    private static SOSHibernateSession initDBConnection() {
        return Globals.createSosHibernateStatelessConnection(AgentStoreUtils.class.getSimpleName());
    }
    
    private static void closeDBConnection(SOSHibernateSession connection) {
        Globals.disconnect(connection);
    }
}