package com.sos.webservices.order.initiator.db;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDaysPlanned;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.webservices.order.initiator.classes.PlannedOrder;

public class DBLayerDaysPlanned {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDaysPlanned.class);
    private static final String DBItemDaysPlanned = DBItemDaysPlanned.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDaysPlanned(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterDaysPlanned resetFilter() {
        FilterDaysPlanned filter = new FilterDaysPlanned();
        filter.setMasterId("");
        return filter;
    }

    public int delete(FilterDaysPlanned filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDaysPlanned + getWhere(filter);
        Query<DBItemDaysPlanned> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    private String getWhere(FilterDaysPlanned filter) {
        String where = "";
        String and = "";
        if (filter.getDay() != null) {
            where += and + " day = :day";
            and = " and ";
        }
        if (filter.getYear() != null) {
            where += and + " year = :year";
            and = " and ";
        }
        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            where += and + " masterId = :masterId";
            and = " and ";
        }

        if (filter.getDayFrom() != null) {
            where += and + " day >= :dayFrom";
            and = " and ";
        }

        if (filter.getDayTo() != null) {
            where += and + " day < :dayTo";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterDaysPlanned filter, Query<T> query) {
        if (filter.getDay() != null) {
            query.setParameter("day", filter.getDay());
        }
        if (filter.getDayFrom() != null) {
            query.setParameter("dayFrom", filter.getDayFrom());
        }
        if (filter.getDayTo() != null) {
            query.setParameter("dayTo", filter.getDayTo());
        }
        if (filter.getYear() != null) {
            query.setParameter("year", filter.getYear());
        }
        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            query.setParameter("masterId", filter.getMasterId());
        }

        return query;

    }

    public List<DBItemDaysPlanned> getDaysPlannedList(FilterDaysPlanned filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDaysPlanned + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDaysPlanned> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public DBItemDaysPlanned getPlannedDay(FilterDaysPlanned filter) throws SOSHibernateException {
        List<DBItemDaysPlanned> l = getDaysPlannedList(filter, 0);
        if (l != null && l.size() > 0) {
            return l.get(0);
        } else {
            return null;
        }
    }

    public int deletePlan(FilterDaysPlanned filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDaysPlanned  + getWhere(filter);
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

 
    public DBItemDaysPlanned storePlan(FilterDaysPlanned filter) throws SOSHibernateException {
        DBItemDaysPlanned dbItemDaysPlanned = new DBItemDaysPlanned();
        dbItemDaysPlanned.setMasterId(filter.getMasterId());
        dbItemDaysPlanned.setDay(filter.getDay());
        dbItemDaysPlanned.setYear(filter.getYear());
        dbItemDaysPlanned.setModified(new Date());
        dbItemDaysPlanned.setCreated(new Date());

        sosHibernateSession.save(dbItemDaysPlanned);
        return dbItemDaysPlanned;
    }

}