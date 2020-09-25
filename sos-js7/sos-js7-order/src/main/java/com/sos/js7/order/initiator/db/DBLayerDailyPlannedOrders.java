package com.sos.js7.order.initiator.db;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissionHistory;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.Folder;
import com.sos.js7.order.initiator.classes.PlannedOrder;

import js7.data.order.OrderId;

public class DBLayerDailyPlannedOrders {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlannedOrders.class);
    private static final String DBItemDailyPlannedOrders = DBItemDailyPlanOrders.class.getSimpleName();
    private static final String DBItemDailyPlanVariables = DBItemDailyPlanVariables.class.getSimpleName();
    private static final String DBItemOrder = DBItemHistoryOrder.class.getSimpleName();
    private static final String DBItemDailyPlanWithHistory = DBItemDailyPlanWithHistory.class.getName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDailyPlannedOrders(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemDailyPlanSubmissionHistory getPlanDbItem(final Long id) throws Exception {
        return (DBItemDailyPlanSubmissionHistory) sosHibernateSession.get(DBItemDailyPlanSubmissionHistory.class, id);
    }

    public FilterDailyPlannedOrders resetFilter() {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setControllerId("");
        filter.setWorkflow("");
        filter.setOrderKey("");
        filter.setPlannedStart(null);
        filter.setSubmitted(false);
        return filter;
    }

    public int deleteVariables(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        int row = 0;
        List<DBItemDailyPlanWithHistory> listOfPlannedOrders = getDailyPlanWithHistoryList(filter, 0);
        for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {
            String hql = "delete from " + DBItemDailyPlanVariables + " where plannedOrderId = :plannedOrderId";
            Query<DBItemDailyPlanSubmissionHistory> query = sosHibernateSession.createQuery(hql);
            query.setParameter("plannedOrderId", dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getId());
            row = row + sosHibernateSession.executeUpdate(query);
        }
        return row;
    }

    public int delete(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        deleteVariables(filter);
        String hql = "delete from " + DBItemDailyPlannedOrders + " p " + getWhere(filter);
        Query<DBItemDailyPlanSubmissionHistory> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public long deleteInterval(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlannedOrders + " p " + getWhere(filter);
        int row = 0;
        Query<DBItemDailyPlanSubmissionHistory> query = sosHibernateSession.createQuery(hql);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public String getWhere(FilterDailyPlannedOrders filter) {
        return getWhere(filter, "");
    }

    private String getWhere(FilterDailyPlannedOrders filter, String pathField) {
        String where = "";
        String and = "";

        if (filter.getPlannedStart() != null) {
            where += and + " p.plannedStart = :plannedStart";
            and = " and ";
        }
        if (filter.getOrderPlannedStartFrom() != null && filter.getOrderPlannedStartTo() != null) {
            where += and + " p.plannedStart >= :plannedStartFrom and p.plannedStart < :plannedStartTo";
            and = " and ";
        }
        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            where += and + " p.controllerId = :controllerId";
            and = " and ";
        }
        if (filter.getSubmissionHistoryId() != null) {
            where += and + " p.submissionHistoryId = :submissionHistoryId";
            and = " and ";
        }
        if (filter.getSubmitted() != null) {
            if (filter.getSubmitted()) {
                where += and + " not p.submitTime is null";
            } else {
                where += and + " p.submitTime is null";
            }

            and = " and ";
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            where += String.format(and + " p.workflow %s :workflow", SearchStringHelper.getSearchPathOperator(filter.getWorkflow()));
            and = " and ";
        }

        if (filter.getOrderKey() != null && !"".equals(filter.getOrderKey())) {
            where += String.format(and + " p.orderKey %s :orderKey", SearchStringHelper.getSearchOperator(filter.getOrderKey()));
            and = " and ";
        }
        if (filter.getIsLate() != null) {
            if (filter.isLate()) {
                where += and
                        + " (o.status = 'planned' and p.plannedStart < current_date()) or (o.status <> 'planned' and o.startTime - p.plannedStart > 600) ";
            } else {
                where += and
                        + " not ((o.status = 'planned' and p.plannedStart < current_date()) or (o.status <> 'planned' and o.startTime - p.plannedStart > 600)) ";
            }
            and = " and ";
        }
        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where += " o.status = '" + state + "' or";
            }
            where += " 1=0)";
            and = " and ";
        }

        if (filter.getOrderTemplates() != null && filter.getOrderTemplates().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getOrderTemplates(), "p.orderTemplatePath");
            and = " and ";
        }
        
        if (filter.getSetOfOrders() != null && filter.getSetOfOrders().size() > 0) {
            where += and + "(";
            for (OrderId orderKey : filter.getSetOfOrders()) {
                where += "p.order_key = '" + orderKey.toString() + "' or";
            }
            where += " 1=0)";
            where += ")";
        }
        if (filter.getListOfOrders() != null && filter.getListOfOrders().size() > 0) {
            where += and + "(";
            for (String orderKey : filter.getListOfOrders()) {
                where += "p.order_key = '" + orderKey + "' or";
            }
            where += " 1=0)";
            where += ")";
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

    private <T> Query<T> bindParameters(FilterDailyPlannedOrders filter, Query<T> query) {

        if (filter.getPlannedStart() != null) {
            query.setParameter("plannedStart", filter.getPlannedStart());
        }
        if (filter.getOrderPlannedStartFrom() != null && filter.getOrderPlannedStartTo() != null) {
            query.setParameter("plannedStartFrom", filter.getOrderPlannedStartFrom());
            query.setParameter("plannedStartTo", filter.getOrderPlannedStartTo());
        }
        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            query.setParameter("controllerId", filter.getControllerId());
        }
        if (filter.getSubmitted() != null) {
            // query.setParameter("submitted", filter.getSubmitted());
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            query.setParameter("workflow", SearchStringHelper.getSearchPathValue(filter.getWorkflow()));
        }

        if (filter.getSubmissionHistoryId() != null) {
            query.setParameter("submissionHistoryId", filter.getSubmissionHistoryId());
        }

        if (filter.getOrderKey() != null && !"".equals(filter.getOrderKey())) {
            query.setParameter("orderKey", filter.getOrderKey());
        }
        return query;

    }

    public List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryList(FilterDailyPlannedOrders filter, final int limit)
            throws SOSHibernateException {
        String q = "Select new " + DBItemDailyPlanWithHistory + "(p,o) from " + DBItemDailyPlannedOrders + " p left outer join " + DBItemOrder
                + " o on p.orderKey = o.orderKey " + getWhere(filter);

        Query<DBItemDailyPlanWithHistory> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemDailyPlanOrders> getDailyPlanList(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlannedOrders + " p " + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlanOrders> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public DBItemDailyPlanOrders getUniqueDailyPlan(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlannedOrders + " p " + getWhere(filter);
        Query<DBItemDailyPlanOrders> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        List<DBItemDailyPlanOrders> uniqueDailyPlanItem = sosHibernateSession.getResultList(query);
        if (uniqueDailyPlanItem.size() > 0) {
            return sosHibernateSession.getResultList(query).get(0);
        } else {
            return null;
        }
    }

    public Date getMaxPlannedStart(String jobschedulerId) {
        String q = "select max(plannedStart) from " + DBItemDailyPlannedOrders + " where jobschedulerId=:jobschedulerId";
        Query<Date> query;
        try {
            query = sosHibernateSession.createQuery(q);
            query.setParameter("^jobschedulerId", jobschedulerId);
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

    public void delete(DBItemDailyPlanOrders dbItemDailyPlanOrders) throws SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setOrderKey(dbItemDailyPlanOrders.getOrderKey());
        filter.setControllerId(dbItemDailyPlanOrders.getControllerId());
        filter.setWorkflow(dbItemDailyPlanOrders.getWorkflow());
        delete(filter);
    }

    public DBItemDailyPlanOrders getUniqueDailyPlan(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException,
            SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
        LOGGER.info("----> " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder.getFreshOrder().getScheduledFor()));
        filter.setControllerId(plannedOrder.getOrderTemplate().getControllerId());
        filter.setWorkflow(plannedOrder.getFreshOrder().getWorkflowPath());
        return getUniqueDailyPlan(filter);
    }

    private Date nowInUtc() {
        String now = JobSchedulerDate.getNowInISO();
        Instant instant = JobSchedulerDate.getScheduledForInUTC(now, TimeZone.getDefault().getID()).get();
        return Date.from(instant);
    }

    public void storeVariables(PlannedOrder plannedOrder, Long id) throws SOSHibernateException {
        DBItemDailyPlanVariables dbItemDailyPlanVariables = new DBItemDailyPlanVariables();
        for (Entry<String, String> variable : plannedOrder.getFreshOrder().getArguments().getAdditionalProperties().entrySet()) {
            dbItemDailyPlanVariables.setCreated(nowInUtc());
            dbItemDailyPlanVariables.setModified(nowInUtc());
            dbItemDailyPlanVariables.setPlannedOrderId(id);
            dbItemDailyPlanVariables.setVariableName(variable.getKey());
            dbItemDailyPlanVariables.setVariableValue(variable.getValue());
            sosHibernateSession.save(dbItemDailyPlanVariables);
        }
    }

    public void store(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException,
            ParseException {
        DBItemDailyPlanOrders dbItemDailyPlannedOrders = new DBItemDailyPlanOrders();
        dbItemDailyPlannedOrders.setOrderTemplatePath(plannedOrder.getOrderTemplate().getPath());
        dbItemDailyPlannedOrders.setOrderKey(plannedOrder.getFreshOrder().getId());
        Date start = new Date(plannedOrder.getFreshOrder().getScheduledFor());
        dbItemDailyPlannedOrders.setPlannedStart(start);
        if (plannedOrder.getPeriod().getSingleStart() == null) {
            dbItemDailyPlannedOrders.setPeriodBegin(start, plannedOrder.getPeriod().getBegin());
            dbItemDailyPlannedOrders.setPeriodEnd(start, plannedOrder.getPeriod().getEnd());
            dbItemDailyPlannedOrders.setRepeatInterval(plannedOrder.getPeriod().getRepeat());
        }
        dbItemDailyPlannedOrders.setControllerId(plannedOrder.getOrderTemplate().getControllerId());
        dbItemDailyPlannedOrders.setWorkflow(plannedOrder.getFreshOrder().getWorkflowPath());
        dbItemDailyPlannedOrders.setSubmitted(false);
        dbItemDailyPlannedOrders.setSubmissionHistoryId(plannedOrder.getSubmissionHistoryId());
        dbItemDailyPlannedOrders.setCalendarId(plannedOrder.getCalendarId());
        dbItemDailyPlannedOrders.setCreated(nowInUtc());
        dbItemDailyPlannedOrders.setExpectedEnd(new Date(plannedOrder.getFreshOrder().getScheduledFor() + plannedOrder.getAverageDuration()));
        dbItemDailyPlannedOrders.setModified(nowInUtc());
        sosHibernateSession.save(dbItemDailyPlannedOrders);
        String id = "0000000000" + String.valueOf(dbItemDailyPlannedOrders.getId());
        id = id.substring(id.length() - 10);
        dbItemDailyPlannedOrders.setOrderKey(dbItemDailyPlannedOrders.getOrderKey().replaceAll("<id.*>", id));
        plannedOrder.getFreshOrder().setId(dbItemDailyPlannedOrders.getOrderKey());
        sosHibernateSession.update(dbItemDailyPlannedOrders);
        storeVariables(plannedOrder, dbItemDailyPlannedOrders.getId());
    }

    public int markOrdersAsSubmitted(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        String hql = "update  " + DBItemDailyPlannedOrders + " p set submitted=1 " + getWhere(filter);
        Query<DBItemDailyPlanSubmissionHistory> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;        
    }

}