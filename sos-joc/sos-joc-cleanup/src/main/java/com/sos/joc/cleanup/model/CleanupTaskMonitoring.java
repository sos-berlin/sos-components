package com.sos.joc.cleanup.model;

import java.util.Date;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cleanup.helper.CleanupPartialResult;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.db.DBLayer;
import com.sos.monitoring.notification.NotificationApplication;

public class CleanupTaskMonitoring extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskMonitoring.class);

    protected enum MontitoringScope {
        MAIN, REMAINING
    }

    protected enum MontitoringRange {
        ALL, STEPS
    }

    private int totalOrders = 0;
    private int totalOrderSteps = 0;
    private int totalOrderNotifications = 0;
    private int totalOrderNotificationWorkflows = 0;
    private int totalSystemNotifications = 0;
    private int totalNotificationMonitors = 0;
    private int totalNotificationAcknowledgements = 0;

    private String columnQuotedHistoryId;
    private String columnQuotedHoMainParentId;
    private String columnQuotedMainParentId;

    public CleanupTaskMonitoring(JocClusterHibernateFactory factory, IJocActiveMemberService service, int batchSize, boolean forceCleanup) {
        super(factory, service, batchSize, forceCleanup);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime monitoringDatetime = datetimes.get(0);
            TaskDateTime notificationDatetime = datetimes.get(1);
            JocServiceTaskAnswerState state = null;

            tryOpenSession();
            if (notificationDatetime.getDatetime() != null) {
                state = cleanupNotifications(MontitoringScope.MAIN, notificationDatetime);
            } else {
                LOGGER.info(String.format("[%s][order/system_notifications][%s]skip", getIdentifier(), notificationDatetime.getAge()
                        .getConfigured()));
            }

            if (monitoringDatetime.getDatetime() != null && (state == null || state.equals(JocServiceTaskAnswerState.COMPLETED))) {
                String logPrefix = String.format("[%s][monitoring][%s][%s]", getIdentifier(), monitoringDatetime.getAge().getConfigured(),
                        monitoringDatetime.getZonedDatetime());

                LOGGER.info(logPrefix + "start cleanup");
                state = cleanupOrders(MontitoringScope.MAIN, MontitoringRange.ALL, monitoringDatetime.getDatetime(), monitoringDatetime.getAge()
                        .getConfigured());
                if (isCompleted(state)) {
                    Date remainingStartTime = getRemainingStartTime(notificationDatetime);
                    String remainingAgeInfo = getRemainingAgeInfo(notificationDatetime);

                    state = cleanupOrders(MontitoringScope.REMAINING, MontitoringRange.ALL, remainingStartTime, remainingAgeInfo);
                    if (isCompleted(state)) {
                        state = cleanupOrders(MontitoringScope.REMAINING, MontitoringRange.STEPS, remainingStartTime, remainingAgeInfo);
                    }
                }
                LOGGER.info(logPrefix + "end cleanup");
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

    private JocServiceTaskAnswerState cleanupNotifications(MontitoringScope scope, TaskDateTime datetime) throws SOSHibernateException {
        String logPrefix = String.format("[%s][order_notifications][%s][%s]", getIdentifier(), datetime.getAge().getConfigured(), datetime
                .getZonedDatetime());

        LOGGER.info(logPrefix + "start cleanup");
        JocServiceTaskAnswerState state = cleanupOrderNotifications(scope, datetime);
        if (state.equals(JocServiceTaskAnswerState.COMPLETED)) {
            String logPrefixSN = String.format("[%s][system_notifications][%s][%s]", getIdentifier(), datetime.getAge().getConfigured(), datetime
                    .getZonedDatetime());
            LOGGER.info(logPrefixSN + "start cleanup");
            state = cleanupSystemNotifications(scope, datetime);
            LOGGER.info(logPrefixSN + "end cleanup");
        }
        LOGGER.info(logPrefix + "end cleanup");
        return state;
    }

    private JocServiceTaskAnswerState cleanupOrderNotifications(MontitoringScope scope, TaskDateTime datetime) throws SOSHibernateException {
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

    private JocServiceTaskAnswerState cleanupSystemNotifications(MontitoringScope scope, TaskDateTime datetime) throws SOSHibernateException {
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

    private void setQuotedColumns() {
        if (columnQuotedHoMainParentId == null) {
            Dialect d = getFactory().getDialect();
            columnQuotedHoMainParentId = SOSHibernate.quoteColumn(d, "HO_MAIN_PARENT_ID");
            columnQuotedMainParentId = SOSHibernate.quoteColumn(d, "MAIN_PARENT_ID");
            columnQuotedHistoryId = SOSHibernate.quoteColumn(d, "HISTORY_ID");
        }
    }

    protected JocServiceTaskAnswerState cleanupOrders(MontitoringScope scope, MontitoringRange range, Date startTime, String ageInfo)
            throws SOSHibernateException {

        setQuotedColumns();

        boolean runm = true;
        while (runm) {
            tryOpenSession();

            Long maxMainParentId = getOrderMaxMainParentId(scope, range, startTime, ageInfo);
            if (maxMainParentId == null || maxMainParentId.intValue() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            boolean completed = cleanupOrders(scope, range, startTime, ageInfo, maxMainParentId);
            if (!completed) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private Long getOrderMaxMainParentId(MontitoringScope scope, MontitoringRange range, Date startTime, String ageInfo)
            throws SOSHibernateException {
        String table = DBLayer.TABLE_MON_ORDERS;
        StringBuilder hql = null;
        switch (scope) {
        case MAIN:
            hql = new StringBuilder("select  max(mainParentId) from ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
            hql.append("where startTime < :startTime ");
            hql.append("and parentId=0");
            break;
        case REMAINING:
            switch (range) {
            case ALL:
                hql = new StringBuilder("select max(mainParentId) from ").append(DBLayer.DBITEM_MON_ORDERS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            case STEPS:
                table = DBLayer.TABLE_MON_ORDER_STEPS;
                hql = new StringBuilder("select max(historyOrderMainParentId) from ").append(DBLayer.DBITEM_MON_ORDER_STEPS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            }
            break;
        }

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        Long r = getDbLayer().getSession().getSingleValue(query);

        if (r == null || r.intValue() == 0) {
            r = 0L;
            LOGGER.info(String.format("[%s][monitoring][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo,
                    getDateTime(startTime), table, r));
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][monitoring][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo,
                        getDateTime(startTime), table, r));
            }
        }
        return r;
    }

    private CleanupPartialResult deleteOrderSteps(Long maxMainParentId) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(DBLayer.TABLE_MON_ORDER_STEPS);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(DBLayer.TABLE_MON_ORDER_STEPS).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedHistoryId).append(" in (");
            sql.append("select ").append(columnQuotedHistoryId).append(" from ").append(DBLayer.TABLE_MON_ORDER_STEPS).append(" ");
            sql.append("where ").append(columnQuotedHoMainParentId).append(" <= ").append(maxMainParentId).append(" ");
            sql.append("limit ").append(getBatchSize());
            sql.append(")");
        } else {
            sql.append("where ").append(columnQuotedHoMainParentId).append(" <= ").append(maxMainParentId).append(" ");
            sql.append(getLimitWhere());
        }

        r.run(this, sql, maxMainParentId);
        return r;
    }

    private CleanupPartialResult deleteOrders(Long maxMainParentId) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(DBLayer.TABLE_MON_ORDERS);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(DBLayer.TABLE_MON_ORDERS).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedHistoryId).append(" in (");
            sql.append("select ").append(columnQuotedHistoryId).append(" from ").append(DBLayer.TABLE_MON_ORDERS).append(" ");
            sql.append("where ").append(columnQuotedMainParentId).append(" <= ").append(maxMainParentId).append(" ");
            sql.append("limit ").append(getBatchSize());
            sql.append(")");
        } else {
            sql.append("where ").append(columnQuotedMainParentId).append(" <= ").append(maxMainParentId).append(" ");
            sql.append(getLimitWhere());
        }

        r.run(this, sql, maxMainParentId);
        return r;
    }

    private boolean cleanupOrders(MontitoringScope scope, MontitoringRange range, Date startTime, String ageInfo, Long maxMainParentId)
            throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][monitoring][");
        log.append(getScope(scope)).append(" ").append(getRange(range)).append("]");
        log.append("[").append(ageInfo).append(" ").append(getDateTime(startTime)).append("][maxMainParentId=" + maxMainParentId + "][deleted]");

        CleanupPartialResult r = deleteOrderSteps(maxMainParentId);
        totalOrderSteps += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_MON_ORDER_STEPS, r.getDeletedTotal(), totalOrderSteps));

        if (range.equals(MontitoringRange.ALL)) {
            if (isStopped()) {
                LOGGER.info(log.toString());
                return false;
            }

            r = deleteOrders(maxMainParentId);
            totalOrders += r.getDeletedTotal();
            log.append(getDeleted(DBLayer.TABLE_MON_ORDERS, r.getDeletedTotal(), totalOrders));
        }

        LOGGER.info(log.toString());
        return true;
    }

    private StringBuilder deleteOrderNotifications(MontitoringScope scope, TaskDateTime datetime, List<Long> notificationIds)
            throws SOSHibernateException {
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

    private StringBuilder deleteSystemNotifications(MontitoringScope scope, TaskDateTime datetime, List<Long> notificationIds)
            throws SOSHibernateException {
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

    private List<Long> getOrderNotificationIds(MontitoringScope scope, TaskDateTime datetime) throws SOSHibernateException {
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

    private List<Long> getSystemNotificationIds(MontitoringScope scope, TaskDateTime datetime) throws SOSHibernateException {
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

    private String getScope(MontitoringScope val) {
        return val.name().toLowerCase();
    }

    private String getRange(MontitoringRange val) {
        return val.name().toLowerCase();
    }
}
