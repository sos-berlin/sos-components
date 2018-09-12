package com.sos.jobscheduler.history.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.general.DBItemSetting;
import com.sos.jobscheduler.db.history.DBItemAgent;
import com.sos.jobscheduler.db.history.DBItemMaster;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.history.DBItemOrderStep;

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

    public DBItemSetting getSetting(String name) throws SOSHibernateException {
        String hql = String.format("from %s where name = :name", DBLayer.GENERAL_DBITEM_SETTING);
        Query<DBItemSetting> query = session.createQuery(hql);
        query.setParameter("name", name);
        return session.getSingleResult(query);
    }

    public DBItemSetting insertSetting(String name, String eventId) throws SOSHibernateException {
        DBItemSetting item = new DBItemSetting();
        item.setName(name);
        item.setTextValue(String.valueOf(eventId));
        session.save(item);
        return item;
    }

    public DBItemSetting updateSetting(DBItemSetting item, Long eventId) throws SOSHibernateException {
        item.setTextValue(String.valueOf(eventId));
        session.update(item);
        return item;
    }

    public String getLastMasterTimezone(String masterId) throws SOSHibernateException {
        String hql = String.format("select timezone from %s where masterId=:masterId and lastEntry=true", DBLayer.HISTORY_DBITEM_MASTER);
        Query<String> query = session.createQuery(hql);
        query.setParameter("masterId", masterId);
        return session.getSingleResult(query);
    }

    public int setMasterLastEntry(String masterId, boolean lastEntry) throws SOSHibernateException {
        String hql = String.format("update %s set lastEntry=:lastEntry where masterId=:masterId", DBLayer.HISTORY_DBITEM_MASTER);
        Query<DBItemMaster> query = session.createQuery(hql.toString());
        query.setParameter("masterId", masterId);
        query.setParameter("lastEntry", lastEntry);
        return session.executeUpdate(query);
    }

    public DBItemAgent getLastAgent(String masterId, String agentPath) throws SOSHibernateException {
        String hql = String.format("from %s where masterId=:masterId and path=:agentPath and lastEntry=true", DBLayer.HISTORY_DBITEM_AGENT);
        Query<DBItemAgent> query = session.createQuery(hql);
        query.setParameter("masterId", masterId);
        query.setParameter("agentPath", agentPath);
        return session.getSingleResult(query);
    }

    public int setAgentLastEntry(String masterId, String agentPath, boolean lastEntry) throws SOSHibernateException {
        String hql = String.format("update %s set lastEntry=:lastEntry where masterId=:masterId and path=:agentPath", DBLayer.HISTORY_DBITEM_AGENT);
        Query<DBItemAgent> query = session.createQuery(hql.toString());
        query.setParameter("masterId", masterId);
        query.setParameter("agentPath", agentPath);
        query.setParameter("lastEntry", lastEntry);
        return session.executeUpdate(query);
    }

    public DBItemOrder getOrder(String masterId, String orderKey) throws SOSHibernateException {
        return getOrder(masterId, orderKey, null);
    }

    public DBItemOrder getOrder(String masterId, String orderKey, String startEventId) throws SOSHibernateException {
        Query<DBItemOrder> query = session.createQuery(String.format("from %s where masterId=:masterId and orderKey=:orderKey",
                DBLayer.HISTORY_DBITEM_ORDER));
        query.setParameter("masterId", masterId);
        query.setParameter("orderKey", orderKey);

        List<DBItemOrder> result = session.getResultList(query);
        if (result != null) {
            switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                DBItemOrder order = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemOrder item : result) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            order = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemOrder item : result) {
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

    public DBItemOrderStep getOrderStepById(Long id) throws SOSHibernateException {
        Query<DBItemOrderStep> query = session.createQuery(String.format("from %s where id=:id", DBLayer.HISTORY_DBITEM_ORDER_STEP));
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public DBItemOrderStep getOrderStep(String masterId, String orderKey) throws SOSHibernateException {
        return getOrderStep(masterId, orderKey, null);
    }

    public DBItemOrderStep getOrderStep(String masterId, String orderKey, String startEventId) throws SOSHibernateException {
        Query<DBItemOrderStep> query = session.createQuery(String.format("from %s where masterId=:masterId and orderKey=:orderKey",
                DBLayer.HISTORY_DBITEM_ORDER_STEP));
        query.setParameter("masterId", masterId);
        query.setParameter("orderKey", orderKey);

        List<DBItemOrderStep> result = session.getResultList(query);
        if (result != null) {
            switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                DBItemOrderStep step = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemOrderStep item : result) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            step = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemOrderStep item : result) {
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
        String hql = String.format("update %s set mainParentId=:mainParentId  where id=:id", DBLayer.HISTORY_DBITEM_ORDER);
        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("mainParentId", mainParentId);
        return session.executeUpdate(query);
    }

    public int setHasChildren(Long id) throws SOSHibernateException {
        String hql = String.format("update %s set hasChildren=true where id=:id", DBLayer.HISTORY_DBITEM_ORDER);
        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Date startTime, String state, Long currentStepId, Date modified) throws SOSHibernateException {
        String hql = null;
        if (startTime == null) {
            hql = String.format("update %s set currentStepId=:currentStepId, modified=:modified  where id=:id", DBLayer.HISTORY_DBITEM_ORDER);
        } else {
            hql = String.format("update %s set startTime=:startTime, state=:state, currentStepId=:currentStepId, modified=:modified  where id=:id",
                    DBLayer.HISTORY_DBITEM_ORDER);
        }
        Query<DBItemOrder> query = session.createQuery(hql.toString());
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
                DBLayer.HISTORY_DBITEM_ORDER_STEP);
        Query<DBItemOrderStep> query = session.createQuery(hql.toString());
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
                DBLayer.HISTORY_DBITEM_ORDER);
        Query<DBItemOrder> query = session.createQuery(hql.toString());
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
        String hql = String.format("update %s set lockVersion=0  where name=:name", DBLayer.GENERAL_DBITEM_SETTING);
        Query<DBItemSetting> query = session.createQuery(hql.toString());
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }
}
