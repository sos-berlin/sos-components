package com.sos.joc.dailyplan.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;

public class DBLayerDeployHistory {

    private static final String DBItemDeploymentHistory = com.sos.joc.db.deployment.DBItemDeploymentHistory.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDeployHistory(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterDailyPlanHistory resetFilter() {
        FilterDailyPlanHistory filter = new FilterDailyPlanHistory();
        return filter;
    }

    private String getWhere(FilterDeployHistory filter) {
        String where = " ";
        String and = " ";

        if (filter.getType() != null) {
            where += and + " type = :type";
            and = " and ";
        }

        if (filter.getState() != null) {
            where += and + " state = :state";
            and = " and ";
        }

        if (filter.getName() != null) {
            where += and + " name = :name";
            and = " and ";
        }
    
        if (filter.getListOfControllerIds() != null && filter.getListOfControllerIds().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfControllerIds(), "controllerId");
            and = " and ";
        }
        
        if (filter.getInventoryId() != null) {
            where += and + " inventoryConfigurationId = :inventoryConfigurationId";
            and = " and ";
        }
        
        if (filter.getOperation() != null) {
            where += and + " operation = :operation";
            and = " and ";
        }
         
        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterDeployHistory filter, Query<T> query) {
        if (filter.getType() != null) {
            query.setParameter("type", filter.getType().intValue());
        }
        if (filter.getName() != null) {
            query.setParameter("name", filter.getName());
        }
        if (filter.getState() != null) {
            query.setParameter("state", filter.getState().value());
        }
        if (filter.getOperation() != null) {
            query.setParameter("operation", filter.getOperation());
        }
        if (filter.getInventoryId() != null) {
            query.setParameter("inventoryConfigurationId", filter.getInventoryId());
        }
      
        return query;
    }

    public List<DBItemDeploymentHistory> getDeployments(FilterDeployHistory filter, final int limit) throws SOSHibernateException {

        String q = "from " + DBItemDeploymentHistory + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDeploymentHistory> query = sosHibernateSession.createQuery(q);
      
        bindParameters(filter,query);
        
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        return sosHibernateSession.getResultList(query);
    }

}