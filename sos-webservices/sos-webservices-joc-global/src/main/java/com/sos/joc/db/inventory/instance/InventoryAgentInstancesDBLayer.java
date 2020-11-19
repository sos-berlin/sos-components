package com.sos.joc.db.inventory.instance;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.common.JocSecurityLevel;

public class InventoryAgentInstancesDBLayer {

    private SOSHibernateSession session;
    private JocSecurityLevel level;

    public InventoryAgentInstancesDBLayer(SOSHibernateSession conn) {
        this.session = conn;
        this.level = Globals.getJocSecurityLevel();
    }

    public DBItemInventoryAgentInstance getAgentInstance(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id == null) {
                return null;
            }
            return session.get(DBItemInventoryAgentInstance.class, id);
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

    public List<DBItemInventoryAgentInstance> getEnabledClusterWatcherByControllerId(String controllerId) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        return getAgentsByControllerIds(Arrays.asList(controllerId), true, true);
    }

    public List<DBItemInventoryAgentInstance> getAgentsByControllerIds(Collection<String> controllerIds, boolean onlyWatcher,
            boolean onlyEnabledAgents) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            if (controllerIds != null && !controllerIds.isEmpty()) {
                sql.append(" and controllerId in (:controllerIds)");
            }
            if (onlyWatcher) {
                sql.append(" and isWatcher = 1");
            }
            if (onlyEnabledAgents) {
                sql.append(" and disabled = 0");
            }
            Query<DBItemInventoryAgentInstance> query = session.createQuery(sql.toString());
            if (controllerIds != null && !controllerIds.isEmpty()) {
                query.setParameterList("controllerId", controllerIds);
            }
            query.setParameter("securityLevel", level.intValue());
            return session.getResultList(query);
        } catch (DBMissingDataException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<String> getEnabledAgentNames() throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getAgentNames(true);
    }
    
    public List<String> getAgentNames(boolean onlyEnabledAgents) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select agentName from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            if (onlyEnabledAgents) {
                sql.append(" and disabled = 0");
            }
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("securityLevel", level.intValue());
            return session.getResultList(query);
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
            agent.setSecurityLevel(level.intValue());
            session.save(agent);
            return agent.getId();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long updateAgent(DBItemInventoryAgentInstance agent) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            agent.setModified(Date.from(Instant.now()));
            agent.setSecurityLevel(level.intValue());
            session.update(agent);
            return agent.getId();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
//    public boolean instanceAlreadyExists(Collection<URI> uris, Collection<Long> ids) throws DBInvalidDataException,
//            DBConnectionRefusedException, JocObjectAlreadyExistException {
//        try {
//            StringBuilder sql = new StringBuilder();
//            sql.append("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
//            sql.append(" where securityLevel = :securityLevel");
//            sql.append(" and agentId in (:agentIds)");
//            if (ids != null && !ids.isEmpty()) {
//                sql.append(" and id not in (:ids)");
//            }
//            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
//            query.setParameter("uris", uris.stream().map(u -> u.toString().toLowerCase()).collect(Collectors.toSet()));
//            if (ids != null && !ids.isEmpty()) {
//                query.setParameter("ids", ids);
//            }
//            query.setParameter("securityLevel", level.intValue());
//            List<DBItemInventoryJSInstance> result = session.getResultList(query);
//            if (result != null && !result.isEmpty()) {
//                throw new JocObjectAlreadyExistException(getConstraintErrorMessage(result.get(0).getControllerId(), result.get(0).getUri()));
//            }
//            return false;
//        } catch (JocObjectAlreadyExistException ex) {
//            throw ex;
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }
    
//    private String getConstraintErrorMessage(String controllerId, String agentId) {
//        return String.format("JobScheduler Agent instance (controllerId:%1$s, agentId:%2$s, security level:%3$s) already exists in table %4$s",
//                controllerId, agentId, level.value(), DBLayer.TABLE_INV_JS_INSTANCES);
//    }

//    public List<DBItemInventoryAgentInstance> getAgents() throws DBInvalidDataException, DBConnectionRefusedException {
//        try {
//            StringBuilder sql = new StringBuilder();
//            sql.append("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
//            sql.append(" where securityLevel = :securityLevel");
//            Query<DBItemInventoryAgentInstance> query = session.createQuery(sql.toString());
//            query.setParameter("securityLevel", level.intValue());
//            return session.getResultList(query);
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }

    public void deleteInstance(DBItemInventoryAgentInstance agent) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (agent != null) {
                session.delete(agent);
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}