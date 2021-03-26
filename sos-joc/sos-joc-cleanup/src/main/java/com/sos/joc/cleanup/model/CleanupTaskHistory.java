package com.sos.joc.cleanup.model;

import java.util.ArrayList;
import java.util.Date;
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

public class CleanupTaskHistory extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskHistory.class);

    public CleanupTaskHistory(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime orderDatetime = datetimes.get(0);
            TaskDateTime logsDatetime = datetimes.get(1);

            if (orderDatetime.getAge().getConfigured().equals(logsDatetime.getAge().getConfigured())) {
                LOGGER.info(String.format("[%s][orders,logs][%s][%s]start cleanup", getIdentifier(), orderDatetime.getAge().getConfigured(),
                        orderDatetime.getZonedDatetime()));
                return cleanupOrders(orderDatetime, true);
            }

            JocServiceTaskAnswerState state = null;
            if (logsDatetime.getDatetime() != null) {
                LOGGER.info(String.format("[%s][logs][%s][%s]start cleanup", getIdentifier(), logsDatetime.getAge().getConfigured(), logsDatetime
                        .getZonedDatetime()));
                state = cleanupLogs(logsDatetime);
                if (state.equals(JocServiceTaskAnswerState.COMPLETED)) {
                    state = cleanupTempLogs(logsDatetime);
                }
            } else {
                LOGGER.info(String.format("[%s][logs][%s]skip", getIdentifier(), logsDatetime.getAge().getConfigured()));
            }

            if (orderDatetime.getDatetime() != null && (state == null || state.equals(JocServiceTaskAnswerState.COMPLETED))) {
                LOGGER.info(String.format("[%s][orders][%s][%s]start cleanup", getIdentifier(), orderDatetime.getAge().getConfigured(), orderDatetime
                        .getZonedDatetime()));
                state = cleanupOrders(orderDatetime, false);
            } else {
                LOGGER.info(String.format("[%s][orders][%s]skip", getIdentifier(), orderDatetime.getAge().getConfigured()));
            }

            return state;
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
    }

    private JocServiceTaskAnswerState cleanupLogs(TaskDateTime datetime) throws Exception {

        getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
        List<Long> ids = getOrderIds(datetime);
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
                    state = deleteLogs(datetime, subList, null);
                }
                return state;

            } else {
                return deleteLogs(datetime, ids, null);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            getDbLayer().close();
        }
    }

    private JocServiceTaskAnswerState cleanupTempLogs(TaskDateTime datetime) throws Exception {
        getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
        List<Long> ids = getMainParentIdsFromTempLogs(datetime);
        getDbLayer().close();
        if (ids == null || ids.size() == 0) {
            return JocServiceTaskAnswerState.COMPLETED;
        }

        try {
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            return deleteLogs(datetime, ids, datetime.getDatetime());
        } catch (Throwable e) {
            throw e;
        } finally {
            getDbLayer().close();
        }

    }

    private JocServiceTaskAnswerState deleteLogs(TaskDateTime datetime, List<Long> orderIds, Date tempLogsDate) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        if (tempLogsDate == null) {
            StringBuilder hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_LOG).append(" ");
            hql.append("where historyOrderId in (:orderIds)");
            Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            int r = getDbLayer().getSession().executeUpdate(query);
            LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_LOGS, r));
        } else {
            StringBuilder hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_TEMP_LOG).append(" ");
            hql.append("where historyOrderMainParentId in (");
            hql.append("select id from ").append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
            hql.append("where id in (:orderIds) ");
            hql.append("and startTime < :startTime");
            hql.append(")");
            Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            query.setParameter("startTime", tempLogsDate);
            int r = getDbLayer().getSession().executeUpdate(query);
            LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_TEMP_LOGS,
                    r));
        }
        getDbLayer().getSession().commit();
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private JocServiceTaskAnswerState cleanupOrders(TaskDateTime datetime, boolean deleteLogs) throws SOSHibernateException {
        getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
        cleanupControllersAndAgents(datetime);
        getDbLayer().close();

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
                    if (!cleanupOrders(datetime, "childs", rc, deleteLogs, false)) {
                        return JocServiceTaskAnswerState.UNCOMPLETED;
                    }
                }
                getDbLayer().close();
                runc = false;
            }
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            if (!cleanupOrders(datetime, "main", rm, deleteLogs, true)) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private List<Long> getMainOrderIds(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where startTime < :startTime ");
        hql.append("and parentId=0");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        LOGGER.info(String.format("[%s][%s][%s][main]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_ORDERS, r
                .size()));
        return r;
    }

    private List<Long> getOrderIds(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where startTime < :startTime ");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        LOGGER.info(String.format("[%s][%s][%s]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_ORDERS, r
                .size()));
        return r;
    }

    private List<Long> getMainParentIdsFromTempLogs(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select historyOrderMainParentId ");
        hql.append("from ").append(DBLayer.DBITEM_HISTORY_TEMP_LOG).append(" ");
        hql.append("group by historyOrderMainParentId");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setMaxResults(SOSHibernate.LIMIT_IN_CLAUSE);
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        LOGGER.info(String.format("[%s][%s][%s]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_TEMP_LOGS, r
                .size()));
        return r;
    }

    private List<Long> getChildOrderIds(TaskDateTime datetime, List<Long> mainOrderIds) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where parentId in (:mainOrderIds)");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("mainOrderIds", mainOrderIds);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        LOGGER.info(String.format("[%s][%s][%s][childs]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_ORDERS, r
                .size()));
        return r;
    }

    private boolean cleanupOrders(TaskDateTime datetime, String range, List<Long> orderIds, boolean deleteLogs, boolean deleteTmpLogs)
            throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("][").append(range).append(
                "]");

        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STATE).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().getSession().commit();
        log.append("[").append(DBLayer.TABLE_HISTORY_ORDER_STATES).append("=").append(r).append("]");
        
        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().getSession().commit();
        log.append("[").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append("=").append(r).append("]");
        
        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }

        if (deleteLogs) {
            getDbLayer().getSession().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_LOG).append(" ");
            hql.append("where historyOrderId in (:orderIds)");
            query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            r = getDbLayer().getSession().executeUpdate(query);
            getDbLayer().getSession().commit();
            log.append("[").append(DBLayer.TABLE_HISTORY_LOGS).append("=").append(r).append("]");
            
            if (isStopped()) {
                LOGGER.info(log.toString());
                return false;
            }
        }

        if (deleteTmpLogs) {
            getDbLayer().getSession().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_TEMP_LOG).append(" ");
            hql.append("where historyOrderMainParentId in (:orderIds)");
            query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            r = getDbLayer().getSession().executeUpdate(query);
            getDbLayer().getSession().commit();
            log.append("[").append(DBLayer.TABLE_HISTORY_TEMP_LOGS).append("=").append(r).append("]");
            
            if (isStopped()) {
                LOGGER.info(log.toString());
                return false;
            }
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where id in (:orderIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().getSession().commit();
        log.append("[").append(DBLayer.TABLE_HISTORY_ORDERS).append("=").append(r).append("]");
        
        LOGGER.info(log.toString());
        return true;
    }

    private void cleanupControllersAndAgents(TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("]");

        Long eventId = new Long(datetime.getDatetime().getTime() * 1_000 + 999);

        getDbLayer().getSession().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where readyEventId < :eventId");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        int r = getDbLayer().getSession().executeUpdate(query);
        log.append("[").append(DBLayer.TABLE_HISTORY_CONTROLLERS).append("=").append(r).append("]");

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId < :eventId");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        r = getDbLayer().getSession().executeUpdate(query);
        log.append("[").append(DBLayer.TABLE_HISTORY_AGENTS).append("=").append(r).append("]");

        getDbLayer().getSession().commit();

        LOGGER.info(log.toString());
    }
}
