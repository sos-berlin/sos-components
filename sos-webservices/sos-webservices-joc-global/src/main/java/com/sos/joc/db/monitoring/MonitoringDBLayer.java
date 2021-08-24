package com.sos.joc.db.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;

public class MonitoringDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public MonitoringDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public ScrollableResults getNotifications(Date dateFrom, Set<String> controllerIds, List<Integer> types, Integer limit) throws SOSHibernateException {
        if (controllerIds == null) {
            controllerIds = Collections.emptySet();
        }
        StringBuilder hql = new StringBuilder(getNotificationsMainHQL());
        if (dateFrom != null) {
            hql.append("and n.created >= :dateFrom ");
        }
        if (!controllerIds.isEmpty()) {
            hql.append("and o.controllerId in (:controllerIds) ");
        }
        if (types != null && types.size() > 0) {
            if (types.size() == 1) {
                hql.append("and n.type=:type ");
            } else {
                hql.append("and n.type in :types ");
            }
        }
        hql.append("order by n.id desc");

        Query<NotificationDBItemEntity> query = getSession().createQuery(hql.toString(), NotificationDBItemEntity.class);
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (!controllerIds.isEmpty()) {
            query.setParameterList("controllerId", controllerIds);
        }
        if (types != null && types.size() > 0) {
            if (types.size() == 1) {
                query.setParameter("type", types.get(0));
            } else {
                query.setParameterList("types", types);
            }
        }
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }
        return getSession().scroll(query);
    }

    public ScrollableResults getNotifications(List<Long> notificationIds, Set<String> controllerIds, List<Integer> types) throws SOSHibernateException {
        int size = notificationIds.size();
        if (controllerIds == null) {
            controllerIds = Collections.emptySet();
        }
        StringBuilder hql = new StringBuilder(getNotificationsMainHQL());
        if (!controllerIds.isEmpty()) {
            hql.append("and o.controllerId in (:controllerIds) ");
        }
        if (types != null && types.size() > 0) {
            if (types.size() == 1) {
                hql.append("and n.type=:type ");
            } else {
                hql.append("and n.type in :types ");
            }
        }
        if (size == 1) {
            hql.append("and n.id=:notificationId");
        } else {
            hql.append("and n.id in :notificationIds ");
            hql.append("order by n.id desc");
        }
        Query<NotificationDBItemEntity> query = getSession().createQuery(hql.toString(), NotificationDBItemEntity.class);
        if (!controllerIds.isEmpty()) {
            query.setParameterList("controllerId", controllerIds);
        }
        if (types != null && types.size() > 0) {
            if (types.size() == 1) {
                query.setParameter("type", types.get(0));
            } else {
                query.setParameterList("types", types);
            }
        }
        if (size == 1) {
            query.setParameter("notificationId", notificationIds.get(0));
        } else {
            query.setParameterList("notificationIds", notificationIds);
        }
        return getSession().scroll(query);
    }

    public List<DBItemNotificationMonitor> getNotificationMonitors(Long notificationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_NOTIFICATION_MONITOR).append(" ");
        hql.append("where notificationId=:notificationId");

        Query<DBItemNotificationMonitor> query = getSession().createQuery(hql.toString());
        query.setParameter("notificationId", notificationId);

        return getSession().getResultList(query);
    }

    public DBItemNotificationAcknowledgement getNotificationAcknowledgement(Long notificationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_NOTIFICATION_ACKNOWLEDGEMENT).append(" ");
        hql.append("where notificationId=:notificationId");

        Query<DBItemNotificationAcknowledgement> query = getSession().createQuery(hql.toString());
        query.setParameter("notificationId", notificationId);

        return getSession().getSingleResult(query);
    }

    public DBItemNotification getNotification(Long notificationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_NOTIFICATION).append(" ");
        hql.append("where id=:notificationId");

        Query<DBItemNotification> query = getSession().createQuery(hql.toString());
        query.setParameter("notificationId", notificationId);

        return getSession().getSingleResult(query);
    }

    private StringBuilder getNotificationsMainHQL() {
        StringBuilder hql = new StringBuilder("select n.id as id"); // set aliases for all properties
        hql.append(",n.type as type");
        hql.append(",n.recoveredId as recoveredNotificationId");
        hql.append(",n.created as created");
        hql.append(",n.notificationId as notificationId");
        hql.append(",n.hasMonitors as hasMonitors");
        hql.append(",o.historyId as orderHistoryId");
        hql.append(",o.controllerId as controllerId");
        hql.append(",o.orderId as orderId");
        hql.append(",o.workflowPath as workflowPath");
        hql.append(",o.errorText as orderErrorText");
        hql.append(",os.historyId as orderStepHistoryId");
        hql.append(",os.jobName as orderStepJobName");
        hql.append(",os.jobLabel as orderStepJobLabel");
        hql.append(",os.jobTitle as orderStepJobTitle");
        hql.append(",os.jobCriticality as orderStepJobCriticality");
        hql.append(",os.startTime as orderStepStartTime");
        hql.append(",os.endTime as orderStepEndTime");
        hql.append(",os.workflowPosition as orderStepWorkflowPosition");
        hql.append(",os.severity as orderStepSeverity");
        hql.append(",os.agentUri as orderStepAgentUri");
        hql.append(",os.returnCode as orderStepReturnCode");
        hql.append(",os.error as orderStepError");
        hql.append(",os.errorText as orderStepErrorText");
        hql.append(",os.warn as orderStepWarn");
        hql.append(",os.warnText as orderStepWarnText");
        hql.append(",a.account as acknowledgementAccount");
        hql.append(",a.comment as acknowledgementComment");
        hql.append(",a.created as acknowledgementCreated ");
        hql.append("from ").append(DBITEM_NOTIFICATION).append(" n ");
        hql.append(",").append(DBITEM_NOTIFICATION_WORKFLOW).append(" w ");
        hql.append("left join ").append(DBITEM_MONITORING_ORDER).append(" o on w.orderHistoryId=o.historyId ");
        hql.append("left join ").append(DBITEM_MONITORING_ORDER_STEP).append(" os on w.orderStepHistoryId=os.historyId ");
        hql.append("left join ").append(DBITEM_NOTIFICATION_ACKNOWLEDGEMENT).append(" a on n.id=a.notificationId ");
        hql.append("where n.id=w.notificationId ");
        return hql;
    }

    public ScrollableResults getControllers(String controllerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        List<String> where = new ArrayList<>();
        if (!SOSString.isEmpty(controllerId)) {
            where.add("controllerId=:controllerId");
        }
        if (dateFrom != null) {
            where.add("readyEventId >= :dateFrom");
        }
        if (dateTo != null) {
            where.add("readyEventId <= :dateTo");
        }
        if (where.size() > 0) {
            hql.append("where ").append(String.join(" and ", where)).append(" ");
        }
        hql.append("order by readyEventId asc");

        Query<DBItemHistoryController> query = getSession().createQuery(hql.toString());
        if (!SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        if (dateFrom != null) {
            query.setParameter("dateFrom", getDateAsEventId(dateFrom));
        }
        if (dateTo != null) {
            query.setParameter("dateTo", getDateAsEventId(dateTo));
        }
        return getSession().scroll(query);
    }

    public DBItemHistoryController getPreviousController(String controllerId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId = ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId < :readyEventId ");
        hql.append(")");

        Query<DBItemHistoryController> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("readyEventId", readyEventId);

        return getSession().getSingleResult(query);
    }

    public List<Object[]> getLastControllers() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("group by controllerId");
        hql.append(")");

        return getSession().getResultList(getSession().createQuery(hql.toString()));
    }

    public List<Object[]> getPreviousControllers() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where readyEventId not in ");
        hql.append(" (select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" group by controllerId)");
        hql.append("group by controllerId");
        hql.append(")");

        return getSession().getResultList(getSession().createQuery(hql.toString()));
    }

    public ScrollableResults getAgents(String controllerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        List<String> where = new ArrayList<>();
        if (!SOSString.isEmpty(controllerId)) {
            where.add("controllerId=:controllerId");
        }
        if (dateFrom != null) {
            where.add("readyEventId >= :dateFrom");
        }
        if (dateTo != null) {
            where.add("readyEventId <= :dateTo");
        }
        if (where.size() > 0) {
            hql.append("where ").append(String.join(" and ", where)).append(" ");
        }
        hql.append("order by readyEventId asc");

        Query<DBItemHistoryAgent> query = getSession().createQuery(hql.toString());
        if (!SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        if (dateFrom != null) {
            query.setParameter("dateFrom", getDateAsEventId(dateFrom));
        }
        if (dateTo != null) {
            query.setParameter("dateTo", getDateAsEventId(dateTo));
        }
        return getSession().scroll(query);
    }

    public DBItemHistoryAgent getPreviousAgent(String controllerId, String agentId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId= ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and agentId=:agentId ");
        hql.append("and readyEventId < :readyEventId ");
        hql.append(")");

        Query<DBItemHistoryAgent> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        query.setParameter("readyEventId", readyEventId);

        return getSession().getSingleResult(query);
    }

    public List<Object[]> getLastAgents() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId,agentId from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("group by controllerId,agentId");
        hql.append(")");

        return getSession().getResultList(getSession().createQuery(hql.toString()));
    }

    public List<Object[]> getPreviousAgents() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId,agentId from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId not in ");
        hql.append(" (select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENT).append(" group by controllerId,agentId)");
        hql.append("group by controllerId,agentId");
        hql.append(")");

        return getSession().getResultList(getSession().createQuery(hql.toString()));
    }

    private Long getDateAsEventId(Date date) {
        return date == null ? null : date.getTime() * 1_000;
    }
}
