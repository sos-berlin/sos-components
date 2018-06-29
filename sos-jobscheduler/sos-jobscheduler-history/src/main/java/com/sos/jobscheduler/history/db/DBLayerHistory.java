package com.sos.jobscheduler.history.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBItemJobSchedulerOrderHistory;
import com.sos.jobscheduler.db.DBItemJobSchedulerOrderStepHistory;
import com.sos.jobscheduler.db.DBItemJobSchedulerSettings;
import com.sos.jobscheduler.db.DBLayer;

public class DBLayerHistory {

    private final SOSHibernateSession session;

    public DBLayerHistory(SOSHibernateSession hibernateSession) {
        session = hibernateSession;
    }

    public SOSHibernateSession getSession() {
        return session;
    }
    
    public void close() {
        if (session != null) {
            session.close();
        }
    }

    public DBItemJobSchedulerSettings getSchedulerSettings(String name) throws SOSHibernateException {
        String hql = String.format("from %s where name = :name", DBLayer.DBITEM_JOBSCHEDULER_SETTINGS);
        Query<DBItemJobSchedulerSettings> query = session.createQuery(hql);
        query.setParameter("name", name);
        return session.getSingleResult(query);
    }

    public DBItemJobSchedulerSettings insertSchedulerSettings(String name, String eventId) throws SOSHibernateException {
        DBItemJobSchedulerSettings item = new DBItemJobSchedulerSettings();
        item.setName(name);
        item.setTextValue(String.valueOf(eventId));
        session.save(item);
        return item;
    }

    public DBItemJobSchedulerSettings updateSchedulerSettings(DBItemJobSchedulerSettings item, Long eventId) throws SOSHibernateException {
        item.setTextValue(String.valueOf(eventId));
        session.update(item);
        return item;
    }

    public DBItemJobSchedulerOrderHistory getOrderHistory(String schedulerId, String orderKey) throws SOSHibernateException {

        return getOrderHistory(schedulerId, orderKey, null);
    }

    public DBItemJobSchedulerOrderHistory getOrderHistory(String schedulerId, String orderKey, String startEventId) throws SOSHibernateException {
        Query<DBItemJobSchedulerOrderHistory> query = session.createQuery(String.format(
                "from %s where schedulerId=:schedulerId and orderKey=:orderKey", DBLayer.DBITEM_JOBSCHEDULER_ORDER_HISTORY));
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderKey", orderKey);

        List<DBItemJobSchedulerOrderHistory> result = session.getResultList(query);
        if (result != null) {
            switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                DBItemJobSchedulerOrderHistory order = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemJobSchedulerOrderHistory item : result) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            order = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemJobSchedulerOrderHistory item : result) {
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

    public DBItemJobSchedulerOrderStepHistory getOrderStepHistoryById(Long id) throws SOSHibernateException {
        Query<DBItemJobSchedulerOrderStepHistory> query = session.createQuery(String.format("from %s where id=:id",
                DBLayer.DBITEM_JOBSCHEDULER_ORDER_STEP_HISTORY));
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public DBItemJobSchedulerOrderStepHistory getOrderStepHistory(String schedulerId, String orderKey) throws SOSHibernateException {

        return getOrderStepHistory(schedulerId, orderKey, null);
    }

    public DBItemJobSchedulerOrderStepHistory getOrderStepHistory(String schedulerId, String orderKey, String startEventId)
            throws SOSHibernateException {
        Query<DBItemJobSchedulerOrderStepHistory> query = session.createQuery(String.format(
                "from %s where schedulerId=:schedulerId and orderKey=:orderKey", DBLayer.DBITEM_JOBSCHEDULER_ORDER_STEP_HISTORY));
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderKey", orderKey);

        List<DBItemJobSchedulerOrderStepHistory> result = session.getResultList(query);
        if (result != null) {
            switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                DBItemJobSchedulerOrderStepHistory step = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemJobSchedulerOrderStepHistory item : result) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            step = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemJobSchedulerOrderStepHistory item : result) {
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

    public int setMainParentId(Long id, Long mainParentId) throws SOSHibernateException {
        String hql = String.format("update %s set mainParentId=:mainParentId  where id=:id", DBLayer.DBITEM_JOBSCHEDULER_ORDER_HISTORY);
        Query<DBItemJobSchedulerOrderHistory> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("mainParentId", mainParentId);
        return session.executeUpdate(query);
    }

    public int setHasChildren(Long id) throws SOSHibernateException {
        String hql = String.format("update %s set hasChildren=true  where id=:id", DBLayer.DBITEM_JOBSCHEDULER_ORDER_HISTORY);
        Query<DBItemJobSchedulerOrderHistory> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Date startTime, String state, Long currentStepId, Date modified) throws SOSHibernateException {
        String hql = null;
        if (startTime == null) {
            hql = String.format("update %s set currentStepId=:currentStepId, modified=:modified  where id=:id",
                    DBLayer.DBITEM_JOBSCHEDULER_ORDER_HISTORY);
        } else {
            hql = String.format("update %s set startTime=:startTime, state=:state, currentStepId=:currentStepId, modified=:modified  where id=:id",
                    DBLayer.DBITEM_JOBSCHEDULER_ORDER_HISTORY);
        }
        Query<DBItemJobSchedulerOrderHistory> query = session.createQuery(hql.toString());
        if (startTime != null) {
            query.setParameter("startTime", startTime);
            query.setParameter("state", state);
        }
        query.setParameter("currentStepId", currentStepId);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderStepEnd(Long id, Date endTime, String endEventId, String endParameters, Long returnCode, String state, Date modified)
            throws SOSHibernateException {
        String hql = String.format(
                "update %s set endTime=:endTime, endEventId=:endEventId, endParameters=:endParameters, returnCode=:returnCode, state=:state, modified=:modified  where id=:id",
                DBLayer.DBITEM_JOBSCHEDULER_ORDER_STEP_HISTORY);
        Query<DBItemJobSchedulerOrderStepHistory> query = session.createQuery(hql.toString());
        query.setParameter("endTime", endTime);
        query.setParameter("endEventId", endEventId);
        query.setParameter("endParameters", endParameters);
        query.setParameter("returnCode", returnCode);
        query.setParameter("state", state);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderEnd(Long id, Date endTime, String endWorkflowPosition, Long endStepId, String endEventId, String state, boolean error,
            String errorCode, String errorText, Date modified) throws SOSHibernateException {
        String hql = String.format(
                "update %s set endTime=:endTime, endWorkflowPosition=:endWorkflowPosition, endStepId=:endStepId, endEventId=:endEventId, state=:state, error=:error, errorCode=:errorCode, errorText=:errorText, modified=:modified  where id=:id",
                DBLayer.DBITEM_JOBSCHEDULER_ORDER_HISTORY);
        Query<DBItemJobSchedulerOrderHistory> query = session.createQuery(hql.toString());
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

    public int resetLockVersion(String name) throws SOSHibernateException {
        String hql = String.format("update %s set lockVersion=0  where name=:name", DBLayer.DBITEM_JOBSCHEDULER_SETTINGS);
        Query<DBItemJobSchedulerSettings> query = session.createQuery(hql.toString());
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }
}
