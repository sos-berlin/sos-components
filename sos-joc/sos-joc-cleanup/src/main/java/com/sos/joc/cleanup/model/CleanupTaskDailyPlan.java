package com.sos.joc.cleanup.model;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskDailyPlan extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskDailyPlan.class);

    private int totalVariables = 0;
    private int totalHistory = 0;
    private int totalOrders = 0;
    private int totalSubmissions;

    public CleanupTaskDailyPlan(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        JocServiceTaskAnswerState state = JocServiceTaskAnswerState.COMPLETED;
        try {
            TaskDateTime datetime = datetimes.get(0);
            LOGGER.info(String.format("[%s][%s][%s]start cleanup", getIdentifier(), datetime.getAge().getConfigured(), datetime.getZonedDatetime()));

            boolean run = true;
            while (run) {
                if (isStopped()) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }

                if (askService()) {
                    tryOpenSession();

                    state = cleanupEntries(datetime);
                    run = false;
                } else {
                    getDbLayer().close();
                    waitFor(WAIT_INTERVAL_ON_BUSY);
                }
            }
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            close();
        }
        return state;
    }

    private JocServiceTaskAnswerState cleanupEntries(TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder submissionIdsStatement = getSubmissionIdsStatement();

        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("]");

        getDbLayer().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where orderId in (");
        hql.append("    select orderId from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("    where submissionHistoryId in (").append(submissionIdsStatement).append(")");
        hql.append(")");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("created", datetime.getDatetime());
        int r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().commit();
        totalVariables += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_ORDER_VARIABLES, r, totalVariables));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return JocServiceTaskAnswerState.UNCOMPLETED;
        }

        getDbLayer().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_HISTORY).append(" ");
        hql.append("where orderId in (");
        hql.append("    select orderId from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("    where submissionHistoryId in (").append(submissionIdsStatement).append(")");
        hql.append(")");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("created", datetime.getDatetime());
        r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().commit();
        totalHistory += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_HISTORY, r, totalHistory));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return JocServiceTaskAnswerState.UNCOMPLETED;
        }

        getDbLayer().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where submissionHistoryId in (").append(submissionIdsStatement).append(")");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("created", datetime.getDatetime());
        r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().commit();
        totalOrders += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_ORDERS, r, totalOrders));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return JocServiceTaskAnswerState.UNCOMPLETED;
        }

        getDbLayer().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where created < :created ");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("created", datetime.getDatetime());
        r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().commit();
        totalSubmissions += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_SUBMISSIONS, r, totalSubmissions));

        LOGGER.info(log.toString());
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private StringBuilder getSubmissionIdsStatement() {
        StringBuilder hql = new StringBuilder("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where created < :created ");
        return hql;
    }
}
