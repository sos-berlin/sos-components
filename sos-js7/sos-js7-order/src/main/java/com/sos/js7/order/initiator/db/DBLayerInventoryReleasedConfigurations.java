package com.sos.js7.order.initiator.db;

import java.io.IOException;
import java.util.List;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class DBLayerInventoryReleasedConfigurations {

    private static final String DBItemInventoryReleasedConfiguration = com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration.class
            .getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public DBLayerInventoryReleasedConfigurations(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterInventoryReleasedConfigurations resetFilter() {
        FilterInventoryReleasedConfigurations filter = new FilterInventoryReleasedConfigurations();
        return filter;
    }

    private String getWhere(FilterInventoryReleasedConfigurations filter) {
        String where = " ";
        String and = " ";

        if (filter.getName() != null && !filter.getName().isEmpty()) {
            where += " name = :name";
            and = " and ";
        }

        if (filter.getId() != null) {
            where += " id = :id";
            and = " and ";
        }

        if (filter.getType() != null) {
            where += and + " type = :type";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterInventoryReleasedConfigurations filter, Query<T> query) {

        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }
        if (filter.getName() != null && !filter.getName().isEmpty()) {
            query.setParameter("name", filter.getName());
        }
        if (filter.getType() != null) {
            query.setParameter("type", filter.getType().intValue());
        }

        return query;
    }

    public List<com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration> getInventoryReleasedConfigurations(
            FilterInventoryReleasedConfigurations filter, final int limit) throws SOSHibernateException, JsonParseException, JsonMappingException,
            IOException {

        String q = "from " + DBItemInventoryReleasedConfiguration + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration> resultset = sosHibernateSession.getResultList(query);

        return resultset;
    }

    public com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration getSingleInventoryReleasedConfigurations(
            FilterInventoryReleasedConfigurations filter) throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {

        String q = "from " + DBItemInventoryReleasedConfiguration + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        List<com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration> resultset = sosHibernateSession.getResultList(query);
        if (resultset.size() > 0) {
            return resultset.get(0);
        }

        return null;
    }
}