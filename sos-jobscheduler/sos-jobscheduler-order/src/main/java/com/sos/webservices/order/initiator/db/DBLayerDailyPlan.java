package com.sos.webservices.order.initiator.db;

import java.util.Date;
import java.util.List;
import javax.persistence.TemporalType;
import org.hibernate.query.Query;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.model.common.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBLayerDailyPlan {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlan.class);
    private static final String DBItemDailyPlan = DBItemDailyPlan.class.getSimpleName();
    private static final String DBItemOrder = DBItemOrder.class.getSimpleName();
    private static final String DBItemDailyPlanWithHistory = DBItemDailyPlanWithHistory.class.getName();
    private final SOSHibernateSession sosHibernateSession;
    private FilterDailyPlan filter = null;

    public DBLayerDailyPlan(SOSHibernateSession session) {
        this.sosHibernateSession = session;
        resetFilter();
    }

    public DBItemDailyPlan getPlanDbItem(final Long id) throws Exception {
        return (DBItemDailyPlan) sosHibernateSession.get(DBItemDailyPlan.class, id);
    }

    public void resetFilter() {
        filter = new FilterDailyPlan();
        filter.setMasterId("");
        filter.setWorkflow("");
        filter.setOrderName("");
        filter.setOrderKey("");
        filter.setPlannedStart(null);
    }

    public int delete() throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlan + " p " + getWhere();
        int row = 0;
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStart() != null) {
            query.setParameter("plannedStart", filter.getPlannedStart(), TemporalType.TIMESTAMP);
        } else {
            if (filter.getPlannedStartFrom() != null) {
                query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
            }
            if (filter.getPlannedStartTo() != null) {
                query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
            }
        }
        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            query.setParameter("masterId", filter.getMasterId());
        }
        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            query.setParameter("orderId", filter.getOrderName());
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            query.setParameter("jobChain", filter.getWorkflow());
        }
        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public long deleteInterval() throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlan + " p " + getWhere();
        int row = 0;
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStartFrom() != null) {
            query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStartTo() != null) {
            query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
        }
        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public String getWhere() {
        return getWhere("");
    }

    private String getWhere(String pathField) {
        String where = "";
        String and = "";
        if (filter.getPlannedStart() != null) {
            where += and + " p.plannedStart = :plannedStart";
            and = " and ";
        } else {
            if (filter.getPlannedStartFrom() != null) {
                where += and + " p.plannedStart>= :plannedStartFrom";
                and = " and ";
            }
            if (filter.getPlannedStartTo() != null) {
                where += and + " p.plannedStart < :plannedStartTo ";
                and = " and ";
            }
        }
        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            where += and + " p.masterId = :masterId";
            and = " and ";
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            where += String.format(and + " p.workflow %s :workflow", SearchStringHelper.getSearchPathOperator(filter.getWorkflow()));
            and = " and ";
        }
        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            where += String.format(and + " p.orderId %s :orderId", SearchStringHelper.getSearchOperator(filter.getOrderName()));
            and = " and ";
        }
        if (filter.getOrderKey() != null && !"".equals(filter.getOrderKey())) {
            where += String.format(and + " p.orderKey %s :orderKey", SearchStringHelper.getSearchOperator(filter.getOrderKey()));
            and = " and ";
        }
        if (filter.getIsLate() != null) {
            if (filter.isLate()) {
                where += and + " p.isLate = 1";
            } else {
                where += and + " p.isLate = 0";
            }
            and = " and ";
        }
        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where += " p.state = '" + state + "' or";
            }
            where += " 1=0)";
            and = " and ";
        }

        if (!"".equals(pathField) && filter.getListOfFolders() != null && filter.getListOfFolders().size() > 0) {
            where += and + "(";
            for (Folder filterFolder : filter.getListOfFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where += " (" + pathField + " = '" + filterFolder.getFolder() + "' or " + pathField + " like '" + likeFolder + "')";
                } else {
                    where += String.format(pathField + " %s '" + filterFolder.getFolder() + "'", SearchStringHelper.getSearchOperator(filterFolder
                            .getFolder()));
                }
                where += " or ";
            }
            where += " 0=1)";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(Query<T> query) {
        if (filter.getPlannedStartFrom() != null) {
            query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStartTo() != null) {
            query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStart() != null) {
            query.setParameter("plannedStart", filter.getPlannedStart(), TemporalType.TIMESTAMP);
        }
        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            query.setParameter("masterId", filter.getMasterId());
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            query.setParameter("workflow", SearchStringHelper.getSearchPathValue(filter.getWorkflow()));
        }
        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            query.setParameter("orderId", filter.getOrderName());
        }
        if (filter.getOrderKey() != null && !"".equals(filter.getOrderKey())) {
            query.setParameter("orderKey", filter.getOrderKey());
        }
        return query;

    }
    public List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryList(final int limit) throws SOSHibernateException {
        String q = "Select new " + DBItemDailyPlanWithHistory + "(p,o) from " + DBItemDailyPlan + " p left outer join " + DBItemOrder + " o on p.orderKey = o.orderKey" + getWhere();
        LOGGER.debug("DailyPlan sql: " + q + " from " + filter.getPlannedStartFrom() + " to " + filter.getPlannedStartTo());
         Query<DBItemDailyPlanWithHistory> query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemDailyPlan> getDailyPlanList(final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlan + " p " + getWhere();
        LOGGER.debug("DailyPlan sql: " + q + " from " + filter.getPlannedStartFrom() + " to " + filter.getPlannedStartTo());

        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public DBItemDailyPlan getUniqueDailyPlan(DBItemDailyPlan item) throws SOSHibernateException {
        resetFilter();
        filter.setMasterId(item.getMasterId());
        filter.setWorkflow(item.getWorkflow());
        filter.setOrderKey(item.getOrderKey());

        String q = "from " + DBItemDailyPlan + " p " + getWhere();
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        List<DBItemDailyPlan> uniqueDailyPlanItem = sosHibernateSession.getResultList(query);
        if (uniqueDailyPlanItem.size() > 0) {
            return sosHibernateSession.getResultList(query).get(0);
        } else {
            return null;
        }
    }

    public FilterDailyPlan getFilter() {
        return filter;
    }

    public void setFilter(final FilterDailyPlan filter) {
        this.filter = filter;
    }

    public Date getMaxPlannedStart(String masterId) {
        String q = "select max(plannedStart) from " + DBItemDailyPlan + " where masterId=:masterId";
        Query<Date> query;
        try {
            query = sosHibernateSession.createQuery(q);
            query.setParameter("masterId", masterId);
            Date d = sosHibernateSession.getSingleValue(query);

            if (d != null) {
                return d;
            } else {
                return new Date();
            }
        } catch (SOSHibernateException e) {
            return new Date();
        }
    }

    public void delete(DBItemDailyPlan DBItemDailyPlan) throws SOSHibernateException {
        filter.setOrderKey(DBItemDailyPlan.getOrderKey());
        filter.setMasterId(DBItemDailyPlan.getMasterId());
        filter.setWorkflow(DBItemDailyPlan.getWorkflow());
        delete();
    }

}