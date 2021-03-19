package com.sos.js7.history.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.model.inventory.common.ConfigurationType;

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

    public String getLastControllerTimezone(String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select timezone from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER);
        hql.append(" where readyEventId = ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER);
        hql.append(" where controllerId=:controllerId");
        hql.append(")");

        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public DBItemHistoryController getControllerByShutDownEventId(String controllerId, Long shutDownEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where readyEventId = ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER);
        hql.append(" where controllerId=:controllerId ");
        hql.append(" and readyEventId < :shutDownEventId ");
        hql.append(")");

        Query<DBItemHistoryController> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("shutDownEventId", shutDownEventId);

        return session.getSingleResult(query);
    }

    public DBItemHistoryAgent getLastAgent(String controllerId, String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT);
        hql.append(" where readyEventId = ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT);
        hql.append(" where controllerId=:controllerId ");
        hql.append(" and agentId=:agentId");
        hql.append(")");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        return session.getSingleResult(query);
    }

    public DBItemHistoryAgent getAgentByCouplingFailedEventId(String controllerId, String agentId, Long couplingFailedEventId)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId = ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT);
        hql.append(" where controllerId=:controllerId ");
        hql.append(" and agentId=:agentId ");
        hql.append(" and readyEventId < :couplingFailedEventId ");
        hql.append(")");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        query.setParameter("couplingFailedEventId", couplingFailedEventId);

        return session.getSingleResult(query);
    }

    public DBItemHistoryAgent getAgentByReadyEventId(Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId =:readyEventId");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("readyEventId", readyEventId);

        return session.getSingleResult(query);
    }

    public DBItemInventoryAgentInstance getAgentInstance(String controllerId, String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
        hql.append(" where controllerId=:controllerId");
        hql.append(" and agentId=:agentId");

        Query<DBItemInventoryAgentInstance> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        return session.getSingleResult(query);
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

    public DBItemHistoryOrderStep getOrderStepByConstraint(String constraintHash) throws SOSHibernateException {
        Query<DBItemHistoryOrderStep> query = session.createQuery(String.format("from %s where constraintHash=:constraintHash",
                DBLayer.DBITEM_HISTORY_ORDER_STEP));
        query.setParameter("constraintHash", constraintHash);
        return session.getSingleResult(query);
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

    public int updateOrderOnFork(Long id, Integer state, Date stateTime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);
        hql.append(" set hasChildren=true");
        hql.append(", severity=:severity ");
        hql.append(", state=:state ");
        hql.append(", stateTime=:stateTime ");
        hql.append("where id=:id");
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("severity", HistorySeverity.map2DbSeverity(state));
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Long currentHistoryOrderStepId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);
        hql.append(" set currentHistoryOrderStepId=:currentHistoryOrderStepId ");
        hql.append(",modified=:modified ");
        hql.append("where id=:id");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("currentHistoryOrderStepId", currentHistoryOrderStepId);
        query.setParameter("modified", new Date());
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderStepEnd(Long id, Date endTime, Long endEventId, String endParameters, Integer returnCode, Integer severity, boolean error,
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
        query.setParameter("errorCode", DBItemHistoryOrderStep.normalizeErrorCode(errorCode));
        query.setParameter("errorText", DBItemHistoryOrderStep.normalizeErrorText(errorText));
        query.setParameter("modified", modified);
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderEnd(Long id, Date endTime, String endWorkflowPosition, Long endHistoryOrderStepId, Long endEventId, Integer state,
            Date stateTime, boolean hasStates, boolean error, String errorState, String errorReason, Integer errorReturnCode, String errorCode,
            String errorText) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER);

        hql.append(" set modified=:modified");
        if (endTime != null) {
            hql.append(", endTime=:endTime");
            hql.append(", endWorkflowPosition=:endWorkflowPosition ");
            hql.append(", endHistoryOrderStepId=:endHistoryOrderStepId ");
            hql.append(", endEventId=:endEventId ");
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
            query.setParameter("endHistoryOrderStepId", endHistoryOrderStepId);
            query.setParameter("endEventId", endEventId);
        }

        query.setParameter("severity", HistorySeverity.map2DbSeverity(state));
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        query.setParameter("hasStates", hasStates);
        query.setParameter("error", error);
        query.setParameter("errorState", errorState);
        query.setParameter("errorReason", errorReason);
        query.setParameter("errorReturnCode", errorReturnCode);
        query.setParameter("errorCode", DBItemHistoryOrder.normalizeErrorCode(errorCode));
        query.setParameter("errorText", DBItemHistoryOrder.normalizeErrorText(errorText));
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

    public String getDeployedWorkflowPath(String controllerId, String workflowName, String workflowVersionId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append("where type=:type ");
        hql.append("and controllerId=:controllerId ");
        hql.append("and name=:workflowName ");
        hql.append("and commitId=:workflowVersionId");

        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        query.setParameter("controllerId", controllerId);
        query.setParameter("workflowName", workflowName);
        query.setParameter("workflowVersionId", workflowVersionId);
        List<String> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

}
