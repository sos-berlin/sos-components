package com.sos.js7.order.initiator.db;

import java.nio.file.Paths;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
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

    public int deleteCascading(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        int size = filter.getOrderIds() == null ? 0 : filter.getOrderIds().size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            FilterDailyPlannedOrders filterCopy = filter.copy();
            int result = 0;
            ArrayList<String> copy = (ArrayList<String>) filterCopy.getOrderIds().stream().collect(Collectors.toList());
            for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                    filterCopy.setOrderIds(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                } else {
                    filterCopy.setOrderIds(copy.subList(i, size));
                }
                result += executeDeleteCascading(filterCopy);
            }
            return result;
        } else {
            return executeDeleteCascading(filter);
        }
    }

    private int executeDeleteCascading(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        executeDeleteVariables(filter);
        return executeDelete(filter);
    }

    private int executeDeleteVariables(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        StringBuilder subSelect = new StringBuilder("select id from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
        subSelect.append(getWhere(filter, "p.schedulePath", true));

        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDER_VARIABLES).append(" ");
        hql.append("where plannedOrderId in (").append(subSelect).append(")");

        Query<DBItemDailyPlanVariable> query = session.createQuery(hql);
        query = bindParameters(filter, query);
        return executeUpdate("executeDeleteVariables", query);
    }

    public int delete(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        int size = filter.getOrderIds() == null ? 0 : filter.getOrderIds().size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            FilterDailyPlannedOrders filterCopy = filter.copy();
            int result = 0;
            ArrayList<String> copy = (ArrayList<String>) filterCopy.getOrderIds().stream().collect(Collectors.toList());
            for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                    filterCopy.setOrderIds(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                } else {
                    filterCopy.setOrderIds(copy.subList(i, size));
                }
                result += executeDelete(filterCopy);
            }
            return result;
        } else {
            return executeDelete(filter);
        }
    }

    private int executeDelete(FilterDailyPlannedOrders filter) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_DPL_ORDERS).append(" p ");
        hql.append(getWhere(filter, "p.schedulePath", true));

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql);
        bindParameters(filter, query);
        return executeUpdate("executeDelete", query);
    }

    private String getOrderListSql(Collection<String> list) {
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

        if (filter.getOrderPlannedStartFrom() != null && filter.getOrderPlannedStartTo() != null) {
            where.append(and).append(" p.plannedStart >= :plannedStartFrom and p.plannedStart < :plannedStartTo");
            and = " and ";
        }
        if (filter.getControllerId() != null && !"".equals(filter.getControllerId())) {
            where.append(and).append(" p.controllerId = :controllerId");
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
                        where.append("and  o.startTime - p.plannedStart >= ").append(DAILY_PLAN_LATE_TOLERANCE);
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
                        where.append("and (o.startTime - p.plannedStart >= ").append(DAILY_PLAN_LATE_TOLERANCE).append(") ");
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
                        where.append("and  o.startTime - p.plannedStart >= ").append(DAILY_PLAN_LATE_TOLERANCE);
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
                where.append("and  o.startTime - p.plannedStart >= ").append(DAILY_PLAN_LATE_TOLERANCE);
                where.append(") ");
                where.append("or ");
                where.append("(o.state is null and p.plannedStart < :").append(WHERE_PARAM_CURRENT_TIME).append(") ");
                where.append(") ");
                and = " and ";
            }
        }

        if (filter.getSubmissionIds() != null && filter.getSubmissionIds().size() > 0) {
            where.append(and).append(SearchStringHelper.getLongSetSql(filter.getSubmissionIds(), "p.submissionHistoryId"));
            and = " and ";
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
            where.append(getOrderListSql(filter.getOrderIds()));
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
            q.append("group by p.repeatInterval,p.periodBegin,p.periodEnd,p.orderName ");

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

        Query<Object[]> query = session.createNativeQuery(sql.toString());
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

        if (filter.getOrderIds() != null) {
            List<DBItemDailyPlanWithHistory> resultList = new ArrayList<DBItemDailyPlanWithHistory>();
            int size = filter.getOrderIds().size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                ArrayList<String> copy = (ArrayList<String>) filter.getOrderIds().stream().collect(Collectors.toList());
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

    private List<DBItemDailyPlanOrder> getDailyPlanListExecute(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBLayer.DBITEM_DPL_ORDERS + " p " + getWhere(filter, "p.schedulePath", true) + filter.getOrderCriteria() + filter
                .getSortMode();
        Query<DBItemDailyPlanOrder> query = session.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return session.getResultList(query);
    }

    public List<DBItemDailyPlanOrder> getDailyPlanList(FilterDailyPlannedOrders filter, final int limit) throws SOSHibernateException {

        if (filter.getOrderIds() != null) {
            List<DBItemDailyPlanOrder> resultList = new ArrayList<DBItemDailyPlanOrder>();
            int size = filter.getOrderIds().size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                ArrayList<String> copy = (ArrayList<String>) filter.getOrderIds().stream().collect(Collectors.toList());
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
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setPlannedStart(new Date(order.getFreshOrder().getScheduledFor()));
        filter.setControllerId(order.getControllerId());
        filter.setWorkflowName(order.getFreshOrder().getWorkflowPath());
        filter.setOrderName(order.getOrderName());

        return getUniqueDailyPlan(filter);
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

    public void storeVariables(PlannedOrder order, Long id) throws SOSHibernateException, JsonProcessingException {
        DBItemDailyPlanVariable item = new DBItemDailyPlanVariable();
        if (order.getFreshOrder().getArguments() != null) {
            item.setCreated(JobSchedulerDate.nowInUtc());
            item.setModified(JobSchedulerDate.nowInUtc());
            item.setPlannedOrderId(id);
            item.setVariableValue(new ObjectMapper().writeValueAsString(order.getFreshOrder().getArguments()));
            session.save(item);
        }
    }

    public DBItemDailyPlanOrder store(PlannedOrder plannedOrder, String id, Integer nr, Integer size) throws JocConfigurationException,
            DBConnectionRefusedException, SOSHibernateException, ParseException, JsonProcessingException {

        DBItemDailyPlanOrder item = new DBItemDailyPlanOrder();
        item.setSchedulePath(plannedOrder.getSchedule().getPath());
        item.setScheduleName(Paths.get(plannedOrder.getSchedule().getPath()).getFileName().toString());
        item.setOrderName(plannedOrder.getOrderName());
        item.setOrderId(plannedOrder.getFreshOrder().getId());

        Date start = new Date(plannedOrder.getFreshOrder().getScheduledFor());
        item.setPlannedStart(start);
        if (plannedOrder.getPeriod().getSingleStart() == null) {
            item.setStartMode(1);
            item.setPeriodBegin(start, plannedOrder.getPeriod().getBegin());
            item.setPeriodEnd(start, plannedOrder.getPeriod().getEnd());
            item.setRepeatInterval(plannedOrder.getPeriod().getRepeat());
        } else {
            item.setStartMode(0);
        }

        String workflowFolder = Paths.get(plannedOrder.getSchedule().getWorkflowPath()).getParent().toString().replace('\\', '/');
        String scheduleFolder = Paths.get(plannedOrder.getSchedule().getPath()).getParent().toString().replace('\\', '/');

        item.setControllerId(plannedOrder.getControllerId());
        item.setWorkflowPath(plannedOrder.getSchedule().getWorkflowPath());
        item.setWorkflowFolder(workflowFolder);
        item.setScheduleFolder(scheduleFolder);
        item.setWorkflowName(plannedOrder.getFreshOrder().getWorkflowPath());
        item.setSubmitted(false);
        item.setSubmissionHistoryId(plannedOrder.getSubmissionHistoryId());
        item.setCalendarId(plannedOrder.getCalendarId());
        item.setCreated(JobSchedulerDate.nowInUtc());
        item.setExpectedEnd(new Date(plannedOrder.getFreshOrder().getScheduledFor() + plannedOrder.getAverageDuration()));
        item.setModified(JobSchedulerDate.nowInUtc());

        if (nr != 0) {
            String nrAsString = "00000" + String.valueOf(nr);
            nrAsString = nrAsString.substring(nrAsString.length() - 5);

            String sizeAsString = String.valueOf(size);
            item.setOrderId(item.getOrderId().replaceAll("<nr.....>", nrAsString));
            item.setOrderId(item.getOrderId().replaceAll("<size>", sizeAsString));
        }
        item.setOrderId(item.getOrderId().replaceAll("<id.*>", id));
        plannedOrder.getFreshOrder().setId(item.getOrderId());
        session.save(item);
        storeVariables(plannedOrder, item.getId());
        return item;
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
            ArrayList<String> copy = (ArrayList<String>) filterCopy.getOrderIds().stream().collect(Collectors.toList());
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
        hql.append("set submitted=true ");
        hql.append("where controllerId=:controllerId ");
        hql.append("and orderId=:orderId");

        Query<DBItemDailyPlanOrder> query = session.createQuery(hql);
        query.setParameter("controllerId", controllerId);
        query.setParameter("orderId", orderId);

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

    public DBItemDailyPlanOrder store(PlannedOrder plannedOrder) throws JocConfigurationException, DBConnectionRefusedException,
            SOSHibernateException, ParseException, JsonProcessingException {
        return store(plannedOrder, Long.valueOf(Instant.now().toEpochMilli()).toString().substring(3), 0, 0);
    }

    public DBItemDailyPlanOrder insertFrom(DBItemDailyPlanOrder item) throws SOSHibernateException {
        item.setSubmitted(false);
        item.setCreated(JobSchedulerDate.nowInUtc());
        item.setModified(JobSchedulerDate.nowInUtc());
        session.save(item);

        String newOrderId = DailyPlanHelper.modifiedOrderId(item.getOrderId(), item.getId());
        item.setOrderId(newOrderId);
        session.update(item);

        return item;
    }

    public DBItemDailyPlanOrder addCyclicOrderIds(List<String> orderIds, String orderId, String controllerId, String timeZone, String periodBegin)
            throws SOSHibernateException {
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
                if (item.getStartMode() == 1) {
                    FilterDailyPlannedOrders filterCyclic = new FilterDailyPlannedOrders();
                    filterCyclic.setSortMode(null);
                    filterCyclic.setOrderCriteria(null);
                    filterCyclic.setControllerId(controllerId);
                    filterCyclic.setDailyPlanDate(item.getDailyPlanDate(timeZone), timeZone, periodBegin);
                    String cyclicMainPart = null;
                    try {
                        cyclicMainPart = OrdersHelper.getCyclicOrderIdMainPart(orderId);
                        filterCyclic.setCyclicOrdersMainParts(Collections.singletonList(cyclicMainPart));
                        filterCyclic.setStartMode(new Integer(1));
                    } catch (Throwable e) {
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

                    for (DBItemDailyPlanOrder cyclicItem : cyclicItems) {
                        if (!cyclicItem.getOrderId().equals(orderId)) {
                            orderIds.add(cyclicItem.getOrderId());
                        }
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[addCyclicOrderIds][%s=%s]orderIds total=%s", cyclicMainPart, cyclicItems.size(), orderIds
                                .size()));
                    }
                }
                return item;

            } else {
                LOGGER.warn("Expected one record for order-id " + filter.getOrderId());
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId());
            }
        } finally {
            Globals.disconnect(session);
        }
    }

}