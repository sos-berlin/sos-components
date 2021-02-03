package com.sos.js7.order.initiator.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;

public class DBLayerDailyPlanHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlanHistory.class);
    private static final String DBItemDailyPlanHistory = DBItemDailyPlanHistory.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDailyPlanHistory(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterDailyPlanHistory resetFilter() {
        FilterDailyPlanHistory filter = new FilterDailyPlanHistory();
        filter.setControllerId("");
        return filter;
    }

    private String getWhere(FilterDailyPlanHistory filter) {
        String where = "";
        String and = "";

        if (filter.getDailyPlanDate() != null) {
            where += and + " dailyPlanDate = :dailyPlanDate";
            and = " and ";
        }

        if (filter.getDailyPlanDateFrom() != null) {
            where += and + " dailyPlanDate >= :dailyPlanDateFrom";
            and = " and ";
        }

        if (filter.getDailyPlanDateTo() != null) {
            where += and + " dailyPlanDate <= :dailyPlanDateTo";
            and = " and ";
        }

        if (filter.getSubmitted() != null) {
            where += and + " submitted = :submitted";
            and = " and ";
        }

        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            where += and + " controllerId = :controllerId";
            and = " and ";
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += and + " orderId = :orderId";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterDailyPlanHistory filter, Query<T> query) {

        if (filter.getDailyPlanDate() != null) {
            query.setParameter("dailyPlanDate", filter.getDailyPlanDate());
        }
        if (filter.getDailyPlanDateFrom() != null) {
            query.setParameter("dailyPlanDateFrom", filter.getDailyPlanDateFrom());
        }
        if (filter.getDailyPlanDateTo() != null) {
            query.setParameter("dailyPlanDateTo", filter.getDailyPlanDateTo());
        }

        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            query.setParameter("controllerId", filter.getControllerId());
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }
        
        if (filter.getSubmitted() != null) {
            query.setParameter("submitted", filter.getSubmitted());
        }

        return query;

    }

    public List<DBItemDailyPlanHistory> getDailyPlanHistory(FilterDailyPlanHistory filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlanHistory + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlanHistory> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public void storeDailyPlanHistory(DBItemDailyPlanHistory dbItemDailyPlanHistory) throws SOSHibernateException {
        sosHibernateSession.save(dbItemDailyPlanHistory);
    }

}