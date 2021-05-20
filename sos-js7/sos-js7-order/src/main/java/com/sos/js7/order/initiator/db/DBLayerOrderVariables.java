package com.sos.js7.order.initiator.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissions;

public class DBLayerOrderVariables {

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
        if ((filter.getVariableName() != null) && (!"".equals(filter.getVariableName()))){
            where += " variableName = :variableName";
            and = " and ";
        }
        if (filter.getPlannedOrderId() != null) {
            where += and + " plannedOrderId = :plannedOrderId";
            and = " and ";
        } else {
            where = "v.plannedOrderId = p.id";
            and = " and ";
            if (filter.getOrderId() != null && !filter.getOrderId().isEmpty()) {
                where += and + " p.orderId = :orderId";
                and = " and ";
            }
        }
        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterOrderVariables filter, Query<T> query) {
        if (filter.getOrderId() != null) {
            query.setParameter("orderId", filter.getOrderId());
        }
        if (filter.getPlannedOrderId() != null) {
            query.setParameter("plannedOrderId", filter.getPlannedOrderId());
        }
        if ((filter.getVariableName() != null) && (!"".equals(filter.getVariableName()))){
            query.setParameter("variableName", filter.getVariableName());
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

    public int delete(FilterOrderVariables filter) throws SOSHibernateException {
        int row = 0;
        String hql = "delete from " + DBItemDailyPlanVariables + getWhere(filter);
        Query<DBItemDailyPlanSubmissions> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);
        row = row + sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int update(Long oldId, Long newId) throws SOSHibernateException {
        int row = 0;
        String hql = "update  " + DBItemDailyPlanVariables + " set plannedOrderId=:newId where plannedOrderId = :oldId";
        Query<DBItemDailyPlanSubmissions> query = sosHibernateSession.createQuery(hql);
        query.setParameter("newId", newId);
        query.setParameter("oldId", oldId);
        row = row + sosHibernateSession.executeUpdate(query);
        return row;
    }
}