package com.sos.js7.order.initiator.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class DBLayerOrderVariables {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerOrderVariables.class);
    private static final String DBItemDailyPlanVariables = com.sos.joc.db.orders.DBItemDailyPlanVariables.class.getSimpleName();
    private static final String DBItemDailyPlan = com.sos.joc.db.orders.DBItemDailyPlanOrders.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerOrderVariables(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterOrderVariables resetFilter() {
        FilterOrderVariables filter = new FilterOrderVariables();
        return filter;
    }

    private String getWhere(FilterOrderVariables filter) {
        String where = " ";
        String and = " ";
        if (filter.getPlannedOrderId() != null) {
            where +=  " plannedOrderId = :plannedOrderId";
            and = " and ";
        } else {
            where = "v.plannedOrderId = p.id";
            if (filter.getPlannedOrderKey() != null && !filter.getPlannedOrderKey().isEmpty()) {
                where += and + " p.orderKey = :plannedOrderKey";
                and = " and ";
            }
        }
        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterOrderVariables filter, Query<T> query) {
        if (filter.getPlannedOrderKey() != null) {
            query.setParameter("plannedOrderKey", filter.getPlannedOrderKey());
        }
        if (filter.getPlannedOrderId() != null) {
            query.setParameter("plannedOrderId", filter.getPlannedOrderKey());
        }
        return query;
    }

    public List<com.sos.joc.db.orders.DBItemDailyPlanVariables> getOrderVariables(FilterOrderVariables filter, final int limit)
            throws SOSHibernateException {
        String q = "";
        if (filter.getPlannedOrderId() != null) {
            q = "from " + DBItemDailyPlanVariables + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        } else {
            q = "select v from " + DBItemDailyPlanVariables + " v, " + DBItemDailyPlan + " p " + getWhere(filter) + filter.getOrderCriteria() + filter
                    .getSortMode();
        }
        Query<com.sos.joc.db.orders.DBItemDailyPlanVariables> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

}