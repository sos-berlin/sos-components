package com.sos.js7.history.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.common.HistorySeverity;

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

    public DBItemJocVariable getVariable(String name) throws SOSHibernateException {
        String hql = String.format("from %s where name = :name", DBLayer.DBITEM_JOC_VARIABLE);
        Query<DBItemJocVariable> query = session.createQuery(hql);
        query.setParameter("name", name);
        return session.getSingleResult(query);
    }

    public DBItemJocVariable insertJocVariable(String name, String eventId) throws SOSHibernateException {
        DBItemJocVariable item = new DBItemJocVariable();
        item.setName(name);
        item.setTextValue(String.valueOf(eventId));
        session.save(item);
        return item;
    }

    public int updateJocVariable(String name, Long eventId, boolean resetLockVersion) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_JOC_VARIABLE).append(" ");
        hql.append("set textValue=:textValue ");
        if (resetLockVersion) {
            hql.append(",lockVersion=0 ");
        }
        hql.append("where name=:name");
        Query<DBItemJocVariable> query = session.createQuery(hql.toString());
        query.setParameter("textValue", String.valueOf(eventId));
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }

    public String getControllerTimezone(String jobSchedulerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select timezone from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER);
        hql.append(" where id = ");
        hql.append("(");
        hql.append("select max(id) from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER);
        hql.append(" where jobSchedulerId=:jobSchedulerId");
        hql.append(")");

        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("jobSchedulerId", jobSchedulerId);
        return session.getSingleResult(query);
    }

    public DBItemHistoryAgent getAgent(String jobSchedulerId, String agentPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT);
        hql.append(" where id = ");
        hql.append("(");
        hql.append("select max(id) from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT);
        hql.append(" where jobSchedulerId=:jobSchedulerId");
        hql.append(" and path=:agentPath");
        hql.append(")");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("jobSchedulerId", jobSchedulerId);
        query.setParameter("agentPath", agentPath);
        return session.getSingleResult(query);
    }

    public int updateAgent(Long id, String uri) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT);
        hql.append(" set uri=:uri ");
        hql.append("where id=:id");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("uri", uri);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public List<DBItemHistoryOrder> getOrder(String jobSchedulerId, String orderKey) throws SOSHibernateException {
        Query<DBItemHistoryOrder> query = session.createQuery(String.format("from %s where jobSchedulerId=:jobSchedulerId and orderKey=:orderKey",
                DBLayer.DBITEM_HISTORY_ORDER));
        query.setParameter("jobSchedulerId", jobSchedulerId);
        query.setParameter("orderKey", orderKey);

        return session.getResultList(query);
    }

    public DBItemHistoryOrder getOrderByConstraint(String constraintHash) throws SOSHibernateException {
        Query<DBItemHistoryOrder> query = session.createQuery(String.format("from %s where constraintHash=:constraintHash",
                DBLayer.DBITEM_HISTORY_ORDER));
        query.setParameter("constraintHash", constraintHash);
        return session.getSingleResult(query);
    }

    public DBItemHistoryOrderStep getOrderStep(Long id) throws SOSHibernateException {
        Query<DBItemHistoryOrderStep> query = session.createQuery(String.format("from %s where id=:id", DBLayer.DBITEM_HISTORY_ORDER_STEP));
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public DBItemHistoryOrderStep getOrderStep(String jobSchedulerId, String orderKey) throws SOSHibernateException {
        return getOrderStep(jobSchedulerId, orderKey, null);
    }

    public DBItemHistoryOrderStep getOrderStepByConstraint(String constraintHash) throws SOSHibernateException {
        Query<DBItemHistoryOrderStep> query = session.createQuery(String.format("from %s where constraintHash=:constraintHash",
                DBLayer.DBITEM_HISTORY_ORDER_STEP));
        query.setParameter("constraintHash", constraintHash);
        return session.getSingleResult(query);
    }

    public DBItemHistoryOrderStep getOrderStep(String jobSchedulerId, String orderKey, String startEventId) throws SOSHibernateException {
        Query<DBItemHistoryOrderStep> query = session.createQuery(String.format("from %s where jobSchedulerId=:jobSchedulerId and orderKey=:orderKey",
                DBLayer.DBITEM_HISTORY_ORDER_STEP));
        query.setParameter("jobSchedulerId", jobSchedulerId);
        query.setParameter("orderKey", orderKey);

        List<DBItemHistoryOrderStep> result = session.getResultList(query);
        if (result != null) {
            switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                DBItemHistoryOrderStep step = null;
                if (startEventId == null) {
                    Long eventId = new Long(0);
                    for (DBItemHistoryOrderStep item : result) {
                        Long itemEventId = Long.parseLong(item.getStartEventId());
                        if (itemEventId > eventId) {
                            step = item;
                            eventId = itemEventId;
                        }
                    }
                } else {
                    for (DBItemHistoryOrderStep item : result) {
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
        String hql = String.format("update %s set mainParentId=:mainParentId  where id=:id", DBLayer.DBITEM_HISTORY_ORDER);
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("mainParentId", mainParentId);
        return session.executeUpdate(query);
    }

    public int updateOrderOnResumed(Long id, Integer state, Date stateTime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);
        hql.append(" set severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append(",hasStates=true ");
        hql.append("where id=:id");
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("severity", HistorySeverity.map2DbSeverity(state));
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        return session.executeUpdate(query);
    }

    public int updateOrderOnFork(Long id, Integer state, Date stateTime, String startEventId, Date startTime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);
        hql.append(" set hasChildren=true");
        if (startEventId != null) {
            hql.append(", startTime=:startTime ");
            hql.append(", startEventId=:startEventId ");
        }
        hql.append(", severity=:severity ");
        hql.append(", state=:state ");
        hql.append(", stateTime=:stateTime ");
        hql.append("where id=:id");
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        if (startEventId != null) {
            query.setParameter("startTime", startTime);
            query.setParameter("startEventId", startEventId);
        }
        query.setParameter("severity", HistorySeverity.map2DbSeverity(state));
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Long currentOrderStepId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);
        hql.append(" set currentOrderStepId=:currentOrderStepId ");
        hql.append(",modified=:modified ");
        hql.append("where id=:id");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("currentOrderStepId", currentOrderStepId);
        query.setParameter("modified", new Date());
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Date startTime, String startEventId, Integer state, Date stateTime, Long currentOrderStepId)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);
        hql.append(" set startTime=:startTime ");
        hql.append(",startEventId=:startEventId ");
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append(",currentOrderStepId=:currentOrderStepId ");
        hql.append(",modified=:modified ");
        hql.append("where id=:id");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        query.setParameter("startEventId", startEventId);
        query.setParameter("severity", HistorySeverity.map2DbSeverity(state));
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        query.setParameter("currentOrderStepId", currentOrderStepId);
        query.setParameter("modified", new Date());
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderStepEnd(Long id, Date endTime, String endEventId, String endParameters, Integer returnCode, Integer severity, boolean error,
            String errorState, String errorReason, String errorCode, String errorText, Date modified) throws SOSHibernateException {

        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STEP);
        hql.append(" set endTime=:endTime ");
        hql.append(",endEventId=:endEventId ");
        hql.append(",endParameters=:endParameters ");
        hql.append(",returnCode=:returnCode ");
        hql.append(",severity=:severity ");
        hql.append(",error=:error ");
        hql.append(",errorState=:errorState ");
        hql.append(",errorReason=:errorReason ");
        hql.append(",errorCode=:errorCode ");
        hql.append(",errorText=:errorText ");
        hql.append(",modified=:modified ");
        hql.append("where id=:id");

        Query<DBItemHistoryOrderStep> query = session.createQuery(hql.toString());
        query.setParameter("endTime", endTime);
        query.setParameter("endEventId", endEventId);
        query.setParameter("endParameters", endParameters);
        query.setParameter("returnCode", returnCode);
        query.setParameter("severity", severity);
        query.setParameter("error", error);
        query.setParameter("errorState", errorState);
        query.setParameter("errorReason", errorReason);
        query.setParameter("errorCode", errorCode);
        query.setParameter("errorText", errorText);
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderEnd(Long id, Date endTime, String endWorkflowPosition, Long endOrderStepId, String endEventId, Integer state, Date stateTime,
            boolean hasStates, boolean error, String errorState, String errorReason, Integer errorReturnCode, String errorCode, String errorText,
            Date startTime, String startEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);

        hql.append(" set modified=:modified");
        if (endTime != null) {
            hql.append(", endTime=:endTime");
            hql.append(", endWorkflowPosition=:endWorkflowPosition ");
            hql.append(", endOrderStepId=:endOrderStepId ");
            hql.append(", endEventId=:endEventId ");
        }
        if (startTime != null) {
            hql.append(", startTime=:startTime");
            hql.append(", startEventId=:startEventId");
        }
        hql.append(", severity=:severity ");
        hql.append(", state=:state ");
        hql.append(", stateTime=:stateTime ");
        hql.append(", hasStates=:hasStates ");
        hql.append(", error=:error ");
        hql.append(", errorState=:errorState ");
        hql.append(", errorReason=:errorReason ");
        hql.append(", errorReturnCode=:errorReturnCode ");
        hql.append(", errorCode=:errorCode ");
        hql.append(", errorText=:errorText ");
        hql.append("where id=:id");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("modified", new Date());
        if (endTime != null) {
            query.setParameter("endTime", endTime);
            query.setParameter("endWorkflowPosition", endWorkflowPosition);
            query.setParameter("endOrderStepId", endOrderStepId);
            query.setParameter("endEventId", endEventId);
        }
        if (startTime != null) {
            query.setParameter("startTime", startTime);
            query.setParameter("startEventId", startEventId);
        }
        query.setParameter("severity", HistorySeverity.map2DbSeverity(state));
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        query.setParameter("hasStates", hasStates);
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
        String hql = String.format("update %s set logId=:logId  where id=:id", DBLayer.DBITEM_HISTORY_ORDER);
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("logId", logId);
        return session.executeUpdate(query);
    }

    public int setOrderStepLogId(Long id, Long logId) throws SOSHibernateException {
        String hql = String.format("update %s set logId=:logId  where id=:id", DBLayer.DBITEM_HISTORY_ORDER_STEP);
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("logId", logId);
        return session.executeUpdate(query);
    }

}
