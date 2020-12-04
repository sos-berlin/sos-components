package com.sos.joc.db.inventory.instance;

import java.net.URI;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.jobscheduler.Controller;

public class InventoryInstancesDBLayer {

    private SOSHibernateSession session;
    private JocSecurityLevel level;

    public InventoryInstancesDBLayer(SOSHibernateSession conn) {
        this.session = conn;
        this.level = Globals.getJocSecurityLevel();
    }

    public DBItemInventoryJSInstance getInventoryInstance(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id == null) {
                return null;
            }
            return session.get(DBItemInventoryJSInstance.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemInventoryJSInstance getInventoryInstanceByURI(URI uri) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            sql.append(" and lower(uri) = :uri");
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("uri", uri.toString().toLowerCase());
            query.setParameter("securityLevel", level.intValue());
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public boolean instanceAlreadyExists(Collection<URI> uris, Collection<Long> ids) throws DBInvalidDataException,
            DBConnectionRefusedException, JocObjectAlreadyExistException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            sql.append(" and lower(uri) in (:uris)");
            if (ids != null && !ids.isEmpty()) {
                sql.append(" and id not in (:ids)");
            }
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("uris", uris.stream().map(u -> u.toString().toLowerCase()).collect(Collectors.toSet()));
            if (ids != null && !ids.isEmpty()) {
                query.setParameter("ids", ids);
            }
            query.setParameter("securityLevel", level.intValue());
            List<DBItemInventoryJSInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                throw new JocObjectAlreadyExistException(getConstraintErrorMessage(result.get(0).getControllerId(), result.get(0).getUri()));
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
    
    private String getConstraintErrorMessage(String controllerId, String url) {
        return String.format("JobScheduler Controller instance (controllerId:%1$s, url:%2$s, security level:%3$s) already exists in table %4$s",
                controllerId, url, level.value(), DBLayer.TABLE_INV_JS_INSTANCES);
    }

    public List<DBItemInventoryJSInstance> getInventoryInstancesByControllerId(String controllerId) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            if (controllerId == null) {
                controllerId = "";
            }
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            if (!controllerId.isEmpty()) {
                sql.append(" and controllerId = :controllerId");
            }
            if (!controllerId.isEmpty()) {
                sql.append(" order by isPrimary desc, startedAt desc");
            } else {
                sql.append(" order by controllerId asc, isPrimary desc, startedAt desc");
            }
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            if (!controllerId.isEmpty()) {
                query.setParameter("controllerId", controllerId);
            }
            query.setParameter("securityLevel", level.intValue());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemInventoryJSInstance getOtherClusterMember(String controllerId, Long id) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            sql.append(" and controllerId = :controllerId");
            sql.append(" and id != :id");
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("securityLevel", level.intValue());
            query.setParameter("id", id);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemInventoryJSInstance getOtherClusterMember(String controllerId, String uri) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            sql.append(" and controllerId = :controllerId");
            sql.append(" and uri != :uri");
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("securityLevel", level.intValue());
            query.setParameter("uri", uri);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemInventoryJSInstance> getInventoryInstances() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("securityLevel", level.intValue());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemInventoryJSInstance> getInventoryInstancesOfAllSecurityLevels(String controllerId) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            if (controllerId != null && !controllerId.isEmpty()) {
                sql.append(" where controllerId = :controllerId");
            }
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            if (controllerId != null && !controllerId.isEmpty()) {
                query.setParameter("controllerId", controllerId);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getControllerIds() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            return session.getResultList(String.format("select controllerId from %s where securityLevel = %d order by modified desc",
                    DBLayer.DBITEM_INV_JS_INSTANCES, level.intValue()));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<Controller> getControllerIdsWithSecurityLevel(boolean onlyCurrentSecurityLevel) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select controllerId, securityLevel from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            if (onlyCurrentSecurityLevel) {
                sql.append(" where securityLevel = :securityLevel");
            }
            Query<Object[]> query = session.createQuery(sql.toString());
            if (onlyCurrentSecurityLevel) {
                query.setParameter("securityLevel", level.intValue());
            }
            List<Object[]> result = session.getResultList(query);
            if (result != null) {
                return result.stream().map(item -> {
                    Controller c = new Controller();
                    c.setControllerId((String) item[0]);
                    c.setIsCoupled(null);
                    c.setSecurityLevel(JocSecurityLevel.fromValue((int) item[1]));
                    return c;
                }).distinct().collect(Collectors.toList()); 
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public Integer getSecurityLevel(String controllerId) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select securityLevel from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where controllerId = :controllerId");
            Query<Integer> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            query.setMaxResults(1);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void updateInstance(DBItemInventoryJSInstance dbInstance) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            dbInstance.setModified(Date.from(Instant.now()));
            dbInstance.setSecurityLevel(level.intValue());
            session.update(dbInstance);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long saveInstance(DBItemInventoryJSInstance dbInstance) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            dbInstance.setModified(Date.from(Instant.now()));
            dbInstance.setSecurityLevel(level.intValue());
            session.save(dbInstance);
            return dbInstance.getId();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public boolean isOperatingSystemUsed(Long osId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select count(*) from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where osId = :osId");
            Query<Long> query = session.createQuery(sql.toString());
            query.setParameter("osId", osId);
            if (session.getSingleResult(query) > 0L) {
                return true;
            }
            sql = new StringBuilder();
            sql.append("select count(*) from ").append(DBLayer.DBITEM_JOC_INSTANCES);
            sql.append(" where osId = :osId");
            query = session.createQuery(sql.toString());
            query.setParameter("osId", osId);
            return session.getSingleResult(query) > 0L;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public void deleteInstance(DBItemInventoryJSInstance dbInstance) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (dbInstance != null) {
                session.delete(dbInstance);
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public boolean isEmpty() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select count(*) from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where securityLevel = :securityLevel");
            Query<Long> query = session.createQuery(sql.toString());
            query.setParameter("securityLevel", level.intValue());
            return session.getSingleResult(query) == 0L;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}