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

    public String getMasterTimezone(String jobSchedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select timezone from ");
        hql.append(DBLayer.HISTORY_DBITEM_MASTER);
        hql.append(" where id = ");
        hql.append("(");
        hql.append("select max(id) from ");
        hql.append(DBLayer.HISTORY_DBITEM_MASTER);
        hql.append(" where jobSchedulerId=:jobSchedulerId");
        hql.append(")");

        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("jobSchedulerId", jobSchedulerId);
        return session.getSingleResult(query);
    }

    public DBItemAgent getAgent(String jobSchedulerId, String agentPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.HISTORY_DBITEM_AGENT);
        hql.append(" where id = ");
        hql.append("(");
        hql.append("select max(id) from ");
        hql.append(DBLayer.HISTORY_DBITEM_AGENT);
        hql.append(" where jobSchedulerId=:jobSchedulerId");
        hql.append(" and path=:agentPath");
        hql.append(")");

        Query<DBItemAgent> query = session.createQuery(hql.toString());
        query.setParameter("jobSchedulerId", jobSchedulerId);
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

    public List<DBItemOrder> getOrder(String jobSchedulerId, String orderKey) throws SOSHibernateException {
        Query<DBItemOrder> query = session.createQuery(String.format("from %s where jobSchedulerId=:jobSchedulerId and orderKey=:orderKey",
                DBLayer.HISTORY_DBITEM_ORDER));
        query.setParameter("jobSchedulerId", jobSchedulerId);
        query.setParameter("orderKey", orderKey);

        return session.getResultList(query);
    }

    public DBItemOrderStep getOrderStep(Long id) throws SOSHibernateException {
        Query<DBItemOrderStep> query = session.createQuery(String.format("from %s where id=:id", DBLayer.HISTORY_DBITEM_ORDER_STEP));
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public DBItemOrderStep getOrderStep(String jobSchedulerId, String orderKey) throws SOSHibernateException {
        return getOrderStep(jobSchedulerId, orderKey, null);
    }

    public DBItemOrderStep getOrderStep(String jobSchedulerId, String orderKey, String startEventId) throws SOSHibernateException {
        Query<DBItemOrderStep> query = session.createQuery(String.format("from %s where jobSchedulerId=:jobSchedulerId and orderKey=:orderKey",
                DBLayer.HISTORY_DBITEM_ORDER_STEP));
        query.setParameter("jobSchedulerId", jobSchedulerId);
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

    public int updateOrderOnFork(Long id, String state) throws SOSHibernateException {
        return updateOrderOnFork(id, null, state);
    }

    public int updateOrderOnFork(Long id, Date startTime, String state) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_ORDER);
        hql.append(" set hasChildren=true");
        if (startTime != null) {
            hql.append(", startTime=:startTime ");
        }
        hql.append(", state=:state ");
        hql.append("where id=:id");
        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        if (startTime != null) {
            query.setParameter("startTime", startTime);
        }
        query.setParameter("state", state);
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

    public int updateOrderOnOrderStep(Long id, Date startTime, String state, Long currentOrderStepId, Date modified) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_ORDER);
        hql.append(" set currentOrderStepId=:currentOrderStepId ");
        hql.append(",modified=:modified ");
        hql.append(",startTime=:startTime ");
        hql.append(",state=:state ");
        hql.append("where id=:id");

        Query<DBItemOrder> query = session.createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        query.setParameter("state", state);
        query.setParameter("currentOrderStepId", currentOrderStepId);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderStepEnd(Long id, Date endTime, String endEventId, String endParameters, Long returnCode, String state, boolean error,
            String errorState, String errorReason, String errorCode, String errorText, Date modified) throws SOSHibernateException {

        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.HISTORY_DBITEM_ORDER_STEP);
        hql.append(" set endTime=:endTime ");
        hql.append(",endEventId=:endEventId ");
        hql.append(",endParameters=:endParameters ");
        hql.append(",returnCode=:returnCode ");
        hql.append(",state=:state ");
        hql.append(",error=:error ");
        hql.append(",errorState=:errorState ");
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
        query.setParameter("state", state);
        query.setParameter("error", error);
        query.setParameter("errorState", errorState);
        query.setParameter("errorReason", errorReason);
        query.setParameter("errorCode", errorCode);
        query.setParameter("errorText", errorText);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderEnd(Long id, Date endTime, String endWorkflowPosition, Long endOrderStepId, String endEventId, String state, Date stateTime,
            boolean error, String errorState, String errorReason, Long errorReturnCode, String errorCode, String errorText, Date modified)
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
        hql.append(", state=:state ");
        hql.append(", stateTime=:stateTime ");
        hql.append(", error=:error ");
        hql.append(", errorState=:errorState ");
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
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        query.setParameter("error", error);
        query.setParameter("errorState", errorState);
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
