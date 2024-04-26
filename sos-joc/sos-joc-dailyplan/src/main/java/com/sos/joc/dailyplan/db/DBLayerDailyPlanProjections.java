package com.sos.joc.dailyplan.db;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanProjectionEvent;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthsItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;

public class DBLayerDailyPlanProjections extends DBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlanProjections.class);

    private static final long serialVersionUID = 1L;
    public static Optional<Instant> projectionsStart = Optional.empty();

    public DBLayerDailyPlanProjections(SOSHibernateSession session) {
        super(session);
    }

    public int cleanup() throws SOSHibernateException {
        projectionsStart = Optional.of(Instant.now());
        return getSession().executeUpdate("delete from " + DBITEM_DPL_PROJECTIONS);
    }

    public void insert(int year, YearsItem o) throws Exception {
        if (o == null) {
            return;
        }
        // store monthly not yearly
        Date d = new Date();
        for (Map.Entry<String, MonthItem> monthEntry : o.getAdditionalProperties().getOrDefault(year + "", new MonthsItem()).getAdditionalProperties()
                .entrySet()) {
            String yearMonth = monthEntry.getKey().replace("-", "");
            insert(Long.valueOf(yearMonth), monthEntry.getValue(), d);
        }
    }

    private void insert(long yearMonth, MonthItem o, Date created) throws Exception {
        if (o == null) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug(String.format("[insert][%s]%s", yearMonth, Globals.objectMapper.writeValueAsString(o)));
            } catch (Throwable e) {
            }
        }

        DBItemDailyPlanProjection item = new DBItemDailyPlanProjection();
        item.setId(yearMonth);
        item.setContent(Globals.objectMapper.writeValueAsBytes(o));
        item.setCreated(created);

        getSession().save(item);
    }

    public void insertMeta(MetaItem o) throws Exception {
        if (o == null) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug(String.format("[insertMeta]%s", Globals.objectMapper.writeValueAsString(o)));
            } catch (Throwable e) {
            }
        }

        DBItemDailyPlanProjection item = new DBItemDailyPlanProjection();
        item.setId(DBItemDailyPlanProjection.METADATEN_ID);
        item.setContent(Globals.objectMapper.writeValueAsBytes(o));
        item.setCreated(new Date());

        getSession().save(item);

        projectionsStart = Optional.empty();
        EventBus.getInstance().post(new DailyPlanProjectionEvent());
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

    public List<DBItemDailyPlanSubmission> getSubmissions(Date dateFrom) throws SOSHibernateException {
        // Pending orders have the virtual submission date 9999-12-31 00:00:00 that won't be part of this result
        final long millisOf99991231000000 = 253402214400000L;
        final Date dateOf99991231000000 =  Date.from(Instant.ofEpochMilli(millisOf99991231000000));
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where submissionForDate >= :dateFrom ");
        hql.append("and submissionForDate < :date99991231 ");
        hql.append("order by submissionForDate");

        Query<DBItemDailyPlanSubmission> query = getSession().createQuery(hql.toString());
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("date99991231", dateOf99991231000000);

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