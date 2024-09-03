package com.sos.joc.cleanup.model;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.dialect.Dialect;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cleanup.helper.CleanupPartialResult;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.cluster.configuration.JocHistoryConfiguration;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.db.DBLayer;

public class CleanupTaskHistory extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskHistory.class);

    protected enum Scope {
        MAIN, REMAINING
    }

    protected enum Range {
        ALL, STEPS, STATES, LOGS
    }

    // TODO read from history/cluster/globals ..
    private String logDir = "logs/history";
    private String logDirTmpOrders = logDir + "/" + JocHistoryConfiguration.ID_NOT_STARTED_ORDER;

    private long totalOrders = 0;
    private long totalOrderStates = 0;
    private long totalOrderSteps = 0;
    private long totalOrderLogs = 0;
    private long totalOrderTags = 0;

    private String columnQuotedId;
    private String columnQuotedHoMainParentId;
    private String columnQuotedMainParentId;

    public CleanupTaskHistory(JocClusterHibernateFactory factory, IJocActiveMemberService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime orderDatetime = datetimes.get(0);
            TaskDateTime logsDatetime = datetimes.get(1);
            JocServiceTaskAnswerState state = null;

            tryOpenSession();

            // Cleanup Logs and Orders/Steps/States/Tags
            if (orderDatetime.getAge().getConfigured().equals(logsDatetime.getAge().getConfigured())) {
                String logPrefix = String.format("[%s][orders,logs][%s][%s]", getIdentifier(), orderDatetime.getAge().getConfigured(), orderDatetime
                        .getZonedDatetime());
                LOGGER.info(logPrefix + "start cleanup");

                // Cleanup DB Logs and Orders/Steps/States/Tags
                state = cleanupOrders(orderDatetime, true);
                deleteNotStartedOrdersLogs(orderDatetime);
                deleteNotReferencedLogs(state);

                LOGGER.info(logPrefix + "end cleanup");

                return state;
            }

            TaskDateTime notStartedOrdersLogsDatetime = logsDatetime;
            // Cleanup Logs
            if (logsDatetime.getDatetime() != null) {
                String logPrefix = String.format("[%s][logs][%s][%s]", getIdentifier(), logsDatetime.getAge().getConfigured(), logsDatetime
                        .getZonedDatetime());

                LOGGER.info(logPrefix + "start cleanup");
                state = cleanupLogs(Scope.MAIN, Range.ALL, logsDatetime);
                LOGGER.info(logPrefix + "end cleanup");
            } else {
                LOGGER.info(String.format("[%s][logs][%s]skip", getIdentifier(), logsDatetime.getAge().getConfigured()));
            }

            // Cleanup Orders/Steps/States/Tags
            if (orderDatetime.getDatetime() != null && (state == null || state.equals(JocServiceTaskAnswerState.COMPLETED))) {
                notStartedOrdersLogsDatetime = orderDatetime;
                String logPrefix = String.format("[%s][orders][%s][%s]", getIdentifier(), orderDatetime.getAge().getConfigured(), orderDatetime
                        .getZonedDatetime());

                LOGGER.info(logPrefix + "start cleanup");
                state = cleanupOrders(orderDatetime, false);
                LOGGER.info(logPrefix + "end cleanup");
            } else {
                LOGGER.info(String.format("[%s][orders][%s]skip", getIdentifier(), orderDatetime.getAge().getConfigured()));
            }

            deleteNotStartedOrdersLogs(notStartedOrdersLogsDatetime);
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

    private void setQuotedColumns() {
        if (columnQuotedHoMainParentId == null) {
            Dialect d = getFactory().getDialect();
            columnQuotedHoMainParentId = SOSHibernate.quoteColumn(d, "HO_MAIN_PARENT_ID");
            columnQuotedMainParentId = SOSHibernate.quoteColumn(d, "MAIN_PARENT_ID");
            columnQuotedId = SOSHibernate.quoteColumn(d, "ID");
        }
    }

    protected JocServiceTaskAnswerState cleanupOrders(Scope scope, Range range, Date startTime, String ageInfo, boolean deleteLogs)
            throws SOSHibernateException {

        setQuotedColumns();

        if (scope.equals(Scope.MAIN)) {
            tryOpenSession();

            getDbLayer().beginTransaction();
            deleteControllersAndAgents(startTime, ageInfo);
            getDbLayer().commit();

            boolean completed = cleanupOrderTags(startTime);
            if (!completed && isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }
        }

        boolean runm = true;
        while (runm) {
            if (isStopped()) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }

            tryOpenSession();

            Long maxMainParentId = getOrderMaxMainParentId(scope, range, startTime, ageInfo);
            if (maxMainParentId == null || maxMainParentId.intValue() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }

            boolean completed = cleanupOrders(scope, range, startTime, ageInfo, deleteLogs, maxMainParentId);
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

    private Long getOrderMaxMainParentId(Scope scope, Range range, Date startTime, String ageInfo) throws SOSHibernateException {
        String table = DBLayer.TABLE_HISTORY_ORDERS;
        StringBuilder hql = null;
        switch (scope) {
        case MAIN:
            hql = new StringBuilder("select max(mainParentId) from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
            hql.append("where startTime < :startTime ");
            hql.append("and parentId=0");
            break;
        case REMAINING:
            switch (range) {
            case ALL:
                hql = new StringBuilder("select max(mainParentId) from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            case STEPS:
                table = DBLayer.TABLE_MON_ORDER_STEPS;
                hql = new StringBuilder("select max(historyOrderMainParentId) from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(" ");
                hql.append("where startTime < :startTime");
                break;
            default:
                return null;
            }
            break;
        }
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        Long r = getDbLayer().getSession().getSingleValue(query);

        if (r == null || r.intValue() == 0) {
            r = 0L;
            LOGGER.info(String.format("[%s][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo, getDateTime(
                    startTime), table, r));
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo, getDateTime(
                        startTime), table, r));
            }
        }
        return r;
    }

    private Long getOrderLogsMaxMainParentId(Scope scope, Range range, Date startTime, String ageInfo) throws SOSHibernateException {
        String table = DBLayer.TABLE_HISTORY_LOGS;
        StringBuilder hql = new StringBuilder("select max(historyOrderMainParentId) from ").append(DBLayer.DBITEM_HISTORY_LOGS).append(" ");
        hql.append("where created < :startTime ");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", startTime);
        Long r = getDbLayer().getSession().getSingleValue(query);

        if (r == null || r.intValue() == 0) {
            r = 0L;
            LOGGER.info(String.format("[%s][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo, getDateTime(
                    startTime), table, r));
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s %s][%s %s][%s]found=%s", getIdentifier(), getScope(scope), getRange(range), ageInfo, getDateTime(
                        startTime), table, r));
            }
        }
        return r;
    }

    private CleanupPartialResult deleteOrderStates(Long maxMainParentId) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(DBLayer.TABLE_HISTORY_ORDER_STATES);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(DBLayer.TABLE_HISTORY_ORDER_STATES).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedId).append(" in (");
            sql.append("select ").append(columnQuotedId).append(" from ").append(DBLayer.TABLE_HISTORY_ORDER_STATES).append(" ");
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

    private CleanupPartialResult deleteOrderSteps(Long maxMainParentId) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(DBLayer.TABLE_HISTORY_ORDER_STEPS);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedId).append(" in (");
            sql.append("select ").append(columnQuotedId).append(" from ").append(DBLayer.TABLE_HISTORY_ORDER_STEPS).append(" ");
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

    private CleanupPartialResult deleteLogs(Long maxMainParentId) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(DBLayer.TABLE_HISTORY_LOGS);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(DBLayer.TABLE_HISTORY_LOGS).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedId).append(" in (");
            sql.append("select ").append(columnQuotedId).append(" from ").append(DBLayer.TABLE_HISTORY_LOGS).append(" ");
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
        CleanupPartialResult r = new CleanupPartialResult(DBLayer.TABLE_HISTORY_ORDERS);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(DBLayer.TABLE_HISTORY_ORDERS).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedId).append(" in (");
            sql.append("select ").append(columnQuotedId).append(" from ").append(DBLayer.TABLE_HISTORY_ORDERS).append(" ");
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

    private boolean cleanupOrderTags(Date startTime) throws SOSHibernateException {
        CleanupPartialResult r = deleteOrderTags(startTime);
        totalOrderTags += r.getDeletedTotal();
        return JocServiceTaskAnswerState.COMPLETED.equals(r.getState());
    }

    private CleanupPartialResult deleteOrderTags(Date date) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(DBLayer.TABLE_HISTORY_ORDER_TAGS);
        r.addParameter("date", date);

        String column = SOSHibernate.quoteColumn(getFactory().getDialect(), "START_TIME");
        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(DBLayer.TABLE_HISTORY_ORDER_TAGS).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedId).append(" in (");
            sql.append("select ").append(columnQuotedId).append(" from ").append(DBLayer.TABLE_HISTORY_ORDER_TAGS).append(" ");
            sql.append("where ").append(column).append(" < :date ");
            sql.append("limit ").append(getBatchSize());
            sql.append(")");
        } else {
            sql.append("where ").append(column).append(" < :date ");
            sql.append(getLimitWhere());
        }

        r.run(this, sql);
        return r;
    }

    private boolean cleanupOrders(Scope scope, Range range, Date startTime, String ageInfo, boolean deleteLogs, Long maxMainParentId)
            throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][");
        log.append(getScope(scope)).append(" ").append(getRange(range)).append("]");
        log.append("[").append(ageInfo).append(" ").append(getDateTime(startTime)).append("][maxMainParentId=" + maxMainParentId + "][deleted]");

        CleanupPartialResult r = deleteOrderStates(maxMainParentId);
        totalOrderStates += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDER_STATES, r.getDeletedTotal(), totalOrderStates));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }

        r = deleteOrderSteps(maxMainParentId);
        totalOrderSteps += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDER_STEPS, r.getDeletedTotal(), totalOrderSteps));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }
        if (deleteLogs) {
            r = deleteLogs(maxMainParentId);
            totalOrderLogs += r.getDeletedTotal();
            log.append(getDeleted(DBLayer.TABLE_HISTORY_LOGS, r.getDeletedTotal(), totalOrderLogs));

            if (isStopped()) {
                LOGGER.info(log.toString());
                return false;
            }
        }

        if (Range.ALL.equals(range)) {
            r = deleteOrders(maxMainParentId);
            totalOrders += r.getDeletedTotal();
            log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDERS, r.getDeletedTotal(), totalOrders));

            // order tags cleanup already performed - complete the order summary log line with this information
            if (Scope.MAIN.equals(scope)) {
                log.append(getDeleted(DBLayer.TABLE_HISTORY_ORDER_TAGS, totalOrderTags, totalOrderTags));
            }
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
        log = deleteControllers(eventId, log);
        log = deleteAgents(eventId, log);
        // getDbLayer().commit();

        LOGGER.info(log.toString());
    }

    protected StringBuilder deleteControllers(Long eventId, StringBuilder log) throws SOSHibernateException {
        // 1) delete controllers that no longer exist
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");
        hql.append("where readyEventId > 0 ");
        hql.append("and controllerId not in (");
        hql.append("select controllerId from ").append(DBLayer.DBITEM_INV_JS_INSTANCES);
        hql.append(")");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        int r = getDbLayer().getSession().executeUpdate(query);

        // 2) leaves 1 last row per controller
        Dialect d = getFactory().getDialect();
        String columnReid = SOSHibernate.quoteColumn(d, "READY_EVENT_ID");
        String columnCid = SOSHibernate.quoteColumn(d, "CONTROLLER_ID");

        hql = new StringBuilder("delete from ").append(DBLayer.TABLE_HISTORY_CONTROLLERS).append(" ");
        hql.append("where " + columnReid + " < :eventId ");
        if (getFactory().getDbms().equals(Dbms.MYSQL)) {
            hql.append("and (").append(columnReid + "," + columnCid + ") not in (");
            hql.append("  select tmp.meid,tmp.cid from(");
            hql.append("      select max(" + columnReid + ") as meid," + columnCid + " as cid ");
            hql.append("      from ").append(DBLayer.TABLE_HISTORY_CONTROLLERS).append(" ");
            hql.append("      group by ").append(columnCid);
            hql.append("   ) as tmp ");
            hql.append(")");
        } else {
            hql.append("and concat(").append(columnReid + "," + columnCid + ") not in (");
            hql.append("   select concat(max(" + columnReid + ")," + columnCid + ") ");
            hql.append("   from ").append(DBLayer.TABLE_HISTORY_CONTROLLERS).append(" ");
            hql.append("   group by ").append(columnCid);
            hql.append(")");
        }
        query = getDbLayer().getSession().createNativeQuery(hql.toString());
        query.setParameter("eventId", eventId);
        r += getDbLayer().getSession().executeUpdate(query);

        log.append("[").append(DBLayer.TABLE_HISTORY_CONTROLLERS).append("=").append(r).append("]");
        return log;
    }

    protected StringBuilder deleteAgents(Long eventId, StringBuilder log) throws SOSHibernateException {
        // 1) delete agents that no longer exist
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENTS).append(" ");
        hql.append("where concat(controllerId,agentId) not in (");
        hql.append("select concat(controllerId,agentId) from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
        hql.append(")");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        int r = getDbLayer().getSession().executeUpdate(query);

        // 2) leaves 1 last row per controller/agent
        Dialect d = getFactory().getDialect();
        String columnReid = SOSHibernate.quoteColumn(d, "READY_EVENT_ID");
        String columnCid = SOSHibernate.quoteColumn(d, "CONTROLLER_ID");
        String columnAid = SOSHibernate.quoteColumn(d, "AGENT_ID");

        hql = new StringBuilder("delete from ").append(DBLayer.TABLE_HISTORY_AGENTS).append(" ");
        hql.append("where " + columnReid + "  < :eventId ");
        switch (getFactory().getDbms()) {
        case MYSQL:
            hql.append("and (" + columnReid + "," + columnCid + "," + columnAid + ") not in (");
            hql.append("  select meid,cid,aid from (");
            hql.append("      select max(" + columnReid + ") as meid," + columnCid + " as cid," + columnAid + " as aid ");
            hql.append("      from ").append(DBLayer.TABLE_HISTORY_AGENTS).append(" ");
            hql.append("      group by " + columnCid + "," + columnAid);
            hql.append("   ) as tmp ");
            hql.append(")");
            break;
        case ORACLE:
            hql.append("and (" + columnReid + "||" + columnCid + "||" + columnAid + ") not in (");
            hql.append("  select (max(" + columnReid + ")||" + columnCid + "||" + columnAid + ") ");
            hql.append("  from ").append(DBLayer.TABLE_HISTORY_AGENTS).append(" ");
            hql.append("  group by " + columnCid + "," + columnAid);
            hql.append(")");
            break;
        default:
            hql.append("and concat(" + columnReid + "," + columnCid + "," + columnAid + ") not in (");
            hql.append("  select concat(max(" + columnReid + ")," + columnCid + "," + columnAid + ") ");
            hql.append("  from ").append(DBLayer.TABLE_HISTORY_AGENTS).append(" ");
            hql.append("  group by " + columnCid + "," + columnAid);
            hql.append(")");
            break;
        }

        query = getDbLayer().getSession().createNativeQuery(hql.toString());
        query.setParameter("eventId", eventId);
        r += getDbLayer().getSession().executeUpdate(query);

        log.append("[").append(DBLayer.TABLE_HISTORY_AGENTS).append("=").append(r).append("]");
        return log;
    }

    private void deleteNotStartedOrdersLogs(TaskDateTime datetime) {
        if (isStopped()) {
            return;
        }
        if (datetime == null || datetime.getDatetime() == null) {
            return;
        }
        Path dir = Paths.get(logDirTmpOrders).toAbsolutePath();
        String method = "deleteNotStartedOrdersLogs";
        String info = new StringBuilder().append(datetime.getAge().getConfigured()).append(" ").append(getDateTime(datetime.getDatetime()))
                .toString();
        LOGGER.info(String.format("[%s][%s][%s]%s", getIdentifier(), method, info, dir));
        if (Files.exists(dir)) {
            try {
                if (SOSPath.isDirectoryEmpty(dir)) {
                    LOGGER.info(String.format("[%s][%s][%s][skip]directory is empty", getIdentifier(), method, info));
                } else {
                    int i = 0;
                    BiPredicate<Path, BasicFileAttributes> predicate = (path, attributes) -> attributes.isRegularFile() && (datetime.getDatetime()
                            .getTime() > attributes.lastModifiedTime().toMillis());

                    try (Stream<Path> stream = Files.find(dir, 1, predicate)) {
                        for (Path p : stream.collect(Collectors.toList())) {
                            try {
                                String lm = getDateTime(SOSPath.getLastModified(p));
                                if (SOSPath.deleteIfExists(p)) {
                                    LOGGER.info(String.format("[%s][%s][%s][deleted][lastModified(UTC)=%s]%s", getIdentifier(), method, info, lm, p));
                                    i++;
                                }
                            } catch (Throwable e) {// in the same moment deleted by history
                            }
                        }
                    }
                    LOGGER.info(String.format("[%s][%s][%s][deleted][total]%s", getIdentifier(), method, info, i));
                }
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][%s][%s]%s", getIdentifier(), method, info, e.toString()), e);
            }
        } else {
            LOGGER.info(String.format("[%s][%s][%s][skip]directory not found", getIdentifier(), method, info));
        }
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
        String method = "deleteNotReferencedLogs";
        LOGGER.info(String.format("[%s][%s]%s", getIdentifier(), method, dir));
        if (Files.exists(dir)) {
            try {
                if (SOSPath.isDirectoryEmpty(dir)) {
                    LOGGER.info(String.format("[%s][%s][skip]directory is empty", getIdentifier(), method));
                } else {
                    tryOpenSession();
                    int i = 0;
                    try (Stream<Path> stream = Files.walk(dir)) {
                        for (Path p : stream.filter(f -> !f.equals(dir)).collect(Collectors.toList())) {
                            File f = p.toFile();
                            if (f.isDirectory()) {
                                try {
                                    Long id = Long.parseLong(f.getName());
                                    if (id > JocHistoryConfiguration.ID_NOT_STARTED_ORDER) {// id=0 is a temporary folder for not started orders - see
                                                                                            // config.getLogDirTmpOrders()
                                        if (!getDbLayer().mainOrderLogNotFinished(id)) {
                                            try {
                                                if (SOSPath.deleteIfExists(p)) {
                                                    LOGGER.info(String.format("[%s][%s][deleted]%s", getIdentifier(), method, p));
                                                    i++;
                                                }
                                            } catch (Throwable e) {// in the same moment deleted by history
                                            }
                                        }
                                    }
                                } catch (Throwable e) {
                                    LOGGER.info(String.format("[%s][%s][skip][non numeric]%s", getIdentifier(), method, p));
                                }
                            }
                        }
                    }
                    LOGGER.info(String.format("[%s][%s][deleted][total]%s", getIdentifier(), method, i));
                }
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][%s]%s", getIdentifier(), method, e.toString()), e);
            }
        } else {
            LOGGER.info(String.format("[%s][%s][skip]directory not found", getIdentifier(), method));
        }
    }

    private JocServiceTaskAnswerState cleanupLogs(Scope scope, Range range, TaskDateTime datetime) throws Exception {
        setQuotedColumns();

        String ageInfo = datetime.getAge().getConfigured();
        Date startTime = datetime.getDatetime();

        tryOpenSession();

        // based on the order start time
        Long maxMainParentId = getOrderMaxMainParentId(scope, range, startTime, ageInfo);
        if (maxMainParentId == null || maxMainParentId.intValue() == 0) {
            // not found in orders history - possibly due to setting of order history (already deleted)
            // check based on the order logs created
            maxMainParentId = getOrderLogsMaxMainParentId(scope, range, startTime, ageInfo);
            if (maxMainParentId == null || maxMainParentId.intValue() == 0) {
                return JocServiceTaskAnswerState.COMPLETED;
            }
        }

        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][");
        log.append(getScope(scope)).append(" ").append(getRange(range)).append("]");
        log.append("[").append(ageInfo).append(" ").append(getDateTime(startTime)).append("][maxMainParentId=" + maxMainParentId + "][deleted]");

        CleanupPartialResult r = deleteLogs(maxMainParentId);
        totalOrderLogs += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_HISTORY_LOGS, r.getDeletedTotal(), totalOrderLogs));

        LOGGER.info(log.toString());
        return r.getState();
    }

    private String getScope(Scope val) {
        return val.name().toLowerCase();
    }

    private String getRange(Range val) {
        return val.name().toLowerCase();
    }
}
