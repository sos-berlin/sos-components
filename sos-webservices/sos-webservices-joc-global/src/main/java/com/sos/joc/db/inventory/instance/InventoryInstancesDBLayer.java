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
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;

public class InventoryInstancesDBLayer {

    private SOSHibernateSession session;

    public InventoryInstancesDBLayer(SOSHibernateSession conn) {
        this.session = conn;
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

    public DBItemInventoryJSInstance getInventoryInstanceBySchedulerId(String schedulerId, String accessToken) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        try {
            //TODO do we need isActive?? String sql = String.format("from %s where schedulerId = :schedulerId order by isActive desc, isPrimary desc",
            //DBLayer.DBITEM_INVENTORY_INSTANCES);
            String sql = String.format("from %s where schedulerId = :schedulerId order by isPrimary desc",
                    DBLayer.DBITEM_INV_JS_INSTANCES);
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            List<DBItemInventoryJSInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return getRunningJobSchedulerClusterMember(result, accessToken);
            } else {
                String errMessage = String.format("jobschedulerId %1$s not found in table %2$s", schedulerId, DBLayer.TABLE_INV_JS_INSTANCES);
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
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("uri", uri.toString().toLowerCase());
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
            if (ids != null && !ids.isEmpty()) {
                sql.append(" and id not in (:ids)");
            }
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("uris", uris.stream().map(u -> u.toString().toLowerCase()).collect(Collectors.toSet()));
            if (ids != null && !ids.isEmpty()) {
                query.setParameter("ids", ids);
            }
            List<DBItemInventoryJSInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                throw new JocObjectAlreadyExistException(getConstraintErrorMessage(result.get(0).getSchedulerId(), result.get(0).getUri()));
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
    
    private String getConstraintErrorMessage(String jobschedulerId, String url) {
        return String.format("JobScheduler instance (jobschedulerId:%1$s, url:%2$s) already exists in table %3$s",
                jobschedulerId, url, DBLayer.TABLE_INV_JS_INSTANCES);
    }

    public List<DBItemInventoryJSInstance> getInventoryInstancesBySchedulerId(String schedulerId) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            if (schedulerId == null) {
                schedulerId = "";
            }
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            if (!schedulerId.isEmpty()) {
                sql.append(" where schedulerId = :schedulerId").append(" order by isPrimary desc, startedAt desc");
            } else {
                sql.append(" order by schedulerId asc, isPrimary desc, startedAt desc");
            }
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            if (!schedulerId.isEmpty()) {
                query.setParameter("schedulerId", schedulerId);
            }
            List<DBItemInventoryJSInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemInventoryJSInstance getOtherClusterMember(String schedulerId, Long id) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where schedulerId = :schedulerId");
            sql.append(" and id != :id");
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            query.setParameter("id", id);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemInventoryJSInstance getOtherClusterMember(String schedulerId, String uri) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
            sql.append(" where schedulerId = :schedulerId");
            sql.append(" and uri != :uri");
            Query<DBItemInventoryJSInstance> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
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
            return session.getResultList("from " + DBLayer.DBITEM_INV_JS_INSTANCES);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getJobSchedulerIds() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            return session.getResultList(String.format("select schedulerId from %1$s order by modified desc", DBLayer.DBITEM_INV_JS_INSTANCES));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void updateInstance(DBItemInventoryJSInstance dbInstance) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            dbInstance.setModified(Date.from(Instant.now()));
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
            Query<Long> query = session.createQuery(sql.toString());
            return session.getSingleResult(query) == 0L;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

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