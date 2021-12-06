package com.sos.joc.db.inventory.instance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.agent.SubagentDirectorType;

public class InventoryAgentInstancesDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryAgentInstancesDBLayer(SOSHibernateSession conn) {
        super(conn);
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

    public List<String> getUrisOfEnabledClusterWatcherByControllerId(String controllerId) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
        if (controllerId == null || controllerId.isEmpty()) {
            return null;
        }
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("select uri from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            hql.append(" where controllerId = :controllerId");
            hql.append(" and isWatcher = 1");
            hql.append(" and disabled = 0");

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
            boolean onlyEnabledAgents) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getAgentsByControllerIdAndAgentIds(controllerIds, null, onlyWatcher, onlyEnabledAgents);
    }

    public List<DBItemInventoryAgentInstance> getAgentsByControllerIdAndAgentIds(Collection<String> controllerIds, Collection<String> agentIds,
            boolean onlyWatcher, boolean onlyEnabledAgents) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
        // TODO 1000 in in() clause
        boolean withAgentIds = agentIds != null && !agentIds.isEmpty();
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
            if (withAgentIds) {
                clauses.add("agentId in (:agentIds)");
            }
            if (onlyWatcher) {
                clauses.add("isWatcher = 1");
            }
            if (onlyEnabledAgents) {
                clauses.add("disabled = 0");
            }
            if (!clauses.isEmpty()) {
                hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            hql.append(" order by isWatcher desc");
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
            return getSession().getResultList(query);
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Set<String> getEnabledAgentNames() throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getAgentNames(null, true);
    }

    public Set<String> getEnabledAgentNames(Collection<String> controllerIds) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
        return getAgentNames(controllerIds, true);
    }

    public Set<String> getAgentNames(Collection<String> controllerIds, boolean onlyEnabledAgents) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        try {
            List<DBItemInventoryAgentInstance> agents = getAgentsByControllerIds(controllerIds, false, onlyEnabledAgents);
            if (agents == null || agents.isEmpty()) {
                return Collections.emptySet();
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
        } catch (DBMissingDataException ex) {
            throw ex;
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
                    SubagentDirectorType.STANDBY_DIRECTOR.intValue()));
            List<DBItemInventorySubAgentInstance> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.groupingBy(DBItemInventorySubAgentInstance::getAgentId, Collectors.toMap(
                        DBItemInventorySubAgentInstance::getDirectorAsEnum, Function.identity(), (k, v) -> v)));
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
            StringBuilder hql = new StringBuilder("select subAgentId from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(" where agentId = :agentId");
            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameter("agentId", agentId);
            List<String> result = getSession().getResultList(query);
            if (result != null) {
                return result;
            }
            return Collections.emptyList();
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Map<String, List<DBItemInventorySubAgentInstance>> getSubAgentInstancesByControllerId(String controllerId, boolean onlyWatcher,
            boolean onlyEnabledAgents) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
            if ((controllerId != null && !controllerId.isEmpty()) || onlyWatcher || onlyEnabledAgents) {
                List<String> clauses = new ArrayList<>(3);
                hql.append(" where agentId in (select agentId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                if (controllerId != null) {
                    clauses.add("controllerId = :controllerId");
                }
                if (onlyWatcher) {
                    clauses.add("isWatcher = 1");
                }
                if (onlyEnabledAgents) {
                    clauses.add("disabled = 0");
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
                hql.append(")");
            }
            Query<DBItemInventorySubAgentInstance> query = getSession().createQuery(hql.toString());
            if (controllerId != null && !controllerId.isEmpty()) {
                query.setParameter("controllerId", controllerId);
            }
            List<DBItemInventorySubAgentInstance> result = getSession().getResultList(query);
            if (result != null) {
                return result.stream().collect(Collectors.groupingBy(DBItemInventorySubAgentInstance::getAgentId));
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
    
    public DBItemInventorySubAgentInstance solveAgentWithoutSubAgent(DBItemInventoryAgentInstance agent) {
        try {
            Date now = Date.from(Instant.now());
            DBItemInventorySubAgentInstance subAgent = new DBItemInventorySubAgentInstance();
            subAgent.setId(null);
            subAgent.setCertificate(agent.getCertificate());
            subAgent.setAgentId(agent.getAgentId());
            subAgent.setIsDirector(1);
            subAgent.setSubAgentId(agent.getAgentId());
            subAgent.setUri(agent.getUri());
            subAgent.setModified(now);
            //getSession().save(subAgent);
            return subAgent;
        } catch (DBMissingDataException ex) {
            throw ex;
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    private int deleteSubAgents(String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES).append(" where agentId =: agentId");
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter("agentId", agentId);
        return getSession().executeUpdate(query);
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
    
}