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

public class CleanupTaskHistory extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskHistory.class);

    public CleanupTaskHistory(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        super(factory, service, batchSize);
    }

    @Override
    public JocServiceTaskAnswerState cleanup(Date date) throws SOSHibernateException {
        DBLayerCleanup dbLayer = null;
        try {

            dbLayer = new DBLayerCleanup(getFactory().openStatelessSession());
            dbLayer.getSession().setIdentifier(getIdentifier());

            cleanupControllersAndAgents(dbLayer, date);

            boolean runm = true;
            while (runm) {
                List<Long> rm = getMainOrderIds(dbLayer, date);
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

                    List<Long> rc = getChildOrderIds(dbLayer, rm);
                    if (rc != null && rc.size() > 0) {
                        if (!cleanupOrders(dbLayer, "childs", rc, false)) {
                            return JocServiceTaskAnswerState.UNCOMPLETED;
                        }
                    }
                    runc = false;
                }
                if (!cleanupOrders(dbLayer, "main", rm, true)) {
                    return JocServiceTaskAnswerState.UNCOMPLETED;
                }
            }
        } catch (Exception e) {
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

    private List<Long> getMainOrderIds(DBLayerCleanup dbLayer, Date date) throws SOSHibernateException {
        dbLayer.getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where startTime < :startTime ");
        hql.append("and parentId=0");
        Query<Long> query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameter("startTime", date);
        query.setMaxResults(getBatchSize());
        List<Long> r = dbLayer.getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s][main]found=%s", getIdentifier(), DBLayer.TABLE_HISTORY_ORDERS, r.size()));
        dbLayer.getSession().commit();
        return r;
    }

    private List<Long> getChildOrderIds(DBLayerCleanup dbLayer, List<Long> mainOrderIds) throws SOSHibernateException {
        dbLayer.getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where parentId in (:mainOrderIds)");
        Query<Long> query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("mainOrderIds", mainOrderIds);
        query.setMaxResults(getBatchSize());
        List<Long> r = dbLayer.getSession().getResultList(query);
        LOGGER.info(String.format("[%s][%s][childs]found=%s", getIdentifier(), DBLayer.TABLE_HISTORY_ORDERS, r.size()));
        dbLayer.getSession().commit();
        return r;
    }

    private boolean cleanupOrders(DBLayerCleanup dbLayer, String range, List<Long> orderIds, boolean deleteTmpLogs) throws SOSHibernateException {
        dbLayer.getSession().beginTransaction();
        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STATE).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        Query<?> query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        int r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_ORDER_STATES, r));
        dbLayer.getSession().commit();

        if (isStopped()) {
            return false;
        }

        dbLayer.getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_ORDER_STEPS, r));
        dbLayer.getSession().commit();

        if (isStopped()) {
            return false;
        }

        dbLayer.getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_LOG).append(" ");
        hql.append("where historyOrderId in (:orderIds)");
        query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_LOGS, r));
        dbLayer.getSession().commit();

        if (isStopped()) {
            return false;
        }

        if (deleteTmpLogs) {
            dbLayer.getSession().beginTransaction();
            hql = new StringBuilder("delete from ");
            hql.append(DBLayer.DBITEM_HISTORY_TEMP_LOG).append(" ");
            hql.append("where historyOrderMainParentId in (:orderIds)");
            query = dbLayer.getSession().createQuery(hql.toString());
            query.setParameterList("orderIds", orderIds);
            r = dbLayer.getSession().executeUpdate(query);
            LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_TEMP_LOGS, r));
            dbLayer.getSession().commit();

            if (isStopped()) {
                return false;
            }
        }

        dbLayer.getSession().beginTransaction();
        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where id in (:orderIds)");
        query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameterList("orderIds", orderIds);
        r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s][%s]deleted=%s", getIdentifier(), range, DBLayer.TABLE_HISTORY_ORDERS, r));
        dbLayer.getSession().commit();

        return true;
    }

    private void cleanupControllersAndAgents(DBLayerCleanup dbLayer, Date date) throws SOSHibernateException {
        Long eventId = new Long(date.getTime() * 1_000 + 999);

        dbLayer.getSession().beginTransaction();

        StringBuilder hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_CONTROLLER).append(" ");
        hql.append("where readyEventId < :eventId");
        Query<?> query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        int r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_HISTORY_CONTROLLERS, r));

        hql = new StringBuilder("delete from ");
        hql.append(DBLayer.DBITEM_HISTORY_AGENT).append(" ");
        hql.append("where readyEventId < :eventId");
        query = dbLayer.getSession().createQuery(hql.toString());
        query.setParameter("eventId", eventId);
        r = dbLayer.getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]deleted=%s", getIdentifier(), DBLayer.TABLE_HISTORY_AGENTS, r));

        dbLayer.getSession().commit();

    }
}
