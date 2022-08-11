package com.sos.joc.monitoring.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.function.date.SOSHibernateSecondsDiff;
import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.db.monitoring.DBItemNotificationMonitor;
import com.sos.joc.db.monitoring.DBItemNotificationWorkflow;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.HistoryOrderStepResult;
import com.sos.joc.monitoring.model.NotifyAnalyzer;
import com.sos.joc.monitoring.notification.notifier.NotifyResult;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

public class DBLayerMonitoring extends DBLayer {

    private static final long serialVersionUID = 1L;
    private final String identifier;
    private final String jocVariableName;

    public DBLayerMonitoring(String identifier, String jocVariableName) {
        this.identifier = identifier;
        this.jocVariableName = jocVariableName;
    }

    public void setSession(SOSHibernateSession session) {
        super.setSession(session);
        getSession().setIdentifier(identifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getJocVariableName() {
        return jocVariableName;
    }

    public DBItemHistoryOrder getHistoryOrder(Long historyId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("where id=:historyId");

        Query<DBItemHistoryOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("historyId", historyId);
        return getSession().getSingleResult(query);
    }

    private DBItemHistoryOrderStep getHistoryOrderStep(Long historyId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
        hql.append("where id=:historyId");

        Query<DBItemHistoryOrderStep> query = getSession().createQuery(hql.toString());
        query.setParameter("historyId", historyId);
        return getSession().getSingleResult(query);
    }

    public boolean updateOrderOnResumed(HistoryOrderBean hob) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("severity", hob.getSeverity());
        query.setParameter("state", hob.getState());
        query.setParameter("stateTime", hob.getStateTime());
        query.setParameter("historyId", hob.getHistoryId());

        int r = getSession().executeUpdate(query);
        return r != 0;
    }

    public boolean updateOrderOnForked(HistoryOrderBean hob) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",hasChildren=true ");
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("severity", hob.getSeverity());
        query.setParameter("state", hob.getState());
        query.setParameter("stateTime", hob.getStateTime());
        query.setParameter("historyId", hob.getHistoryId());

        int r = getSession().executeUpdate(query);
        return r != 0;
    }

    public boolean updateOrder(HistoryOrderBean hob) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("set modified=:modified ");

        if (hob.getEndTime() != null) {
            hql.append(",endTime=:endTime ");
            hql.append(",endWorkflowPosition=:endWorkflowPosition ");
            hql.append(",endHistoryOrderStepId=:endHistoryOrderStepId ");
        }
        if (hob.getCurrentHistoryOrderStepId() != null) {
            hql.append(",currentHistoryOrderStepId=:currentHistoryOrderStepId ");
        }
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append(",error=:error ");
        hql.append(",errorState=:errorState ");
        hql.append(",errorReason=:errorReason ");
        hql.append(",errorReturnCode=:errorReturnCode ");
        hql.append(",errorCode=:errorCode ");
        hql.append(",errorText=:errorText ");
        hql.append(",logId=:logId ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());

        if (hob.getEndTime() != null) {
            query.setParameter("endTime", hob.getEndTime());
            query.setParameter("endWorkflowPosition", DBItemMonitoringOrder.normalizeWorkflowPosition(hob.getEndWorkflowPosition()));
            query.setParameter("endHistoryOrderStepId", hob.getEndHistoryOrderStepId());
        }
        if (hob.getCurrentHistoryOrderStepId() != null) {
            query.setParameter("currentHistoryOrderStepId", hob.getCurrentHistoryOrderStepId());
        }
        query.setParameter("severity", hob.getSeverity());
        query.setParameter("state", hob.getState());
        query.setParameter("stateTime", hob.getStateTime());
        query.setParameter("error", hob.getError());
        query.setParameter("errorState", hob.getErrorState());
        query.setParameter("errorReason", hob.getErrorReason());
        query.setParameter("errorReturnCode", hob.getErrorReturnCode());
        query.setParameter("errorCode", DBItemMonitoringOrder.normalizeErrorCode(hob.getErrorCode()));
        query.setParameter("errorText", DBItemMonitoringOrder.normalizeErrorText(hob.getErrorText()));
        query.setParameter("logId", hob.getLogId());
        query.setParameter("historyId", hob.getHistoryId());

        int r = getSession().executeUpdate(query);
        return r != 0;
    }

    public boolean updateOrderOnOrderStep(Long historyId, Long currentHistoryOrderStepId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",currentHistoryOrderStepId=:currentHistoryOrderStepId ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("currentHistoryOrderStepId", currentHistoryOrderStepId);
        query.setParameter("historyId", historyId);

        int r = getSession().executeUpdate(query);
        return r != 0;
    }

    public int setOrderStepEnd(HistoryOrderStepResult result) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MON_ORDER_STEPS).append(" ");
        hql.append("set endTime=:endTime ");
        hql.append(",endVariables=:endVariables ");
        hql.append(",returnCode=:returnCode ");
        hql.append(",severity=:severity ");
        hql.append(",error=:error ");
        hql.append(",errorState=:errorState ");
        hql.append(",errorReason=:errorReason ");
        hql.append(",errorCode=:errorCode ");
        hql.append(",errorText=:errorText ");
        hql.append(",modified=:modified ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrderStep> query = getSession().createQuery(hql.toString());
        HistoryOrderStepBean hosb = result.getStep();
        query.setParameter("endTime", hosb.getEndTime());
        query.setParameter("endVariables", hosb.getEndVariables());
        query.setParameter("returnCode", hosb.getReturnCode());
        query.setParameter("severity", hosb.getSeverity());
        query.setParameter("error", hosb.getError());
        query.setParameter("errorState", hosb.getErrorState());
        query.setParameter("errorReason", hosb.getErrorReason());
        query.setParameter("errorCode", DBItemHistoryOrderStep.normalizeErrorCode(hosb.getErrorCode()));
        query.setParameter("errorText", DBItemHistoryOrderStep.normalizeErrorText(hosb.getErrorText()));
        query.setParameter("modified", new Date());
        query.setParameter("historyId", hosb.getHistoryId());
        return getSession().executeUpdate(query);
    }

    public DBItemJocVariable getVariable() throws SOSHibernateException {
        String hql = String.format("from %s where name=:name", DBLayer.DBITEM_JOC_VARIABLES);
        Query<DBItemJocVariable> query = getSession().createQuery(hql);
        query.setParameter("name", jocVariableName);
        return getSession().getSingleResult(query);
    }

    public int deleteVariable() throws SOSHibernateException {
        String hql = String.format("delete from %s where name=:name", DBLayer.DBITEM_JOC_VARIABLES);
        Query<DBItemJocVariable> query = getSession().createQuery(hql);
        query.setParameter("name", jocVariableName);
        return getSession().executeUpdate(query);
    }

    public void saveVariable(byte[] val) throws SOSHibernateException {
        DBItemJocVariable item = getVariable();
        if (item == null) {
            item = new DBItemJocVariable();
            item.setName(jocVariableName);
            item.setBinaryValue(val);
            getSession().save(item);
        } else {
            item.setBinaryValue(val);
            getSession().update(item);
        }
    }

    public String getReleasedConfiguration() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select configurationReleased ");
        hql.append("from ").append(DBLayer.DBITEM_XML_EDITOR_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");

        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ObjectType.NOTIFICATION.name());
        return getSession().getSingleValue(query);
    }

    public List<Object[]> getDeployedJobResources(List<String> names) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select name, content ");
        hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and name in :names ");

        Query<Object[]> query = getSession().createQuery(hql.toString());
        query.setParameter("type", DeployType.JOBRESOURCE.intValue());
        query.setParameterList("names", names);
        return getSession().getResultList(query);
    }

    public DBItemMonitoringOrder getMonitoringOrder(Long historyId, boolean forceHistory) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("historyId", historyId);
        DBItemMonitoringOrder item = getSession().getSingleResult(query);
        if (item == null && forceHistory) {
            item = convert(getHistoryOrder(historyId));
        }
        return item;
    }

    public DBItemMonitoringOrderStep getMonitoringOrderStep(Long historyId, boolean forceHistory) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_MON_ORDER_STEPS).append(" ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrderStep> query = getSession().createQuery(hql.toString());
        query.setParameter("historyId", historyId);
        DBItemMonitoringOrderStep item = getSession().getSingleResult(query);
        if (item == null && forceHistory) {
            item = convert(getHistoryOrderStep(historyId));
        }
        return item;
    }

    public List<DBItemNotification> getNotifications(NotificationType type, NotificationRange range, Long orderId, Long stepId)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select n from ").append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" n ");
        hql.append(",").append(DBLayer.DBITEM_MON_NOT_WORKFLOWS).append(" w ");
        hql.append("where n.id=w.notificationId ");
        hql.append("and n.type=:type ");
        hql.append("and n.range=:range ");
        hql.append("and w.orderHistoryId=:orderId ");
        hql.append("and w.orderStepHistoryId=:stepId");

        Query<DBItemNotification> query = getSession().createQuery(hql.toString());
        query.setParameter("type", type.intValue());
        query.setParameter("range", range.intValue());
        query.setParameter("orderId", orderId);
        query.setParameter("stepId", stepId);

        return getSession().getResultList(query);
    }

    public LastWorkflowNotificationDBItemEntity getLastNotification(String notificationId, NotificationRange range, Long orderId)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(
                "select n.id as id, n.type as type, n.notificationId as notificationId, w.orderStepHistoryId as stepId ");
        hql.append("from ").append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" n ");
        hql.append(",").append(DBLayer.DBITEM_MON_NOT_WORKFLOWS).append(" w ");
        hql.append("where n.id=w.notificationId ");
        hql.append("and n.id = (");
        hql.append("select max(n2.id) from ").append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" n2 ");
        hql.append(",").append(DBLayer.DBITEM_MON_NOT_WORKFLOWS).append(" w2 ");
        hql.append("where n2.id=w2.notificationId ");
        hql.append("and n2.range=:range ");
        hql.append("and n2.notificationId = :notificationId ");
        hql.append("and w2.orderHistoryId=:orderId");
        hql.append(")");

        Query<LastWorkflowNotificationDBItemEntity> query = getSession().createQuery(hql.toString(), LastWorkflowNotificationDBItemEntity.class);
        query.setParameter("range", range.intValue());
        query.setParameter("orderId", orderId);
        query.setParameter("notificationId", notificationId);
        return getSession().getSingleResult(query);
    }

    public DBItemNotification saveNotification(Notification notification, NotifyAnalyzer analyzer, NotificationType type,
            Long recoveredNotificationId, JobWarning warn, String warnText) throws SOSHibernateException {
        DBItemNotification item = new DBItemNotification();
        item.setType(type);
        item.setRange(analyzer.getRange());
        item.setNotificationId(notification.getNotificationId());
        item.setRecoveredId(recoveredNotificationId);
        item.setHasMonitors(notification.getMonitors().size() > 0);
        item.setWarn(warn);
        item.setWarnText(warnText);
        item.setCreated(new Date());
        getSession().save(item);

        DBItemNotificationWorkflow wItem = new DBItemNotificationWorkflow();
        wItem.setNotificationId(item.getId());
        wItem.setOrderHistoryId(analyzer.getOrderId());
        wItem.setOrderStepHistoryId(analyzer.getStepId());
        wItem.setWorkflowPosition(analyzer.getWorkflowPosition());
        getSession().save(wItem);

        return item;
    }

    public DBItemNotificationMonitor saveNotificationMonitor(DBItemNotification notification, AMonitor monitor, NotifyResult notifyResult)
            throws SOSHibernateException {
        DBItemNotificationMonitor item = new DBItemNotificationMonitor();
        item.setNotificationId(notification.getId());
        item.setType(monitor.getType());
        item.setName(monitor.getMonitorName());
        item.setMessage(notifyResult.getMessage());
        item.setConfiguration(monitor.getInfo().toString());
        if (notifyResult.getError() != null && !SOSString.isEmpty(notifyResult.getError().getMessage())) {
            item.setError(true);
            item.setErrorText(notifyResult.getError().getMessage());
        }
        item.setCreated(new Date());

        getSession().save(item);
        return item;
    }

    public DBItemNotificationMonitor saveNotificationMonitor(DBItemNotification notification, AMonitor monitor, Throwable exception)
            throws SOSHibernateException {
        DBItemNotificationMonitor item = new DBItemNotificationMonitor();
        item.setNotificationId(notification.getId());
        item.setType(monitor.getType());
        item.setName(monitor.getMonitorName());
        item.setMessage(monitor.getMessage());
        item.setConfiguration(monitor.getInfo().toString());
        if (exception != null) {
            item.setError(true);
            item.setErrorText(exception.getMessage());
        }
        item.setCreated(new Date());

        getSession().save(item);
        return item;
    }

    public Long getJobAvg(String controllerId, String workflowPath, String jobName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ");
        hql.append("round(");
        hql.append("sum(").append(SOSHibernateSecondsDiff.getFunction("startTime", "endTime")).append(")/count(id)");
        hql.append(",0) ");// ,0 precision only because of MSSQL
        hql.append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and workflowName=:workflowName ");
        hql.append("and jobName=:jobName ");
        hql.append("and severity=:severity ");
        hql.append("and endTime >= startTime ");

        // hibernate returns Long and not Double ...
        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("workflowName", JocClusterUtil.getBasenameFromPath(workflowPath));
        query.setParameter("jobName", jobName);
        query.setParameter("severity", HistorySeverity.SUCCESSFUL);
        return getSession().getSingleValue(query);
    }

    public DBItemMonitoringOrder convert(DBItemHistoryOrder history) {
        if (history == null) {
            return null;
        }
        DBItemMonitoringOrder item = new DBItemMonitoringOrder();
        item.setHistoryId(history.getId());
        item.setControllerId(history.getControllerId());
        item.setOrderId(history.getOrderId());
        item.setWorkflowPath(history.getWorkflowPath());
        item.setWorkflowVersionId(history.getWorkflowVersionId());
        item.setWorkflowPosition(history.getWorkflowPosition());
        item.setWorkflowFolder(history.getWorkflowFolder());
        item.setWorkflowName(history.getWorkflowName());
        item.setWorkflowTitle(history.getWorkflowTitle());
        item.setMainParentId(history.getMainParentId());
        item.setParentId(history.getParentId());
        item.setParentOrderId(history.getParentOrderId());
        item.setHasChildren(history.getHasChildren());
        item.setName(history.getName());
        item.setStartCause(history.getStartCause());
        item.setStartTimeScheduled(history.getStartTimeScheduled());
        item.setStartTime(history.getStartTime());
        item.setStartWorkflowPosition(history.getStartWorkflowPosition());
        item.setStartVariables(history.getStartVariables());
        item.setCurrentHistoryOrderStepId(history.getCurrentHistoryOrderStepId());
        item.setEndTime(history.getEndTime());
        item.setEndWorkflowPosition(history.getEndWorkflowPosition());
        item.setEndHistoryOrderStepId(history.getEndHistoryOrderStepId());
        item.setSeverity(history.getSeverity());
        item.setState(history.getState());
        item.setStateTime(history.getStateTime());
        item.setError(history.getError());
        item.setErrorState(history.getErrorState());
        item.setErrorReason(history.getErrorReason());
        item.setErrorReturnCode(history.getErrorReturnCode());
        item.setErrorCode(history.getErrorCode());
        item.setErrorText(history.getErrorText());
        item.setLogId(history.getLogId());

        item.setCreated(new Date());
        item.setModified(item.getCreated());

        return item;
    }

    public DBItemMonitoringOrderStep convert(DBItemHistoryOrderStep history) {
        if (history == null) {
            return null;
        }
        DBItemMonitoringOrderStep item = new DBItemMonitoringOrderStep();
        item.setHistoryId(history.getId());
        item.setWorkflowPosition(history.getWorkflowPosition());
        item.setHistoryOrderMainParentId(history.getHistoryOrderMainParentId());
        item.setHistoryOrderId(history.getHistoryOrderId());
        item.setPosition(history.getPosition());
        item.setJobName(history.getJobName());
        item.setJobLabel(history.getJobLabel());
        item.setJobTitle(history.getJobTitle());
        item.setJobCriticality(history.getCriticality());
        item.setAgentId(history.getAgentId());
        item.setAgentUri(history.getAgentUri());
        item.setStartCause(history.getStartCause());
        item.setStartTime(history.getStartTime());
        item.setStartVariables(history.getStartVariables());
        item.setEndTime(history.getEndTime());
        item.setEndVariables(history.getEndVariables());
        item.setReturnCode(history.getReturnCode());
        item.setSeverity(history.getSeverity());
        item.setError(history.getError());
        item.setErrorState(history.getErrorState());
        item.setErrorReason(history.getErrorReason());
        item.setErrorCode(history.getErrorCode());
        item.setErrorText(history.getErrorText());
        item.setLogId(history.getLogId());

        item.setCreated(new Date());
        item.setModified(item.getCreated());

        return item;
    }
}
