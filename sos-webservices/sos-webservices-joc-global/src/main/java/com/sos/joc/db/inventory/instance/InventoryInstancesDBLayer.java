package com.sos.joc.db.inventory.instance;

import java.util.Date;
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
import com.sos.joc.exceptions.UnknownJobSchedulerMasterException;

public class InventoryInstancesDBLayer {
	
	private SOSHibernateSession session;

    public InventoryInstancesDBLayer(SOSHibernateSession conn) {
        this.session = conn;
    }
    
    public DBItemInventoryInstance getInventoryInstanceBySchedulerId(String schedulerId, String accessToken)
    		throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getInventoryInstanceBySchedulerId(schedulerId, accessToken, false);
    }

    public DBItemInventoryInstance getInventoryInstanceBySchedulerId(String schedulerId, String accessToken, boolean verbose)
    		throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getInventoryInstanceBySchedulerId(schedulerId, accessToken, verbose, null);
    }
    
    public DBItemInventoryInstance getInventoryInstanceBySchedulerId(String schedulerId, String accessToken,
    		DBItemInventoryInstance curInstance) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        return getInventoryInstanceBySchedulerId(schedulerId, accessToken, false, curInstance);
    }

    public DBItemInventoryInstance getInventoryInstanceBySchedulerId(String schedulerId, String accessToken, boolean verbose,
    		DBItemInventoryInstance curInstance) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        try {
            String sql = String.format("from %s where schedulerId = :schedulerId order by precedence", 
                    DBLayer.DBITEM_INVENTORY_INSTANCES);
            Query<DBItemInventoryInstance> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            List<DBItemInventoryInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return getRunningJobSchedulerClusterMember(result, accessToken, curInstance);
            } else {
                String errMessage = String.format("jobschedulerId %1$s not found in table %2$s", schedulerId,
                		DBLayer.TABLE_INVENTORY_INSTANCES);
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

    public DBItemInventoryInstance getInventoryInstanceByHostPort(String host, Integer port, String schedulerId)
    		throws DBInvalidDataException, DBConnectionRefusedException, UnknownJobSchedulerMasterException {
        try {
            String sql = String.format("from %s where schedulerId = :schedulerId and hostname = :hostname and port = :port",
            		DBLayer.DBITEM_INVENTORY_INSTANCES);
            Query<DBItemInventoryInstance> query = session.createQuery(sql.toString());
            query.setParameter("hostname", host);
            query.setParameter("port", port);
            query.setParameter("schedulerId", schedulerId);

            List<DBItemInventoryInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            } else {
                String errMessage = String.format("JobScheduler with id:%1$s, host:%2$s and port:%3$s couldn't be found in table %4$s",
                		schedulerId, host, port, DBLayer.TABLE_INVENTORY_INSTANCES);
                throw new UnknownJobSchedulerMasterException(errMessage);
            }
        } catch (JocException e) {
            throw e;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemInventoryInstance> getInventoryInstancesBySchedulerId(String schedulerId)
    		throws DBInvalidDataException, DBConnectionRefusedException {
        return getInventoryInstancesBySchedulerId(schedulerId, false);
    }

    public List<DBItemInventoryInstance> getInventoryInstancesBySchedulerId(String schedulerId, boolean ordered)
    		throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            if (schedulerId == null) {
                schedulerId = "";  
            }
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INVENTORY_INSTANCES);
            if (!schedulerId.isEmpty()) {
                sql.append(" where schedulerId = :schedulerId").append(" order by precedence");
            } else {
                sql.append(" order by schedulerId, precedence");
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

    public List<DBItemInventoryInstance> getInventoryInstances() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            return session.getResultList("from " + DBLayer.DBITEM_INVENTORY_INSTANCES);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryInstance> getJobSchedulerIds() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            return session.getResultList(String.format("from %1$s order by created desc",
            		DBLayer.DBITEM_INVENTORY_INSTANCES));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemInventoryInstance getInventoryInstanceByKey(Long id) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
        	return session.get(DBItemInventoryInstance.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public long getInventoryMods() throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            String sql = String.format("select modified from %s",DBLayer.DBITEM_INVENTORY_INSTANCES);
            Query<Date> query = session.createQuery(sql);
            List<Date> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                long mods = 0L;
                for (Date mod : result) {
                    mods += mod.getTime() / 1000;
                }
                return mods;
            }
            return 0L;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private DBItemInventoryInstance getRunningJobSchedulerClusterMember(List<DBItemInventoryInstance> schedulerInstancesDBList,
    		String accessToken, DBItemInventoryInstance curInstance) {
        if (schedulerInstancesDBList.get(0).getCluster()) {
            DBItemInventoryInstance schedulerInstancesDBItemOfWaitingScheduler = null;
            for (DBItemInventoryInstance schedulerInstancesDBItem : schedulerInstancesDBList) {
                try {
                    String state = getJobSchedulerState(schedulerInstancesDBItem, accessToken);
        			if ("running,paused".contains(state)) {
						schedulerInstancesDBItemOfWaitingScheduler = null;
						return schedulerInstancesDBItem;
					}
					if (schedulerInstancesDBItemOfWaitingScheduler == null && "waiting_for_activation".equals(state)) {
						schedulerInstancesDBItemOfWaitingScheduler = schedulerInstancesDBItem;
	                }
                } catch (Exception e) {
                    // unreachable
                }
            }
//            if (schedulerInstancesDBItemOfWaitingScheduler != null) {
//                return schedulerInstancesDBItemOfWaitingScheduler;
//            }
        }
        return schedulerInstancesDBList.get(0);
    }
    
    private String getJobSchedulerState(DBItemInventoryInstance schedulerInstancesDBItem, String accessToken) throws JocException {
    	JOCJsonCommand jocJsonCommand = new JOCJsonCommand(schedulerInstancesDBItem);
        jocJsonCommand.setUriBuilderForOverview();
		JsonObject answer = jocJsonCommand.getJsonObjectFromGet(accessToken);
		return answer.getString("state", "");
    }

}