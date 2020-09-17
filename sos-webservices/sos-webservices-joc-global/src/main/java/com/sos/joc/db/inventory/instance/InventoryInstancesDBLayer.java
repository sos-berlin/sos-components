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
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.common.JocSecurityLevel;

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

    @Deprecated
    public DBItemInventoryJSInstance getInventoryInstanceByControllerId(String controllerId, String accessToken) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        try {
            //TODO do we need isActive?? String sql = String.format("from %s where controllerId = :controllerId order by isActive desc, isPrimary desc",
            //DBLayer.DBITEM_INVENTORY_INSTANCES);
            String sql = String.format("from %s where controllerId = :controllerId order by isPrimary desc",
                    DBLayer.DBITEM_INV_JS_INSTANCES);
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            List<DBItemInventoryJSInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return getRunningJobSchedulerClusterMember(result, accessToken);
            } else {
                String errMessage = String.format("controllerId %1$s not found in table %2$s", controllerId, DBLayer.TABLE_INV_JS_INSTANCES);
                throw new DBMissingDataException(errMessage);
            }
        } catch (DBMissingDataException ex) {
            throw ex;
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
            sql.append(" where lower(uri) = :uri");
            sql.append(" and securityLevel = :securityLevel");
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
            sql.append(" where lower(uri) in (:uris)");
            sql.append(" and securityLevel = :securityLevel");
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
            if (!controllerId.isEmpty()) {
                sql.append(" where controllerId = :controllerId");
            }
            sql.append(" and securityLevel = :securityLevel");
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
            sql.append(" where controllerId = :controllerId");
            sql.append(" and securityLevel = :securityLevel");
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
            sql.append(" where controllerId = :controllerId");
            sql.append(" and securityLevel = :securityLevel");
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

    @Deprecated
    private DBItemInventoryJSInstance getRunningJobSchedulerClusterMember(List<DBItemInventoryJSInstance> schedulerInstancesDBList, String accessToken) {
        if (schedulerInstancesDBList.get(0).getIsCluster()) {
            for (DBItemInventoryJSInstance schedulerInstancesDBItem : schedulerInstancesDBList) {
                if (getJobSchedulerState(schedulerInstancesDBItem, accessToken)) {
                    return schedulerInstancesDBItem;
                }
            }
        }
        return schedulerInstancesDBList.get(0);
    }

    @Deprecated
    private boolean getJobSchedulerState(DBItemInventoryJSInstance schedulerInstancesDBItem, String accessToken) {
        try {
            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(schedulerInstancesDBItem, accessToken);
            jocJsonCommand.setUriBuilderForCluster();
            ClusterState clusterState = jocJsonCommand.getJsonObjectFromGet(ClusterState.class);
            if (clusterState != null) {
                switch (clusterState.getTYPE()) {
                case EMPTY:
                    return true;
                default:
                    String activeClusterUri = clusterState.getIdToUri().getAdditionalProperties().get(clusterState.getActiveId());
                    return activeClusterUri.equalsIgnoreCase(schedulerInstancesDBItem.getClusterUri()) || activeClusterUri.equalsIgnoreCase(
                            schedulerInstancesDBItem.getUri());
                }
            }
            return true; //TODO ??
        } catch (JobSchedulerInvalidResponseDataException e) {
            return true; //TODO ??
        } catch (JocException e) {
            return false;
        }
    }

}