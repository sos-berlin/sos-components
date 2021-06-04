package com.sos.joc.db.inventory.instance;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;

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
        return getAgentsByControllerIdAndAgentIdsAndUrls(controllerIds, null, null, onlyWatcher, onlyEnabledAgents);
    }

    public List<DBItemInventoryAgentInstance> getAgentsByControllerIdAndAgentIdsAndUrls(Collection<String> controllerIds, Collection<String> agentIds,
            Collection<String> agentUrls, boolean onlyWatcher, boolean onlyEnabledAgents) throws DBInvalidDataException, DBMissingDataException,
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
            if (onlyEnabledAgents) {
                clauses.add("disabled = 0");
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

    public Set<String> getEnabledAgentNames() throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getAgentNames(true);
    }

    public Set<String> getAgentNames(boolean onlyEnabledAgents) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        try {
            List<DBItemInventoryAgentInstance> agents = getAgentsByControllerIds(null, false, onlyEnabledAgents);
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

    public Map<String, Set<String>> getAgentNamesByAgentIds(Collection<String> agentIds) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
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
                throw new JocObjectAlreadyExistException("Agent Ids " + result.toString() + " already in use.");
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
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}