package com.sos.joc.cleanup.model;

import java.util.List;

import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cleanup.helper.CleanupPartialResult;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskReporting extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskReporting.class);

    private int totalReports = 0;
    private int totalReportRuns = 0;

    private String columnQuotedId;
    private String columnQuotedCreated;
    private String columnQuotedModified;

    public CleanupTaskReporting(JocClusterHibernateFactory factory, int batchSize, String identifier) {
        super(factory, batchSize, identifier);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {

        setQuotedColumns();

        try {
            TaskDateTime datetime = datetimes.get(0);
            tryOpenSession();

            cleanupReports(datetime);
            cleanupReportRuns(datetime);

            getDbLayer().close();
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private void cleanupReports(TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][reports]");
        log.append("[").append(datetime.getAge().getConfigured()).append(" ").append(getDateTime(datetime.getDatetime())).append("][deleted]");

        CleanupPartialResult r = deleteEntries(datetime, DBLayer.TABLE_REPORTS, columnQuotedCreated);
        totalReports += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_REPORTS, r.getDeletedTotal(), totalReports));
        LOGGER.info(log.toString());
    }

    private void cleanupReportRuns(TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder log = new StringBuilder("[").append(getIdentifier()).append("][reportRuns]");
        log.append("[").append(datetime.getAge().getConfigured()).append(" ").append(getDateTime(datetime.getDatetime())).append("][deleted]");

        CleanupPartialResult r = deleteEntries(datetime, DBLayer.TABLE_REPORT_RUNS, columnQuotedModified);
        totalReportRuns += r.getDeletedTotal();
        log.append(getDeleted(DBLayer.TABLE_REPORT_RUNS, r.getDeletedTotal(), totalReportRuns));
        LOGGER.info(log.toString());
    }

    private CleanupPartialResult deleteEntries(TaskDateTime datetime, String table, String column) throws SOSHibernateException {
        CleanupPartialResult r = new CleanupPartialResult(table);
        r.addParameter("date", datetime.getDatetime());

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

    private void setQuotedColumns() {
        Dialect d = getFactory().getDialect();
        columnQuotedId = SOSHibernate.quoteColumn(d, "ID");
        columnQuotedCreated = SOSHibernate.quoteColumn(d, "CREATED");
        columnQuotedModified = SOSHibernate.quoteColumn(d, "MODIFIED");
    }
}
