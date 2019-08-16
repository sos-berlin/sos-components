package com.sos.joc.db.inventory.instance;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

import javax.json.JsonObject;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;

public class InventoryInstancesDBLayer {

    private SOSHibernateSession session;

    public InventoryInstancesDBLayer(SOSHibernateSession conn) {
        this.session = conn;
    }

    public DBItemInventoryInstance getInventoryInstance(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id == null) {
                return null;
            }
            return session.get(DBItemInventoryInstance.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemInventoryInstance getInventoryInstanceBySchedulerId(String schedulerId, String accessToken) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {
        try {
            String sql = String.format("from %s where schedulerId = :schedulerId order by primaryMaster desc", DBLayer.DBITEM_INVENTORY_INSTANCES);
            Query<DBItemInventoryInstance> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            List<DBItemInventoryInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return getRunningJobSchedulerClusterMember(result, accessToken);
            } else {
                String errMessage = String.format("jobschedulerId %1$s not found in table %2$s", schedulerId, DBLayer.TABLE_INVENTORY_INSTANCES);
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

    public DBItemInventoryInstance getInventoryInstanceByURI(String schedulerId, String uri) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            String sql = String.format("from %s where schedulerId = :schedulerId and lower(uri) = :uri", DBLayer.DBITEM_INVENTORY_INSTANCES);
            Query<DBItemInventoryInstance> query = session.createQuery(sql.toString());
            query.setParameter("uri", uri.toLowerCase());
            query.setParameter("schedulerId", schedulerId);

            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryInstance> getInventoryInstancesBySchedulerId(String schedulerId) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            if (schedulerId == null) {
                schedulerId = "";
            }
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INVENTORY_INSTANCES);
            if (!schedulerId.isEmpty()) {
                sql.append(" where schedulerId = :schedulerId").append(" order by primaryMaster desc");
            } else {
                sql.append(" order by schedulerId asc, primaryMaster desc");
            }
            Query<DBItemInventoryInstance> query = session.createQuery(sql.toString());
            if (!schedulerId.isEmpty()) {
                query.setParameter("schedulerId", schedulerId);
            }
            List<DBItemInventoryInstance> result = session.getResultList(query);
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

    public List<String> getJobSchedulerIds() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            return session.getResultList(String.format("select schedulerId from %1$s order by modified desc", DBLayer.DBITEM_INVENTORY_INSTANCES));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void updateInstance(DBItemInventoryInstance dbInstance) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            dbInstance.setModified(Date.from(Instant.now()));
            session.update(dbInstance);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long saveInstance(DBItemInventoryInstance dbInstance) throws DBConnectionRefusedException, DBInvalidDataException {
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

    private DBItemInventoryInstance getRunningJobSchedulerClusterMember(List<DBItemInventoryInstance> schedulerInstancesDBList, String accessToken) {
        if (schedulerInstancesDBList.get(0).getCluster()) {
            for (DBItemInventoryInstance schedulerInstancesDBItem : schedulerInstancesDBList) {
                try {
                    String state = getJobSchedulerState(schedulerInstancesDBItem, accessToken);
                    if ("running,paused".contains(state)) {
                        return schedulerInstancesDBItem;
                    }
                } catch (Exception e) {
                    // unreachable
                }
            }
        }
        return schedulerInstancesDBList.get(0);
    }

    private String getJobSchedulerState(DBItemInventoryInstance schedulerInstancesDBItem, String accessToken) throws JocException {
        JOCJsonCommand jocJsonCommand = new JOCJsonCommand(schedulerInstancesDBItem, accessToken);
        jocJsonCommand.setUriBuilderForOverview();
        JsonObject answer = jocJsonCommand.getJsonObjectFromGet();
        // TODO JS2 liefert keinen "state"
        // return answer.getString("state", "");
        return answer.getString("state", "running");
    }

}