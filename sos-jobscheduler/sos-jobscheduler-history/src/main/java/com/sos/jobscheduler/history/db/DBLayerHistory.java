package com.sos.jobscheduler.history.db;

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

    public DBItemSchedulerOrderHistory getOrderHistory(SOSHibernateSession session, String schedulerId, String orderKey, String workflowPosition)
            throws SOSHibernateException {
        Query<DBItemSchedulerOrderHistory> query = session.createQuery(String.format(
                "from %s where schedulerId=:schedulerId and orderKey=:orderKey and workflowPosition=:workflowPosition",
                DBLayer.DBITEM_SCHEDULER_ORDER_HISTORY));
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderKey", orderKey);
        query.setParameter("workflowPosition", workflowPosition);
        return session.getSingleResult(query);
    }

    public DBItemSchedulerOrderStepHistory getOrderStepHistoryById(SOSHibernateSession session, Long id) throws SOSHibernateException {
        Query<DBItemSchedulerOrderStepHistory> query = session.createQuery(String.format("from %s where id=:id",
                DBLayer.DBITEM_SCHEDULER_ORDER_STEP_HISTORY));
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public DBItemSchedulerOrderStepHistory getOrderStepHistory(SOSHibernateSession session, String schedulerId, String orderKey)
            throws SOSHibernateException {
        Query<DBItemSchedulerOrderStepHistory> query = session.createQuery(String.format(
                "from %s where schedulerId=:schedulerId and orderKey=:orderKey", DBLayer.DBITEM_SCHEDULER_ORDER_STEP_HISTORY));
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderKey", orderKey);

        List<DBItemSchedulerOrderStepHistory> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            DBItemSchedulerOrderStepHistory step = null;
            for (DBItemSchedulerOrderStepHistory item : result) {
                if (item.getEndTime() != null) {
                    step = item;
                    break;
                }
            }
            return step;
        }
        return null;
    }

    public DBItemSchedulerOrderStepHistory getLastOrderStepHistoryByWorkflowPositionXXX(SOSHibernateSession session, Long orderHistoryId,
            String workflowPosition) throws SOSHibernateException {
        Query<DBItemSchedulerOrderStepHistory> query = session.createQuery(String.format(
                "from %s where orderHistoryId=:orderHistoryId and workflowPosition=:workflowPosition", DBLayer.DBITEM_SCHEDULER_ORDER_STEP_HISTORY));
        query.setParameter("orderHistoryId", orderHistoryId);
        query.setParameter("workflowPosition", workflowPosition);

        List<DBItemSchedulerOrderStepHistory> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            DBItemSchedulerOrderStepHistory step = null;
            Long retryCounter = new Long(-1);
            for (DBItemSchedulerOrderStepHistory item : result) {
                if (item.getRetryCounter() > retryCounter) {
                    step = item;
                    retryCounter = item.getRetryCounter();
                }
            }
            return step;
        }
        return null;
    }
}
