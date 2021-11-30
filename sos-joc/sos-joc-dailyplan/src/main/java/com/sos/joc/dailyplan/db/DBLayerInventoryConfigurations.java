package com.sos.joc.dailyplan.db;

import java.io.IOException;
import java.util.List;

import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class DBLayerInventoryConfigurations {

    private static final String DBItemInventoryConfiguration = com.sos.joc.db.inventory.DBItemInventoryConfiguration.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public DBLayerInventoryConfigurations(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterInventoryConfigurations resetFilter() {
        FilterInventoryConfigurations filter = new FilterInventoryConfigurations();
        return filter;
    }

    private String getWhere(FilterInventoryConfigurations filter) {
        String where = " ";
        String and = " ";

        if (filter.getName() != null && !filter.getName().isEmpty()) {
            where += " name = :name";
            and = " and ";
        }

        if (filter.getType() != null) {
            where += and + " type = :type";
            and = " and ";
        }

        if (filter.getDeployed() != null) {
            where += and + " deployed = :deployed";
            and = " and ";
        }

        if (filter.getReleased() != null) {
            where += and + " released = :released";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterInventoryConfigurations filter, Query<T> query) {

        if (filter.getName() != null && !filter.getName().isEmpty()) {
            query.setParameter("name", filter.getName());
        }
        if (filter.getType() != null) {
            query.setParameter("type", filter.getType().intValue());
        }
        if (filter.getDeployed() != null) {
            query.setParameter("deployed", filter.getDeployed());
        }
        if (filter.getReleased() != null) {
            query.setParameter("released", filter.getReleased());
        }
        return query;
    }

    public List<com.sos.joc.db.inventory.DBItemInventoryConfiguration> getInventoryConfigurations(FilterInventoryConfigurations filter,
            final int limit) throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {

        String q = "from " + DBItemInventoryConfiguration + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<com.sos.joc.db.inventory.DBItemInventoryConfiguration> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<com.sos.joc.db.inventory.DBItemInventoryConfiguration> resultset = sosHibernateSession.getResultList(query);

        return resultset;
    }

    public com.sos.joc.db.inventory.DBItemInventoryConfiguration getSingleInventoryConfigurations(FilterInventoryConfigurations filter)
            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException {

        String q = "from " + DBItemInventoryConfiguration + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<com.sos.joc.db.inventory.DBItemInventoryConfiguration> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        List<com.sos.joc.db.inventory.DBItemInventoryConfiguration> resultset = sosHibernateSession.getResultList(query);
        if (resultset.size() > 0) {
            return resultset.get(0);
        }

        return null;
    }
}