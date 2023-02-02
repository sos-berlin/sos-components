package com.sos.joc.cleanup.model;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.db.DBLayer;
import com.sos.monitoring.notification.NotificationApplication;

public class CleanupTaskMonitoring extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskMonitoring.class);

    private enum Scope {
        MAIN, REMAINING
    }

    private enum MontitoringRange {
        ALL, STEPS
    }

    private int totalOrders = 0;
    private int totalOrderSteps = 0;
    private int totalOrderNotifications = 0;
    private int totalOrderNotificationWorkflows = 0;
    private int totalSystemNotifications = 0;
    private int totalNotificationMonitors = 0;
    private int totalNotificationAcknowledgements = 0;

    public CleanupTaskMonitoring(JocClusterHibernateFactory factory, IJocActiveMemberService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime monitoringDatetime = datetimes.get(0);
            TaskDateTime notificationDatetime = datetimes.get(1);
            JocServiceTaskAnswerState state = null;

            tryOpenSession();
            if (notificationDatetime.getDatetime() != null) {
                state = cleanupNotifications(Scope.MAIN, notificationDatetime);
            } else {
                LOGGER.info(String.format("[%s][order/system_notifications][%s]skip", getIdentifier(), notificationDatetime.getAge()
                        .getConfigured()));
            }

            if (monitoringDatetime.getDatetime() != null && (state == null || state.equals(JocServiceTaskAnswerState.COMPLETED))) {
                LOGGER.info(String.format("[%s][monitoring][%s][%s]start cleanup", getIdentifier(), monitoringDatetime.getAge().getConfigured(),
                        monitoringDatetime.getZonedDatetime()));

                state = cleanupMonitoring(Scope.MAIN, MontitoringRange.ALL, monitoringDatetime.getDatetime(), monitoringDatetime.getAge()
                        .getConfigured());
                if (isCompleted(state)) {
                    Date remainingStartTime = getRemainingStartTime(notificationDatetime);
                    String remainingAgeInfo = getRemainingAgeInfo(notificationDatetime);

                    state = cleanupMonitoring(Scope.REMAINING, MontitoringRange.ALL, remainingStartTime, remainingAgeInfo);
                    if (isCompleted(state)) {
                        state = cleanupMonitoring(Scope.REMAINING, MontitoringRange.STEPS, remainingStartTime, remainingAgeInfo);
                    }
                }
            } else {
                LOGGER.info(String.format("[%s][monitoring][%s]skip", getIdentifier(), monitoringDatetime.getAge().getConfigured()));
            }

            return state;
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            close();
        }
    }

    private JocServiceTaskAnswerState cleanupNotifications(Scope scope, TaskDateTime datetime) throws SOSHibernateException {
        JocServiceTaskAnswerState state = cleanupOrderNotifications(scope, datetime);
        if (state.equals(JocServiceTaskAnswerState.COMPLETED)) {
            state = cleanupSystemNotifications(scope, datetime);
        }
        return state;
    }

    private JocServiceTaskAnswerState cleanupOrderNotifications(Scope scope, TaskDateTime datetime) throws SOSHibernateException {
        LOGGER.info(String.format("[%s][order_notifications][%s][%s]start cleanup", getIdentifier(), datetime.getAge().getConfigured(), datetime
                .getZonedDatetime()));

        boolean runm = true;
        while (runm) {
            tryOpenSession();

            List<Long> rm = getOrderNotificationIds(scope, datetime);
            if (rm == null || rm.size() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            getDbLayer().beginTransaction();
            deleteOrderNotifications(scope, datetime, rm);
            getDbLayer().commit();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private JocServiceTaskAnswerState cleanupSystemNotifications(Scope scope, TaskDateTime datetime) throws SOSHibernateException {
        LOGGER.info(String.format("[%s][system_notifications][%s][%s]start cleanup", getIdentifier(), datetime.getAge().getConfigured(), datetime
                .getZonedDatetime()));

        boolean runm = true;
        while (runm) {
            tryOpenSession();

            List<Long> rm = getSystemNotificationIds(scope, datetime);
            if (rm == null || rm.size() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            getDbLayer().beginTransaction();
            deleteSystemNotifications(scope, datetime, rm);
            getDbLayer().commit();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private JocServiceTaskAnswerState cleanupMonitoring(Scope scope, MontitoringRange range, Date startTime, String ageInfo)
            throws SOSHibernateException {
        boolean runm = true;
        while (runm) {
            tryOpenSession();

            List<Long> rm = getMonitoringMainOrderIds(scope, range, startTime, ageInfo);
            if (rm == null || rm.size() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            getDbLayer().beginTransaction();
            boolean completed = deleteMonitoring(scope, range, startTime, ageInfo, rm);
            getDbLayer().commit();
            if (!completed) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private List<Long> getMonitoringMainOrderIds(Scope scope, MontitoringRange range, Date startTime, String ageInfo) throws SOSHibernateException {
        String table = DBLayer.TABLE_MON_ORDERS;
        StringBuilder hql = null;
        switch (scope) {
        case MAIN:
            hql = new StringBuilder("select historyId from ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
            hql.append("where startTime < :startTime ");
            hql.append("and parentId=0");
            break;
        case REMAINING:
            switch (range) {
            case ALL:
                hql = new StringBuilder("select distinct mainParentId from ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            case STEPS:
                table = DBLayer.TABLE_MON_ORDER_STEPS;
                hql = new StringBuilder("select distinct historyOrderMainParentId from ").append(DBLayer.DBITEM_MON_ORDER_STEPS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            }
            break;
        }

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);

        int size = r.size();
        if (size == 0) {
            LOGGER.info(String.format("[%s][monitoring][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo,
                    getDateTime(startTime), table, size));
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][monitoring][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo,
                        getDateTime(startTime), table, size));
            }
        }
        return r;
    }

    private boolean deleteMonitoring(Scope scope, MontitoringRange range, Date startTime, String ageInfo, List<Long> orderIds)
            throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][monitoring][");
        log.append(getScope(scope)).append(" ").append(getRange(range)).append("]");
        log.append("[").append(ageInfo).append(" ").append(getDateTime(startTime)).append("][deleted]");

        // getDbLayer().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_ORDER_STEPS).append(" ");
        hql.append("where historyOrderMainParentId in (:orderIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalOrderSteps += r;
        log.append(getDeleted(DBLayer.TABLE_MON_ORDER_STEPS, r, totalOrderSteps));

        if (range.equals(MontitoringRange.ALL)) {
            if (isStopped()) {
                LOGGER.info(log.toString());
                return false;
            }

            // getDbLayer().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_MON_ORDERS).append(" ");
            hql.append("where mainParentId in (:orderIds)");
            query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            r = getDbLayer().getSession().executeUpdate(query);
            // getDbLayer().commit();
            totalOrders += r;
            log.append(getDeleted(DBLayer.TABLE_MON_ORDERS, r, totalOrders));
        }

        LOGGER.info(log.toString());
        return true;
    }

    private StringBuilder deleteOrderNotifications(Scope scope, TaskDateTime datetime, List<Long> notificationIds) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][order_notifications][").append(getScope(scope)).append("][").append(datetime.getAge()
                .getConfigured()).append("][deleted]");

        // getDbLayer().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_MONITORS).append(" ");
        hql.append("where notificationId in (:notificationIds) ");
        hql.append("and application=:application");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        query.setParameter("application", NotificationApplication.ORDER_NOTIFICATION.intValue());
        int r = getDbLayer().getSession().executeUpdate(query);
        totalNotificationMonitors += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_MONITORS, r, totalNotificationMonitors));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_WORKFLOWS).append(" ");
        hql.append("where notificationId in (:notificationIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        r = getDbLayer().getSession().executeUpdate(query);
        totalOrderNotificationWorkflows += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_WORKFLOWS, r, totalOrderNotificationWorkflows));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_ACKNOWLEDGEMENTS).append(" ");
        hql.append("where id.notificationId in (:notificationIds) ");
        hql.append("and id.application=:application");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        query.setParameter("application", NotificationApplication.ORDER_NOTIFICATION.intValue());
        r = getDbLayer().getSession().executeUpdate(query);
        totalNotificationAcknowledgements += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_ACKNOWLEDGEMENTS, r, totalNotificationAcknowledgements));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" ");
        hql.append("where id in (:notificationIds) ");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        r = getDbLayer().getSession().executeUpdate(query);
        totalOrderNotifications += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOTIFICATIONS, r, totalOrderNotifications));

        // getDbLayer().commit();
        LOGGER.info(log.toString());
        return log;
    }

    private StringBuilder deleteSystemNotifications(Scope scope, TaskDateTime datetime, List<Long> notificationIds) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][system_notifications][").append(getScope(scope)).append("][").append(datetime.getAge()
                .getConfigured()).append("][deleted]");

        // getDbLayer().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_MONITORS).append(" ");
        hql.append("where notificationId in (:notificationIds) ");
        hql.append("and application=:application");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        query.setParameter("application", NotificationApplication.SYSTEM_NOTIFICATION.intValue());
        int r = getDbLayer().getSession().executeUpdate(query);
        totalNotificationMonitors += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_MONITORS, r, totalNotificationMonitors));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_ACKNOWLEDGEMENTS).append(" ");
        hql.append("where id.notificationId in (:notificationIds) ");
        hql.append("and id.application=:application");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        query.setParameter("application", NotificationApplication.SYSTEM_NOTIFICATION.intValue());
        r = getDbLayer().getSession().executeUpdate(query);
        totalNotificationAcknowledgements += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_ACKNOWLEDGEMENTS, r, totalNotificationAcknowledgements));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_SYSNOTIFICATIONS).append(" ");
        hql.append("where id in (:notificationIds) ");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        r = getDbLayer().getSession().executeUpdate(query);
        totalSystemNotifications += r;
        log.append(getDeleted(DBLayer.TABLE_MON_SYSNOTIFICATIONS, r, totalSystemNotifications));

        // getDbLayer().commit();
        LOGGER.info(log.toString());
        return log;
    }

    private List<Long> getOrderNotificationIds(Scope scope, TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" ");
        hql.append("where created < :startTime ");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][order_notifications][%s][%s][%s]found=%s", getIdentifier(), getScope(scope), datetime.getAge()
                    .getConfigured(), DBLayer.TABLE_MON_NOTIFICATIONS, r.size()));
        }
        return r;
    }

    private List<Long> getSystemNotificationIds(Scope scope, TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_MON_SYSNOTIFICATIONS).append(" ");
        hql.append("where time < :startTime ");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][system_notifications][%s][%s][%s]found=%s", getIdentifier(), getScope(scope), datetime.getAge()
                    .getConfigured(), DBLayer.TABLE_MON_SYSNOTIFICATIONS, r.size()));
        }
        return r;
    }

    private String getScope(Scope val) {
        return val.name().toLowerCase();
    }

    private String getRange(MontitoringRange val) {
        return val.name().toLowerCase();
    }
}
