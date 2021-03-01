package com.sos.joc.cleanup.model;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskDailyPlan extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskDailyPlan.class);

    public CleanupTaskDailyPlan(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(Date date) throws SOSHibernateException {
        try {
            boolean run = true;
            while (run) {
                getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                List<Long> r = getSubmissionIds(date);
                getDbLayer().close();

                if (r == null || r.size() == 0) {
                    return JocServiceTaskAnswerState.COMPLETED;
                }
                if (isStopped()) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }

                if (askService()) {
                    getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                    cleanup(r);
                    getDbLayer().close();
                } else {
                    waitFor(WAIT_INTERVAL_ON_BUSY);
                }
            }
        } catch (Exception e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private void cleanup(List<Long> ids) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_VARIABLES_DBITEM).append(" ");
        hql.append("where plannedOrderId in (");
        hql.append("    select id from ").append(DBLayer.DAILY_PLAN_ORDERS_DBITEM).append(" ");
        hql.append("    where submissionHistoryId in (:ids)");
        hql.append(")");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        int r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_VARIABLES_TABLE, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_HISTORY_DBITEM).append(" ");
        hql.append("where orderId in (");
        hql.append("    select orderId from ").append(DBLayer.DAILY_PLAN_ORDERS_DBITEM).append(" ");
        hql.append("    where submissionHistoryId in (:ids)");
        hql.append(")");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_HISTORY_TABLE, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_ORDERS_DBITEM).append(" ");
        hql.append("where submissionHistoryId in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_ORDERS_TABLE, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_SUBMISSIONS_DBITEM).append(" ");
        hql.append("where id in (:ids)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_SUBMISSIONS_TABLE, r));
        getDbLayer().getSession().commit();
    }

    private List<Long> getSubmissionIds(Date date) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DAILY_PLAN_SUBMISSIONS_DBITEM).append(" ");
        hql.append("where created < :created ");
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("created", date);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s]found=%s", getIdentifier(), DBLayer.DAILY_PLAN_SUBMISSIONS_TABLE, r.size()));
        getDbLayer().getSession().commit();
        return r;
    }

}
