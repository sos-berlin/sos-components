package com.sos.joc.dailyplan.db;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;

public class DBLayerDailyPlanSubmissions extends DBLayer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlanSubmissions.class);

    public DBLayerDailyPlanSubmissions(SOSHibernateSession session) {
        super(session);
    }

    public List<DBItemDailyPlanSubmission> getSubmissions(String controllerId, Date date) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and submissionForDate=:date ");
        hql.append("order by id");

        Query<DBItemDailyPlanSubmission> query = getSession().createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("date", date);
        return getSession().getResultList(query);
    }

    public List<DBItemDailyPlanSubmission> getSubmissions(String controllerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where controllerId=:controllerId ");
        if (dateFrom != null) {
            hql.append("and submissionForDate >= :dateFrom ");
        }
        hql.append("and submissionForDate <= :dateTo ");

        Query<DBItemDailyPlanSubmission> query = getSession().createQuery(hql);
        query.setParameter("controllerId", controllerId);
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        query.setParameter("dateTo", dateTo);
        return getSession().getResultList(query);
    }

    public int delete(StartupMode mode, String controllerId, String dateFor, String dateFrom, String dateTo) throws Exception {
        SubmissionsDeleteWhere where = new SubmissionsDeleteWhere(controllerId, dateFor, dateFrom, dateTo);

        Long countSubmitted = getCountSubmittedOrders(where);
        int result = 0;
        if (countSubmitted.equals(0L)) {
            result = deleteOrderVariabless(where);
            result += deleteOrders(where);
            result += deleteSubmissions(where);
        } else {
            LOGGER.info(String.format("[%s][delete daily plan][skip][%s][dateFor=%s]found %s submitted orders", mode, controllerId, dateFor,
                    countSubmitted));
        }
        return result;
    }

    private Long getCountSubmittedOrders(SubmissionsDeleteWhere where) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where submitted=true ");
        hql.append("and submissionHistoryId in (");
        hql.append("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ").append(where.getHql());
        hql.append(")");

        Query<Long> query = getSession().createQuery(hql.toString());
        return getSession().getSingleResult(where.bindParams(query));
    }

    private int deleteOrderVariabless(SubmissionsDeleteWhere where) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where orderId in (");
        hql.append("select orderId from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and submitted=false ");
        hql.append("and submissionHistoryId in (");
        hql.append("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ").append(where.getHql());
        hql.append(")");
        hql.append(") ");

        Query<DBItemDailyPlanVariable> query = getSession().createQuery(hql.toString());
        return tryDelete(where.bindParams(query), "deleteOrderVariabless");
    }

    private int deleteOrders(SubmissionsDeleteWhere where) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where submitted=false ");
        hql.append("and submissionHistoryId in (");
        hql.append("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ").append(where.getHql());
        hql.append(")");

        Query<DBItemDailyPlanOrder> query = getSession().createQuery(hql.toString());
        return tryDelete(where.bindParams(query), "deleteOrders");
    }

    private int deleteSubmissions(SubmissionsDeleteWhere where) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append(where.getHql());

        Query<DBItemDailyPlanSubmission> query = getSession().createQuery(hql);
        return tryDelete(where.bindParams(query), "deleteSubmissions");
    }

    private int tryDelete(Query<?> query, String caller) throws SOSHibernateException {
        try {
            return getSession().executeUpdate(query);
        } catch (SOSHibernateException e) {
            LOGGER.warn(String.format("[%s][failed][wait 1s and try again]%s", caller, e.toString()));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e1) {

            }
            return getSession().executeUpdate(query);
        }
    }

    private class SubmissionsDeleteWhere {

        private final StringBuilder hql;
        private final Map<String, Object> params;

        private SubmissionsDeleteWhere(String controllerId, String dateFor, String dateFrom, String dateTo) throws Exception {
            params = new HashMap<>();
            hql = new StringBuilder("where controllerId=:controllerId ");
            params.put("controllerId", controllerId);

            if (dateFor != null) {
                hql.append("and submissionForDate = :dateFor ");
                params.put("dateFor", SOSDate.getDate(dateFor));
            } else {
                if (dateFrom != null) {
                    hql.append("and submissionForDate >= :dateFrom ");
                    params.put("dateFrom", SOSDate.getDate(dateFrom));
                }
                hql.append("and submissionForDate <= :dateTo ");
                params.put("dateTo", SOSDate.getDate(dateTo));
            }
        }

        private StringBuilder getHql() {
            return hql;
        }

        private <T> Query<T> bindParams(Query<T> query) {
            params.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
            return query;
        }
    }
}