package com.sos.js7.order.initiator.db;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.LockModeType;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.orders.DBItemDailyPlanOrder;
import com.sos.joc.db.orders.DBItemDailyPlanSubmission;
import com.sos.joc.db.orders.DBItemDailyPlanVariable;

public class DBLayerDailyPlanSubmissions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlanSubmissions.class);
    private static final int LOCK_TIMEOUT = 3000;
    private final SOSHibernateSession session;

    public DBLayerDailyPlanSubmissions(SOSHibernateSession session) {
        this.session = session;
    }

    public FilterDailyPlanSubmissions resetFilter() {
        FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
        filter.setControllerId("");
        return filter;
    }

    public int delete(FilterDailyPlanSubmissions filter) throws SOSHibernateException {
        int result = deleteOrderVariabless(filter);
        result += deleteOrders(filter);
        result += deleteSubmissions(filter);
        return result;
    }

    private int deleteOrderVariabless(FilterDailyPlanSubmissions filter) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLE).append(" ");
        hql.append("where plannedOrderId in (");
        hql.append("select id from ").append(DBLayer.DBITEM_DPL_ORDER).append(" ");
        hql.append("where submitted=false ");
        hql.append("and submissionHistoryId in (");
        hql.append("select id from " + DBLayer.DBITEM_DPL_SUBMISSION).append(" ");
        hql.append(getWhere(filter));
        hql.append(")");
        hql.append(")");

        Query<DBItemDailyPlanVariable> query = session.createQuery(hql.toString());
        bindParameters(filter, query);
        return tryDelete(query, "deleteOrderVariabless");
    }

    private int deleteOrders(FilterDailyPlanSubmissions filter) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER).append(" ");
        hql.append("where submitted=false ");
        hql.append("and submissionHistoryId in (");
        hql.append("select id from " + DBLayer.DBITEM_DPL_SUBMISSION).append(" ");
        hql.append(getWhere(filter));
        hql.append(")");

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        bindParameters(filter, query);
        return tryDelete(query, "deleteOrders");
    }

    private int deleteSubmissions(FilterDailyPlanSubmissions filter) throws SOSHibernateException {
        String hql = " from " + DBLayer.DBITEM_DPL_SUBMISSION + getWhere(filter);
        Query<DBItemDailyPlanSubmission> query = session.createQuery(hql);
        query = bindParameters(filter, query);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setHint("javax.persistence.lock.timeout", LOCK_TIMEOUT);
        session.getResultList(query);

        hql = "delete " + hql;
        query = session.createQuery(hql);
        bindParameters(filter, query);
        return session.executeUpdate(query);
    }

    private int tryDelete(Query<?> query, String caller) throws SOSHibernateException {
        try {
            return session.executeUpdate(query);
        } catch (SOSHibernateException e) {
            LOGGER.warn(String.format("[%s][failed][wait 1s and try again]%s", caller, e.toString()));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e1) {

            }
            return session.executeUpdate(query);
        }
    }

    private String getWhere(FilterDailyPlanSubmissions filter) {
        String where = "";
        String and = "";

        if (filter.getUserAccount() != null) {
            where += and + " userAccount = :userAccount";
            and = " and ";
        }
        if (filter.getDateFor() != null) {
            where += and + " submissionForDate = :dateFor";
            and = " and ";
        }

        if (filter.getDateFrom() != null) {
            where += and + " submissionForDate >= :dateFrom";
            and = " and ";
        }

        if (filter.getDateTo() != null) {
            where += and + " submissionForDate <= :dateTo";
            and = " and ";
        }

        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            where += and + " controllerId = :controllerId";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterDailyPlanSubmissions filter, Query<T> query) {

        if (filter.getUserAccount() != null) {
            query.setParameter("userAccount", filter.getUserAccount());
        }
        if (filter.getDateFor() != null) {
            query.setParameter("dateFor", filter.getDateFor());
        }
        if (filter.getDateFrom() != null) {
            query.setParameter("dateFrom", filter.getDateFrom());
        }
        if (filter.getDateTo() != null) {
            query.setParameter("dateTo", filter.getDateTo());
        }

        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            query.setParameter("controllerId", filter.getControllerId());
        }

        return query;

    }

    public List<DBItemDailyPlanSubmission> getDailyPlanSubmissions(FilterDailyPlanSubmissions filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBLayer.DBITEM_DPL_SUBMISSION + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlanSubmission> query = session.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return session.getResultList(query);
    }

    public int deleteSubmission(FilterDailyPlanSubmissions filter) throws SOSHibernateException {
        String hql = "delete from " + DBLayer.DBITEM_DPL_SUBMISSION + getWhere(filter);
        Query<DBItemDailyPlanSubmission> query = session.createQuery(hql);
        bindParameters(filter, query);
        int row = session.executeUpdate(query);
        return row;
    }

    public void storeSubmission(DBItemDailyPlanSubmission dbItemDailyPlanSubmissions, Date submissionTime) throws SOSHibernateException {
        dbItemDailyPlanSubmissions.setCreated(submissionTime);
        session.save(dbItemDailyPlanSubmissions);
    }

}