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
        try {
            TaskDateTime datetime = datetimes.get(0);
            LOGGER.info(String.format("[%s][%s][%s]start cleanup", getIdentifier(), datetime.getAge().getConfigured(), datetime.getZonedDatetime()));

            boolean run = true;
            while (run) {
                tryOpenSession();

                List<Long> r = getSubmissionIds(datetime);
                if (r == null || r.size() == 0) {
                    return JocServiceTaskAnswerState.COMPLETED;
                }
                if (isStopped()) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }

                if (askService()) {
                    getDbLayer().beginTransaction();
                    cleanupEntries(datetime, r);
                    getDbLayer().commit();
                } else {
                    getDbLayer().close();
                    waitFor(WAIT_INTERVAL_ON_BUSY);
                }
            }
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private void cleanupEntries(TaskDateTime datetime, List<Long> ids) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("]");

        // getDbLayer().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where orderId in (");
        hql.append("    select orderId from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("    where submissionHistoryId in (:ids)");
        hql.append(")");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        int r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalVariables += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_ORDER_VARIABLES, r, totalVariables));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return;
        }

        // getDbLayer().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_HISTORY).append(" ");
        hql.append("where orderId in (");
        hql.append("    select orderId from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("    where submissionHistoryId in (:ids)");
        hql.append(")");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalHistory += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_HISTORY, r, totalHistory));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return;
        }

        // getDbLayer().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where submissionHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalOrders += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_ORDERS, r, totalOrders));

        if (isStopped()) {
            LOGGER.info(log.toString());
            return;
        }

        // getDbLayer().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where id in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        // getDbLayer().commit();
        totalSubmissions += r;
        log.append(getDeleted(DBLayer.TABLE_DPL_SUBMISSIONS, r, totalSubmissions));

        LOGGER.info(log.toString());
    }

    private List<Long> getSubmissionIds(TaskDateTime datetime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where created < :created ");
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("created", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);

        int size = r.size();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s][%s]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_DPL_SUBMISSIONS,
                    size));

        } else {
            if (size == 0) {
                LOGGER.info(String.format("[%s][%s][%s]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_DPL_SUBMISSIONS,
                        size));
            }
        }
        return r;
    }

}
