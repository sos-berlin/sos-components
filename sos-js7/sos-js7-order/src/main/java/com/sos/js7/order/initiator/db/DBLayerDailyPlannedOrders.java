package com.sos.js7.order.initiator.db;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
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
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.classes.PlannedOrder;

import js7.data.order.OrderId;

public class DBLayerDailyPlannedOrders {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlannedOrders.class);
    private static final String DBItemDailyPlannedOrders = DBItemDailyPlanOrders.class.getSimpleName();
    private static final String DBItemDailyPlanVariables = DBItemDailyPlanVariables.class.getSimpleName();
    private static final String DBItemHistoryOrder = DBItemHistoryOrder.class.getSimpleName();
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
        filter.setOrderId("");
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
            query.setParameter("plannedOrderId", dbItemDailyPlanWithHistory.getPlannedOrderId());
            row = row + sosHibernateSession.executeUpdate(query);
        }
        return row;
    }

    public int delete(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        deleteVariables(filter);
        String hql = "delete from " + DBItemDailyPlannedOrders + " p " + getWhere(filter,"p.schedulePath");
        Query<DBItemDailyPlanSubmissionHistory> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public long deleteInterval(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlannedOrders + " p " + getWhere(filter,"p.schedulePath");
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

        if (filter.getCalendarId() != null) {
            where += and + " p.calendarId = :calendarId";
            and = " and ";
        }
        if (filter.getSubmitted() != null) {
            where += and + "  p.submitted = :submitted";
            and = " and ";
        }

        if (filter.getListOfWorkflowPaths() != null && filter.getListOfWorkflowPaths().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfWorkflowPaths(), "p.workflowPath");
            and = " and ";
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += String.format(and + " p.orderId %s :orderId", SearchStringHelper.getSearchOperator(filter.getOrderId()));
            and = " and ";
        }
        if (filter.getIsLate() != null) {
            if (filter.isLate()) {
                where += and + " (o.state = " + OrderStateText.PLANNED.intValue() + " and p.plannedStart < current_date()) or (o.state <> "
                        + OrderStateText.PLANNED.intValue() + " and o.startTime - p.plannedStart > 600) ";
            } else {
                where += and + " not ((o.state = " + OrderStateText.PLANNED.intValue() + " and p.plannedStart < current_date()) or (o.state <> "
                        + OrderStateText.PLANNED.intValue() + " and o.startTime - p.plannedStart > 600)) ";
            }
            and = " and ";
        }
        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (OrderStateText state : filter.getStates()) {
                if (state.intValue() == OrderStateText.PLANNED.intValue()) {
                    where += " p.submitted= 0 or";
                } else {
                    where += " o.state = " + state.intValue() + " or";
                }
            }
            where += " 1=0)";
            and = " and ";
        }

        if (filter.getListOfSchedules() != null && filter.getListOfSchedules().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfSchedules(), "p.schedulePath");
            and = " and ";
        }

        if (filter.getListOfSubmissionIds() != null && filter.getListOfSubmissionIds().size() > 0) {
            where += and + SearchStringHelper.getLongSetSql(filter.getListOfSubmissionIds(), "p.submissionHistoryId");
            and = " and ";
        }

        if (filter.getSetOfPlannedOrder() != null && filter.getSetOfPlannedOrder().size() > 0) {
            where += and + "(";
            for (PlannedOrder plannedOrder : filter.getSetOfPlannedOrder()) {
                where += " p.orderId = '" + plannedOrder.getFreshOrder().getId() + "' or";
            }
            where += " 1=0)";
        }
        if (filter.getSetOfOrders() != null && filter.getSetOfOrders().size() > 0) {
            where += and + "(";
            for (OrderId orderId : filter.getSetOfOrders()) {
                where += " p.orderId = '" + orderId.toString() + "' or";
            }
            where += " 1=0)";
        }
        if (filter.getListOfOrders() != null && filter.getListOfOrders().size() > 0) {
            where += and + "(";
            for (String orderId : filter.getListOfOrders()) {
                where += " p.orderId = '" + orderId + "' or";
            }
            where += " 1=0)";
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
            query.setParameter("submitted", filter.getSubmitted());
        }

        if (filter.getSubmitTime() != null) {
            query.setParameter("submitTime", filter.getSubmitTime());
        }

        if (filter.getCalendarId() != null) {
            query.setParameter("calendarId", filter.getCalendarId());
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }
        return query;

    }

    public List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryList(FilterDailyPlannedOrders filter, final int limit)
            throws SOSHibernateException {
        String q =
                "Select p.id as plannedOrderId,p.submissionHistoryId as submissionHistoryId,p.controllerId as controllerId,p.workflowPath as workflowPath,p.orderId as orderId,p.schedulePath as schedulePath,"
                        + "    p.calendarId as calendarId,p.submitted as submitted,p.submitTime as submitTime,p.periodBegin as periodBegin,p.periodEnd as periodEnd,p.repeatInterval as repeatInterval,"
                        + "    p.plannedStart as plannedStart, p.expectedEnd as expectedEnd,p.created as plannedOrderCreated, "
                        + "    o.id as orderHistoryId, o.startTime as startTime, o.endTime as endTime, o.state as state " +

                        " from " + DBItemDailyPlannedOrders + " p left outer join " + DBItemHistoryOrder + " o on p.orderId = o.orderId " + getWhere(
                                filter, "p.schedulePath");

        Query<DBItemDailyPlanWithHistory> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }

        query.setResultTransformer(Transformers.aliasToBean(DBItemDailyPlanWithHistory.class));

        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemDailyPlanOrders> getDailyPlanList(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlannedOrders + " p " + getWhere(filter,"p.schedulePath") + filter.getOrderCriteria() + filter.getSortMode();
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
            query.setParameter("jobschedulerId", jobschedulerId);
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
        filter.setOrderId(dbItemDailyPlanOrders.getOrderId());
        filter.setControllerId(dbItemDailyPlanOrders.getControllerId());
        filter.addWorkflowPath(dbItemDailyPlanOrders.getWorkflowPath());
        delete(filter);
    }

    public DBItemDailyPlanOrders getUniqueDailyPlan(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException,
            SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
        LOGGER.info("----> " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder.getFreshOrder().getScheduledFor()));
        filter.setControllerId(plannedOrder.getControllerId());
        filter.addWorkflowPath(plannedOrder.getFreshOrder().getWorkflowPath());
        return getUniqueDailyPlan(filter);
    }

    public void storeVariables(PlannedOrder plannedOrder, Long id) throws SOSHibernateException {
        DBItemDailyPlanVariables dbItemDailyPlanVariables = new DBItemDailyPlanVariables();
        for (Entry<String, String> variable : plannedOrder.getFreshOrder().getArguments().getAdditionalProperties().entrySet()) {
            dbItemDailyPlanVariables.setCreated(JobSchedulerDate.nowInUtc());
            dbItemDailyPlanVariables.setModified(JobSchedulerDate.nowInUtc());
            dbItemDailyPlanVariables.setPlannedOrderId(id);
            dbItemDailyPlanVariables.setVariableName(variable.getKey());
            dbItemDailyPlanVariables.setVariableValue(variable.getValue());
            sosHibernateSession.save(dbItemDailyPlanVariables);
        }
    }

    public void store(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException,
            ParseException {
        DBItemDailyPlanOrders dbItemDailyPlannedOrders = new DBItemDailyPlanOrders();
        dbItemDailyPlannedOrders.setSchedulePath(plannedOrder.getSchedule().getPath());
        dbItemDailyPlannedOrders.setOrderId(plannedOrder.getFreshOrder().getId());
        Date start = new Date(plannedOrder.getFreshOrder().getScheduledFor());
        dbItemDailyPlannedOrders.setPlannedStart(start);
        if (plannedOrder.getPeriod().getSingleStart() == null) {
            dbItemDailyPlannedOrders.setPeriodBegin(start, plannedOrder.getPeriod().getBegin());
            dbItemDailyPlannedOrders.setPeriodEnd(start, plannedOrder.getPeriod().getEnd());
            dbItemDailyPlannedOrders.setRepeatInterval(plannedOrder.getPeriod().getRepeat());
        }
        dbItemDailyPlannedOrders.setControllerId(plannedOrder.getControllerId());
        dbItemDailyPlannedOrders.setWorkflowPath(plannedOrder.getFreshOrder().getWorkflowPath());
        dbItemDailyPlannedOrders.setSubmitted(false);
        dbItemDailyPlannedOrders.setSubmissionHistoryId(plannedOrder.getSubmissionHistoryId());
        dbItemDailyPlannedOrders.setCalendarId(plannedOrder.getCalendarId());
        dbItemDailyPlannedOrders.setCreated(JobSchedulerDate.nowInUtc());
        dbItemDailyPlannedOrders.setExpectedEnd(new Date(plannedOrder.getFreshOrder().getScheduledFor() + plannedOrder.getAverageDuration()));
        dbItemDailyPlannedOrders.setModified(JobSchedulerDate.nowInUtc());
        sosHibernateSession.save(dbItemDailyPlannedOrders);
        String id = "0000000000" + String.valueOf(dbItemDailyPlannedOrders.getId());
        id = id.substring(id.length() - 10);
        dbItemDailyPlannedOrders.setOrderId(dbItemDailyPlannedOrders.getOrderId().replaceAll("<id.*>", id));
        plannedOrder.getFreshOrder().setId(dbItemDailyPlannedOrders.getOrderId());
        sosHibernateSession.update(dbItemDailyPlannedOrders);
        storeVariables(plannedOrder, dbItemDailyPlannedOrders.getId());
    }

    public int setSubmitted(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        String hql;
        filter.setStates(null);
        filter.setSubmitTime(null);
        if (filter.getSubmitted()) {
            filter.setSubmitted(null);
            hql = "update  " + DBItemDailyPlannedOrders + " p set submitted=1,submitTime=:submitTime  " + getWhere(filter);
            filter.setSubmitTime(JobSchedulerDate.nowInUtc());
        } else {
            filter.setSubmitted(null);
            hql = "update  " + DBItemDailyPlannedOrders + " p set submitted=0,submitTime=null  " + getWhere(filter);
        }

        Query<DBItemDailyPlanSubmissionHistory> query = sosHibernateSession.createQuery(hql);

        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

}