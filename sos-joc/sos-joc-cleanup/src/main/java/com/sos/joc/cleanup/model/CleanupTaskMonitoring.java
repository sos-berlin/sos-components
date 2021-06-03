package com.sos.joc.cleanup.model;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.db.DBLayer;

public class CleanupTaskMonitoring extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskMonitoring.class);

    public CleanupTaskMonitoring(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(List<TaskDateTime> datetimes) throws Exception {
        try {
            TaskDateTime datetime = datetimes.get(0);
            // TMP - only MYSQL, see MonitoringService
            if (!SOSHibernateFactory.Dbms.MYSQL.equals(getFactory().getDbms())) {
                LOGGER.info(String.format("[%s][%s][%s][skip]not implemented yet for %s", getIdentifier(), datetime.getAge().getConfigured(), datetime
                        .getZonedDatetime(), getFactory().getDbms()));
                return JocServiceTaskAnswerState.COMPLETED;
            }

            LOGGER.info(String.format("[%s][%s][%s]start cleanup", getIdentifier(), datetime.getAge().getConfigured(), datetime.getZonedDatetime()));

            return cleanupOrders(datetime);
        } catch (Throwable e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
    }

    private JocServiceTaskAnswerState cleanupOrders(TaskDateTime datetime) throws SOSHibernateException {
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
                    if (!cleanupOrders(datetime, "childs", rc)) {
                        return JocServiceTaskAnswerState.UNCOMPLETED;
                    }
                }
                getDbLayer().close();
                runc = false;
            }
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            if (!cleanupOrders(datetime, "main", rm)) {
                return JocServiceTaskAnswerState.UNCOMPLETED;
            }
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private List<Long> getMainOrderIds(TaskDateTime datetime) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select historyId from ");
        hql.append(DBLayer.DBITEM_MONITORING_ORDER).append(" ");
        hql.append("where startTime < :startTime ");
        hql.append("and parentId=0");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", datetime.getDatetime());
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        LOGGER.info(String.format("[%s][%s][%s][main]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_MONITORING_ORDERS, r
                .size()));
        return r;
    }

    private List<Long> getChildOrderIds(TaskDateTime datetime, List<Long> mainOrderIds) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select historyId from ");
        hql.append(DBLayer.DBITEM_MONITORING_ORDER).append(" ");
        hql.append("where parentId in (:mainOrderIds)");

        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("mainOrderIds", mainOrderIds);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        getDbLayer().getSession().commit();

        LOGGER.info(String.format("[%s][%s][%s][childs]found=%s", getIdentifier(), datetime.getAge().getConfigured(), DBLayer.TABLE_MONITORING_ORDERS,
                r.size()));
        return r;
    }

    private boolean cleanupOrders(TaskDateTime datetime, String range, List<Long> orderIds) throws SOSHibernateException {
        StringBuilder log = new StringBuilder();
        log.append("[").append(getIdentifier()).append("][deleted][").append(datetime.getAge().getConfigured()).append("][").append(range).append(
                "]");

        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MONITORING_ORDER_STEP).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().getSession().commit();
        log.append("[").append(DBLayer.TABLE_MONITORING_ORDER_STEPS).append("=").append(r).append("]");

        if (isStopped()) {
            LOGGER.info(log.toString());
            return false;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_MONITORING_ORDER).append(" ");
        hql.append("where historyId in (:orderIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = getDbLayer().getSession().executeUpdate(query);
        getDbLayer().getSession().commit();
        log.append("[").append(DBLayer.TABLE_MONITORING_ORDERS).append("=").append(r).append("]");

        LOGGER.info(log.toString());
        return true;
    }
}
