package com.sos.js7.order.initiator.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissions;

public class DBLayerDailyPlanSubmissions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlanSubmissions.class);
    private static final String DBItemDailyPlanSubmissions = DBItemDailyPlanSubmissions.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDailyPlanSubmissions(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterDailyPlanSubmissions resetFilter() {
        FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
        filter.setControllerId("");
        return filter;
    }

    public int delete(FilterDailyPlanSubmissions filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlanSubmissions + getWhere(filter);
        Query<DBItemDailyPlanSubmissions> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
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

    public List<DBItemDailyPlanSubmissions> getDailyPlanSubmissions(FilterDailyPlanSubmissions filter, final int limit)
            throws SOSHibernateException {
        String q = "from " + DBItemDailyPlanSubmissions + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlanSubmissions> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public int deletePlan(FilterDailyPlanSubmissions filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlanSubmissions + getWhere(filter);
        Query<DBItemDailyPlanSubmissions> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void storePlan(DBItemDailyPlanSubmissions dbItemDailyPlanSubmissions) throws SOSHibernateException {
        dbItemDailyPlanSubmissions.setCreated(new Date());
        sosHibernateSession.save(dbItemDailyPlanSubmissions);
    }

}