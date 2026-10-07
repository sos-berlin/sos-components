package com.sos.joc.cleanup.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceConfiguration.Age;
import com.sos.joc.cleanup.CleanupServiceConfiguration.ForceCleanup;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cleanup.helper.CleanupPartialResult;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.state.JocClusterServiceTaskState;

public class CleanupTaskDailyPlan extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskDailyPlan.class);

    private int totalVariables = 0;
    private int totalHistory = 0;
    private int totalOrders = 0;
    private int totalSubmissions;

    private String columnQuotedId;
    private String columnQuotedDailyPlanDate;
    private String columnQuotedSubmissionForDate;

    private String columnQuotedOrderID;
    private String columnQuotedSubmissionHistoryId;

    public CleanupTaskDailyPlan(JocClusterHibernateFactory factory, IJocActiveMemberService service, int batchSize, ForceCleanup forceCleanup) {
        super(factory, service, batchSize, forceCleanup);
    }

    @Override
    public JocClusterServiceTaskState cleanup(List<TaskDateTime> datetimes) throws Exception {
        setQuotedColumns();

        JocClusterServiceTaskState state = null;
        try {
            TaskDateTime datetime = datetimes.get(0);
            Age age = datetime.getAge();
            LocalDateTime date = toDailyPlanDate(datetime.getDatetime());

            tryOpenSession();

            state = cleanupHistory(age, date);
            if (isCompleted(state)) {
                state = cleanupSubmissions(age, date);
                if (isCompleted(state)) {
                    state = cleanupOrders();
                    if (isCompleted(state)) {
                        state = cleanupOrderVariables();
                    }
                }
            }

            getDbLayer().close();
        } catch (Throwable e) {
            throw e;
        } finally {
            close();
        }
        return state;
    }

    private JocClusterServiceTaskState cleanupHistory(Age age, LocalDateTime datetime) throws Exception {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("]");
        log.append("[").append(age.getConfigured()).append(" ").append(getDateTime(datetime)).append("][deleted]");

        CleanupPartialResult r = deleteEntries(datetime, DBLayer.TABLE_DPL_HISTORY, columnQuotedDailyPlanDate);
        totalHistory += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_DPL_HISTORY, r.getDeletedTotal(), totalHistory));
        LOGGER.info(log.toString());
        return r.getState();
    }

    private JocClusterServiceTaskState cleanupSubmissions(Age age, LocalDateTime datetime) throws Exception {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("]");
        log.append("[").append(age.getConfigured()).append(" ").append(getDateTime(datetime)).append("][deleted]");

        CleanupPartialResult r = deleteEntries(datetime, DBLayer.TABLE_DPL_SUBMISSIONS, columnQuotedSubmissionForDate);
        totalSubmissions += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_DPL_SUBMISSIONS, r.getDeletedTotal(), totalSubmissions));
        LOGGER.info(log.toString());
        return r.getState();
    }

    private JocClusterServiceTaskState cleanupOrders() throws Exception {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][deleted]");

        CleanupPartialResult r = deleteNotExistsEntries(DBLayer.TABLE_DPL_ORDERS, columnQuotedOrderID, columnQuotedSubmissionHistoryId,
                DBLayer.TABLE_DPL_SUBMISSIONS, columnQuotedId);
        totalOrders += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_DPL_ORDERS, r.getDeletedTotal(), totalOrders));
        LOGGER.info(log.toString());
        return r.getState();
    }

    private JocClusterServiceTaskState cleanupOrderVariables() throws Exception {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][deleted]");

        CleanupPartialResult r = deleteNotExistsEntries(DBLayer.TABLE_DPL_ORDER_VARIABLES, columnQuotedOrderID, columnQuotedOrderID,
                DBLayer.TABLE_DPL_ORDERS, columnQuotedOrderID);
        totalVariables += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_DPL_ORDER_VARIABLES, r.getDeletedTotal(), totalVariables));
        LOGGER.info(log.toString());
        return r.getState();
    }

    private CleanupPartialResult deleteEntries(LocalDateTime datetime, String table, String column) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(table);
        r.addParameter("date", datetime);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(table).append(" ");
        if (isPGSQL()) {
            sql.append("where ").append(columnQuotedId).append(" in (");
            sql.append("select ").append(columnQuotedId).append(" from ").append(table).append(" ");
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

    private CleanupPartialResult deleteNotExistsEntries(String table, String tableColumn, String tableReferenceColumn, String referenceTable,
            String referenceTableColumn) throws SOSHibernateException {

        CleanupPartialResult r = new CleanupPartialResult(table);

        StringBuilder sql = new StringBuilder("delete ");
        sql.append(getLimitTop());
        sql.append("from ").append(table).append(" ");

        if (isPGSQL()) {
            sql.append("where ").append(tableColumn).append(" in (");
            sql.append("select a.").append(tableColumn).append(" ");
            sql.append("from ").append(table).append(" a ");
            sql.append("where not exists (");
            sql.append("select 1 from ").append(referenceTable).append(" b ");
            sql.append("where a.").append(tableReferenceColumn).append(" = b.").append(referenceTableColumn);
            sql.append(") ");
            sql.append("limit ").append(getBatchSize());
            sql.append(")");
        } else {
            sql.append("where ").append(tableColumn).append(" in (");
            sql.append("select x.").append(tableColumn).append(" ");
            sql.append("from (");
            sql.append("select a.").append(tableColumn).append(" ");
            sql.append("from ").append(table).append(" a ");
            sql.append("where not exists (");
            sql.append("select 1 from ").append(referenceTable).append(" b ");
            sql.append("where a.").append(tableReferenceColumn).append(" = b.").append(referenceTableColumn);
            sql.append(") ");
            sql.append(getLimitWhere());
            sql.append(") x");
            sql.append(")");

        }

        r.run(this, sql);
        return r;
    }

    private void setQuotedColumns() {
        Dialect d = getFactory().getDialect();
        columnQuotedId = SOSHibernate.quoteColumn(d, "ID");
        columnQuotedDailyPlanDate = SOSHibernate.quoteColumn(d, "DAILY_PLAN_DATE");
        columnQuotedSubmissionForDate = SOSHibernate.quoteColumn(d, "SUBMISSION_FOR_DATE");

        columnQuotedOrderID = SOSHibernate.quoteColumn(d, "ORDER_ID");
        columnQuotedSubmissionHistoryId = SOSHibernate.quoteColumn(d, "SUBMISSION_HISTORY_ID");

    }

    private LocalDateTime toDailyPlanDate(LocalDateTime date) {
        return date.with(LocalTime.MIDNIGHT);
    }

}
