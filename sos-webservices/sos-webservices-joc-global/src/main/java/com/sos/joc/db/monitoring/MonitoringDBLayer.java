package com.sos.joc.db.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.monitoring.notification.NotificationApplication;

public class MonitoringDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public MonitoringDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public ScrollableResults getOrderNotifications(Date dateFrom, Collection<String> controllerIds, List<Integer> types, Integer limit)
            throws SOSHibernateException {
        if (controllerIds == null) {
            controllerIds = Collections.emptySet();
        }
        StringBuilder hql = new StringBuilder(getOrderNotificationsMainHQL());
        if (dateFrom != null) {
            hql.append("and n.created >= :dateFrom ");
        }
        if (!controllerIds.isEmpty()) {
            hql.append("and o.controllerId in (:controllerIds) ");
        }
        int typesSize = types == null ? 0 : types.size();
        if (typesSize > 0) {
            hql.append("and ");
            hql.append(typesSize == 1 ? "n.type=:type" : "n.type in :types");
            hql.append(" ");
        }
        hql.append("order by n.id desc");

        Query<NotificationDBItemEntity> query = getSession().createQuery(hql.toString(), NotificationDBItemEntity.class);
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (!controllerIds.isEmpty()) {
            query.setParameterList("controllerIds", controllerIds);
        }
        if (typesSize > 0) {
            if (typesSize == 1) {
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

    public ScrollableResults getSystemNotifications(Date dateFrom, List<Integer> types, List<Integer> categories, Integer limit)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(getSystemNotificationsMainHQL());
        String add = "where ";
        if (dateFrom != null) {
            hql.append(add).append("n.time >= :dateFrom ");
            add = "and ";
        }
        int typesSize = types == null ? 0 : types.size();
        if (typesSize > 0) {
            hql.append(add);
            hql.append(typesSize == 1 ? "n.type=:type" : "n.type in :types");
            hql.append(" ");
            add = "and ";
        }
        int categoriesSize = categories == null ? 0 : categories.size();
        if (categoriesSize > 0) {
            hql.append(add);
            hql.append(categoriesSize == 1 ? "n.category=:category" : "n.category in :categories");
            hql.append(" ");
            add = "and ";
        }
        hql.append("order by n.id desc");

        Query<SystemNotificationDBItemEntity> query = getSession().createQuery(hql.toString(), SystemNotificationDBItemEntity.class);
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (typesSize > 0) {
            if (typesSize == 1) {
                query.setParameter("type", types.get(0));
            } else {
                query.setParameterList("types", types);
            }
        }
        if (categoriesSize > 0) {
            if (categoriesSize == 1) {
                query.setParameter("category", categories.get(0));
            } else {
                query.setParameterList("categories", categories);
            }
        }
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }
        return getSession().scroll(query);
    }

    public ScrollableResults getOrderNotifications(List<Long> notificationIds, Collection<String> controllerIds, List<Integer> types)
            throws SOSHibernateException {
        int size = notificationIds.size();
        if (controllerIds == null) {
            controllerIds = Collections.emptySet();
        }
        StringBuilder hql = new StringBuilder(getOrderNotificationsMainHQL());
        if (!controllerIds.isEmpty()) {
            hql.append("and o.controllerId in (:controllerIds) ");
        }
        int typesSize = types == null ? 0 : types.size();
        if (typesSize > 0) {
            hql.append("and ");
            hql.append(typesSize == 1 ? "n.type=:type" : "n.type in :types");
            hql.append(" ");
        }
        if (size == 1) {
            hql.append("and n.id=:notificationId");
        } else {
            hql.append("and n.id in :notificationIds ");
            hql.append("order by n.id desc");
        }
        Query<NotificationDBItemEntity> query = getSession().createQuery(hql.toString(), NotificationDBItemEntity.class);
        if (!controllerIds.isEmpty()) {
            query.setParameterList("controllerIds", controllerIds);
        }
        if (typesSize > 0) {
            if (typesSize == 1) {
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

    public ScrollableResults getSystemNotifications(List<Long> notificationIds, List<Integer> types, List<Integer> categories)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(getSystemNotificationsMainHQL());
        String add = "where ";
        int typesSize = types == null ? 0 : types.size();
        if (typesSize > 0) {
            hql.append(add);
            hql.append(typesSize == 1 ? "n.type=:type" : "n.type in :types");
            hql.append(" ");
            add = "and ";
        }
        int categoriesSize = categories == null ? 0 : categories.size();
        if (categoriesSize > 0) {
            hql.append(add);
            hql.append(categoriesSize == 1 ? "n.category=:category" : "n.category in :categories");
            hql.append(" ");
            add = "and ";
        }

        hql.append(add);
        int size = notificationIds.size();
        if (size == 1) {
            hql.append("n.id=:notificationId");
        } else {
            hql.append("n.id in :notificationIds ");
            hql.append("order by n.time desc");
        }

        Query<SystemNotificationDBItemEntity> query = getSession().createQuery(hql.toString(), SystemNotificationDBItemEntity.class);
        if (typesSize > 0) {
            if (typesSize == 1) {
                query.setParameter("type", types.get(0));
            } else {
                query.setParameterList("types", types);
            }
        }
        if (categoriesSize > 0) {
            if (categoriesSize == 1) {
                query.setParameter("category", categories.get(0));
            } else {
                query.setParameterList("categories", categories);
            }
        }
        if (size == 1) {
            query.setParameter("notificationId", notificationIds.get(0));
        } else {
            query.setParameterList("notificationIds", notificationIds);
        }
        return getSession().scroll(query);
    }

    public List<DBItemNotificationMonitor> getNotificationMonitors(NotificationApplication application, Long notificationId)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_MON_NOT_MONITORS).append(" ");
        hql.append("where application=:application ");
        hql.append("and notificationId=:notificationId");

        Query<DBItemNotificationMonitor> query = getSession().createQuery(hql.toString());
        query.setParameter("application", application.intValue());
        query.setParameter("notificationId", notificationId);
        return getSession().getResultList(query);
    }

    public DBItemNotificationAcknowledgement getNotificationAcknowledgement(NotificationApplication application, Long notificationId)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_MON_NOT_ACKNOWLEDGEMENTS).append(" ");
        hql.append("where id.application=:application ");
        hql.append("and id.notificationId=:notificationId");

        Query<DBItemNotificationAcknowledgement> query = getSession().createQuery(hql.toString());
        query.setParameter("application", application.intValue());
        query.setParameter("notificationId", notificationId);
        return getSession().getSingleResult(query);
    }

    public DBItemNotification getOrderNotification(Long notificationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" ");
        hql.append("where id=:notificationId");

        Query<DBItemNotification> query = getSession().createQuery(hql.toString());
        query.setParameter("notificationId", notificationId);

        return getSession().getSingleResult(query);
    }

    public DBItemSystemNotification getSystemNotification(Long notificationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_MON_SYSNOTIFICATIONS).append(" ");
        hql.append("where id=:notificationId");

        Query<DBItemSystemNotification> query = getSession().createQuery(hql.toString());
        query.setParameter("notificationId", notificationId);

        return getSession().getSingleResult(query);
    }

    private StringBuilder getOrderNotificationsMainHQL() {
        StringBuilder hql = new StringBuilder("select n.id as id"); // set aliases for all properties
        hql.append(",n.type as type");
        hql.append(",n.recoveredId as recoveredNotificationId");
        hql.append(",n.created as created");
        hql.append(",n.notificationId as notificationId");
        hql.append(",n.hasMonitors as hasMonitors");
        hql.append(",n.warn as orderStepWarn");
        hql.append(",n.warnText as orderStepWarnText");
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
        hql.append(",a.account as acknowledgementAccount");
        hql.append(",a.comment as acknowledgementComment");
        hql.append(",a.created as acknowledgementCreated ");
        hql.append("from ").append(DBITEM_MON_NOTIFICATIONS).append(" n ");
        hql.append(",").append(DBITEM_MON_NOT_WORKFLOWS).append(" w ");
        hql.append("left join ").append(DBITEM_MON_ORDERS).append(" o on w.orderHistoryId=o.historyId ");
        hql.append("left join ").append(DBITEM_MON_ORDER_STEPS).append(" os on w.orderStepHistoryId=os.historyId ");
        hql.append("left join ").append(DBITEM_MON_NOT_ACKNOWLEDGEMENTS).append(" a on n.id=a.id.notificationId ");
        hql.append("and a.id.application=").append(NotificationApplication.ORDER_NOTIFICATION.intValue()).append(" ");
        hql.append("where n.id=w.notificationId ");
        return hql;
    }

    private StringBuilder getSystemNotificationsMainHQL() {
        StringBuilder hql = new StringBuilder("select n.id as id"); // set aliases for all properties
        hql.append(",n.type as type");
        hql.append(",n.category as category");
        hql.append(",n.jocId as jocId");
        hql.append(",n.hasMonitors as hasMonitors");
        hql.append(",n.source as source");
        hql.append(",n.notifier as notifier");
        hql.append(",n.time as time");
        hql.append(",n.message as message");
        hql.append(",n.exception as exception");
        hql.append(",n.created as created");
        hql.append(",a.account as acknowledgementAccount");
        hql.append(",a.comment as acknowledgementComment");
        hql.append(",a.created as acknowledgementCreated ");
        hql.append("from ").append(DBITEM_MON_SYSNOTIFICATIONS).append(" n ");
        hql.append("left join ").append(DBITEM_MON_NOT_ACKNOWLEDGEMENTS).append(" a on n.id=a.id.notificationId ");
        hql.append("and a.id.application=").append(NotificationApplication.SYSTEM_NOTIFICATION.intValue()).append(" ");
        return hql;
    }

    public ScrollableResults getControllers(String controllerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        List<String> where = new ArrayList<>();
        if (!SOSString.isEmpty(controllerId)) {
            where.add("controllerId=:controllerId");
        }
        if (dateFrom != null) {
            where.add("readyEventId >= :dateFrom");
        }
        if (dateTo != null) {
            where.add("readyEventId < :dateTo");
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

    public DBItemHistoryController getController(String controllerId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId = :readyEventId ");

        Query<DBItemHistoryController> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("readyEventId", readyEventId);
        return getSession().getSingleResult(query);
    }

    public DBItemHistoryController getPreviousController(String controllerId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId = ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId < :readyEventId ");
        hql.append(")");

        Query<DBItemHistoryController> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("readyEventId", readyEventId);

        return getSession().getSingleResult(query);
    }

    public List<Object[]> getPreviousControllers(Date dateFrom) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where readyEventId < :readyEventId ");
        hql.append("group by controllerId");
        hql.append(")");

        Query<Object[]> query = getSession().createQuery(hql.toString());
        query.setParameter("readyEventId", getDateAsEventId(dateFrom));
        return getSession().getResultList(query);
    }

    public List<Object[]> getLastControllers(Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        if (dateTo != null) {
            hql.append("where readyEventId > :dateTo ");
        }
        hql.append("group by controllerId");
        hql.append(")");

        Query<Object[]> query = getSession().createQuery(hql.toString());
        if (dateTo != null) {
            query.setParameter("dateTo", getDateAsEventId(dateTo));
        }
        return getSession().getResultList(query);
    }

    public ScrollableResults getAgents(String controllerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        List<String> where = new ArrayList<>();
        if (!SOSString.isEmpty(controllerId)) {
            where.add("controllerId=:controllerId");
        }
        if (dateFrom != null) {
            where.add("readyEventId >= :dateFrom");
        }
        if (dateTo != null) {
            where.add("readyEventId < :dateTo");
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

    public DBItemHistoryAgent getAgent(String controllerId, String agentId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and agentId=:agentId ");
        hql.append("and readyEventId = :readyEventId ");

        Query<DBItemHistoryAgent> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("agentId", agentId);
        query.setParameter("readyEventId", readyEventId);
        return getSession().getSingleResult(query);
    }

    public DBItemHistoryAgent getPreviousAgent(String controllerId, String agentId, Long readyEventId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and readyEventId= ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
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

    public List<Object[]> getLastAgents(Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId,agentId from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        if (dateTo != null) {
            hql.append("where readyEventId > :dateTo ");
        }
        hql.append("group by controllerId,agentId");
        hql.append(")");

        Query<Object[]> query = getSession().createQuery(hql.toString());
        if (dateTo != null) {
            query.setParameter("dateTo", getDateAsEventId(dateTo));
        }
        return getSession().getResultList(query);
    }

    public List<Object[]> getPreviousAgents(Date dateFrom) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId,agentId from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where readyEventId < :readyEventId ");
        hql.append("group by controllerId,agentId");
        hql.append(")");

        Query<Object[]> query = getSession().createQuery(hql.toString());
        query.setParameter("readyEventId", getDateAsEventId(dateFrom));
        return getSession().getResultList(query);
    }

    public List<Object[]> getPreviousAgents() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select readyEventId,controllerId,agentId from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where readyEventId in ");
        hql.append("(");
        hql.append("select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where readyEventId not in ");
        hql.append(" (select max(readyEventId) from ").append(DBLayer.DBITEM_HISTORY_AGENTS).append(" group by controllerId,agentId)");
        hql.append("group by controllerId,agentId");
        hql.append(")");

        return getSession().getResultList(getSession().createQuery(hql.toString()));
    }

    private Long getDateAsEventId(Date date) {
        return date == null ? null : date.getTime() * 1_000;
    }
}
