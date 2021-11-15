package com.sos.joc.cleanup.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskMonitoring extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskMonitoring.class);

    private int totalOrders = 0;
    private int totalOrderSteps = 0;
    private int totalNotifications = 0;
    private int totalNotificationWorkflows = 0;
    private int totalNotificationMonitors = 0;
    private int totalNotificationAcknowledgements = 0;

    public CleanupTaskMonitoring(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime monitoringDatetime = datetimes.get(0);
            TaskDateTime notificationDatetime = datetimes.get(1);

            JocServiceTaskAnswerState state = null;
            if (notificationDatetime.getDatetime() != null) {
                LOGGER.info(String.format("[%s][notifications][%s][%s]start cleanup", getIdentifier(), notificationDatetime.getAge().getConfigured(),
                        notificationDatetime.getZonedDatetime()));
                state = cleanupNotifications(notificationDatetime);
            } else {
                LOGGER.info(String.format("[%s][notifications][%s]skip", getIdentifier(), notificationDatetime.getAge().getConfigured()));
            }

            if (monitoringDatetime.getDatetime() != null && (state == null || state.equals(JocServiceTaskAnswerState.COMPLETED))) {
                LOGGER.info(String.format("[%s][monitoring][%s][%s]start cleanup", getIdentifier(), monitoringDatetime.getAge().getConfigured(),
                        monitoringDatetime.getZonedDatetime()));
                state = cleanupMonitoring(monitoringDatetime);
            } else {
                LOGGER.info(String.format("[%s][monitoring][%s]skip", getIdentifier(), monitoringDatetime.getAge().getConfigured()));
            }

            return state;
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
    }

    private JocServiceTaskAnswerState cleanupMonitoring(TaskDateTime datetime) throws SOSHibernateException {
        boolean runm = true;
        while (runm) {
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            List<Long> rm = getMainOrderIds(datetime);
            getDbLayer().close();

            if (rm == null || rm.size() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            boolean runc = true;
            while (runc) {
                if (isStopped()) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }
                if (!askService()) {
                    waitFor(WAIT_INTERVAL_ON_BUSY);
                    continue;
                }

                getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                List<Long> rc = getChildOrderIds(datetime, rm);
                if (rc != null && rc.size() > 0) {
                    if (!cleanupOrders(datetime, "children", rc)) {
                        getDbLayer().close();
                        return JocServiceTaskAnswerState.UNCOMPLETED;
                    }
                }
                getDbLayer().close();
                runc = false;
            }
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            if (!cleanupOrders(datetime, "main", rm)) {
                getDbLayer().close();
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private JocServiceTaskAnswerState cleanupNotifications(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
        List<Long> ids = getNotificationIds(datetime);
        getDbLayer().close();

        if (ids == null || ids.size() == 0) {
            return JocServiceTaskAnswerState.COMPLETED;
        }

        try {
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            int size = ids.size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                ArrayList<Long> copy = (ArrayList<Long>) ids.stream().collect(Collectors.toList());

                JocServiceTaskAnswerState state = null;
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    List<Long> subList;
                    if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                        subList = copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE));
                    } else {
                        subList = copy.subList(i, size);
                    }
                    deleteNotifications(datetime, subList);
                }
                return state;

            } else {
                deleteNotifications(datetime, ids);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private List<Long> getMainOrderIds(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select historyId from ");
        hql.append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("where startTime < :startTime ");
        hql.append("and parentId=0");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        int size = r.size();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info(String.format("[%s][%s][%s][main]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_MON_ORDERS,
                    size));
        } else {
            if (size == 0) {
                LOGGER.info(String.format("[%s][%s][%s][main]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_MON_ORDERS,
                        size));
            }
        }
        return r;
    }

    private List<Long> getChildOrderIds(TaskDateTime datetime, List<Long> mainOrderIds) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select historyId from ");
        hql.append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("where parentId in (:mainOrderIds)");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("mainOrderIds", mainOrderIds);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s][%s][children]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_MON_ORDERS,
                    r.size()));
        }
        return r;
    }

    private boolean cleanupOrders(TaskDateTime datetime, String range, List<Long> orderIds) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("][").append(range).append(
                "]");

        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_ORDER_STEPS).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().getSession().commit();
        totalOrderSteps += r;
        log.append(getDeleted(DBLayer.TABLE_MON_ORDER_STEPS, r, totalOrderSteps));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_ORDERS).append(" ");
        hql.append("where historyId in (:orderIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().getSession().commit();
        totalOrders += r;
        log.append(getDeleted(DBLayer.TABLE_MON_ORDERS, r, totalOrders));

        LOGGER.info(log.toString());
        return true;
    }

    private StringBuilder deleteNotifications(TaskDateTime datetime, List<Long> notificationIds) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("]");

        getDbLayer().getSession().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_MONITORS).append(" ");
        hql.append("where notificationId in (:notificationIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        totalNotificationMonitors += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_MONITORS, r, totalNotificationMonitors));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_WORKFLOWS).append(" ");
        hql.append("where notificationId in (:notificationIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        r = getDbLayer().getSession().executeUpdate(query);
        totalNotificationWorkflows += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_WORKFLOWS, r, totalNotificationWorkflows));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOT_ACKNOWLEDGEMENTS).append(" ");
        hql.append("where notificationId in (:notificationIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        r = getDbLayer().getSession().executeUpdate(query);
        totalNotificationAcknowledgements += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOT_ACKNOWLEDGEMENTS, r, totalNotificationAcknowledgements));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" ");
        hql.append("where id in (:notificationIds) ");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("notificationIds", notificationIds);
        r = getDbLayer().getSession().executeUpdate(query);
        totalNotifications += r;
        log.append(getDeleted(DBLayer.TABLE_MON_NOTIFICATIONS, r, totalNotifications));

        getDbLayer().getSession().commit();
        LOGGER.info(log.toString());
        return log;
    }

    private List<Long> getNotificationIds(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_MON_NOTIFICATIONS).append(" ");
        hql.append("where created < :startTime ");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s][%s]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_MON_NOTIFICATIONS, r
                    .size()));
        }
        return r;
    }

}
