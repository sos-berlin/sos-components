package com.sos.js7.order.initiator.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.orders.DBItemDailyPlan;

public class DBLayerDailyPlan {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlan.class);
    private static final String DBItemDailyPlan = DBItemDailyPlan.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDailyPlan(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterDailyPlan resetFilter() {
        FilterDailyPlan filter = new FilterDailyPlan();
        filter.setJobschedulerId("");
        return filter;
    }

    public int delete(FilterDailyPlan filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlan + getWhere(filter);
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    private String getWhere(FilterDailyPlan filter) {
        String where = "";
        String and = "";

        if (filter.getDayFrom() != null && filter.getDayFrom().equals(filter.getDayTo())) {
            filter.setDay(filter.getDayFrom());
            filter.setDayFrom(null);
            filter.setDayTo(null);
        }
        if (filter.getYearFrom() != null && filter.getYearFrom().equals(filter.getYearTo())) {
            filter.setYear(filter.getYearFrom());
            filter.setYearFrom(null);
            filter.setYearTo(null);
        }
        if (filter.getPlanId() != null) { 
            where += and + " id = :id";
            and = " and ";
        }
        if (filter.getDay() != null) {
            where += and + " day = :day";
            and = " and ";
        }
        if (filter.getYear() != null) {
            where += and + " year = :year";
            and = " and ";
        }
        if (filter.getJobschedulerId() != null && !"".equals(filter.getJobschedulerId())) {
            where += and + " jobschedulerId = :jobschedulerId";
            and = " and ";
        }

        if (filter.getDayFrom() != null) {
            where += and + " day >= :dayFrom";
            and = " and ";
        }

        if (filter.getDayTo() != null) {
            where += and + " day <= :dayTo";
            and = " and ";
        }

        if (filter.getYearFrom() != null) {
            where += and + " year >= :yearFrom";
            and = " and ";
        }

        if (filter.getYearTo() != null) {
            where += and + " year <= :yearTo";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterDailyPlan filter, Query<T> query) {

        if (filter.getPlanId() != null) {
            query.setParameter("id", filter.getPlanId());
        }

        if (filter.getDay() != null) {
            query.setParameter("day", filter.getDay());
        }
        if (filter.getDayFrom() != null) {
            query.setParameter("dayFrom", filter.getDayFrom());
        }
        if (filter.getDayTo() != null) {
            query.setParameter("dayTo", filter.getDayTo());
        }
        if (filter.getYearFrom() != null) {
            query.setParameter("yearFrom", filter.getYearFrom());
        }
        if (filter.getYearTo() != null) {
            query.setParameter("yearTo", filter.getYearTo());
        }
        if (filter.getYear() != null) {
            query.setParameter("year", filter.getYear());
        }
        if (filter.getJobschedulerId() != null && !"".equals(filter.getJobschedulerId())) {
            query.setParameter("jobschedulerId", filter.getJobschedulerId());
        }

        return query;

    }

    public List<DBItemDailyPlan> getPlans(FilterDailyPlan filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlan + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public DBItemDailyPlan getPlannedDay(FilterDailyPlan filter) throws SOSHibernateException {
        List<DBItemDailyPlan> l = getPlans(filter, 0);
        if (l != null && l.size() > 0) {
            return l.get(0);
        } else {
            return null;
        }
    }

    public int deletePlan(FilterDailyPlan filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlan + getWhere(filter);
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public DBItemDailyPlan storePlan(FilterDailyPlan filter) throws SOSHibernateException {
        DBItemDailyPlan dbItemDaysPlanned = new DBItemDailyPlan();
        dbItemDaysPlanned.setJobschedulerId(filter.getJobschedulerId());
        dbItemDaysPlanned.setDay(filter.getDay());
        dbItemDaysPlanned.setYear(filter.getYear());
        dbItemDaysPlanned.setModified(new Date());
        dbItemDaysPlanned.setCreated(new Date());

        sosHibernateSession.save(dbItemDaysPlanned);
        return dbItemDaysPlanned;
    }

}