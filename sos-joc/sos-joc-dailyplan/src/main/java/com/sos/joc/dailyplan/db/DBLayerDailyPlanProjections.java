package com.sos.joc.dailyplan.db;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;

public class DBLayerDailyPlanProjections extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerDailyPlanProjections(SOSHibernateSession session) {
        super(session);
    }

    public List<DBItemDailyPlanProjection> getProjections(List<Long> yearMonths) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_DPL_PROJECTIONS).append(" ");
        Set<Long> ids = null;
        if (yearMonths != null && yearMonths.size() > 0) {
            ids = new HashSet<>(yearMonths);
            ids.add(DBItemDailyPlanProjection.METADATEN_ID);

            hql.append("where id in (:ids) ");
        }
        hql.append("order by id");

        Query<DBItemDailyPlanProjection> query = getSession().createQuery(hql.toString());
        if (ids != null) {
            query.setParameterList("ids", ids);
        }
        return getSession().getResultList(query);
    }

    public List<DBItemDailyPlanProjection> getProjections(Long dateFrom, Long dateTo) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_DPL_PROJECTIONS).append(" ");
        if (dateFrom != null || dateTo != null) {
            hql.append("where id = ").append(DBItemDailyPlanProjection.METADATEN_ID).append(" or (");
            List<String> dateFromTo = new ArrayList<>(2);
            if (dateFrom != null) {
                dateFromTo.add("id >= :dateFrom");
            }
            if (dateTo != null) {
                dateFromTo.add("id <= :dateTo");
            }
            hql.append(String.join(" and ", dateFromTo)).append(")");
        }
        hql.append("order by id");

        Query<DBItemDailyPlanProjection> query = getSession().createQuery(hql.toString());
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo);
        }
        return getSession().getResultList(query);
    }

    public List<DBItemDailyPlanSubmission> getSubmissions(Date dateFrom, Date dateTo) throws SOSHibernateException {
        Date queryDateTo = dateTo;
        if (queryDateTo == null) {
            // Pending orders have the virtual submission date 9999-12-31 00:00:00 that won't be part of this result
            queryDateTo = Date.from(Instant.ofEpochMilli(253402214400000L));
        }

        StringBuilder hql = new StringBuilder("from ").append(DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where submissionForDate >= :dateFrom ");
        hql.append("and submissionForDate < :queryDateTo ");
        hql.append("order by submissionForDate");

        Query<DBItemDailyPlanSubmission> query = getSession().createQuery(hql.toString());
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("queryDateTo", queryDateTo);

        return getSession().getResultList(query);
    }

    public ScrollableResults<DBItemDailyPlanOrder> getDailyPlanOrdersBySubmission(Long submissionHistoryId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where submissionHistoryId=:submissionHistoryId ");

        Query<DBItemDailyPlanOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("submissionHistoryId", submissionHistoryId);
        return getSession().scroll(query);
    }

}