package com.sos.jobscheduler.history.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBItemSchedulerOrderHistory;
import com.sos.jobscheduler.db.DBItemSchedulerOrderStepHistory;
import com.sos.jobscheduler.db.DBItemSchedulerSettings;
import com.sos.jobscheduler.db.DBLayer;

public class DBLayerHistory {

    private final String schedulerVariablesName;

    public DBLayerHistory(String name) {
        schedulerVariablesName = name;
    }

    public DBItemSchedulerSettings getSchedulerSettings(SOSHibernateSession session) throws SOSHibernateException {
        String hql = String.format("from %s where name = :name", DBLayer.DBITEM_SCHEDULER_SETTINGS);
        Query<DBItemSchedulerSettings> query = session.createQuery(hql);
        query.setParameter("name", schedulerVariablesName);
        return session.getSingleResult(query);
    }

    public DBItemSchedulerSettings insertSchedulerSettings(SOSHibernateSession session, String eventId) throws SOSHibernateException {
        DBItemSchedulerSettings item = new DBItemSchedulerSettings();
        item.setName(schedulerVariablesName);
        item.setTextValue(String.valueOf(eventId));
        session.save(item);
        return item;
    }

    public DBItemSchedulerSettings updateSchedulerSettings(SOSHibernateSession session, DBItemSchedulerSettings item, Long eventId)
            throws SOSHibernateException {
        item.setTextValue(String.valueOf(eventId));
        session.update(item);
        return item;
    }

    public DBItemSchedulerOrderHistory getOrderHistory(SOSHibernateSession session, String schedulerId, String orderKey)
            throws SOSHibernateException {

        return getOrderHistory(session, schedulerId, orderKey, null);
    }

    public DBItemSchedulerOrderHistory getOrderHistory(SOSHibernateSession session, String schedulerId, String orderKey, String startEventId)
            throws SOSHibernateException {
        Query<DBItemSchedulerOrderHistory> query = session.createQuery(String.format("from %s where schedulerId=:schedulerId and orderKey=:orderKey",
                DBLayer.DBITEM_SCHEDULER_ORDER_HISTORY));
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderKey", orderKey);

        List<DBItemSchedulerOrderHistory> result = session.getResultList(query);
        if (result != null) {
            switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                DBItemSchedulerOrderHistory order = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemSchedulerOrderHistory item : result) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            order = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemSchedulerOrderHistory item : result) {
                        if (item.getStartEventId().equals(startEventId)) {
                            order = item;
                            break;
                        }
                    }
                }
                return order;
            }
        }
        return null;
    }

    public DBItemSchedulerOrderStepHistory getOrderStepHistoryById(SOSHibernateSession session, Long id) throws SOSHibernateException {
        Query<DBItemSchedulerOrderStepHistory> query = session.createQuery(String.format("from %s where id=:id",
                DBLayer.DBITEM_SCHEDULER_ORDER_STEP_HISTORY));
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public DBItemSchedulerOrderStepHistory getOrderStepHistory(SOSHibernateSession session, String schedulerId, String orderKey)
            throws SOSHibernateException {

        return getOrderStepHistory(session, schedulerId, orderKey, null);
    }

    public DBItemSchedulerOrderStepHistory getOrderStepHistory(SOSHibernateSession session, String schedulerId, String orderKey, String startEventId)
            throws SOSHibernateException {
        Query<DBItemSchedulerOrderStepHistory> query = session.createQuery(String.format(
                "from %s where schedulerId=:schedulerId and orderKey=:orderKey", DBLayer.DBITEM_SCHEDULER_ORDER_STEP_HISTORY));
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderKey", orderKey);

        List<DBItemSchedulerOrderStepHistory> result = session.getResultList(query);
        if (result != null) {
            switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                DBItemSchedulerOrderStepHistory step = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemSchedulerOrderStepHistory item : result) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            step = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemSchedulerOrderStepHistory item : result) {
                        if (item.getStartEventId().equals(startEventId)) {
                            step = item;
                            break;
                        }
                    }
                }
                return step;
            }
        }
        return null;
    }

    public int setMainParentId(SOSHibernateSession session, Long id, Long mainParentId) throws SOSHibernateException {
        String hql = String.format("update %s set mainParentId=:mainParentId  where id=:id", DBLayer.DBITEM_SCHEDULER_ORDER_HISTORY);
        Query<DBItemSchedulerOrderHistory> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("mainParentId", mainParentId);
        return session.executeUpdate(query);
    }

    public int setHasChildren(SOSHibernateSession session, Long id) throws SOSHibernateException {
        String hql = String.format("update %s set hasChildren=true  where id=:id", DBLayer.DBITEM_SCHEDULER_ORDER_HISTORY);
        Query<DBItemSchedulerOrderHistory> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(SOSHibernateSession session, Long id, Date startTime, String state, Long currentStepId, Date modified)
            throws SOSHibernateException {
        String hql = null;
        if (startTime == null) {
            hql = String.format("update %s set currentStepId=:currentStepId, modified=:modified  where id=:id",
                    DBLayer.DBITEM_SCHEDULER_ORDER_HISTORY);
        } else {
            hql = String.format("update %s set startTime=:startTime, state=:state, currentStepId=:currentStepId, modified=:modified  where id=:id",
                    DBLayer.DBITEM_SCHEDULER_ORDER_HISTORY);
        }
        Query<DBItemSchedulerOrderHistory> query = session.createQuery(hql.toString());
        if (startTime != null) {
            query.setParameter("startTime", startTime);
            query.setParameter("state", state);
        }
        query.setParameter("currentStepId", currentStepId);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderStepEnd(SOSHibernateSession session, Long id, Date endTime, String endEventId, String endParameters, Long returnCode,
            String state, Date modified) throws SOSHibernateException {
        String hql = String.format(
                "update %s set endTime=:endTime, endEventId=:endEventId, endParameters=:endParameters, returnCode=:returnCode, state=:state, modified=:modified  where id=:id",
                DBLayer.DBITEM_SCHEDULER_ORDER_STEP_HISTORY);
        Query<DBItemSchedulerOrderStepHistory> query = session.createQuery(hql.toString());
        query.setParameter("endTime", endTime);
        query.setParameter("endEventId", endEventId);
        query.setParameter("endParameters", endParameters);
        query.setParameter("returnCode", returnCode);
        query.setParameter("state", state);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderEnd(SOSHibernateSession session, Long id, Date endTime, String endWorkflowPosition, Long endStepId, String endEventId,
            String state, boolean error, String errorCode, String errorText, Date modified) throws SOSHibernateException {
        String hql = String.format(
                "update %s set endTime=:endTime, endWorkflowPosition=:endWorkflowPosition, endStepId=:endStepId, endEventId=:endEventId, state=:state, error=:error, errorCode=:errorCode, errorText=:errorText, modified=:modified  where id=:id",
                DBLayer.DBITEM_SCHEDULER_ORDER_HISTORY);
        Query<DBItemSchedulerOrderHistory> query = session.createQuery(hql.toString());
        query.setParameter("endTime", endTime);
        query.setParameter("endWorkflowPosition", endWorkflowPosition);
        query.setParameter("endStepId", endStepId);
        query.setParameter("endEventId", endEventId);
        query.setParameter("state", state);
        query.setParameter("error", error);
        query.setParameter("errorCode", errorCode);
        query.setParameter("errorText", errorText);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }
    
    public int resetLockVersion(SOSHibernateSession session, String name) throws SOSHibernateException {
        String hql = String.format("update %s set lockVersion=0  where name=:name", DBLayer.DBITEM_SCHEDULER_SETTINGS);
        Query<DBItemSchedulerSettings> query = session.createQuery(hql.toString());
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }
}
