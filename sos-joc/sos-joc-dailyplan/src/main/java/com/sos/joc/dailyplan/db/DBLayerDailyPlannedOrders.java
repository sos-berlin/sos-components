package com.sos.joc.dailyplan.db;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.function.date.SOSHibernateSecondsDiff;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.SearchStringHelper;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;

public class DBLayerDailyPlannedOrders {

    public static final Integer START_MODE_SINGLE = Integer.valueOf(0);
    public static final Integer START_MODE_CYCLIC = Integer.valueOf(1);

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlannedOrders.class);

    private static final String WHERE_PARAM_CURRENT_TIME = "currentTime";
    /** rerun interval in seconds */
    private static final long RERUN_INTERVAL = 1;
    private static final int MAX_RERUNS = 3;

    private SOSHibernateSession session;
    private boolean whereHasCurrentTime;

    public DBLayerDailyPlannedOrders(SOSHibernateSession session) {
        this.session = session;
    }

    public void setSession(SOSHibernateSession session) {
        this.session = session;
    }

    public DBItemDailyPlanOrder getPlanDbItem(final Long id) throws Exception {
        return (DBItemDailyPlanOrder) session.get(DBItemDailyPlanOrder.class, id);
    }

    public FilterDailyPlannedOrders resetFilter() {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setControllerId("");
        filter.setOrderId("");
        filter.setPlannedStart(null);
        filter.setSubmitted(false);
        return filter;
    }

    public void deleteCascading(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        int size = filter.getOrderIds() == null ? 0 : filter.getOrderIds().size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            FilterDailyPlannedOrders filterCopy = filter.copy();
            filterCopy.setOrderPlannedStartFrom(null);
            filterCopy.setOrderPlannedStartTo(null);

            List<String> copy = filterCopy.getOrderIds().stream().collect(Collectors.toList());
            for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                    filterCopy.setOrderIds(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                } else {
                    filterCopy.setOrderIds(copy.subList(i, size));
                }
                executeDeleteCascading(filterCopy);
            }
        } else {
            FilterDailyPlannedOrders filterCopy = filter.copy();
            if (size > 0) {
                filterCopy.setOrderPlannedStartFrom(null);
                filterCopy.setOrderPlannedStartTo(null);
            }
            executeDeleteCascading(filterCopy);
        }
    }

    public int deleteSingleCascading(DBItemDailyPlanOrder item) throws SOSHibernateException {
        // variables
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId=:orderId");

        Query<DBItemDailyPlanVariable> vQuery = session.createQuery(hql);
        vQuery.setParameter("controllerId", item.getControllerId());
        vQuery.setParameter("orderId", item.getOrderId());
        executeUpdate("deleteCascading(variables)", vQuery);

        // order
        hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" where id=:id");
        Query<DBItemDailyPlanOrder> oQuery = session.createQuery(hql);
        oQuery.setParameter("id", item.getId());
        return executeUpdate("deleteCascading(order)", oQuery);
    }

    public int deleteCascading(DBItemDailyPlanOrder item, boolean submitted) throws SOSHibernateException {
        int result = 0;
        if (item.getStartMode().equals(START_MODE_SINGLE)) {
            if (item.getSubmitted() == submitted) {
                result = deleteSingleCascading(item);
            }
        } else {
            String mainPart = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
            deleteByCyclicMainPart(item.getControllerId(), mainPart, submitted);
            Long count = getCountCyclicOrdersByMainPart(item.getControllerId(), mainPart);
            if (count == null || count.equals(0L)) {
                deleteVariablesByCyclicMainPart(item.getControllerId(), mainPart);
            }
        }
        return result;
    }

    private int deleteByCyclicMainPart(String controllerId, String mainPart, boolean submitted) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and submitted=:submitted ");
        hql.append("and orderId like :mainPart");

        Query<DBItemDailyPlanVariable> query = session.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("submitted", submitted);
        query.setParameter("mainPart", mainPart + "%");
        return executeUpdate("deleteByCyclicMainPart", query);
    }

    public int deleteVariablesByCyclicMainPart(String controllerId, String mainPart) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId like :mainPart");

        Query<DBItemDailyPlanVariable> query = session.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("mainPart", mainPart + "%");
        return executeUpdate("deleteVariablesByCyclicMainPart", query);
    }

    public int delete(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" where id=:id");
        Query<DBItemDailyPlanOrder> query = session.createQuery(hql);
        query.setParameter("id", id);
        return executeUpdate("delete", query);
    }

    public Long getCountCyclicOrdersByMainPart(String controllerId, String mainPart) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId like :mainPart ");

        Query<Long> query = session.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("mainPart", mainPart + "%");
        return session.getSingleValue(query);
    }

    private void executeDeleteCascading(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        // two transactions to avoid deadlock
        Globals.beginTransaction(session);
        try {
            executeDeleteVariables(filter);
            Globals.commit(session);
            Globals.beginTransaction(session);
            executeDelete(filter);
            Globals.commit(session);
        } catch (SOSHibernateException e) {
            Globals.rollback(session);
            throw e;
        }
    }

    private void executeDeleteVariables(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        StringBuilder subSelect = new StringBuilder("select orderId from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
        subSelect.append(getWhere(filter, "p.schedulePath", true));
        
        Query<String> subQuery = session.createQuery(subSelect);
        subQuery = bindParameters(filter, subQuery);
        List<String> orderIds = session.getResultList(subQuery);
        
        int size = orderIds == null ? 0 : orderIds.size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                executeDeleteVariables(SOSHibernate.getInClausePartition(i, orderIds), filter.getControllerId());
            }
        } else {
            executeDeleteVariables(orderIds, filter.getControllerId());
        }

        // subselect creates deadlock
//        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
//        hql.append("where orderId in (").append(subSelect).append(") ");
//        if (!SOSString.isEmpty(filter.getControllerId())) {
//            hql.append("and controllerId=:controllerId ");
//        }
//
//        Query<DBItemDailyPlanVariable> query = session.createQuery(hql);
//        query = bindParameters(filter, query);
//        return executeUpdate("executeDeleteVariables", query);
    }
    
    private void executeDeleteVariables(List<String> orderIds, String controllerId) throws SOSHibernateException {
        if (orderIds != null && !orderIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
            hql.append("where orderId in (:orderIds) ");
            if (!SOSString.isEmpty(controllerId)) {
                hql.append("and controllerId=:controllerId ");
            }
            
            Query<DBItemDailyPlanVariable> query = session.createQuery(hql);
            query.setParameterList("orderIds", orderIds);
            if (!SOSString.isEmpty(controllerId)) {
                query.setParameter("controllerId", controllerId);
            }
            executeUpdate("executeDeleteVariables", query);
        }
    }

    private int executeDelete(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
        hql.append(getWhere(filter, "p.schedulePath", true));

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql);
        bindParameters(filter, query);
        return executeUpdate("executeDelete", query);
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

    private String getWhere(FilterDailyPlannedOrders filter) {
        return getWhere(filter, "", true);
    }

    private String getWhere(FilterDailyPlannedOrders filter, String pathField, boolean useHistoryOrderState) {
        whereHasCurrentTime = false;

        StringBuilder where = new StringBuilder();
        String and = "";

        if (filter.getPlannedStart() != null) {
            where.append(and).append(" p.plannedStart = :plannedStart");
            and = " and ";
        }

        if (filter.getPeriodBegin() != null) {
            where.append(and).append(" p.periodBegin = :periodBegin");
            and = " and ";
        }

        if (filter.getPeriodEnd() != null) {
            where.append(and).append(" p.periodEnd = :periodEnd");
            and = " and ";
        }

        if (filter.getRepeatInterval() != null) {
            where.append(and).append(" p.repeatInterval = :repeatInterval");
            and = " and ";
        }

        if (filter.getOrderPlannedStartFrom() != null) {
            where.append(and).append(" p.plannedStart >= :plannedStartFrom");
            and = " and ";
        }
        if (filter.getOrderPlannedStartTo() != null) {
            where.append(and).append(" p.plannedStart < :plannedStartTo");
            and = " and ";
        }
        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            where.append(and).append(" p.controllerId = :controllerId");
            and = " and ";
        } else if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
            where.append(and).append(" p.controllerId in (:controllerIds)");
            and = " and ";
        }

        if (filter.getWorkflowName() != null && !"".equals(filter.getWorkflowName())) {
            where.append(and).append(" p.workflowName = :workflowName");
            and = " and ";
        }

        if (filter.getScheduleName() != null && !"".equals(filter.getScheduleName())) {
            where.append(and).append(" p.scheduleName = :scheduleName");
            and = " and ";
        }

        if (filter.getOrderName() != null && !"".equals(filter.getOrderName())) {
            where.append(and).append(" p.orderName = :orderName");
            and = " and ";
        }

        if (filter.getCalendarId() != null) {
            where.append(and).append(" p.calendarId = :calendarId");
            and = " and ";
        }
        if (filter.getSubmitted() != null) {
            where.append(and).append("  p.submitted = :submitted");
            and = " and ";
        }

        if (filter.getWorkflowNames() != null && filter.getWorkflowNames().size() > 0) {
            where.append(and).append(SearchStringHelper.getStringListSql(filter.getWorkflowNames(), "p.workflowName"));
            and = " and ";
        }
        if (filter.getScheduleNames() != null && filter.getScheduleNames().size() > 0) {
            where.append(and).append(SearchStringHelper.getStringListSql(filter.getScheduleNames(), "p.scheduleName"));
            and = " and ";
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where.append(String.format(and + " p.orderId %s :orderId", SearchStringHelper.getSearchOperator(filter.getOrderId())));
            and = " and ";
        }
        if (filter.getPlannedOrderId() != null) {
            where.append(and).append("p.id=:plannedOrderId");
            and = " and ";
        }

        if (filter.getStartMode() != null) {
            where.append(and).append("p.startMode=:startMode");
            and = " and ";
        }

        boolean isLate = filter.getIsLate() != null && filter.isLate();
        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where.append(and).append(" ");

            DailyPlanOrderStateText state = filter.getStates().get(0);
            switch (state) {
            case PLANNED:
                where.append("p.submitted=0 ");
                if (useHistoryOrderState) {
                    if (isLate) {
                        whereHasCurrentTime = true;

                        where.append("and (");
                        // TODO o.state ???=
                        where.append("(");
                        where.append("o.state <> ").append(DailyPlanOrderStateText.PLANNED.intValue()).append(" ");
                        where.append("and ").append(getLateToleranceStatement());
                        where.append(") ");
                        where.append("or ");
                        where.append("(o.state is null and p.plannedStart < :").append(WHERE_PARAM_CURRENT_TIME).append(") ");
                        where.append(") ");
                    }
                }
                break;
            case FINISHED:
                where.append("p.submitted=1 ");
                if (useHistoryOrderState) {
                    where.append("and o.state=").append(state.intValue()).append(" ");
                    if (isLate) {
                        where.append("and (").append(getLateToleranceStatement()).append(") ");
                    }
                }
                break;
            case SUBMITTED:
                where.append("p.submitted=1 ");
                if (useHistoryOrderState) {
                    if (isLate) {
                        whereHasCurrentTime = true;

                        where.append("and (");
                        where.append("(");
                        where.append("o.state <> ").append(DailyPlanOrderStateText.FINISHED.intValue()).append(" ");
                        where.append("and ").append(getLateToleranceStatement());
                        where.append(") ");
                        where.append("or ");
                        where.append("(o.state is null and p.plannedStart < :").append(WHERE_PARAM_CURRENT_TIME).append(") ");
                        where.append(") ");
                    } else {
                        where.append("and (o.state <> ").append(DailyPlanOrderStateText.FINISHED.intValue()).append(" or o.state is null) ");
                    }
                }
                break;
            }
            and = " and ";
        } else if (isLate) {
            if (useHistoryOrderState) {
                whereHasCurrentTime = true;

                where.append(and).append("(");
                where.append("(");
                where.append("o.state <> ").append(DailyPlanOrderStateText.PLANNED.intValue()).append(" ");
                where.append("and ").append(getLateToleranceStatement());
                where.append(") ");
                where.append("or ");
                where.append("(o.state is null and p.plannedStart < :").append(WHERE_PARAM_CURRENT_TIME).append(") ");
                where.append(") ");
                and = " and ";
            }
        }

        if (filter.getSubmissionIds() != null && filter.getSubmissionIds().size() > 0) {
            // where.append(and).append(SearchStringHelper.getLongSetSql(filter.getSubmissionIds(), "p.submissionHistoryId"));
            where.append(and);
            if (filter.getSubmissionIds().size() == 1) {
                where.append("p.submissionHistoryId=:submissionHistoryId ");
            } else {
                where.append("p.submissionHistoryId in (:submissionHistoryIds) ");
            }
            and = " and ";
        } else {

            if (filter.getSubmissionForDate() != null) {
                where.append(and);
                where.append("p.submissionHistoryId in (");
                where.append("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS);
                where.append(" where submissionForDate=:submissionForDate");
                where.append(")");
                and = " and ";
            } else if (filter.getSubmissionForDateFrom() != null) {
                if (filter.getSubmissionForDateTo() == null) {
                    where.append(and);
                    where.append("p.submissionHistoryId in (");
                    where.append("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS);
                    where.append(" where submissionForDate >= :submissionForDateFrom");
                    where.append(")");
                    and = " and ";
                } else if (filter.getSubmissionForDateFrom().equals(filter.getSubmissionForDateTo())) {
                    where.append(and);
                    where.append("p.submissionHistoryId in (");
                    where.append("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS);
                    where.append(" where submissionForDate=:submissionForDateFrom");
                    where.append(")");
                    and = " and ";
                } else {
                    where.append(and);
                    where.append("p.submissionHistoryId in (");
                    where.append("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS);
                    where.append(" where submissionForDate >= :submissionForDateFrom");
                    where.append(" and submissionForDate <= :submissionForDateTo");
                    where.append(")");
                    and = " and ";
                }
            }
        }

        if (!"".equals(pathField) && filter.getScheduleFolders() != null && filter.getScheduleFolders().size() > 0) {
            where.append(and).append("(");
            for (Folder filterFolder : filter.getScheduleFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where.append(" (" + "p.scheduleFolder" + " = '" + filterFolder.getFolder() + "' or " + "p.scheduleFolder" + " like '" + likeFolder
                            + "')");
                } else {
                    where.append(String.format("p.scheduleFolder" + " %s '" + filterFolder.getFolder() + "'", SearchStringHelper.getSearchOperator(
                            filterFolder.getFolder())));
                }
                where.append(" or ");
            }
            where.append(" 0=1)");
            and = " and ";
        }

        if (!"".equals(pathField) && filter.getWorkflowFolders() != null && filter.getWorkflowFolders().size() > 0) {
            where.append(and).append("(");
            for (Folder filterFolder : filter.getWorkflowFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where.append(" (" + "p.workflowFolder" + " = '" + filterFolder.getFolder() + "' or " + "p.workflowFolder" + " like '" + likeFolder
                            + "')");
                } else {
                    where.append(String.format("p.workflowFolder" + " %s '" + filterFolder.getFolder() + "'", SearchStringHelper.getSearchOperator(
                            filterFolder.getFolder())));
                }
                where.append(" or ");
            }
            where.append(" 0=1)");
            and = " and ";
        }

        boolean hasCyclics = filter.getCyclicOrdersMainParts() != null && filter.getCyclicOrdersMainParts().size() > 0;
        if (filter.getOrderIds() != null && filter.getOrderIds().size() > 0) {
            where.append(and);
            if (hasCyclics) {
                where.append(" ( ");
            }
            where.append(" p.orderId in (:orderIds) ");
            if (hasCyclics) {
                where.append(" or ").append(getCyclicOrderListSql(filter.getCyclicOrdersMainParts()));
                where.append(") ");
            }
            and = " and ";
        } else if (hasCyclics) {
            where.append(and).append(getCyclicOrderListSql(filter.getCyclicOrdersMainParts()));
            and = " and ";
        }

        if (!"".equals(where.toString().trim())) {
            return "where " + where.toString();
        }
        return where.toString();
    }

    private String getLateToleranceStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(SOSHibernateSecondsDiff.getFunction("p.plannedStart", "o.startTime"));
        sb.append(" >= ");
        sb.append(DBItemDailyPlanWithHistory.DAILY_PLAN_LATE_TOLERANCE);
        return sb.toString();
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
        if (filter.getOrderPlannedStartFrom() != null) {
            query.setParameter("plannedStartFrom", filter.getOrderPlannedStartFrom());
        }
        if (filter.getOrderPlannedStartTo() != null) {
            query.setParameter("plannedStartTo", filter.getOrderPlannedStartTo());
        }
        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            query.setParameter("controllerId", filter.getControllerId());
        } else if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
            query.setParameterList("controllerIds", filter.getControllerIds());
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
        if (filter.getOrderIds() != null && filter.getOrderIds().size() > 0) {
            query.setParameterList("orderIds", filter.getOrderIds());
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
        if (filter.getSubmissionIds() != null && filter.getSubmissionIds().size() > 0) {
            if (filter.getSubmissionIds().size() == 1) {
                query.setParameter("submissionHistoryId", filter.getSubmissionIds().get(0));
            } else {
                query.setParameterList("submissionHistoryIds", filter.getSubmissionIds());
            }
        } else {
            if (filter.getSubmissionForDate() != null) {
                query.setParameter("submissionForDate", filter.getSubmissionForDate());
            } else if (filter.getSubmissionForDateFrom() != null) {
                query.setParameter("submissionForDateFrom", filter.getSubmissionForDateFrom());
                if (filter.getSubmissionForDateTo() != null && !filter.getSubmissionForDateFrom().equals(filter.getSubmissionForDateTo())) {
                    query.setParameter("submissionForDateTo", filter.getSubmissionForDateTo());
                }
            }
        }

        if (whereHasCurrentTime) {
            query.setParameter(WHERE_PARAM_CURRENT_TIME, new Date());
        }

        return query;

    }

    private List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryListExecute(FilterDailyPlannedOrders filter, final int limit)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        if (filter.isCyclicStart()) {
            StringBuilder q = new StringBuilder("select max(p.id) ");
            boolean useHistoryOrderState = true;
            if (filter.getStates() != null && filter.getStates().size() > 0) {
                DailyPlanOrderStateText state = filter.getStates().get(0);
                switch (state) {
                case PLANNED:
                    boolean isLate = filter.getIsLate() != null && filter.isLate();
                    if (!isLate) {
                        useHistoryOrderState = false;
                    }
                    break;
                case FINISHED:
                    useHistoryOrderState = false;
                default:
                    break;
                }
            }

            q.append("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
            if (useHistoryOrderState) {
                q.append("left outer join ");
                q.append(DBLayer.DBITEM_HISTORY_ORDERS).append(" o on p.orderId = o.orderId ");
            }
            q.append(getWhere(filter, "p.schedulePath", useHistoryOrderState)).append(" ");
            //q.append("group by p.submissionHistoryId,p.repeatInterval,p.periodBegin,p.periodEnd,p.orderName,p.scheduleName,p.workflowName ");
            q.append("group by p.controllerId, substring(p.orderId, 1, " + OrdersHelper.mainOrderIdLength + ") ");
            
            hql.append("select p.id as plannedOrderId,p.submissionHistoryId as submissionHistoryId,p.controllerId as controllerId");
            hql.append(",p.workflowName as workflowName, p.workflowPath as workflowPath,p.orderId as orderId,p.orderName as orderName");
            hql.append(",p.scheduleName as scheduleName, p.schedulePath as schedulePath");
            hql.append(",p.calendarId as calendarId,p.submitted as submitted,p.submitTime as submitTime,p.periodBegin as periodBegin");
            hql.append(",p.periodEnd as periodEnd,p.repeatInterval as repeatInterval");
            hql.append(",p.plannedStart as plannedStart, p.expectedEnd as expectedEnd,p.created as plannedOrderCreated");
            hql.append(",o.id as orderHistoryId, o.startTime as startTime, o.endTime as endTime, o.state as state ");
            hql.append("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p left outer join ");
            hql.append(DBLayer.DBITEM_HISTORY_ORDERS).append(" o on p.orderId = o.orderId ");
            hql.append(getWhere(filter, "p.schedulePath", true)).append(" ");
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
            hql.append(getWhere(filter, "p.schedulePath", true)).append(" ");
            hql.append(filter.getOrderCriteria());
        }

        Query<DBItemDailyPlanWithHistory> query = session.createQuery(hql.toString(), DBItemDailyPlanWithHistory.class);
        query = bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return session.getResultList(query);
    }

    public Object[] getCyclicOrderMinEntryAndCountTotal(String controllerId, String mainOrderId, Date plannedStartFrom, Date plannedStartTo)
            throws SOSHibernateException {
        StringBuilder sql = new StringBuilder("select ");
        sql.append(quote("a.TOTAL"));
        sql.append(",").append(quote("b.ORDER_ID"));
        sql.append(",").append(quote("b.PLANNED_START"));
        sql.append(",").append(quote("b.EXPECTED_END")).append(" ");
        sql.append("from ( ");
        sql.append("select min(").append(quote("ID")).append(") as ").append(quote("ID"));
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
        //TODO 6.4.5.Final
        Query<Object[]> query = session.createNativeQuery(sql.toString(), Object[].class);
        query.setParameter("orderId", mainOrderId + "%");
        query.setParameter("controllerId", controllerId);
        if (plannedStartFrom != null) {
            query.setParameter("plannedStartFrom", plannedStartFrom);
        }
        if (plannedStartTo != null) {
            query.setParameter("plannedStartTo", plannedStartTo);
        }
        List<Object[]> result = session.getResultList(query);
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    public Date getCyclicMinPlannedStart(String controllerId, String mainOrderId, Date plannedStartFrom, Date plannedStartTo)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select min(plannedStart) ");
        hql.append("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId like :orderId ");
        if (plannedStartFrom != null) {
            hql.append("and plannedStart >= :plannedStartFrom ");
        }
        if (plannedStartTo != null) {
            hql.append("and plannedStart < :plannedStartTo ");
        }

        Query<Date> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("orderId", mainOrderId + "%");
        if (plannedStartFrom != null) {
            query.setParameter("plannedStartFrom", plannedStartFrom);
        }
        if (plannedStartTo != null) {
            query.setParameter("plannedStartTo", plannedStartTo);
        }
        return session.getSingleResult(query);
    }

    private String quote(String fieldName) {
        return session.getFactory().quoteColumn(fieldName);
    }

    public List<DBItemDailyPlanWithHistory> getDailyPlanWithHistoryList(FilterDailyPlannedOrders filter, final int limit)
            throws SOSHibernateException {

        // current - set max SubmissionIds to SOSHibernate.LIMIT_IN_CLAUSE
        // TODO - use all submissionsIds (when orderIds > SOSHibernate.LIMIT_IN_CLAUSE ???), extra a submissions web service ?
        if (filter.getSubmissionIds() != null && filter.getSubmissionIds().size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            filter.setSubmissionIds(SOSHibernate.getInClausePartition(0, filter.getSubmissionIds()));
        }

        if (filter.getOrderIds() != null) {
            List<DBItemDailyPlanWithHistory> resultList = new ArrayList<DBItemDailyPlanWithHistory>();
            int size = filter.getOrderIds().size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                List<String> copy = filter.getOrderIds().stream().collect(Collectors.toList());
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                        filter.setOrderIds(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                    } else {
                        filter.setOrderIds(copy.subList(i, size));
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

    public List<DBItemDailyPlanOrder> getDailyPlanOrdersBySubmission(Long submissionHistoryId, boolean submitted) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where submissionHistoryId=:submissionHistoryId ");
        hql.append("and submitted=:submitted ");

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        query.setParameter("submissionHistoryId", submissionHistoryId);
        query.setParameter("submitted", submitted);
        return session.getResultList(query);
    }

    public List<DBItemDailyPlanOrder> getDailyPlanOrders(String controllerId, String workflowName, Date plannedStart) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and workflowName=:workflowName ");
        hql.append("and plannedStart=:plannedStart ");

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("workflowName", workflowName);
        query.setParameter("plannedStart", plannedStart);
        return session.getResultList(query);
    }

    public List<DBItemDailyPlanOrder> getDailyPlanOrders(String controllerId, List<String> orderIds) throws SOSHibernateException {
        if (orderIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemDailyPlanOrder> result = new ArrayList<>();
            for (int i = 0; i < orderIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.addAll(getDailyPlanOrders(controllerId, SOSHibernate.getInClausePartition(i, orderIds)));
            }
            return result;
        } else {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
            hql.append("where controllerId=:controllerId ");
            hql.append("and orderId in (:orderIds) ");

            Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameterList("orderIds", orderIds);
            return session.getResultList(query);
        }
    }

    public List<DBItemDailyPlanOrder> getDailyPlanOrdersByCyclicMainPart(String controllerId, String mainPart) throws SOSHibernateException {
        return getDailyPlanOrdersByCyclicMainPart(controllerId, mainPart, null);
    }

    public List<DBItemDailyPlanOrder> getDailyPlanOrdersByCyclicMainPart(String controllerId, String mainPart, Boolean submitted)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId like :mainPart ");
        if (submitted != null) {
            hql.append("and submitted=:submitted ");
        }

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("mainPart", mainPart + "%");
        if (submitted != null) {
            query.setParameter("submitted", submitted);
        }
        List<DBItemDailyPlanOrder> result = session.getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public int updateDailyPlanOrdersByCyclicMainPart(String controllerId, String mainPart, String orderParameterisation)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("set orderParameterisation=:orderParameterisation ");
        hql.append(", modified=:modified ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId like :mainPart ");

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("mainPart", mainPart + "%");
        query.setParameter("orderParameterisation", orderParameterisation);
        query.setParameter("modified", Date.from(Instant.now()));
        return executeUpdate("updateOrderParameterisation", query);
    }

    private List<DBItemDailyPlanOrder> getDailyPlanListExecute(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBLayer.DBITEM_DPL_ORDERS + " p " + getWhere(filter, "p.schedulePath", true) + filter.getOrderCriteria() + filter
                .getSortMode();
        Query<DBItemDailyPlanOrder> query = session.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DBItemDailyPlanOrder> result = session.getResultList(query);
        if (result == null) {
           return Collections.emptyList(); 
        }
        return result;
    }

    public List<DBItemDailyPlanOrder> getDailyPlanList(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {

        if (filter.getOrderIds() != null) {
            List<DBItemDailyPlanOrder> resultList = new ArrayList<DBItemDailyPlanOrder>();
            int size = filter.getOrderIds().size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                List<String> copy = filter.getOrderIds().stream().collect(Collectors.toList());
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                        filter.setOrderIds(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                    } else {
                        filter.setOrderIds(copy.subList(i, size));
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

    public DBItemDailyPlanOrder getUniqueDailyPlan(PlannedOrder order) throws JocConfigurationException, DBConnectionRefusedException,
            SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and workflowName=:workflowName ");
        hql.append("and scheduleName=:scheduleName ");
        hql.append("and orderName=:orderName ");
        hql.append("and plannedStart=:plannedStart ");
        hql.append("and startMode=:startMode ");

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", order.getControllerId());
        query.setParameter("workflowName", order.getWorkflowName());
        query.setParameter("scheduleName", order.getScheduleName());
        query.setParameter("orderName", order.getOrderName());
        query.setParameter("plannedStart", new Date(order.getFreshOrder().getScheduledFor()));
        Integer startMode = order.getPeriod().getSingleStart() == null ? START_MODE_CYCLIC : START_MODE_SINGLE;
        query.setParameter("startMode", startMode);

        List<DBItemDailyPlanOrder> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public DBItemDailyPlanOrder getUniqueDailyPlan(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
        hql.append(getWhere(filter));
        Query<DBItemDailyPlanOrder> query = session.createQuery(hql.toString());
        query = bindParameters(filter, query);

        List<DBItemDailyPlanOrder> result = session.getResultList(query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public void storeVariables(PlannedOrder order, String controllerId, String orderId) throws SOSHibernateException, JsonProcessingException {
        if (order.getFreshOrder().getArguments() != null && order.getFreshOrder().getArguments().getAdditionalProperties() != null && order
                .getFreshOrder().getArguments().getAdditionalProperties().size() > 0) {

            DBItemDailyPlanVariable item = new DBItemDailyPlanVariable();
            item.setControllerId(controllerId);
            item.setOrderId(orderId);
            item.setVariableValue(Globals.objectMapper.writeValueAsString(order.getFreshOrder().getArguments()));
            item.setCreated(JobSchedulerDate.nowInUtc());
            item.setModified(JobSchedulerDate.nowInUtc());
            session.save(item);
        }
    }

    public DBItemDailyPlanOrder store(PlannedOrder plannedOrder, String id, Integer nr, Integer size) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException, ParseException, JsonProcessingException {

        DBItemDailyPlanOrder item = new DBItemDailyPlanOrder();
        item.setSchedulePath(plannedOrder.getSchedulePath());
        item.setScheduleName(plannedOrder.getScheduleName());
        item.setOrderName(plannedOrder.getOrderName());
        item.setOrderId(plannedOrder.getFreshOrder().getId());

        Date start = new Date(plannedOrder.getFreshOrder().getScheduledFor());
        item.setPlannedStart(start);
        if (plannedOrder.getPeriod().getSingleStart() == null) {
            item.setStartMode(START_MODE_CYCLIC);
            item.setPeriodBegin(plannedOrder.getSubmissionForDate(), plannedOrder.getPeriod().getBegin());
            item.setPeriodEnd(plannedOrder.getSubmissionForDate(), plannedOrder.getPeriod().getEnd());
            item.setRepeatInterval(plannedOrder.getPeriod().getRepeat());
        } else {
            item.setStartMode(START_MODE_SINGLE);
        }

        item.setControllerId(plannedOrder.getControllerId());
        item.setWorkflowPath(plannedOrder.getWorkflowPath());
        item.setWorkflowFolder(plannedOrder.getWorkflowFolder());
        item.setScheduleFolder(plannedOrder.getScheduleFolder());
        item.setWorkflowName(plannedOrder.getFreshOrder().getWorkflowPath());
        item.setSubmitted(false);
        item.setSubmissionHistoryId(plannedOrder.getSubmissionHistoryId());
        item.setCalendarId(plannedOrder.getCalendarId());
        item.setCreated(JobSchedulerDate.nowInUtc());
        item.setExpectedEnd(new Date(plannedOrder.getFreshOrder().getScheduledFor() + plannedOrder.getAverageDuration()));
        item.setOrderParameterisation(getOrderParameterisation(plannedOrder.getFreshOrder()));
        item.setModified(JobSchedulerDate.nowInUtc());

        if (nr != 0) {// cyclic
            String nrAsString = "00000" + String.valueOf(nr);
            nrAsString = nrAsString.substring(nrAsString.length() - 5);

            String sizeAsString = String.valueOf(size);
            item.setOrderId(item.getOrderId().replaceAll("<nr.....>", nrAsString));
            item.setOrderId(item.getOrderId().replaceAll("<size>", sizeAsString));
        }
        item.setOrderId(item.getOrderId().replaceAll("<id.*>", id));
        plannedOrder.getFreshOrder().setId(item.getOrderId());
        session.save(item);

        if (nr < 2) { // 0-single, 1-first cyclic
            storeVariables(plannedOrder, item.getControllerId(), item.getOrderId());
        }
        return item;
    }

    private String getOrderParameterisation(FreshOrder order) throws JsonProcessingException {
        if (order == null) {
            return null;
        }
        if (order.getPositions() == null && order.getForceJobAdmission() != Boolean.TRUE) {
            return null;
        }
        OrderParameterisation op = new OrderParameterisation();
        op.setPositions(order.getPositions());
        op.setForceJobAdmission(order.getForceJobAdmission());
        return Globals.objectMapper.writeValueAsString(op);
    }

    public int setSubmitted(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        boolean isSubmitted = filter.getSubmitted();
        Date now = JobSchedulerDate.nowInUtc();

        FilterDailyPlannedOrders filterCopy = filter.copy();
        filterCopy.setStates(null);
        filterCopy.setSubmitTime(null);
        filterCopy.setSubmitted(null);

        int size = filterCopy.getOrderIds() == null ? 0 : filterCopy.getOrderIds().size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            int result = 0;
            List<String> copy = filterCopy.getOrderIds().stream().collect(Collectors.toList());
            for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                    filterCopy.setOrderIds(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                } else {
                    filterCopy.setOrderIds(copy.subList(i, size));
                }
                result += executeSetSubmitted(filterCopy, isSubmitted, now);
            }
            return result;
        } else {
            return executeSetSubmitted(filterCopy, isSubmitted, now);
        }
    }

    private int executeSetSubmitted(FilterDailyPlannedOrders filter, boolean isSubmitted, Date now) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
        if (isSubmitted) {
            hql.append("set submitted=true");
            hql.append(",submitTime=:submitTime  ");

            filter.setSubmitTime(now);
        } else {
            hql.append("set submitted=false");
            hql.append(",submitTime=null  ");

            filter.setSubmitTime(null);
        }
        hql.append(getWhere(filter));

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql);
        bindParameters(filter, query);

        return executeUpdate("executeSetSubmitted", query);
    }

    public int setSubmitted(String controllerId, String orderId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_DPL_ORDERS).append(" ");
        hql.append("set submitted=true");
        hql.append(",modified=:modified ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId=:orderId");

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("orderId", orderId);
        query.setParameter("modified", new Date());

        return executeUpdate("setSubmitted", query);
    }

    public int setHistorySubmitted(Long id, boolean submitted, String message) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_DPL_HISTORY).append(" ");
        hql.append("set submitted=:submitted ");
        if (!SOSString.isEmpty(message)) {
            hql.append(",message=:message ");
        }
        hql.append("where id=:id");

        Query<DBItemDailyPlanHistory> query = session.createQuery(hql);
        query.setParameter("submitted", submitted);
        if (!SOSString.isEmpty(message)) {
            query.setParameter("message", message);
        }
        query.setParameter("id", id);

        return executeUpdate("setHistorySubmitted", query);
    }

    private <T> int executeUpdate(String callerMethodName, Query<T> query) throws SOSHibernateException {
        int result = 0;
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                result = session.executeUpdate(query);
                run = false;
            } catch (Exception e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = SOSHibernate.findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        LOGGER.warn(String.format("%s: %s occured, wait %ss and try again (%s of %s) ...", callerMethodName, te.getClass().getName(),
                                RERUN_INTERVAL, count, MAX_RERUNS));
                        try {
                            Thread.sleep(RERUN_INTERVAL * 1000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        }
        return Math.abs(result);
    }

    public DBItemDailyPlanOrder addCyclicOrderIds(Collection<String> orderIds, String orderId, String controllerId, String timeZone,
            String periodBegin) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("addCyclicOrderIds");

            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(controllerId);
            filter.setOrderId(orderId);
            filter.setSortMode(null);
            filter.setOrderCriteria(null);

            List<DBItemDailyPlanOrder> items = dbLayer.getDailyPlanList(filter, 0);
            if (items.size() == 1) {
                DBItemDailyPlanOrder item = items.get(0);
                if (item.getStartMode().equals(START_MODE_CYCLIC)) {
                    FilterDailyPlannedOrders filterCyclic = new FilterDailyPlannedOrders();
                    filterCyclic.setSortMode(null);
                    filterCyclic.setOrderCriteria(null);
                    filterCyclic.setControllerId(controllerId);
                    String cyclicMainPart = null;
                    try {
                        cyclicMainPart = OrdersHelper.getCyclicOrderIdMainPart(orderId);
                        filterCyclic.setCyclicOrdersMainParts(Collections.singletonList(cyclicMainPart));
                        filterCyclic.setStartMode(START_MODE_CYCLIC);
                    } catch (Throwable e) {
                        filterCyclic.setDailyPlanDate(item.getDailyPlanDate(timeZone, periodBegin), timeZone, periodBegin);
                        filterCyclic.setRepeatInterval(item.getRepeatInterval());
                        filterCyclic.setPeriodBegin(item.getPeriodBegin());
                        filterCyclic.setPeriodEnd(item.getPeriodEnd());
                        filterCyclic.setWorkflowName(item.getWorkflowName());
                        filterCyclic.setScheduleName(item.getScheduleName());
                        filterCyclic.setOrderName(item.getOrderName());
                    }

                    List<DBItemDailyPlanOrder> cyclicItems = dbLayer.getDailyPlanList(filterCyclic, 0);
                    session.close();
                    session = null;
                    
                    cyclicItems.stream().map(DBItemDailyPlanOrder::getOrderId).distinct().filter(oId -> !oId.equals(orderId)).forEach(oId -> orderIds
                            .add(oId));

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[addCyclicOrderIds][%s=%s]orderIds total=%s", cyclicMainPart, cyclicItems.size(), orderIds
                                .size()));
                    }
                }
                return item;
            } else if(items.size() > 1) {
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId());
            } else {
                return null;
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    public List<Long> getSubmissionIds(String controllerId, Date dateFrom, Date dateTo) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS);
        hql.append(" where controllerId = :controllerId");
        if (dateFrom != null) {
            hql.append(" and submissionForDate >= :dateFrom");
        }
        if (dateTo != null) {
            hql.append(" and submissionForDate <= :dateTo");
        }

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            query.setParameter("dateTo", dateTo);
        }
        return session.getResultList(query);
    }

    public Long getCountOrdersBySubmissionId(String controllerId, Long submissionHistoryId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_DPL_ORDERS);
        hql.append(" where controllerId = :controllerId");
        hql.append(" and submissionHistoryId = :submissionHistoryId");

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("submissionHistoryId", submissionHistoryId);
        return session.getSingleValue(query);
    }

    public int deleteSubmission(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_SUBMISSIONS).append(" ");
        hql.append("where id = :id");

        Query<DBItemDailyPlanSubmission> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        return executeUpdate("deleteSubmission", query);
    }

    public Long getWorkflowAvg(String controllerId, String workflowPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ");
        hql.append("round(");
        hql.append("sum(").append(SOSHibernateSecondsDiff.getFunction("startTime", "endTime")).append(")/count(id)");
        hql.append(",0) ");// ,0 precision only because of MSSQL
        hql.append("from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("where controllerId = :controllerId ");
        hql.append("and workflowName = :workflowName ");
        hql.append("and parentId = 0 ");
        hql.append("and severity=:severity ");
        hql.append("and endTime >= startTime ");

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("workflowName", JocClusterUtil.getBasenameFromPath(workflowPath));
        query.setParameter("severity", HistorySeverity.SUCCESSFUL);
        return session.getSingleValue(query);
    }
}