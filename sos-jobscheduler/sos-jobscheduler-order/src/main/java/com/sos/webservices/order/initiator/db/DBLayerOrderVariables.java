package com.sos.webservices.order.initiator.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class DBLayerOrderVariables {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerOrderVariables.class);
    private static final String DBItemDailyPlanVariables = com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables.class.getSimpleName();
    private static final String DBItemDailyPlan = com.sos.jobscheduler.db.orders.DBItemDailyPlan.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerOrderVariables(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterOrderVariables resetFilter() {
        FilterOrderVariables filter = new FilterOrderVariables();
        return filter;
    }

    private String getWhere(FilterOrderVariables filter) {
        String where = "v.plannedOrderId = p.id";
        String and = " and ";
        if (filter.getPlannedOrderId() != null && !filter.getPlannedOrderId().isEmpty()) {
            where += and + " p.orderKey = :plannedOrderId";
            and = " and ";
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterOrderVariables filter, Query<T> query) {
        if (filter.getPlannedOrderId() != null) {
            query.setParameter("orderId", filter.getPlannedOrderId());
        }
        return query;
    }

    public List<com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables> getOrderVariables(FilterOrderVariables filter, final int limit)
            throws SOSHibernateException {
        String q = "from " + DBItemDailyPlanVariables + " v, " + DBItemDailyPlan + " p " + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }
}