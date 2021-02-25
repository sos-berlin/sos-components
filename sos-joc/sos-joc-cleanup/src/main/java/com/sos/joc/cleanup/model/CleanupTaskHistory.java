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

public class CleanupTaskHistory extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskHistory.class);

    public CleanupTaskHistory(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(Date date) throws SOSHibernateException {
        try {
            getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
            cleanupControllersAndAgents(date);
            getDbLayer().close();

            boolean runm = true;
            while (runm) {
                getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                List<Long> rm = getMainOrderIds(date);
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
                    List<Long> rc = getChildOrderIds(rm);
                    if (rc != null && rc.size() > 0) {
                        if (!cleanupOrders("childs", rc, false)) {
                            return JocServiceTaskAnswerState.UNCOMPLETED;
                        }
                    }
                    getDbLayer().close();
                    runc = false;
                }
                getDbLayer().setSession(getFactory().openStatelessSession(getIdentifier()));
                if (!cleanupOrders("main", rm, true)) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }
                getDbLayer().close();
            }
        } catch (Exception e) {
            getDbLayer().rollback();
            throw e;
        } finally {
            getDbLayer().close();
        }
        return JocServiceTaskAnswerState.COMPLETED;
    }

    private List<Long> getMainOrderIds(Date date) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where startTime < :startTime ");
        hql.append("and parentId=0");
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("startTime", date);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s][main]found=%s", getIdentifier(), DBLayer.TABLE_HISTORY_ORDERS, r.size()));
        getDbLayer().getSession().commit();
        return r;
    }

    private List<Long> getChildOrderIds(List<Long> mainOrderIds) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where parentId in (:mainOrderIds)");
        Query<Long> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("mainOrderIds", mainOrderIds);
        query.setMaxResults(getBatchSize());
        List<Long> r = getDbLayer().getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s][childs]found=%s", getIdentifier(), DBLayer.TABLE_HISTORY_ORDERS, r.size()));
        getDbLayer().getSession().commit();
        return r;
    }

    private boolean cleanupOrders(String range, List<Long> orderIds, boolean deleteTmpLogs) throws SOSHibernateException {
        getDbLayer().getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STATE).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_ORDER_STATES, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return false;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_ORDER_STEPS, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return false;
        }

        getDbLayer().getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_LOG).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_LOGS, r));
        getDbLayer().getSession().commit();

        if (isStopped()) {
            return false;
        }

        if (deleteTmpLogs) {
            getDbLayer().getSession().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_TEMP_LOG).append(" ");
            hql.append("where historyOrderMainParentId in (:orderIds)");
            query = getDbLayer().getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            r = getDbLayer().getSession().executeUpdate(query);
            LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_TEMP_LOGS, r));
            getDbLayer().getSession().commit();

            if (isStopped()) {
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
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_ORDERS, r));
        getDbLayer().getSession().commit();

        return true;
    }

    private void cleanupControllersAndAgents(Date date) throws SOSHibernateException {
        Long eventId = new Long(date.getTime() * 1_000 + 999);

        getDbLayer().getSession().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where readyEventId < :eventId");
        Query<?> query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        int r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_HISTORY_CONTROLLERS, r));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId < :eventId");
        query = getDbLayer().getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        r = getDbLayer().getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_HISTORY_AGENTS, r));

        getDbLayer().getSession().commit();

    }
}
