package com.sos.joc.cleanup.model;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.db.DBLayerCleanup;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskDeployment extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskDeployment.class);

    public CleanupTaskDeployment(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(Date date) throws SOSHibernateException {
        DBLayerCleanup dbLayer = null;
        try {

            dbLayer = new DBLayerCleanup(getFactory().openStatelessSession());
            dbLayer.getSession().setIdentifier(getIdentifier());

            boolean run = true;
            while (run) {
                List<Long> r = getSubmissionIds(dbLayer, date);
                if (r == null || r.size() == 0) {
                    return JocServiceTaskAnswerState.COMPLETED;
                }
                if (isStopped()) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }
                if (askService()) {
                    cleanup(dbLayer, r);
                } else {
                    waitFor(WAIT_INTERVAL_ON_BUSY);
                }
            }
        } catch (SOSHibernateException e) {
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (Throwable ex) {
                }
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private void cleanup(DBLayerCleanup dbLayer, List<Long> ids) throws SOSHibernateException {
        dbLayer.getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_VARIABLES_DBITEM).append(" ");
        hql.append("where plannedOrderId in (");
        hql.append("    select id from ").append(DBLayer.DAILY_PLAN_ORDERS_DBITEM).append(" ");
        hql.append("    where submissionHistoryId in (:ids)");
        hql.append(")");
        Query<?> query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        int r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_VARIABLES_TABLE, r));
        dbLayer.getSession().commit();

        if (isStopped()) {
            return;
        }

        dbLayer.getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_HISTORY_DBITEM).append(" ");
        hql.append("where orderId in (");
        hql.append("    select orderId from ").append(DBLayer.DAILY_PLAN_ORDERS_DBITEM).append(" ");
        hql.append("    where submissionHistoryId in (:ids)");
        hql.append(")");
        query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_HISTORY_TABLE, r));
        dbLayer.getSession().commit();

        if (isStopped()) {
            return;
        }

        dbLayer.getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_ORDERS_DBITEM).append(" ");
        hql.append("where submissionHistoryId in (:ids)");
        query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_ORDERS_TABLE, r));
        dbLayer.getSession().commit();

        if (isStopped()) {
            return;
        }

        dbLayer.getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DAILY_PLAN_SUBMISSIONS_DBITEM).append(" ");
        hql.append("where id in (:ids)");
        query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("ids", ids);
        r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.DAILY_PLAN_SUBMISSIONS_TABLE, r));
        dbLayer.getSession().commit();
    }

    private List<Long> getSubmissionIds(DBLayerCleanup dbLayer, Date date) throws SOSHibernateException {
        dbLayer.getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DAILY_PLAN_SUBMISSIONS_DBITEM).append(" ");
        hql.append("where created < :created ");
        Query<Long> query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameter("created", date);
        query.setMaxResults(getBatchSize());
        List<Long> r = dbLayer.getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s]found=%s", getIdentifier(), DBLayer.DAILY_PLAN_SUBMISSIONS_TABLE, r.size()));
        dbLayer.getSession().commit();
        return r;
    }

}
