package com.sos.js7.order.initiator.db;

import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateLockAcquisitionException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.orders.DBItemDailyPlanOrder;
import com.sos.joc.db.orders.DBItemDailyPlanSubmission;
import com.sos.joc.db.orders.DBItemDailyPlanVariable;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
import com.sos.js7.order.initiator.classes.PlannedOrder;

public class DBLayerDailyPlannedOrders {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlannedOrders.class);

    private static final int DAILY_PLAN_LATE_TOLERANCE = 60;

    private static final String DBItemDailyPlannedOrders = DBItemDailyPlanOrder.class.getSimpleName();
    private static final String DBItemDailyPlanVariables = DBItemDailyPlanVariable.class.getSimpleName();
    private SOSHibernateSession sosHibernateSession;

    public DBLayerDailyPlannedOrders(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public void setSession(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemDailyPlanOrder getPlanDbItem(final Long id) throws Exception {
        return (DBItemDailyPlanOrder) sosHibernateSession.get(DBItemDailyPlanOrder.class, id);
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

        String q = "select id from " + DBItemDailyPlannedOrders + " p " + getWhere(filter, "p.schedulePath");
        String hql = "delete from " + DBItemDailyPlanVariables + " where plannedOrderId in (" + q + ")";

        Query<DBItemDailyPlanVariable> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int deleteCascading(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        int row = 0;
        int retryCount = 20;
        do {
            try {
                deleteVariables(filter);
                row = delete(filter);
                if (retryCount != 20) {
                    LOGGER.info("deadlock resolved successfully delete from dpl_variables");
                }
                retryCount = 0;
            } catch (SOSHibernateLockAcquisitionException e) {
                LOGGER.info("Try to resolve deadlock delete from dpl_variables");

                try {
                    java.lang.Thread.sleep(500);
                } catch (InterruptedException e1) {
                }
                retryCount = retryCount - 1;
                if (retryCount == 0) {
                    throw e;
                }
                LOGGER.debug("Retry delete orders as SOSHibernateLockAcquisitionException was thrown. Retry-counter: " + retryCount);

            }
        } while (retryCount > 0);
        return row;
    }

    public int delete(FilterDailyPlannedOrders filter) throws SOSHibernateException {

        String hql = "delete " + DBItemDailyPlannedOrders + " p " + getWhere(filter, "p.schedulePath");
        Query<DBItemDailyPlanOrder> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);

        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public long deleteInterval(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemDailyPlannedOrders + " p " + getWhere(filter, "p.schedulePath");
        int row = 0;
        Query<DBItemDailyPlanOrder> query = sosHibernateSession.createQuery(hql);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public String getOrderListSql(Collection<String> list) {
        StringBuilder sql = new StringBuilder();
        sql.append("p.orderId in (");
        for (String s : list) {
            sql.append("'" + s + "',");
        }
        String s = sql.toString();
        s = s.substring(0, s.length() - 1);
        s = s + ")";

        return " (" + s + ") ";
    }

    private String getCyclicOrderListSql(List<String> list) {
        StringBuilder sql = new StringBuilder(" (");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sql.append(" or ");
            }
            sql.append("p.orderId like '" + list.get(i) + "%'");
        }
        sql.append(")");
        return sql.toString();
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

        if (filter.getPeriodBegin() != null) {
            where += and + " p.periodBegin = :periodBegin";
            and = " and ";
        }

        if (filter.getPeriodEnd() != null) {
            where += and + " p.periodEnd = :periodEnd";
            and = " and ";
        }

        if (filter.getRepeatInterval() != null) {
            where += and + " p.repeatInterval = :repeatInterval";
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

        if (filter.getWorkflowName() != null && !"".equals(filter.getWorkflowName())) {
            where += and + " p.workflowName = :workflowName";
            and = " and ";
        }

        if (filter.getScheduleName() != null && !"".equals(filter.getScheduleName())) {
            where += and + " p.scheduleName = :scheduleName";
            and = " and ";
        }

        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            where += and + " p.orderName = :orderName";
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

        if (filter.getListOfWorkflowNames() != null && filter.getListOfWorkflowNames().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfWorkflowNames(), "p.workflowName");
            and = " and ";
        }
        if (filter.getListOfScheduleNames() != null && filter.getListOfScheduleNames().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfScheduleNames(), "p.scheduleName");
            and = " and ";
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += String.format(and + " p.orderId %s :orderId", SearchStringHelper.getSearchOperator(filter.getOrderId()));
            and = " and ";
        }
        if (filter.getPlannedOrderId() != null) {
            where += and + "p.id=:plannedOrderId";
            and = " and ";
        }

        if (filter.getStartMode() != null) {
            where += and + "p.startMode=:startMode";
            and = " and ";
        }

        if (filter.getIsLate() != null) {
            if (filter.isLate()) {
                where += and + " (o.state is null and p.plannedStart < current_time()  or " + "o.state <> " + DailyPlanOrderStateText.PLANNED
                        .intValue() + " and o.startTime - p.plannedStart >= " + DAILY_PLAN_LATE_TOLERANCE + ")  ";
            } else {
                where += and + " (o.state is null and p.plannedStart > current_time() or " + "o.state is not null and o.state <> "
                        + DailyPlanOrderStateText.PLANNED.intValue() + " and o.startTime - p.plannedStart < " + DAILY_PLAN_LATE_TOLERANCE + ") ";
            }

            and = " and ";
        }
        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (DailyPlanOrderStateText state : filter.getStates()) {
                if (state.intValue() == DailyPlanOrderStateText.PLANNED.intValue()) {
                    where += " p.submitted=0 or";
                } else {
                    if ((state.intValue() == DailyPlanOrderStateText.SUBMITTED.intValue())) {
                        where += " ((o.state is null or o.state <> " + DailyPlanOrderStateText.FINISHED.intValue() + ") and (p.submitted=1))" + " or";
                    } else {
                        where += " o.state = " + state.intValue() + " or";
                    }
                }
            }
            where += " 1=0)";
            and = " and ";
        }

        if (filter.getListOfSubmissionIds() != null && filter.getListOfSubmissionIds().size() > 0) {
            where += and + SearchStringHelper.getLongSetSql(filter.getListOfSubmissionIds(), "p.submissionHistoryId");
            and = " and ";
        }

        if (!"".equals(pathField) && filter.getSetOfScheduleFolders() != null && filter.getSetOfScheduleFolders().size() > 0) {
            where += and + "(";
            for (Folder filterFolder : filter.getSetOfScheduleFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where += " (" + "p.scheduleFolder" + " = '" + filterFolder.getFolder() + "' or " + "p.scheduleFolder" + " like '" + likeFolder
                            + "')";
                } else {
                    where += String.format("p.scheduleFolder" + " %s '" + filterFolder.getFolder() + "'", SearchStringHelper.getSearchOperator(
                            filterFolder.getFolder()));
                }
                where += " or ";
            }
            where += " 0=1)";
            and = " and ";
        }

        if (!"".equals(pathField) && filter.getSetOfWorkflowFolders() != null && filter.getSetOfWorkflowFolders().size() > 0) {
            where += and + "(";
            for (Folder filterFolder : filter.getSetOfWorkflowFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where += " (" + "p.workflowFolder" + " = '" + filterFolder.getFolder() + "' or " + "p.workflowFolder" + " like '" + likeFolder
                            + "')";
                } else {
                    where += String.format("p.workflowFolder" + " %s '" + filterFolder.getFolder() + "'", SearchStringHelper.getSearchOperator(
                            filterFolder.getFolder()));
                }
                where += " or ";
            }
            where += " 0=1)";
            and = " and ";
        }

        boolean hasCyclics = filter.getListOfCyclicOrdersMainParts() != null && filter.getListOfCyclicOrdersMainParts().size() > 0;
        if (filter.getListOfOrders() != null && filter.getListOfOrders().size() > 0) {
            where += and;
            if (hasCyclics) {
                where += " ( ";
            }
            where += getOrderListSql(filter.getListOfOrders());
            if (hasCyclics) {
                where += " or " + getCyclicOrderListSql(filter.getListOfCyclicOrdersMainParts());
                where += ") ";
            }
            and = " and ";
        } else if (hasCyclics) {
            where += and + getCyclicOrderListSql(filter.getListOfCyclicOrdersMainParts());
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterDailyPlannedOrders filter, Query<T> query) {

        if (filter.getPeriodBegin() != null) {
            query.setParameter("periodBegin", filter.getPeriodBegin());
        }
        if (filter.getPeriodEnd() != null) {
            query.setParameter("periodEnd", filter.getPeriodEnd());
        }
        if (filter.getRepeatInterval() != null) {
            query.setParameter("repeatInterval", filter.getRepeatInterval());
        }
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
        if (filter.getPlannedOrderId() != null) {
            query.setParameter("plannedOrderId", filter.getPlannedOrderId());
        }
        if (filter.getSubmitTime() != null) {
            query.setParameter("submitTime", filter.getSubmitTime());
        }

        if (filter.getCalendarId() != null) {
            query.setParameter("calendarId", filter.getCalendarId());
        }

        if (filter.getStartMode() != null) {
            query.setParameter("startMode", filter.getStartMode());
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }

        if (filter.getWorkflowName() != null && !"".equals(filter.getWorkflowName())) {
            query.setParameter("workflowName", filter.getWorkflowName());
        }

        if (filter.getScheduleName() != null && !"".equals(filter.getScheduleName())) {
            query.setParameter("scheduleName", filter.getScheduleName());
        }

        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            query.setParameter("orderName", filter.getOrderName());
        }

        return query;

    }

    public List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryListExecute(FilterDailyPlannedOrders filter, final int limit)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        if (filter.isCyclicStart()) {
            StringBuilder q = new StringBuilder("select min(p.id) ");
            q.append("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
            q.append(getWhere(filter, "p.schedulePath")).append(" ");
            q.append("group by orderName,periodBegin,periodEnd,repeatInterval ");

            hql.append("select p.id as plannedOrderId,p.submissionHistoryId as submissionHistoryId,p.controllerId as controllerId");
            hql.append(",p.workflowName as workflowName, p.workflowPath as workflowPath,p.orderId as orderId,p.orderName as orderName");
            hql.append(",p.scheduleName as scheduleName, p.schedulePath as schedulePath");
            hql.append(",p.calendarId as calendarId,p.submitted as submitted,p.submitTime as submitTime,p.periodBegin as periodBegin");
            hql.append(",p.periodEnd as periodEnd,p.repeatInterval as repeatInterval");
            hql.append(",p.plannedStart as plannedStart, p.expectedEnd as expectedEnd,p.created as plannedOrderCreated");
            hql.append(",o.id as orderHistoryId, o.startTime as startTime, o.endTime as endTime, o.state as state ");
            hql.append("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p left outer join ");
            hql.append(DBLayer.DBITEM_HISTORY_ORDERS).append(" o on p.orderId = o.orderId ");
            hql.append(getWhere(filter, "p.schedulePath")).append(" ");
            hql.append("and p.id in (").append(q).append(") ");
            hql.append(filter.getOrderCriteria());
        } else {
            hql.append("select p.id as plannedOrderId,p.submissionHistoryId as submissionHistoryId,p.controllerId as controllerId");
            hql.append(",p.workflowName as workflowName, p.workflowPath as workflowPath,p.orderId as orderId,p.orderName as orderName");
            hql.append(",p.scheduleName as scheduleName, p.schedulePath as schedulePath");
            hql.append(",p.calendarId as calendarId,p.submitted as submitted,p.submitTime as submitTime,p.periodBegin as periodBegin");
            hql.append(",p.periodEnd as periodEnd,p.repeatInterval as repeatInterval");
            hql.append(",p.plannedStart as plannedStart, p.expectedEnd as expectedEnd,p.created as plannedOrderCreated");
            hql.append(",o.id as orderHistoryId, o.startTime as startTime, o.endTime as endTime, o.state as state ");
            hql.append("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p left outer join ");
            hql.append(DBLayer.DBITEM_HISTORY_ORDERS).append(" o on p.orderId = o.orderId ");
            hql.append(getWhere(filter, "p.schedulePath")).append(" ");
            hql.append(filter.getOrderCriteria());
        }

        Query<DBItemDailyPlanWithHistory> query = sosHibernateSession.createQuery(hql.toString(), DBItemDailyPlanWithHistory.class);
        query = bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Object[] getCyclicOrderLastEntryAndCountTotal(String controllerId, String orderId, Date plannedStartFrom, Date plannedStartTo)
            throws SOSHibernateException {
        StringBuilder sql = new StringBuilder("select ");
        sql.append(quote("b.ORDER_ID")).append(",").append(quote("b.PLANNED_START")).append(",").append(quote("a.TOTAL")).append(" ");
        sql.append("from ( ");
        sql.append("select max(").append(quote("ID")).append(") as ").append(quote("ID"));
        sql.append(",count(").append(quote("ID")).append(") as ").append(quote("TOTAL")).append(" ");
        sql.append("from ").append(DBLayer.TABLE_DPL_ORDERS).append(" ");
        sql.append("where ").append(quote("ORDER_ID")).append(" like :orderId ");
        sql.append("and ").append(quote("CONTROLLER_ID")).append("= :controllerId ");
        if (plannedStartFrom != null) {
            sql.append("and ").append(quote("PLANNED_START")).append(" >= :plannedStartFrom ");
        }
        if (plannedStartTo != null) {
            sql.append("and ").append(quote("PLANNED_START")).append(" < :plannedStartTo");
        }
        sql.append(") a ");
        sql.append("join ").append(DBLayer.TABLE_DPL_ORDERS).append(" b ");
        sql.append("on ").append(quote("a.ID")).append("=").append(quote("b.ID"));

        Query<Object[]> query = sosHibernateSession.createNativeQuery(sql.toString());
        query.setParameter("orderId", orderId + "%");
        query.setParameter("controllerId", controllerId);
        if (plannedStartFrom != null) {
            query.setParameter("plannedStartFrom", plannedStartFrom);
        }
        if (plannedStartTo != null) {
            query.setParameter("plannedStartTo", plannedStartTo);
        }
        List<Object[]> result = sosHibernateSession.getResultList(query);
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private String quote(String fieldName) {
        return sosHibernateSession.getFactory().quoteColumn(fieldName);
    }

    public List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryList(FilterDailyPlannedOrders filter, final int limit)
            throws SOSHibernateException {

        if (filter.getListOfOrders() != null) {
            List<DBItemDailyPlanWithHistory> resultList = new ArrayList<DBItemDailyPlanWithHistory>();
            int size = filter.getListOfOrders().size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                ArrayList<String> copy = (ArrayList<String>) filter.getListOfOrders().stream().collect(Collectors.toList());
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                        filter.setListOfOrders(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                    } else {
                        filter.setListOfOrders(copy.subList(i, size));
                    }
                    resultList.addAll(getDailyPlanWithHistoryListExecute(filter, limit));
                }
                return resultList;
            } else {
                return getDailyPlanWithHistoryListExecute(filter, limit);
            }
        } else {
            return getDailyPlanWithHistoryListExecute(filter, limit);
        }
    }

    public List<DBItemDailyPlanOrder> getDailyPlanListExecute(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlannedOrders + " p " + getWhere(filter, "p.schedulePath") + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlanOrder> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemDailyPlanOrder> getDailyPlanList(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {

        if (filter.getListOfOrders() != null) {
            List<DBItemDailyPlanOrder> resultList = new ArrayList<DBItemDailyPlanOrder>();
            int size = filter.getListOfOrders().size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                ArrayList<String> copy = (ArrayList<String>) filter.getListOfOrders().stream().collect(Collectors.toList());
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                        filter.setListOfOrders(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                    } else {
                        filter.setListOfOrders(copy.subList(i, size));
                    }
                    resultList.addAll(getDailyPlanListExecute(filter, limit));
                }
                return resultList;
            } else {
                return getDailyPlanListExecute(filter, limit);
            }
        } else {
            return getDailyPlanListExecute(filter, limit);
        }
    }

    public DBItemDailyPlanOrder getUniqueDailyPlan(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlannedOrders + " p " + getWhere(filter);
        Query<DBItemDailyPlanOrder> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        List<DBItemDailyPlanOrder> result = sosHibernateSession.getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
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

    public void delete(DBItemDailyPlanOrder dbItemDailyPlanOrders) throws SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setOrderId(dbItemDailyPlanOrders.getOrderId());
        filter.setControllerId(dbItemDailyPlanOrders.getControllerId());
        filter.addWorkflowName(dbItemDailyPlanOrders.getWorkflowName());
        deleteCascading(filter);
    }

    public DBItemDailyPlanOrder getUniqueDailyPlan(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException,
            SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setPlannedStart(new Date(plannedOrder.getFreshOrder().getScheduledFor()));
        LOGGER.trace("----> " + plannedOrder.getFreshOrder().getScheduledFor() + ":" + new Date(plannedOrder.getFreshOrder().getScheduledFor()));
        filter.setControllerId(plannedOrder.getControllerId());
        filter.setWorkflowName(plannedOrder.getFreshOrder().getWorkflowPath());
        filter.setOrderName(plannedOrder.getOrderName());
        return getUniqueDailyPlan(filter);
    }

    public void storeVariables(PlannedOrder plannedOrder, Long id) throws SOSHibernateException, JsonProcessingException {
        DBItemDailyPlanVariable dbItemDailyPlanVariables = new DBItemDailyPlanVariable();
        if (plannedOrder.getFreshOrder().getArguments() != null) {
            String variablesJson = new ObjectMapper().writeValueAsString(plannedOrder.getFreshOrder().getArguments());

            dbItemDailyPlanVariables.setCreated(JobSchedulerDate.nowInUtc());
            dbItemDailyPlanVariables.setModified(JobSchedulerDate.nowInUtc());
            dbItemDailyPlanVariables.setPlannedOrderId(id);
            dbItemDailyPlanVariables.setVariableValue(variablesJson);
            sosHibernateSession.save(dbItemDailyPlanVariables);
        }
    }

    public Long store(PlannedOrder plannedOrder, String id, Integer nr, Integer size) throws JocConfigurationException, DBConnectionRefusedException,
            SOSHibernateException, ParseException, JsonProcessingException {

        DBItemDailyPlanOrder dbItemDailyPlannedOrders = new DBItemDailyPlanOrder();
        dbItemDailyPlannedOrders.setSchedulePath(plannedOrder.getSchedule().getPath());
        dbItemDailyPlannedOrders.setScheduleName(Paths.get(plannedOrder.getSchedule().getPath()).getFileName().toString());
        dbItemDailyPlannedOrders.setOrderName(plannedOrder.getOrderName());
        dbItemDailyPlannedOrders.setOrderId(plannedOrder.getFreshOrder().getId());

        Date start = new Date(plannedOrder.getFreshOrder().getScheduledFor());
        dbItemDailyPlannedOrders.setPlannedStart(start);
        if (plannedOrder.getPeriod().getSingleStart() == null) {
            dbItemDailyPlannedOrders.setStartMode(1);
            dbItemDailyPlannedOrders.setPeriodBegin(start, plannedOrder.getPeriod().getBegin());
            dbItemDailyPlannedOrders.setPeriodEnd(start, plannedOrder.getPeriod().getEnd());
            dbItemDailyPlannedOrders.setRepeatInterval(plannedOrder.getPeriod().getRepeat());
        } else {
            dbItemDailyPlannedOrders.setStartMode(0);
        }

        String workflowFolder = Paths.get(plannedOrder.getSchedule().getWorkflowPath()).getParent().toString().replace('\\', '/');
        String scheduleFolder = Paths.get(plannedOrder.getSchedule().getPath()).getParent().toString().replace('\\', '/');

        dbItemDailyPlannedOrders.setControllerId(plannedOrder.getControllerId());
        dbItemDailyPlannedOrders.setWorkflowPath(plannedOrder.getSchedule().getWorkflowPath());
        dbItemDailyPlannedOrders.setWorkflowFolder(workflowFolder);
        dbItemDailyPlannedOrders.setScheduleFolder(scheduleFolder);
        dbItemDailyPlannedOrders.setWorkflowName(plannedOrder.getFreshOrder().getWorkflowPath());
        dbItemDailyPlannedOrders.setSubmitted(false);
        dbItemDailyPlannedOrders.setSubmissionHistoryId(plannedOrder.getSubmissionHistoryId());
        dbItemDailyPlannedOrders.setCalendarId(plannedOrder.getCalendarId());
        dbItemDailyPlannedOrders.setCreated(JobSchedulerDate.nowInUtc());
        dbItemDailyPlannedOrders.setExpectedEnd(new Date(plannedOrder.getFreshOrder().getScheduledFor() + plannedOrder.getAverageDuration()));
        dbItemDailyPlannedOrders.setModified(JobSchedulerDate.nowInUtc());

        if (nr != 0) {
            String nrAsString = "00000" + String.valueOf(nr);
            nrAsString = nrAsString.substring(nrAsString.length() - 5);

            String sizeAsString = String.valueOf(size);
            dbItemDailyPlannedOrders.setOrderId(dbItemDailyPlannedOrders.getOrderId().replaceAll("<nr.....>", nrAsString));
            dbItemDailyPlannedOrders.setOrderId(dbItemDailyPlannedOrders.getOrderId().replaceAll("<size>", sizeAsString));
        }
        dbItemDailyPlannedOrders.setOrderId(dbItemDailyPlannedOrders.getOrderId().replaceAll("<id.*>", id));
        plannedOrder.getFreshOrder().setId(dbItemDailyPlannedOrders.getOrderId());
        sosHibernateSession.save(dbItemDailyPlannedOrders);
        storeVariables(plannedOrder, dbItemDailyPlannedOrders.getId());
        return dbItemDailyPlannedOrders.getId();
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
        LOGGER.debug("Update submitting on controller:" + hql);

        Query<DBItemDailyPlanSubmission> query = sosHibernateSession.createQuery(hql);

        bindParameters(filter, query);
        int retryCount = 20;
        int row = -1;
        do {
            try {
                row = sosHibernateSession.executeUpdate(query);
                if (retryCount != 20) {
                    LOGGER.info("deadlock resolved successfully:" + hql);
                }
                retryCount = 0;
            } catch (SOSHibernateLockAcquisitionException e) {
                LOGGER.info("Try to resolve deadlock:" + hql);
                retryCount = retryCount - 1;
                try {
                    java.lang.Thread.sleep(500);
                } catch (InterruptedException e1) {
                }
                if (retryCount == 0) {
                    throw e;
                }
                LOGGER.debug("Retry update orders as SOSHibernateLockAcquisitionException was thrown. Retry-counter: " + retryCount);

            }
        } while (retryCount > 0);

        return row;
    }

    public void store(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException, SOSHibernateException,
            ParseException, JsonProcessingException {
        store(plannedOrder, Long.valueOf(Instant.now().toEpochMilli()).toString().substring(3), 0, 0);
    }

    public DBItemDailyPlanOrder insertFrom(DBItemDailyPlanOrder dbItemDailyPlanOrders) throws SOSHibernateException {
        dbItemDailyPlanOrders.setSubmitted(false);
        dbItemDailyPlanOrders.setCreated(JobSchedulerDate.nowInUtc());
        dbItemDailyPlanOrders.setModified(JobSchedulerDate.nowInUtc());
        sosHibernateSession.save(dbItemDailyPlanOrders);
        String newOrderId = DailyPlanHelper.modifiedOrderId(dbItemDailyPlanOrders.getOrderId(), dbItemDailyPlanOrders.getId());
        dbItemDailyPlanOrders.setOrderId(newOrderId);
        sosHibernateSession.update(dbItemDailyPlanOrders);

        return dbItemDailyPlanOrders;
    }

    public DBItemDailyPlanOrder addCyclicOrderIds(List<String> orderIds, String orderId, String controllerId, String timeZone, String periodBegin)
            throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("addCyclicOrderIds");

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(controllerId);
            filter.setOrderId(orderId);

            List<DBItemDailyPlanOrder> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);

            if (listOfPlannedOrders.size() == 1) {

                DBItemDailyPlanOrder dbItemDailyPlanOrder = listOfPlannedOrders.get(0);
                if (dbItemDailyPlanOrder.getStartMode() == 1) {

                    FilterDailyPlannedOrders filterCyclic = new FilterDailyPlannedOrders();
                    filterCyclic.setControllerId(controllerId);
                    filterCyclic.setRepeatInterval(dbItemDailyPlanOrder.getRepeatInterval());
                    filterCyclic.setPeriodBegin(dbItemDailyPlanOrder.getPeriodBegin());
                    filterCyclic.setPeriodEnd(dbItemDailyPlanOrder.getPeriodEnd());
                    filterCyclic.setWorkflowName(dbItemDailyPlanOrder.getWorkflowName());
                    filterCyclic.setScheduleName(dbItemDailyPlanOrder.getScheduleName());
                    filterCyclic.setOrderName(dbItemDailyPlanOrder.getOrderName());
                    filterCyclic.setDailyPlanDate(dbItemDailyPlanOrder.getDailyPlanDate(timeZone), timeZone, periodBegin);

                    List<DBItemDailyPlanOrder> listOfPlannedCyclicOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filterCyclic, 0);
                    for (DBItemDailyPlanOrder dbItemDailyPlanOrders : listOfPlannedCyclicOrders) {
                        if (!dbItemDailyPlanOrders.getOrderId().equals(orderId)) {
                            orderIds.add(dbItemDailyPlanOrders.getOrderId());
                        }
                    }
                }
                return dbItemDailyPlanOrder;

            } else {
                LOGGER.warn("Expected one record for order-id " + filter.getOrderId());
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId());
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}