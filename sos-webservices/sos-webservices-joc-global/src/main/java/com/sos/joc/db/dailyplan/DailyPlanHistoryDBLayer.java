package com.sos.joc.db.dailyplan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;

public class DailyPlanHistoryDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DailyPlanHistoryDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public List<Object[]> getDates(String controllerId, Date dateFrom, Date dateTo, Boolean submitted, int limit) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dailyPlanDate, controllerId, count(id) as countTotal ");
        hql.append("from ").append(DBLayer.DBITEM_DPL_HISTORY).append(" ");

        List<String> where = new ArrayList<>();
        if (!SOSString.isEmpty(controllerId)) {
            where.add("controllerId=:controllerId");
        }
        if (dateFrom != null) {
            where.add("dailyPlanDate >= :dateFrom");
        }
        if (dateTo != null) {
            where.add("dailyPlanDate < :dateTo");
        }
        if (submitted != null) {
            where.add("submitted=:submitted");
        }
        if (where.size() > 0) {
            hql.append("where ").append(String.join(" and ", where)).append(" ");
        }
        hql.append("group by dailyPlanDate,controllerId");

        Query<Object[]> query = getSession().createQuery(hql.toString());
        if (!SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo);
        }
        if (submitted != null) {
            query.setParameter("submitted", submitted.booleanValue());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return getSession().getResultList(query);
    }

    public List<Object[]> getSubmissions(String controllerId, Date date, Boolean submitted, int limit) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select submissionTime, count(id) as countTotal ");
        hql.append("from ").append(DBLayer.DBITEM_DPL_HISTORY).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and dailyPlanDate=:date ");
        if (submitted != null) {
            hql.append("and submitted=:submitted ");
        }
        hql.append("group by submissionTime");

        Query<Object[]> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("date", date);
        if (submitted != null) {
            query.setParameter("submitted", submitted.booleanValue());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return getSession().getResultList(query);
    }

    public List<DBItemDailyPlanHistory> getSubmissionOrders(String controllerId, Date date, Date submissionTime, Boolean submitted, int limit)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_HISTORY).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and dailyPlanDate=:date ");
        hql.append("and submissionTime=:submissionTime ");
        if (submitted != null) {
            hql.append("and submitted=:submitted ");
        }

        Query<DBItemDailyPlanHistory> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("date", date);
        query.setParameter("submissionTime", submissionTime);
        if (submitted != null) {
            query.setParameter("submitted", submitted.booleanValue());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return getSession().getResultList(query);
    }
}
