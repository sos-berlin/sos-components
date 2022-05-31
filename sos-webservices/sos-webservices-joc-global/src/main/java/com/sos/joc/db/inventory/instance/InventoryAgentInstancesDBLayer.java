package com.sos.joc.db.inventory.instance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.items.SubAgentItem;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.agent.SubagentDirectorType;

public class InventoryAgentInstancesDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
    private boolean withAgentOrdering = false;

    public InventoryAgentInstancesDBLayer(SOSHibernateSession conn) {
        super(conn);
    }
    
    public void setWithAgentOrdering(boolean withAgentOrdering) {
        this.withAgentOrdering = withAgentOrdering; 
    }

    public DBItemInventoryAgentInstance getAgentInstance(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id == null) {
                return null;
            }
            return getSession().get(DBItemInventoryAgentInstance.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getUrisOfVisibleClusterWatcherByControllerId(String controllerId) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
        if (controllerId == null || controllerId.isEmpty()) {
            return null;
        }
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("select uri from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            hql.append(" where controllerId = :controllerId");
            hql.append(" and isWatcher = 1");
            hql.append(" and hidden = 0");

            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            return getSession().getResultList(query);
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryAgentInstance> getAgentsByControllerIds(Collection<String> controllerIds) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        return getAgentsByControllerIds(controllerIds, false, false);
    }

    public List<DBItemInventoryAgentInstance> getAgentsByControllerIds(Collection<String> controllerIds, boolean onlyWatcher,
            boolean onlyVisibleAgents) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getAgentsByControllerIdAndAgentIds(controllerIds, null, onlyWatcher, onlyVisibleAgents);
    }

    public List<DBItemInventoryAgentInstance> getAgentsByControllerIdAndAgentIds(Collection<String> controllerIds, List<String> agentIds,
            boolean onlyWatcher, boolean onlyVisibleAgents) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        if (agentIds != null && agentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventoryAgentInstance> r = new ArrayList<>();
            for (int i = 0; i < agentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getAgentsByControllerIdAndAgentIds(controllerIds, SOSHibernate.getInClausePartition(i, agentIds), onlyWatcher,
                        onlyVisibleAgents));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                List<String> clauses = new ArrayList<>();
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    if (controllerIds.size() == 1) {
                        clauses.add("controllerId = :controllerId");
                    } else {
                        clauses.add("controllerId in (:controllerIds)");
                    }
                }
                if (agentIds != null && !agentIds.isEmpty()) {
                    clauses.add("agentId in (:agentIds)");
                }
                if (onlyWatcher) {
                    clauses.add("isWatcher = 1");
                }
                if (onlyVisibleAgents) {
                    clauses.add("hidden = 0");
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
                if (withAgentOrdering) {
                    hql.append(" order by ordering");
                }
                Query<DBItemInventoryAgentInstance> query = getSession().createQuery(hql.toString());
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    if (controllerIds.size() == 1) {
                        query.setParameter("controllerId", controllerIds.iterator().next());
                    } else {
                        query.setParameterList("controllerIds", controllerIds);
                    }
                }
                if (agentIds != null && !agentIds.isEmpty()) {
                    query.setParameterList("agentIds", agentIds);
                }
                List<DBItemInventoryAgentInstance> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }
                return result;
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }

    public List<DBItemInventoryAgentInstance> getAgentsByControllerIdAndAgentIdsAndUrls(Collection<String> controllerIds, Collection<String> agentIds,
            Collection<String> agentUrls, boolean onlyWatcher, boolean onlyVisibleAgents) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
        boolean withAgentIds = agentIds != null && !agentIds.isEmpty();
        boolean withAgentUrls = agentUrls != null && !agentUrls.isEmpty();
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            List<String> clauses = new ArrayList<>();
            if (controllerIds != null && !controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    clauses.add("controllerId = :controllerId");
                } else {
                    clauses.add("controllerId in (:controllerIds)");
                }
            }
            if (withAgentIds || withAgentUrls) {
                if (withAgentIds && withAgentUrls) {
                    clauses.add("(agentId in (:agentIds) or uri in (:agentUrls))");
                } else if (withAgentIds) {
                    clauses.add("agentId in (:agentIds)");
                } else {
                    clauses.add("uri in (:agentUrls)");
                }
            }
            if (onlyWatcher) {
                clauses.add("isWatcher = 1");
            }
            if (onlyVisibleAgents) {
                clauses.add("hidden = 0");
            }
            if (!clauses.isEmpty()) {
                hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            Query<DBItemInventoryAgentInstance> query = getSession().createQuery(hql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    query.setParameter("controllerId", controllerIds.iterator().next());
                } else {
                    query.setParameterList("controllerIds", controllerIds);
                }
            }
            if (withAgentIds) {
                query.setParameterList("agentIds", agentIds);
            }
            if (withAgentUrls) {
                query.setParameterList("agentUrls", agentUrls);
            }
            return getSession().getResultList(query);
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Set<String> getVisibleAgentNames() throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getAgentNames(null, true, true);
    }

    public Set<String> getVisibleAgentNames(Collection<String> controllerIds, boolean withClusterLicense) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        return getAgentNames(controllerIds, true, withClusterLicense);
    }
    
    public Map<String, List<String>> getAgentNamesPerAgentId(Collection<String> controllerIds, boolean onlyVisibleAgents)
            throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        try {
            List<DBItemInventoryAgentInstance> agents = getAgentsByControllerIds(controllerIds, false, onlyVisibleAgents);
            if (agents == null || agents.isEmpty()) {
                return Collections.emptyMap();
            }
            
            Map<String, List<String>> namesPerAgentId = agents.stream().collect(Collectors.groupingBy(DBItemInventoryAgentInstance::getAgentId, Collectors.mapping(
                    DBItemInventoryAgentInstance::getAgentName, Collectors.toList())));

            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
            if ((controllerIds != null && !controllerIds.isEmpty()) || onlyVisibleAgents) {
                List<String> clauses = new ArrayList<>(2);
                hql.append(" where agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    if (controllerIds.size() == 1) {
                        clauses.add("controllerId = :controllerId");
                    } else {
                        clauses.add("controllerId in (:controllerIds)");
                    }
                }
                if (onlyVisibleAgents) {
                    clauses.add("hidden = 0");
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
                hql.append(")");
            }
            Query<DBItemInventoryAgentName> query = getSession().createQuery(hql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    query.setParameter("controllerId", controllerIds.iterator().next());
                } else {
                    query.setParameterList("controllerIds", controllerIds);
                }
            }
            
            List<DBItemInventoryAgentName> result = query.getResultList();
            if (result != null) {
                result.stream().collect(Collectors.groupingBy(DBItemInventoryAgentName::getAgentId, Collectors.mapping(
                        DBItemInventoryAgentName::getAgentName, Collectors.toList()))).forEach((agentId, agentNames) -> namesPerAgentId.get(agentId)
                                .addAll(agentNames));
            }
            
            return namesPerAgentId;
            
//            Map<String, List<String>> subAgentIdsPerAgentId = getSubagentClusters(controllerIds).stream().collect(Collectors.groupingBy(
//                    DBItemInventorySubAgentCluster::getAgentId, Collectors.mapping(DBItemInventorySubAgentCluster::getSubAgentClusterId, Collectors
//                            .toList())));
//
//            // determine subagentClusterIds
//            
//            if (!withClusterLicense) {
//                List<String> clusterAgentIds = getClusterAgentIds(controllerIds, onlyVisibleAgents);
//                agents = agents.stream().filter(a -> !clusterAgentIds.contains(a.getAgentId())).collect(Collectors.toList());
//            }
//            Set<String> agentNames = agents.stream().map(DBItemInventoryAgentInstance::getAgentName).filter(Objects::nonNull).collect(Collectors
//                    .toSet());
//            StringBuilder hql = new StringBuilder();
//            hql.append("select agentName from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
//            hql.append(" where agentId in (:agentIds)");
//            Query<String> query = getSession().createQuery(hql.toString());
//            query.setParameterList("agentIds", agents.stream().map(DBItemInventoryAgentInstance::getAgentId).collect(Collectors.toSet()));
//            List<String> aliases = getSession().getResultList(query);
//            if (aliases != null && !aliases.isEmpty()) {
//                agentNames.addAll(aliases);
//            }
//            return agentNames.stream().sorted().collect(Collectors.toSet());
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Set<String> getAgentNames(Collection<String> controllerIds, boolean onlyVisibleAgents, boolean withClusterLicense)
            throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        try {
            List<DBItemInventoryAgentInstance> agents = getAgentsByControllerIds(controllerIds, false, onlyVisibleAgents);
            if (agents == null || agents.isEmpty()) {
                return Collections.emptySet();
            }
            if (!withClusterLicense) {
                List<String> clusterAgentIds = getClusterAgentIds(controllerIds, onlyVisibleAgents);
                agents = agents.stream().filter(a -> !clusterAgentIds.contains(a.getAgentId())).collect(Collectors.toList());
            }
            Set<String> agentNames = agents.stream().map(DBItemInventoryAgentInstance::getAgentName).filter(Objects::nonNull).collect(Collectors
                    .toSet());
            StringBuilder hql = new StringBuilder();
            hql.append("select agentName from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
            hql.append(" where agentId in (:agentIds)");
            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameterList("agentIds", agents.stream().map(DBItemInventoryAgentInstance::getAgentId).collect(Collectors.toSet()));
            List<String> aliases = getSession().getResultList(query);
            if (aliases != null && !aliases.isEmpty()) {
                agentNames.addAll(aliases);
            }
            return agentNames.stream().sorted().collect(Collectors.toSet());
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<String, Map<String, Set<String>>> getAgentWithAliasesByControllerIds(Collection<String> controllerIds) {
        try {
            List<DBItemInventoryAgentInstance> agentIds = getAgentsByControllerIds(controllerIds);
            Map<String, String> agentIDWithAgentName = agentIds.stream().collect(
                    Collectors.toMap(DBItemInventoryAgentInstance::getAgentId, DBItemInventoryAgentInstance::getAgentName, (K,V) -> V));
            Map<String, Set<String>> agentIdsByControllerId = agentIds.stream().collect(
                            Collectors.groupingBy(DBItemInventoryAgentInstance::getControllerId, 
                                    Collectors.mapping(DBItemInventoryAgentInstance::getAgentId, Collectors.toSet())));
            Map<String, Map<String, Set<String>>> agentIdsWithAliasesByControllerIds = new HashMap<>();
            agentIdsByControllerId.forEach((K,V) -> {
                Map<String, Set<String>> a = getAgentNameAliasesByAgentIds(V);
                Map<String, Set<String>> b = new HashMap<>(); 
                for (String agentId : V) {
                    if (a.containsKey(agentId)) {
                        a.get(agentId).add(agentIDWithAgentName.get(agentId));
                    } else {
                        b.put(agentId, Collections.singleton(agentIDWithAgentName.get(agentId)));
                    }
                }
                if (!b.isEmpty()) {
                    if (a != null && !a.isEmpty()) {
                        a.putAll(b);
                    } else {
                        a = b;
                    }
                }
                agentIdsWithAliasesByControllerIds.put(K, a);
            });
            return agentIdsWithAliasesByControllerIds;
        } catch (Exception e) {
            throw e;
        }
    }

    public Map<String, Set<String>> getAgentNameAliasesByAgentIds(Collection<String> agentIds) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
            if (agentIds != null && !agentIds.isEmpty()) {
                hql.append(" where agentId in (:agentIds)");
            }
            Query<DBItemInventoryAgentName> query = getSession().createQuery(hql.toString());
            if (agentIds != null && !agentIds.isEmpty()) {
                query.setParameterList("agentIds", agentIds);
            }
            List<DBItemInventoryAgentName> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().collect(
                        Collectors.groupingBy(DBItemInventoryAgentName::getAgentId, 
                                Collectors.mapping(DBItemInventoryAgentName::getAgentName, Collectors.toSet())));
            }
            return Collections.emptyMap();
        } catch (DBMissingDataException e) {
            throw e;
        } catch (SOSHibernateInvalidSessionException e) {
            throw new DBConnectionRefusedException(e);
        } catch (Exception e) {
            throw new DBInvalidDataException(e);
        }
    }

    public Map<String, Set<String>> getAgentNamesByAgentIds(Collection<String> controllerIds) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
            if (controllerIds != null && !controllerIds.isEmpty()) {
                hql.append(" where agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                hql.append(" where controllerId in (:controllerIds))");
            }
            Query<DBItemInventoryAgentName> query = getSession().createQuery(hql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                query.setParameterList("controllerIds", controllerIds);
            }
            List<DBItemInventoryAgentName> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().collect(Collectors.groupingBy(DBItemInventoryAgentName::getAgentId, Collectors.mapping(
                        DBItemInventoryAgentName::getAgentName, Collectors.toSet())));
            }
            return Collections.emptyMap();
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Set<String> getAgentNamesByAgentIds(String agentId) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        if (agentId == null || agentId.isEmpty()) {
            return null;
        }
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
            hql.append(" where agentId = :agentId)");
            Query<DBItemInventoryAgentName> query = getSession().createQuery(hql.toString());
            query.setParameter("agentId", agentId);
            List<DBItemInventoryAgentName> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(DBItemInventoryAgentName::getAgentName).collect(Collectors.toSet());
            }
            return null;
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<String, Set<DBItemInventoryAgentName>> getAgentNameAliases(Collection<String> agentIds) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
            if (agentIds != null && !agentIds.isEmpty()) {
                hql.append(" where agentId in (:agentIds)");
            }
            Query<DBItemInventoryAgentName> query = getSession().createQuery(hql.toString());
            if (agentIds != null && !agentIds.isEmpty()) {
                query.setParameterList("agentIds", agentIds);
            }
            List<DBItemInventoryAgentName> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().collect(Collectors.groupingBy(DBItemInventoryAgentName::getAgentId, Collectors.toSet()));
            }
            return Collections.emptyMap();
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long saveAgent(DBItemInventoryAgentInstance agent) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            agent.setModified(Date.from(Instant.now()));
            getSession().save(agent);
            return agent.getId();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long updateAgent(DBItemInventoryAgentInstance agent) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            agent.setModified(Date.from(Instant.now()));
            getSession().update(agent);
            return agent.getId();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public boolean agentIdAlreadyExists(Collection<String> agentIds, String controllerId) throws DBInvalidDataException, DBConnectionRefusedException,
            JocObjectAlreadyExistException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            sql.append(" where agentId in (:agentIds)");
            sql.append(" and controllerId != :controllerId");

            Query<String> query = getSession().createQuery(sql.toString());
            query.setParameterList("agentIds", agentIds);
            query.setParameter("controllerId", controllerId);

            List<String> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                if (result.size() == 1) {
                    throw new JocObjectAlreadyExistException("Agent Id " + result.get(0) + " already in use.");
                } else {
                    throw new JocObjectAlreadyExistException("Agent Ids " + result.toString() + " already in use.");
                }
            }
            return false;
        } catch (JocObjectAlreadyExistException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void deleteInstance(DBItemInventoryAgentInstance agent) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (agent != null) {
                getSession().delete(agent);
                deleteSubAgents(agent.getAgentId());
                deleteAliase(agent.getAgentId());
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private Integer deleteAliase(String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
        hql.append(" where agentId = :agentId");
        Query<Integer> query = getSession().createQuery(hql.toString());
        query.setParameter("agentId", agentId);
        return getSession().executeUpdate(query);
    }

    public DBItemInventoryAgentInstance getAgentInstance(String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
        hql.append(" where agentId = :agentId");
        Query<DBItemInventoryAgentInstance> query = getSession().createQuery(hql.toString());
        query.setParameter("agentId", agentId);
        return getSession().getSingleResult(query);
    }

    public List<DBItemInventorySubAgentInstance> getSubAgentInstancesByAgentId(String agentId) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(" where agentId = :agentId");
            Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
            query.setParameter("agentId", agentId);
            List<DBItemInventorySubAgentInstance> result = getSession().getResultList(query);
            if (result != null) {
                return result;
            }
            return Collections.emptyList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<String, Map<SubagentDirectorType, DBItemInventorySubAgentInstance>> getDirectorInstances(Collection<String> controllerIds) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            hql.append(" where isDirector in (:isDirectors)");
            if (controllerIds != null && !controllerIds.isEmpty()) {
                hql.append(" and agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                hql.append(" where controllerId in (:controllerIds))");
            }
            Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                query.setParameterList("controllerIds", controllerIds);
            }
            query.setParameterList("isDirectors", Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR.intValue(),
                    SubagentDirectorType.SECONDARY_DIRECTOR.intValue()));
            List<DBItemInventorySubAgentInstance> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.groupingBy(DBItemInventorySubAgentInstance::getAgentId, Collectors.toMap(
                        DBItemInventorySubAgentInstance::getDirectorAsEnum, Function.identity(), (k, v) -> v)));
            }
            return Collections.emptyMap();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemInventorySubAgentInstance getDirectorInstance(String agentId, Integer isDirector) {
        try {
            if (isDirector == null || !Arrays.asList(1, 2).contains(isDirector)) {
                return null;
            }
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(" where agentId = :agentId");
            hql.append(" and isDirector = :isDirector");
            Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
            query.setParameter("agentId", agentId);
            query.setParameter("isDirector", isDirector);
            return getSession().getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getDirectorSubAgentIds(String agentId) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            if (agentId != null && !agentId.isEmpty()) {
                hql.append(" where agentId = :agentId");
                hql.append(" and isDirector in (:isDirectors) order by isDirector");
            } else {
                hql.append(" where isDirector in (:isDirectors)");
            }
            Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
            if (agentId != null && !agentId.isEmpty()) {
                query.setParameter("agentId", agentId);
            }
            query.setParameterList("isDirectors", Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR.intValue(),
                    SubagentDirectorType.SECONDARY_DIRECTOR.intValue()));
            List<DBItemInventorySubAgentInstance> result = getSession().getResultList(query);
            if (result == null || result.isEmpty() || (result != null && result.size() == 1 && SubagentDirectorType.SECONDARY_DIRECTOR
                    .intValue() == result.get(0).getIsDirector())) {
                throw new DBMissingDataException("The Agent '" + agentId + "' must have a primary director.");
            }
            return result.stream().map(DBItemInventorySubAgentInstance::getSubAgentId).collect(Collectors.toList());
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<SubAgentItem> getDirectorSubAgentIdsByControllerId(String controllerId, List<String> subagentIds) {
        if (subagentIds == null) {
            subagentIds = Collections.emptyList();
        }
        try {
            if (subagentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
                List<SubAgentItem> s = new ArrayList<>();
                for (int i = 0; i < subagentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    s.addAll(getDirectorSubAgentIdsByControllerId(controllerId, SOSHibernate.getInClausePartition(i, subagentIds)));
                }
                return s;
            } else {
                StringBuilder hql = new StringBuilder("select new ").append(SubAgentItem.class.getName());
                hql.append("(agentId, isDirector, subAgentId, ordering) from ");
                hql.append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
                hql.append(" where isDirector in (:isDirectors)");
                hql.append(" and agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(
                        " where controllerId = :controllerId)");
                if (!subagentIds.isEmpty()) {
                    hql.append(" and  subAgentId in (:subagentIds)");
                }
                Query<SubAgentItem> query = getSession().createQuery(hql.toString());
                query.setParameter("controllerId", controllerId);
                if (!subagentIds.isEmpty()) {
                    query.setParameterList("subagentIds", subagentIds);
                }
                query.setParameterList("isDirectors", Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR.intValue(),
                        SubagentDirectorType.SECONDARY_DIRECTOR.intValue()));
                List<SubAgentItem> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }
                return result;
            }
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getSubAgentIdsByAgentId(String agentId) {
        try {
            StringBuilder hql = new StringBuilder("select subAgentId from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(
                    " where agentId = :agentId");
            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameter("agentId", agentId);
            List<String> result = getSession().getResultList(query);
            if (result != null) {
                return result;
            }
            return Collections.emptyList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<String, List<DBItemInventorySubAgentInstance>> getSubAgentInstancesByControllerIds(Collection<String> controllerIds,
            boolean onlyWatcher, boolean onlyVisibleAgents) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            if ((controllerIds != null && !controllerIds.isEmpty()) || onlyWatcher || onlyVisibleAgents) {
                List<String> clauses = new ArrayList<>(3);
                hql.append(" where agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    if (controllerIds.size() == 1) {
                        clauses.add("controllerId = :controllerId");
                    } else {
                        clauses.add("controllerId in (:controllerIds)");
                    }
                }
                if (onlyWatcher) {
                    clauses.add("isWatcher = 1");
                }
                if (onlyVisibleAgents) {
                    clauses.add("hidden = 0");
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
                hql.append(")");
            }
            if (withAgentOrdering) {
                hql.append(" order by ordering");
            }
            Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    query.setParameter("controllerId", controllerIds.iterator().next());
                } else {
                    query.setParameterList("controllerIds", controllerIds);
                }
            }
            List<DBItemInventorySubAgentInstance> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.groupingBy(DBItemInventorySubAgentInstance::getAgentId));
            }
            return Collections.emptyMap();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getClusterAgentIds(Collection<String> controllerIds, boolean onlyVisibleAgents) {
        try {
            StringBuilder hql = new StringBuilder("select agentId from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            if ((controllerIds != null && !controllerIds.isEmpty()) || onlyVisibleAgents) {
                List<String> clauses = new ArrayList<>(2);
                hql.append(" where agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    if (controllerIds.size() == 1) {
                        clauses.add("controllerId = :controllerId");
                    } else {
                        clauses.add("controllerId in (:controllerIds)");
                    }
                }
                if (onlyVisibleAgents) {
                    clauses.add("hidden = 0");
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
                hql.append(")");
            }
            hql.append(" group by agentId");
            Query<String> query = getSession().createQuery(hql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    query.setParameter("controllerId", controllerIds.iterator().next());
                } else {
                    query.setParameterList("controllerIds", controllerIds);
                }
            }
            List<String> result = getSession().getResultList(query);
            if (result != null) {
                return result;
            }
            return Collections.emptyList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventorySubAgentInstance> getSubAgentInstancesByControllerIds(Collection<String> controllerIds) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            if (controllerIds != null && !controllerIds.isEmpty()) {
                hql.append(" where agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                if (controllerIds.size() == 1) {
                    hql.append(" where controllerId = :controllerId");
                } else {
                    hql.append(" where controllerId in (:controllerIds)");
                }
                hql.append(")");
            }
            if (withAgentOrdering) {
                hql.append(" order by ordering");
            }
            Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                if (controllerIds.size() == 1) {
                    query.setParameter("controllerId", controllerIds.iterator().next());
                } else {
                    query.setParameterList("controllerIds", controllerIds);
                }
            }
            List<DBItemInventorySubAgentInstance> result = getSession().getResultList(query);
            if (result != null) {
                return result;
            }
            return Collections.emptyList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemInventorySubAgentInstance solveAgentWithoutSubAgent(DBItemInventoryAgentInstance agent) {
        DBItemInventorySubAgentInstance subAgent = new DBItemInventorySubAgentInstance();
        subAgent.setAgentId(agent.getAgentId());
        subAgent.setSubAgentId(agent.getAgentId());
        subAgent.setUri(agent.getUri());
        subAgent.setIsDirector(SubagentDirectorType.PRIMARY_DIRECTOR.intValue());
        return subAgent;
    }

    private int deleteSubAgents(String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(" where agentId =: agentId");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("agentId", agentId);
        return getSession().executeUpdate(query);
    }

    public int deleteSubAgents(String controllerId, List<String> subagentIds) throws SOSHibernateException {
        if (subagentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < subagentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += deleteSubAgents(controllerId, SOSHibernate.getInClausePartition(i, subagentIds));
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(
                    " where subAgentId in (:subagentIds)");
            hql.append(" and agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            hql.append(" where controllerId = :controllerId)");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameterList("subagentIds", subagentIds);
            return getSession().executeUpdate(query);
        }
    }
    
    
    public int setSubAgentsDisabled(List<String> subagentIds) throws SOSHibernateException {
        return setSubAgentsDisabled(subagentIds, true);
    }
    
    public int setSubAgentsDisabled(List<String> subagentIds, boolean disabled) throws SOSHibernateException {
        if (subagentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < subagentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += setSubAgentsDisabled(SOSHibernate.getInClausePartition(i, subagentIds), disabled);
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(
                    " set disabled = ").append(disabled ? 1 : 0).append(" where subAgentId in (:subagentIds)");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameterList("subagentIds", subagentIds);
            return getSession().executeUpdate(query);
        }
    }
    
    public int setStandaloneAgentsDisabled(List<String> agentIds) throws SOSHibernateException {
        return setStandaloneAgentsDisabled(agentIds, true);
    }
    
    public int setStandaloneAgentsDisabled(List<String> agentIds, boolean disabled) throws SOSHibernateException {
        if (agentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < agentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += setStandaloneAgentsDisabled(SOSHibernate.getInClausePartition(i, agentIds), disabled);
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(
                    " set disabled = ").append(disabled ? 1 : 0).append(" where agentId in (:agentIds)");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameterList("agentIds", agentIds);
            return getSession().executeUpdate(query);
        }
    }
    
    public int setAgentsDeployed(List<String> agentIds) throws SOSHibernateException {
        return setAgentsDeployed(agentIds, true);
    }
    
    public int setAgentsDeployed(List<String> agentIds, boolean deployed) throws SOSHibernateException {
        if (agentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < agentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += setAgentsDeployed(SOSHibernate.getInClausePartition(i, agentIds), deployed);
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(
                    " set deployed = ").append(deployed ? 1 : 0).append(" where agentId in (:agentIds)");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameterList("agentIds", agentIds);
            return getSession().executeUpdate(query);
        }
    }
    
    public int setSubAgentsDeployed(List<String> subagentIds) throws SOSHibernateException {
        return setSubAgentsDeployed(subagentIds, true);
    }
    
    public int setSubAgentsDeployed(List<String> subagentIds, boolean deployed) throws SOSHibernateException {
        if (subagentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < subagentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += setSubAgentsDeployed(SOSHibernate.getInClausePartition(i, subagentIds), deployed);
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(
                    " set deployed = ").append(deployed ? 1 : 0).append(" where subAgentId in (:subagentIds)");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameterList("subagentIds", subagentIds);
            return getSession().executeUpdate(query);
        }
    }
    
    public int setSubAgentClustersDeployed(List<String> subagentClusterIds, boolean deployed) throws SOSHibernateException {
        if (subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += setSubAgentClustersDeployed(SOSHibernate.getInClausePartition(i, subagentClusterIds));
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS).append(" set deployed = ");
            hql.append(deployed ? "1" : "0");
            hql.append(" where subAgentClusterId in (:subagentClusterIds)");
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameterList("subagentClusterIds", subagentClusterIds);
            return getSession().executeUpdate(query);
        }
    }
    
    public int setSubAgentClustersDeployed(List<String> subagentClusterIds) throws SOSHibernateException {
        return setSubAgentClustersDeployed(subagentClusterIds, true);
    }

    public boolean subAgentIdAlreadyExists(Collection<String> subAgentIds, String controllerId) throws DBInvalidDataException,
            DBConnectionRefusedException, JocObjectAlreadyExistException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("select subAgentId from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            hql.append(" where agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            hql.append(" where controllerId = :controllerId)");
            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);

            List<String> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                if (result.size() == 1) {
                    throw new JocObjectAlreadyExistException("Subagent Id " + result.get(0) + " already in use.");
                } else {
                    throw new JocObjectAlreadyExistException("Subagent Ids " + result.toString() + " already in use.");
                }
            }
            return false;
        } catch (JocObjectAlreadyExistException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public void cleanupAgentsOrdering(boolean force) throws SOSHibernateException {
        setWithAgentOrdering(true);
        List<DBItemInventoryAgentInstance> dbAgents = getAgentsByControllerIds(null);
        if (!force) {
            // looking for duplicate orderings
            force = dbAgents.stream().collect(Collectors.groupingBy(DBItemInventoryAgentInstance::getOrdering, Collectors.counting())).entrySet()
                    .stream().anyMatch(e -> e.getValue() > 1L);
        }
        if (force) {
            int position = 0;
            for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                if (dbAgent.getOrdering() != position) {
                    dbAgent.setOrdering(position);
                    getSession().update(dbAgent);
                }
                position++;
            }
        }
    }
    
    public void setAgentsOrdering(String agentId, String predecessorAgentId, boolean forStandaloneAgents) throws SOSHibernateException,
            DBMissingDataException, DBInvalidDataException {
        // TODO better with collect by prior
        DBItemInventoryAgentInstance agent = getAgentInstance(agentId);
        if (agent == null) {
            throw new DBMissingDataException("Agent with ID '" + agentId + "' doesn't exist.");
        }
        int newPosition = -1;
        DBItemInventoryAgentInstance predecessorAgent = null;
        if (predecessorAgentId != null && !predecessorAgentId.isEmpty()) {
            if (agentId.equals(predecessorAgentId)) {
                throw new DBInvalidDataException("Agent ID '" + agentId + "' and predecessor Agent ID '" + predecessorAgentId + "' are the same.");
            }
            predecessorAgent = getAgentInstance(predecessorAgentId);
            if (predecessorAgent == null) {
                throw new DBMissingDataException("Predecessor Agent with ID '" + predecessorAgentId + "' doesn't exist.");
            }
            newPosition = predecessorAgent.getOrdering();
        }
        List<String> clusterAgentIds = getClusterAgentIds(null, false);
        if (forStandaloneAgents) {
            if (clusterAgentIds.contains(agent.getAgentId())) {
                throw new DBInvalidDataException("Agent with ID '" + agentId + "' is not a standalone Agent.");
            }
            if (predecessorAgent != null && clusterAgentIds.contains(predecessorAgent.getAgentId())) {
                throw new DBInvalidDataException("Predecessor Agent with ID '" + predecessorAgentId + "' is not a standalone Agent.");
            }
        } else {
            if (!clusterAgentIds.contains(agent.getAgentId())) {
                throw new DBInvalidDataException("Agent with ID '" + agentId + "' is not a Cluster Agent.");
            }
            if (predecessorAgent != null && !clusterAgentIds.contains(predecessorAgent.getAgentId())) {
                throw new DBInvalidDataException("Predecessor Agent with ID '" + predecessorAgentId + "' is not a Cluster Agent.");
            }
        }
        int oldPosition = agent.getOrdering();
        newPosition++;
        agent.setOrdering(newPosition);
        if (newPosition != oldPosition) {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            if (newPosition < oldPosition) {
                hql.append(" set ordering = ordering + 1").append(" where ordering >= :newPosition and ordering < :oldPosition");
            } else {
                hql.append(" set ordering = ordering - 1").append(" where ordering > :oldPosition and ordering <= :newPosition");
            }
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameter("newPosition", newPosition);
            query.setParameter("oldPosition", oldPosition);

            getSession().executeUpdate(query);
            getSession().update(agent);
        }
    }
    
    public Integer getAgentMaxOrdering() throws SOSHibernateException {
        Query<Integer> query = getSession().createQuery("select max(ordering) from " + DBLayer.DBITEM_INV_AGENT_INSTANCES);
        Integer result = getSession().getSingleResult(query);
        if (result == null) {
            return -1;
        }
        return result;
    }
    
    public DBItemInventorySubAgentInstance getSubAgent(String subagentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
        hql.append(" where subAgentId = :subagentId");
        Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
        query.setParameter("subagentId", subagentId);
        return getSession().getSingleResult(query);
    }
    
    public void cleanupSubAgentsOrdering(boolean force) throws SOSHibernateException {
        setWithAgentOrdering(true);
        List<DBItemInventorySubAgentInstance> dbSubagents = getSubAgentInstancesByControllerIds(null);
        if (!force) {
            // looking for duplicate orderings
            force = dbSubagents.stream().collect(Collectors.groupingBy(DBItemInventorySubAgentInstance::getOrdering, Collectors.counting())).entrySet()
                    .stream().anyMatch(e -> e.getValue() > 1L);
        }
        if (force) {
            int position = 0;
            for (DBItemInventorySubAgentInstance dbSubagent : dbSubagents) {
                if (dbSubagent.getOrdering() != position) {
                    dbSubagent.setOrdering(position);
                    getSession().update(dbSubagent);
                }
                position++;
            }
        }
    }
    
    public void setSubAgentsOrdering(String subagentId, String predecessorSubagentId) throws SOSHibernateException,
            DBMissingDataException, DBInvalidDataException {
        // TODO better with collect by prior
        DBItemInventorySubAgentInstance subagent = getSubAgent(subagentId);
        if (subagent == null) {
            throw new DBMissingDataException("Subagent with ID '" + subagentId + "' doesn't exist.");
        }
        int newPosition = -1;
        DBItemInventorySubAgentInstance predecessorSubagentCluster = null;
        if (predecessorSubagentId != null && !predecessorSubagentId.isEmpty()) {
            if (subagentId.equals(predecessorSubagentId)) {
                throw new DBInvalidDataException("Subagent ID '" + subagentId + "' and predecessor Subagent ID '"
                        + predecessorSubagentId + "' are the same.");
            }
            predecessorSubagentCluster = getSubAgent(predecessorSubagentId);
            if (predecessorSubagentCluster == null) {
                throw new DBMissingDataException("Predecessor Subagent with ID '" + predecessorSubagentId + "' doesn't exist.");
            }
            newPosition = predecessorSubagentCluster.getOrdering();
        }

        // TODO check: subagentId and predecessorSubagentId should have same agentId ??

        int oldPosition = subagent.getOrdering();
        newPosition++;
        subagent.setOrdering(newPosition);
        if (newPosition != oldPosition) {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            if (newPosition < oldPosition) {
                hql.append(" set ordering = ordering + 1").append(" where ordering >= :newPosition and ordering < :oldPosition");
            } else {
                hql.append(" set ordering = ordering - 1").append(" where ordering > :oldPosition and ordering <= :newPosition");
            }
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameter("newPosition", newPosition);
            query.setParameter("oldPosition", oldPosition);

            getSession().executeUpdate(query);
            getSession().update(subagent);
        }
    }
    
    public Integer getSubagentMaxOrdering() throws SOSHibernateException {
        Query<Integer> query = getSession().createQuery("select max(ordering) from " + DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
        Integer result = getSession().getSingleResult(query);
        if (result == null) {
            return -1;
        }
        return result;
    }

}