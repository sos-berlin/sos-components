package com.sos.joc.cleanup.model;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskHistory extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskHistory.class);

    private enum Scope {
        MAIN, REMAINING
    }

    private enum Range {
        ALL, STEPS, STATES, LOGS
    }

    // TODO read from history/cluster/globals ..
    private String logDir = "logs/history";

    private int totalOrders = 0;
    private int totalOrderStates = 0;
    private int totalOrderSteps = 0;
    private int totalOrderLogs = 0;

    public CleanupTaskHistory(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime orderDatetime = datetimes.get(0);
            TaskDateTime logsDatetime = datetimes.get(1);
            JocServiceTaskAnswerState state = null;

            tryOpenSession();

            // Cleanup Logs and Orders/Steps/States
            if (orderDatetime.getAge().getConfigured().equals(logsDatetime.getAge().getConfigured())) {
                LOGGER.info(String.format("[%s][orders,logs][%s][%s]start cleanup", getIdentifier(), orderDatetime.getAge().getConfigured(),
                        orderDatetime.getZonedDatetime()));

                state = cleanupOrders(orderDatetime, true);
                deleteNotReferencedLogs(state);
                return state;
            }

            // Cleanup Logs
            if (logsDatetime.getDatetime() != null) {
                LOGGER.info(String.format("[%s][logs][%s][%s]start cleanup", getIdentifier(), logsDatetime.getAge().getConfigured(), logsDatetime
                        .getZonedDatetime()));
                state = cleanupLogs(Scope.MAIN, Range.ALL, logsDatetime);
            } else {
                LOGGER.info(String.format("[%s][logs][%s]skip", getIdentifier(), logsDatetime.getAge().getConfigured()));
            }

            // Cleanup Orders/Steps/States
            if (orderDatetime.getDatetime() != null && (state == null || state.equals(JocServiceTaskAnswerState.COMPLETED))) {
                LOGGER.info(String.format("[%s][orders][%s][%s]start cleanup", getIdentifier(), orderDatetime.getAge().getConfigured(), orderDatetime
                        .getZonedDatetime()));

                state = cleanupOrders(orderDatetime, false);
            } else {
                LOGGER.info(String.format("[%s][orders][%s]skip", getIdentifier(), orderDatetime.getAge().getConfigured()));
            }

            deleteNotReferencedLogs(state);

            return state;
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            close();
        }
    }

    private JocServiceTaskAnswerState cleanupOrders(TaskDateTime dateTime, boolean deleteLogs) throws SOSHibernateException {
        JocServiceTaskAnswerState state = cleanupOrders(Scope.MAIN, Range.ALL, dateTime.getDatetime(), dateTime.getAge().getConfigured(), deleteLogs);
        if (isCompleted(state)) {
            Date remainingStartTime = getRemainingStartTime(dateTime);
            String remainingAgeInfo = getRemainingAgeInfo(dateTime);

            state = cleanupOrders(Scope.REMAINING, Range.ALL, remainingStartTime, remainingAgeInfo, deleteLogs);
            if (isCompleted(state)) {
                state = cleanupOrders(Scope.REMAINING, Range.STEPS, remainingStartTime, remainingAgeInfo, deleteLogs);
                if (isCompleted(state)) {
                    state = cleanupRemaining(remainingStartTime, remainingAgeInfo, deleteLogs);
                }
            }
        }
        return state;
    }

    private JocServiceTaskAnswerState cleanupOrders(Scope scope, Range range, Date startTime, String ageInfo, boolean deleteLogs)
            throws SOSHibernateException {
        if (scope.equals(Scope.MAIN)) {
            tryOpenSession();

            getDbLayer().beginTransaction();
            deleteControllersAndAgents(startTime, ageInfo);
            getDbLayer().commit();
        }

        boolean runm = true;
        while (runm) {
            tryOpenSession();

            List<Long> rm = getMainOrderIds(scope, range, startTime, ageInfo);
            if (rm == null || rm.size() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            getDbLayer().beginTransaction();
            boolean completed = deleteOrders(scope, range, startTime, ageInfo, rm, deleteLogs);
            getDbLayer().commit();
            if (!completed) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private JocServiceTaskAnswerState cleanupRemaining(Date startTime, String ageInfo, boolean deleteLogs) throws SOSHibernateException {
        if (isStopped()) {
            return JocServiceTaskAnswerState.UNCOMPLETED;
        }
        tryOpenSession();

        JocServiceTaskAnswerState state = JocServiceTaskAnswerState.COMPLETED;
        getDbLayer().beginTransaction();
        deleteRemainingStates(startTime, ageInfo);
        if (deleteLogs) {
            if (isStopped()) {
                state = JocServiceTaskAnswerState.UNCOMPLETED;
            } else {
                deleteRemainingLogs(startTime, ageInfo);
            }
        }
        getDbLayer().commit();

        return state;
    }

    private List<Long> getMainOrderIds(Scope scope, Range range, Date startTime, String ageInfo) throws SOSHibernateException {
        String table = DBLayer.TABLE_HISTORY_ORDERS;
        StringBuilder hql = null;
        switch (scope) {
        case MAIN:
            hql = new StringBuilder("select id from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
            hql.append("where startTime < :startTime ");
            hql.append("and parentId=0");
            break;
        case REMAINING:
            switch (range) {
            case ALL:
                hql = new StringBuilder("select distinct mainParentId from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            case STEPS:
                table = DBLayer.TABLE_MON_ORDER_STEPS;
                hql = new StringBuilder("select distinct historyOrderMainParentId from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            default:
                return null;
            }
            break;
        }
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);

        int size = r.size();
        if (size == 0) {
            LOGGER.info(String.format("[%s][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo, getDateTime(
                    startTime), table, size));
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo, getDateTime(
                        startTime), table, size));
            }
        }
        return r;
    }

    private List<Long> getLogsOrderIds(Scope scope, Range range, TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("where startTime < :startTime ");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s][%s]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_ORDERS, r
                    .size()));
        }
        return r;
    }

    private boolean deleteOrders(Scope scope, Range range, Date startTime, String ageInfo, List<Long> orderIds, boolean deleteLogs)
            throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][");
        log.append(getScope(scope)).append(" ").append(getRange(range)).append("]");
        log.append("[").append(ageInfo).append(" ").append(getDateTime(startTime)).append("][deleted]");

        // getDbLayer().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STATES).append(" ");
        hql.append("where historyOrderMainParentId in (:orderIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalOrderStates += r;
        log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDER_STATES, r, totalOrderStates));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }

        // getDbLayer().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
        hql.append("where historyOrderMainParentId in (:orderIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalOrderSteps += r;
        log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDER_STEPS, r, totalOrderSteps));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }

        if (deleteLogs) {
            // getDbLayer().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_LOGS).append(" ");
            hql.append("where historyOrderMainParentId in (:orderIds)");
            query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            r = getDbLayer().getSession().executeUpdate(query);
            // getDbLayer().commit();
            totalOrderLogs += r;
            log.append(getDeleted(DBLayer.TABLE_HISTORY_LOGS, r, totalOrderLogs));

            if (isStopped()) {
                LOGGER.info(log.toString());
                return false;
            }
        }

        if (range.equals(Range.ALL)) {
            // getDbLayer().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
            hql.append("where mainParentId in (:orderIds)");
            query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            r = getDbLayer().getSession().executeUpdate(query);
            // getDbLayer().commit();
            totalOrders += r;
            log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDERS, r, totalOrders));
        }

        LOGGER.info(log.toString());
        return true;
    }

    private boolean deleteRemainingStates(Date startTime, String ageInfo) throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][");
        log.append(getScope(Scope.REMAINING)).append(" ").append(getRange(Range.STATES)).append("]");
        log.append("[").append(ageInfo).append(" ").append(getDateTime(startTime)).append("][deleted]");

        // getDbLayer().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STATES).append(" ");
        hql.append("where created < :startTime ");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        int r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalOrderStates += r;
        log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDER_STATES, r, totalOrderStates));

        LOGGER.info(log.toString());
        return true;
    }

    private boolean deleteRemainingLogs(Date startTime, String ageInfo) throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][");
        log.append(getScope(Scope.REMAINING)).append(" ").append(getRange(Range.LOGS)).append("]");
        log.append("[").append(ageInfo).append(" ").append(getDateTime(startTime)).append("][deleted]");

        // getDbLayer().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_LOGS).append(" ");
        hql.append("where created < :startTime ");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        int r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalOrderLogs += r;
        log.append(getDeleted(DBLayer.TABLE_HISTORY_LOGS, r, totalOrderLogs));

        LOGGER.info(log.toString());
        return true;
    }

    private void deleteControllersAndAgents(Date startTime, String ageInfo) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][").append(ageInfo).append("][deleted]");

        Long eventId = Long.valueOf(startTime.getTime() * 1_000 + 999);

        // getDbLayer().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where readyEventId < :eventId");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        int r = getDbLayer().getSession().executeUpdate(query);
        log.append("[").append(DBLayer.TABLE_HISTORY_CONTROLLERS).append("=").append(r).append("]");

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where readyEventId < :eventId");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        r = getDbLayer().getSession().executeUpdate(query);
        log.append("[").append(DBLayer.TABLE_HISTORY_AGENTS).append("=").append(r).append("]");

        // getDbLayer().commit();

        LOGGER.info(log.toString());
    }

    // TODO duplicate method (some changes) - see com.sos.js7.history.controller.HistoryService
    private void deleteNotReferencedLogs(JocServiceTaskAnswerState state) {
        if (state != null && !state.equals(JocServiceTaskAnswerState.COMPLETED)) {
            return;
        }
        if (isStopped()) {
            return;
        }
        Path dir = Paths.get(logDir).toAbsolutePath();
        if (Files.exists(dir)) {
            LOGGER.info(String.format("[%s][logDirectory]%s", getIdentifier(), dir));

            try {
                if (SOSPath.isDirectoryEmpty(dir)) {
                    LOGGER.info(String.format("[%s][logDirectory][skip]is empty", getIdentifier()));
                } else {
                    tryOpenSession();
                    int i = 0;
                    try (Stream<Path> stream = Files.walk(dir)) {
                        for (Path p : stream.filter(f -> !f.equals(dir)).collect(Collectors.toList())) {
                            File f = p.toFile();
                            if (f.isDirectory()) {
                                try {
                                    Long id = Long.parseLong(f.getName());
                                    if (!getDbLayer().mainOrderLogNotFinished(id)) {
                                        try {
                                            if (SOSPath.deleteIfExists(p)) {
                                                LOGGER.info(String.format("[%s][logDirectory][deleted]%s", getIdentifier(), p));
                                                i++;
                                            }
                                        } catch (Throwable e) {// in the same moment deleted by history
                                        }
                                    }
                                } catch (Throwable e) {
                                    LOGGER.info(String.format("[%s][logDirectory][skip][non numeric]%s", getIdentifier(), p));
                                }
                            }
                        }
                    }
                    LOGGER.info(String.format("[%s][logDirectory][deleted][total]%s", getIdentifier(), i));
                }
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][logDirectory]%s", getIdentifier(), e.toString()), e);
            }
        }
    }

    private JocServiceTaskAnswerState cleanupLogs(Scope scope, Range range, TaskDateTime datetime) throws Exception {
        tryOpenSession();

        List<Long> ids = getLogsOrderIds(scope, range, datetime);
        if (ids == null || ids.size() == 0) {
            return JocServiceTaskAnswerState.COMPLETED;
        }

        try {
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
                    state = deleteLogs(datetime, subList);
                }
                return state;

            } else {
                return deleteLogs(datetime, ids);
            }
        } catch (Throwable e) {
            throw e;
        }
    }

    private JocServiceTaskAnswerState deleteLogs(TaskDateTime datetime, List<Long> orderIds) throws SOSHibernateException {
        getDbLayer().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_LOGS).append(" ");
        hql.append("where historyOrderMainParentId in (:orderIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        totalOrderLogs += r;
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s, total=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_HISTORY_LOGS,
                r, totalOrderLogs));

        getDbLayer().commit();
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private String getScope(Scope val) {
        return val.name().toLowerCase();
    }

    private String getRange(Range val) {
        return val.name().toLowerCase();
    }
}
