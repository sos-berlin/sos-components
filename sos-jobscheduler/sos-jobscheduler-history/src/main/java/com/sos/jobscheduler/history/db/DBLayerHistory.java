package com.sos.jobscheduler.history.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.general.DBItemVariable;
import com.sos.jobscheduler.db.history.DBItemAgent;
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

    public DBItemVariable getVariable(String name) throws SOSHibernateException {
        String hql = String.format("from %s where name = :name", DBLayer.GENERAL_DBITEM_VARIABLE);
        Query<DBItemVariable> query = session.createQuery(hql);
        query.setParameter("name", name);
        return session.getSingleResult(query);
    }

    public DBItemVariable insertVariable(String name, String eventId) throws SOSHibernateException {
        DBItemVariable item = new DBItemVariable();
        item.setName(name);
        item.setTextValue(String.valueOf(eventId));
        session.save(item);
        return item;
    }

    public DBItemVariable updateVariable(DBItemVariable item, Long eventId) throws SOSHibernateException {
        item.setTextValue(String.valueOf(eventId));
        session.update(item);
        return item;
    }

    public String getMasterTimezone(String masterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select timezone from ");
        hql.append(DBLayer.HISTORY_DBITEM_MASTER);
        hql.append(" where id = ");
        hql.append("(");
        hql.append("select max(id) from ");
        hql.append(DBLayer.HISTORY_DBITEM_MASTER);
        hql.append(" where masterId=:masterId");
        hql.append(")");

        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("masterId", masterId);
        return session.getSingleResult(query);
    }

    public DBItemAgent getAgent(String masterId, String agentPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.HISTORY_DBITEM_AGENT);
        hql.append(" where id = ");
        hql.append("(");
        hql.append("select max(id) from ");
        hql.append(DBLayer.HISTORY_DBITEM_AGENT);
        hql.append(" where masterId=:masterId");
        hql.append(" and path=:agentPath");
        hql.append(")");

        Query<DBItemAgent> query = session.createQuery(hql.toString());
        query.setParameter("masterId", masterId);
        query.setParameter("agentPath", agentPath);
        return session.getSingleResult(query);
    }

    public int updateAgent(Long id, String uri) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_AGENT);
        hql.append(" set uri=:uri ");
        hql.append("where id=:id");

        Query<DBItemAgent> query = session.createQuery(hql.toString());
        query.setParameter("uri", uri);
        query.setParameter("id", id);
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

    public DBItemOrderStep getOrderStep(Long id) throws SOSHibernateException {
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

    public int updateOrderOnFork(Long id, String status) throws SOSHibernateException {
        String hql = String.format("update %s set hasChildren=true, status=:status where id=:id", DBLayer.HISTORY_DBITEM_ORDER);
        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("status", status);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Long currentOrderStepId, Date modified) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_ORDER);
        hql.append(" set currentOrderStepId=:currentOrderStepId ");
        hql.append(",modified=:modified ");
        hql.append("where id=:id");

        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("currentOrderStepId", currentOrderStepId);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Date startTime, String status, Long currentOrderStepId, Date modified) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_ORDER);
        hql.append(" set currentOrderStepId=:currentOrderStepId ");
        hql.append(",modified=:modified ");
        hql.append(",startTime=:startTime ");
        hql.append(",status=:status ");
        hql.append("where id=:id");

        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        query.setParameter("status", status);
        query.setParameter("currentOrderStepId", currentOrderStepId);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderStepEnd(Long id, Date endTime, String endEventId, String endParameters, Long returnCode, String status, boolean error,
            String errorStatus, String errorReason, String errorCode, String errorText, Date modified) throws SOSHibernateException {

        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_ORDER_STEP);
        hql.append(" set endTime=:endTime ");
        hql.append(",endEventId=:endEventId ");
        hql.append(",endParameters=:endParameters ");
        hql.append(",returnCode=:returnCode ");
        hql.append(",status=:status ");
        hql.append(",error=:error ");
        hql.append(",errorStatus=:errorStatus ");
        hql.append(",errorReason=:errorReason ");
        hql.append(",errorCode=:errorCode ");
        hql.append(",errorText=:errorText ");
        hql.append(",modified=:modified ");
        hql.append("where id=:id");

        Query<DBItemOrderStep> query = session.createQuery(hql.toString());
        query.setParameter("endTime", endTime);
        query.setParameter("endEventId", endEventId);
        query.setParameter("endParameters", endParameters);
        query.setParameter("returnCode", returnCode);
        query.setParameter("status", status);
        query.setParameter("error", error);
        query.setParameter("errorStatus", errorStatus);
        query.setParameter("errorReason", errorReason);
        query.setParameter("errorCode", errorCode);
        query.setParameter("errorText", errorText);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderEnd(Long id, Date endTime, String endWorkflowPosition, Long endOrderStepId, String endEventId, String status, Date statusTime,
            boolean error, String errorStatus, String errorReason, Long errorReturnCode, String errorCode, String errorText, Date modified)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_ORDER);

        hql.append(" set modified=:modified");
        if (endTime != null) {
            hql.append(", endTime=:endTime");
            hql.append(", endWorkflowPosition=:endWorkflowPosition ");
            hql.append(", endOrderStepId=:endOrderStepId ");
            hql.append(", endEventId=:endEventId ");
        }
        hql.append(", status=:status ");
        hql.append(", statusTime=:statusTime ");
        hql.append(", error=:error ");
        hql.append(", errorStatus=:errorStatus ");
        hql.append(", errorReason=:errorReason ");
        hql.append(", errorReturnCode=:errorReturnCode ");
        hql.append(", errorCode=:errorCode ");
        hql.append(", errorText=:errorText ");
        hql.append("where id=:id");

        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("modified", modified);
        if (endTime != null) {
            query.setParameter("endTime", endTime);
            query.setParameter("endWorkflowPosition", endWorkflowPosition);
            query.setParameter("endOrderStepId", endOrderStepId);
            query.setParameter("endEventId", endEventId);
        }
        query.setParameter("status", status);
        query.setParameter("statusTime", statusTime);
        query.setParameter("error", error);
        query.setParameter("errorStatus", errorStatus);
        query.setParameter("errorReason", errorReason);
        query.setParameter("errorReturnCode", errorReturnCode);
        query.setParameter("errorCode", errorCode);
        query.setParameter("errorText", errorText);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderLogId(Long id, Long logId) throws SOSHibernateException {
        String hql = String.format("update %s set logId=:logId  where id=:id", DBLayer.HISTORY_DBITEM_ORDER);
        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("logId", logId);
        return session.executeUpdate(query);
    }

    public int setOrderStepLogId(Long id, Long logId) throws SOSHibernateException {
        String hql = String.format("update %s set logId=:logId  where id=:id", DBLayer.HISTORY_DBITEM_ORDER_STEP);
        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("logId", logId);
        return session.executeUpdate(query);
    }

    public int resetLockVersion(String name) throws SOSHibernateException {
        String hql = String.format("update %s set lockVersion=0  where name=:name", DBLayer.GENERAL_DBITEM_VARIABLE);
        Query<DBItemVariable> query = session.createQuery(hql.toString());
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }

}
