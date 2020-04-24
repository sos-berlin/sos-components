package com.sos.webservices.order.initiator.db;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.TemporalType;
import org.hibernate.query.Query;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderPath;
import com.sos.webservices.order.initiator.classes.PlannedOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBLayerDailyPlan {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlan.class);
    private static final String DBItemDailyPlan = DBItemDailyPlan.class.getSimpleName();
    private static final String DBItemDailyPlanVariables = DBItemDailyPlanVariables.class.getSimpleName();
    private static final String DBItemOrder = DBItemOrder.class.getSimpleName();
    private static final String DBItemDailyPlanWithHistory = DBItemDailyPlanWithHistory.class.getName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDailyPlan(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemDailyPlan getPlanDbItem(final Long id) throws Exception {
        return (DBItemDailyPlan) sosHibernateSession.get(DBItemDailyPlan.class, id);
    }

    public FilterDailyPlan resetFilter() {
        FilterDailyPlan filter = new FilterDailyPlan();
        filter.setJobSchedulerId("");
        filter.setWorkflow("");
        filter.setOrderName("");
        filter.setOrderKey("");
        filter.setPlannedStart(null);
        filter.setSubmitted(false);
        filter.setListOfOrders(new ArrayList<OrderPath>());
        return filter;
    }

    public int deleteVariables(FilterDailyPlan filter) throws SOSHibernateException {
        int row = 0;
        List<DBItemDailyPlanWithHistory> listOfPlannedOrders = getDailyPlanWithHistoryList(filter, 0);
        for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {
            String hql = "delete from " + DBItemDailyPlanVariables + " where plannedOrderId = :plannedOrderId";
            Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(hql);
            query.setParameter("plannedOrderId", dbItemDailyPlanWithHistory.getDbItemDailyPlan().getId());
            row = row + sosHibernateSession.executeUpdate(query);
        }
        return row;
    }

    public int delete(FilterDailyPlan filter) throws SOSHibernateException {
        deleteVariables(filter);
        String hql = "delete from " + DBItemDailyPlan + " p " + getWhere(filter);
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public long deleteInterval(FilterDailyPlan filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlan + " p " + getWhere(filter);
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

    public String getWhere(FilterDailyPlan filter) {
        return getWhere(filter, "");
    }

    private String getWhere(FilterDailyPlan filter, String pathField) {
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
        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            where += and + " p.jobschedulerId = :jobschedulerId";
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
        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            where += String.format(and + " p.orderName %s :orderName", SearchStringHelper.getSearchOperator(filter.getOrderName()));
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
            }else {
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

        if (filter.getListOfOrders() != null && filter.getListOfOrders().size() > 0) {
            where += and + "(";
            for (OrderPath orderPath : filter.getListOfOrders()) {
                where += "p.workflow = '" + orderPath.getWorkflow() + "' and " + "p.order_key = '" + orderPath.getOrderId() + "'";
            }
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

    private <T> Query<T> bindParameters(FilterDailyPlan filter, Query<T> query) {
        if (filter.getPlannedStartFrom() != null) {
            query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStartTo() != null) {
            query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStart() != null) {
            query.setParameter("plannedStart", filter.getPlannedStart(), TemporalType.TIMESTAMP);
        }
        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            query.setParameter("jobschedulerId", filter.getJobSchedulerId());
        }
        if (filter.getSubmitted() != null) {
            // query.setParameter("submitted", filter.getSubmitted());
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            query.setParameter("workflow", SearchStringHelper.getSearchPathValue(filter.getWorkflow()));
        }
        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            query.setParameter("orderName", filter.getOrderName());
        }
        if (filter.getOrderKey() != null && !"".equals(filter.getOrderKey())) {
            query.setParameter("orderKey", filter.getOrderKey());
        }
        return query;

    }

    public List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryList(FilterDailyPlan filter, final int limit) throws SOSHibernateException {
        String q = "Select new " + DBItemDailyPlanWithHistory + "(p,o) from " + DBItemDailyPlan + " p left outer join " + DBItemOrder
                + " o on p.orderKey = o.orderKey " + getWhere(filter);
        LOGGER.debug("DailyPlan sql: " + q + " from " + filter.getPlannedStartFrom() + " to " + filter.getPlannedStartTo());
        Query<DBItemDailyPlanWithHistory> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemDailyPlan> getDailyPlanList(FilterDailyPlan filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlan + " p " + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public DBItemDailyPlan getUniqueDailyPlan(FilterDailyPlan filter) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlan + " p " + getWhere(filter);
        Query<DBItemDailyPlan> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        List<DBItemDailyPlan> uniqueDailyPlanItem = sosHibernateSession.getResultList(query);
        if (uniqueDailyPlanItem.size() > 0) {
            return sosHibernateSession.getResultList(query).get(0);
        } else {
            return null;
        }
    }

    public Date getMaxPlannedStart(String jobschedulerId) {
        String q = "select max(plannedStart) from " + DBItemDailyPlan + " where jobschedulerId=:jobschedulerId";
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

    public void delete(DBItemDailyPlan DBItemDailyPlan) throws SOSHibernateException {
        FilterDailyPlan filter = new FilterDailyPlan();
        filter.setOrderKey(DBItemDailyPlan.getOrderKey());
        filter.setJobSchedulerId(DBItemDailyPlan.getJobschedulerId());
        filter.setWorkflow(DBItemDailyPlan.getWorkflow());
        delete(filter);
    }

    public DBItemDailyPlan getUniqueDailyPlan(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException,
            SOSHibernateException {
        FilterDailyPlan filter = new FilterDailyPlan();
        filter.setOrderKey(plannedOrder.getFreshOrder().getId());
        LOGGER.info("----> " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder.getFreshOrder().getScheduledFor()));
        filter.setJobSchedulerId(plannedOrder.getOrderTemplate().getJobschedulerId());
        filter.setWorkflow(plannedOrder.getFreshOrder().getWorkflowPath());
        return getUniqueDailyPlan(filter);
    }

    public void storeVariables(PlannedOrder plannedOrder, Long id) throws SOSHibernateException {
        DBItemDailyPlanVariables dbItemDailyPlanVariables = new DBItemDailyPlanVariables();
        for (Entry<String, String> variable : plannedOrder.getFreshOrder().getArguments().getAdditionalProperties().entrySet()) {
            dbItemDailyPlanVariables.setCreated(new Date());
            dbItemDailyPlanVariables.setModified(new Date());
            dbItemDailyPlanVariables.setPlannedOrderId(id);
            dbItemDailyPlanVariables.setVariableName(variable.getKey());
            dbItemDailyPlanVariables.setVariableValue(variable.getValue());
            sosHibernateSession.save(dbItemDailyPlanVariables);
        }
    }

    public void store(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException,
            ParseException {
        DBItemDailyPlan dbItemDailyPlan = new DBItemDailyPlan();
        dbItemDailyPlan.setOrderName(plannedOrder.getOrderTemplate().getOrderName());
        dbItemDailyPlan.setOrderKey(plannedOrder.getFreshOrder().getId());
        Date start = new Date(plannedOrder.getFreshOrder().getScheduledFor());
        dbItemDailyPlan.setPlannedStart(start);
        if (plannedOrder.getPeriod() != null) {
            dbItemDailyPlan.setPeriodBegin(start, plannedOrder.getPeriod().getBegin());
            dbItemDailyPlan.setPeriodEnd(start, plannedOrder.getPeriod().getEnd());
            dbItemDailyPlan.setRepeatInterval(plannedOrder.getPeriod().getRepeat());
        }
        dbItemDailyPlan.setJobschedulerId(plannedOrder.getOrderTemplate().getJobschedulerId());
        dbItemDailyPlan.setWorkflow(plannedOrder.getFreshOrder().getWorkflowPath());
        dbItemDailyPlan.setSubmitted(false);
        dbItemDailyPlan.setPlanId(plannedOrder.getPlanId());
        dbItemDailyPlan.setCalendarId(plannedOrder.getCalendarId());
        dbItemDailyPlan.setCreated(new Date());
        dbItemDailyPlan.setExpectedEnd(new Date(plannedOrder.getFreshOrder().getScheduledFor() + plannedOrder.getAverageDuration()));
        dbItemDailyPlan.setModified(new Date());
        sosHibernateSession.save(dbItemDailyPlan);
        storeVariables(plannedOrder, dbItemDailyPlan.getId());
    }

}