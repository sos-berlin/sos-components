package com.sos.js7.order.initiator.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DBLayerOrderTemplates {

    private static final String DBItemInventoryConfiguration = com.sos.joc.db.inventory.DBItemInventoryConfiguration.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public DBLayerOrderTemplates(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterOrderTemplates resetFilter() {
        FilterOrderTemplates filter = new FilterOrderTemplates();
        filter.setControllerId("");
        return filter;
    }

    private String getWhere(FilterOrderTemplates filter) {
        String where = " type = " + ConfigurationType.ORDER.intValue();
        String and = " and ";

        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
      //      where += and + " controllerId = :controllerId";
      //      and = " and ";
        }

        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            if (filter.getRecursive()) {
                where += and + " folder like %:folder";
            } else {
                where += and + " folder = :folder";
            }
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterOrderTemplates filter, Query<T> query) {

        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
          //  query.setParameter("controllerId", filter.getControllerId());
        }

        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            query.setParameter("folder", filter.getFolder());
        }

        return query;

    }

    public List<DBItemInventoryConfiguration> getOrderTemplates(FilterOrderTemplates filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemInventoryConfiguration + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemInventoryConfiguration> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

}