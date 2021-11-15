package com.sos.js7.history.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerHistory.class);

    private static final String LOGS_VARIABLE_NAME = "cluster_history_logs";
    /** result rerun interval in seconds */
    private static final long RERUN_INTERVAL = 2;
    private static final int MAX_RERUNS = 3;

    private SOSHibernateSession session;

    public DBLayerHistory(SOSHibernateSession hibernateSession) {
        session = hibernateSession;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public void setSession(SOSHibernateSession val) {
        if (session != null) {
            session.close();
        }
        session = val;
    }

    public void close() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public DBItemJocVariable getControllerVariable(String name) throws SOSHibernateException {
        String hql = String.format("select name,textValue from %s where name=:name", DBLayer.DBITEM_JOC_VARIABLES);
        Query<Object[]> query = session.createQuery(hql);
        query.setParameter("name", name);
        Object[] o = session.getSingleResult(query);
        if (o == null) {
            return null;
        }
        DBItemJocVariable item = new DBItemJocVariable();
        item.setName(name);
        item.setTextValue(o[1].toString());
        return item;
    }

    public DBItemJocVariable insertControllerVariable(String name, String eventId) throws SOSHibernateException {
        DBItemJocVariable item = new DBItemJocVariable();
        item.setName(name);
        item.setTextValue(String.valueOf(eventId));
        session.save(item);
        return item;
    }

    public int updateControllerVariable(String name, Long eventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_JOC_VARIABLES).append(" ");
        hql.append("set textValue=:textValue ");
        hql.append("where name=:name");
        Query<DBItemJocVariable> query = session.createQuery(hql.toString());
        query.setParameter("textValue", String.valueOf(eventId));
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }

    public DBItemJocVariable getLogsVariable() throws SOSHibernateException {
        String hql = String.format("from %s where name=:name", DBLayer.DBITEM_JOC_VARIABLES);
        Query<DBItemJocVariable> query = session.createQuery(hql);
        query.setParameter("name", LOGS_VARIABLE_NAME);
        return session.getSingleResult(query);
    }

    public void handleLogsVariable(String memberId, byte[] compressed) throws SOSHibernateException {
        if (compressed == null) {
            StringBuilder hql = new StringBuilder("delete ");
            hql.append("from ").append(DBLayer.DBITEM_JOC_VARIABLES).append(" ");
            hql.append("where name=:name");

            Query<Long> query = session.createQuery(hql.toString());
            query.setParameter("name", LOGS_VARIABLE_NAME);
            session.executeUpdate(query);

        } else {
            DBItemJocVariable item = new DBItemJocVariable();
            item.setName(LOGS_VARIABLE_NAME);
            item.setTextValue(memberId);
            item.setBinaryValue(compressed);

            StringBuilder hql = new StringBuilder("select count(name) ");
            hql.append("from ").append(DBLayer.DBITEM_JOC_VARIABLES).append(" ");
            hql.append("where name=:name");

            Query<Long> query = session.createQuery(hql.toString());
            query.setParameter("name", LOGS_VARIABLE_NAME);

            Long result = session.getSingleValue(query);
            if (result.equals(0L)) {
                session.save(item);
            } else {
                session.update(item);
            }
        }
    }

    public String getLastControllerTimezone(String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select timezone from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId= ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId");
        hql.append(")");

        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public Long getCountNotFinishedOrderLogs() throws SOSHibernateException {
        return session.getSingleValue("select count(id) from " + DBLayer.DBITEM_HISTORY_ORDERS + " where parentId=0 and logId=0");
    }

    public Long getCountJocInstances() throws SOSHibernateException {
        return session.getSingleValue("select count(id) from " + DBLayer.DBITEM_JOC_INSTANCES);
    }

    public DBItemHistoryController getControllerByNextEventId(String controllerId, Long nextEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId = ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId < :nextEventId ");
        hql.append(")");

        Query<DBItemHistoryController> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("nextEventId", nextEventId);

        return session.getSingleResult(query);
    }

    public Object[] getLastExecution(String controllerId, Long fromEventId, Long toEventId, String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select startTime, endTime from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
        hql.append("where id=");
        hql.append("(");
        hql.append("select max(id) from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and startEventId > :fromEventId ");
        hql.append("and startEventId < :toEventId ");
        if (agentId != null) {
            hql.append("and agentId=:agentId ");
        }
        hql.append(")");

        Query<Object[]> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("fromEventId", fromEventId);
        query.setParameter("toEventId", toEventId);
        if (agentId != null) {
            query.setParameter("agentId", agentId);
        }
        return session.getSingleResult(query);
    }

    public DBItemHistoryController getControllerByReadyEventId(String controllerId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId=:readyEventId");

        Query<DBItemHistoryController> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("readyEventId", readyEventId);
        return session.getSingleResult(query);
    }

    public DBItemHistoryAgent getLastAgent(String controllerId, String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId= ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and agentId=:agentId");
        hql.append(")");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        return session.getSingleResult(query);
    }

    public DBItemHistoryAgent getAgentByNextEventId(String controllerId, String agentId, Long nextEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId= ");
        hql.append("(");
        hql.append("select max(readyEventId) from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and agentId=:agentId ");
        hql.append("and readyEventId < :nextEventId ");
        hql.append(")");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        query.setParameter("nextEventId", nextEventId);

        return session.getSingleResult(query);
    }

    public DBItemHistoryAgent getAgentByReadyEventId(String controllerId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId=:readyEventId");

        Query<DBItemHistoryAgent> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("readyEventId", readyEventId);

        return session.getSingleResult(query);
    }

    public DBItemInventoryAgentInstance getAgentInstance(String controllerId, String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and agentId=:agentId");

        Query<DBItemInventoryAgentInstance> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        return session.getSingleResult(query);
    }

    public DBItemHistoryOrder getOrderByConstraint(String constraintHash) throws SOSHibernateException {
        Query<DBItemHistoryOrder> query = session.createQuery(String.format("from %s where constraintHash=:constraintHash",
                DBLayer.DBITEM_HISTORY_ORDERS));
        query.setParameter("constraintHash", constraintHash);
        return session.getSingleResult(query);
    }

    public DBItemHistoryOrder getOrderByCurrentEventId(String controllerId, String orderId, Date currentEventDateTime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" o1 ");
        hql.append("where o1.controllerId=:controllerId ");
        hql.append("and o1.orderId=:orderId ");
        hql.append("and o1.startTime=");
        hql.append("(select max(o2.startTime) from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDERS).append(" o2 ");
        hql.append("where o2.controllerId=o1.controllerId ");
        hql.append("and o2.orderId=o1.orderId ");
        hql.append("and o2.startTime <= :currentEventDateTime");
        hql.append(")");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("orderId", orderId);
        query.setParameter("currentEventDateTime", currentEventDateTime);
        query.setReadOnly(true);

        List<DBItemHistoryOrder> result = executeQueryList("getOrderByCurrentEventId", query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemHistoryOrderStep getOrderStep(Long id) throws SOSHibernateException {
        Query<DBItemHistoryOrderStep> query = session.createQuery(String.format("from %s where id=:id", DBLayer.DBITEM_HISTORY_ORDER_STEPS));
        query.setParameter("id", id);
        return session.getSingleResult(query);
    }

    public DBItemHistoryOrderStep getOrderStepByConstraint(String constraintHash) throws SOSHibernateException {
        Query<DBItemHistoryOrderStep> query = session.createQuery(String.format("from %s where constraintHash=:constraintHash",
                DBLayer.DBITEM_HISTORY_ORDER_STEPS));
        query.setParameter("constraintHash", constraintHash);
        return session.getSingleResult(query);
    }

    public DBItemHistoryOrderStep getOrderStepByWorkflowPosition(String controllerId, Long historyOrderId, String workflowPosition)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and historyOrderId=:historyOrderId ");
        hql.append("and workflowPosition=:workflowPosition");

        Query<DBItemHistoryOrderStep> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("historyOrderId", historyOrderId);
        query.setParameter("workflowPosition", DBItemHistoryOrder.normalizeWorkflowPosition(workflowPosition));
        query.setReadOnly(true);

        List<DBItemHistoryOrderStep> result = executeQueryList("getOrderStepByWorkflowPosition", query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public int setMainParentId(Long id, Long mainParentId) throws SOSHibernateException {
        String hql = String.format("update %s set mainParentId=:mainParentId  where id=:id", DBLayer.DBITEM_HISTORY_ORDERS);
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("mainParentId", mainParentId);
        return session.executeUpdate(query);
    }

    public int updateOrderOnResumed(Long id, Integer state, Date stateTime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("set severity=:severity ");
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
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("set hasChildren=true");
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append("where id=:id");
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("severity", HistorySeverity.map2DbSeverity(state));
        query.setParameter("state", state);
        query.setParameter("stateTime", stateTime);
        return session.executeUpdate(query);
    }

    public int updateOrderOnOrderStep(Long id, Long currentHistoryOrderStepId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("set currentHistoryOrderStepId=:currentHistoryOrderStepId ");
        hql.append(",modified=:modified ");
        hql.append("where id=:id");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("currentHistoryOrderStepId", currentHistoryOrderStepId);
        query.setParameter("modified", new Date());
        query.setParameter("id", id);
        return session.executeUpdate(query);
    }

    public int setOrderStepEnd(Long id, Date endTime, Long endEventId, String endVariables, Integer returnCode, Integer severity, boolean error,
            String errorState, String errorReason, String errorCode, String errorText, Date modified) throws SOSHibernateException {

        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
        hql.append("set endTime=:endTime ");
        hql.append(",endEventId=:endEventId ");
        hql.append(",endVariables=:endVariables ");
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
        query.setParameter("endVariables", endVariables);
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
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("set modified=:modified ");
        if (endTime != null) {
            hql.append(",endTime=:endTime");
            hql.append(",endWorkflowPosition=:endWorkflowPosition ");
            hql.append(",endHistoryOrderStepId=:endHistoryOrderStepId ");
            hql.append(",endEventId=:endEventId ");
        }

        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append(",hasStates=:hasStates ");
        hql.append(",error=:error ");
        hql.append(",errorState=:errorState ");
        hql.append(",errorReason=:errorReason ");
        hql.append(",errorReturnCode=:errorReturnCode ");
        hql.append(",errorCode=:errorCode ");
        hql.append(",errorText=:errorText ");
        hql.append("where id=:id");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("modified", new Date());
        if (endTime != null) {
            query.setParameter("endTime", endTime);
            query.setParameter("endWorkflowPosition", DBItemHistoryOrder.normalizeWorkflowPosition(endWorkflowPosition));
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
        String hql = String.format("update %s set logId=:logId  where id=:id", DBLayer.DBITEM_HISTORY_ORDERS);
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("logId", logId);
        return session.executeUpdate(query);
    }

    public int setOrderStepLogId(Long id, Long logId) throws SOSHibernateException {
        String hql = String.format("update %s set logId=:logId  where id=:id", DBLayer.DBITEM_HISTORY_ORDER_STEPS);
        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("logId", logId);
        return session.executeUpdate(query);
    }

    public Object[] getDeployedWorkflow(String controllerId, String workflowName, String workflowVersionId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path,invContent from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append("where type=:type ");
        hql.append("and controllerId=:controllerId ");
        hql.append("and name=:workflowName ");
        hql.append("and commitId=:workflowVersionId");

        Query<Object[]> query = session.createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        query.setParameter("controllerId", controllerId);
        query.setParameter("workflowName", workflowName);
        query.setParameter("workflowVersionId", workflowVersionId);
        List<Object[]> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private <T> List<T> executeQueryList(String callerMethodName, Query<T> query) throws SOSHibernateException {
        List<T> result = null;
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                result = session.getResultList(query);
                run = false;
            } catch (Exception e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = SOSHibernate.findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        LOGGER.warn(String.format("%s: %s occured, wait %ss and try again (%s of %s) ...", callerMethodName, te.getClass().getName(),
                                RERUN_INTERVAL, count, MAX_RERUNS));
                        try {
                            Thread.sleep(RERUN_INTERVAL * 1000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        }
        return result;
    }

    // TODO duplicate method - see com.sos.joc.cleanup.db.DBLayerCleanup
    public boolean mainOrderLogNotFinished(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("where id=:id ");
        hql.append("and parentId=0 ");
        hql.append("and logId=0 ");

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        Long result = query.getSingleResult();

        return result.intValue() > 0;
    }

}
