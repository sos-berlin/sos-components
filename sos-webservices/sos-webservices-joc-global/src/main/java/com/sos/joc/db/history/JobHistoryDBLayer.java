package com.sos.joc.db.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.order.OrderStateText;

public class JobHistoryDBLayer {

    private SOSHibernateSession session;
    private HistoryFilter filter;
    private static final Map<HistoryStateText, String> STATEMAP = Collections.unmodifiableMap(new HashMap<HistoryStateText, String>() {

        private static final long serialVersionUID = 1L;
        {
            put(HistoryStateText.SUCCESSFUL, "(state=" + OrderStateText.FINISHED.intValue() + ")");
            put(HistoryStateText.INCOMPLETE, "(state=" + OrderStateText.RUNNING.intValue() + ")");
            put(HistoryStateText.FAILED, "(state=" + OrderStateText.FAILED.intValue() + ")");
        }
    });

    public JobHistoryDBLayer(SOSHibernateSession connection) {
        this.session = connection;
        this.filter = null;
    }

    public JobHistoryDBLayer(SOSHibernateSession connection, HistoryFilter filter) {
        this.session = connection;
        this.filter = filter;
    }

    public List<DBItemHistoryOrderStep> getOrderSteps(OrderHistoryFilter filter) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemHistoryOrderStep> query = session.createQuery(new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEP)
                    .append(" where jobSchedulerId = :jobschedulerId and orderId = :historyId order by id").toString());
            query.setParameter("jobschedulerId", filter.getControllerId());
            query.setParameter("historyId", filter.getHistoryId());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getJobs() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemHistoryOrderStep> query = createQuery(new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(
                    getOrderStepsWhere()).append(" order by startTime desc").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            return session.scroll(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getJobsFromHistoryIdAndPosition(Map<Long, Set<String>> mapOfHistoryIdAndPosition) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            List<String> l = new ArrayList<String>();
            String where = "";
            for (Entry<Long, Set<String>> entry : mapOfHistoryIdAndPosition.entrySet()) {
                String s = "mainOrderId = " + entry.getKey();
                Set<String> workflowPositions = entry.getValue();
                if (!workflowPositions.isEmpty() && !workflowPositions.contains(null)) {
                    if (workflowPositions.size() == 1) {
                        s += " and workflowPosition = '" + workflowPositions.iterator().next() + "'";
                    } else {
                        s += " and workflowPosition in (" + workflowPositions.stream().map(val -> "'" + val + "'").collect(Collectors.joining(","))
                                + ")";
                    }
                }
                l.add("(" + s + ")");
            }
            if (!l.isEmpty()) {
                where = String.join(" or ", l);
            }
            if (!where.trim().isEmpty()) {
                where = " where " + where;
            }
            StringBuilder hql = new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(where).append(
                    " order by startTime desc");
            Query<DBItemHistoryOrderStep> query = session.createQuery(hql.toString());
            return session.scroll(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getJobsFromOrder(Map<String, Map<String, Set<String>>> mapOfWorkflowAndOrderIdAndPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getCountJobs(HistoryStateText state) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            filter.setState(state);
            Query<Long> query = createQuery(new StringBuilder().append("select count(id) from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEP).append(
                    getOrderStepsWhere()).toString());
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemHistoryOrder> getMainOrders() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            boolean isMainOrder = filter.isMainOrder();
            filter.setMainOrder(true);
            Query<DBItemHistoryOrder> query = createQuery(new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER).append(
                    getOrdersWhere()).append(" order by startTime desc").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            filter.setMainOrder(isMainOrder);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemHistoryOrder> getChildOrders(Collection<Long> mainOrderIds) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<DBItemHistoryOrder> childOrders = null;
            if (mainOrderIds != null && !mainOrderIds.isEmpty()) {
                String hql = new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER).append(
                        " where mainParentId in (:mainOrderIds) and parentId != 0 order by startEventId desc").toString();
                Query<DBItemHistoryOrder> query = session.createQuery(hql);
                query.setParameterList("mainOrderIds", mainOrderIds);
                childOrders = session.getResultList(query);
            }
            if (childOrders == null) {
                childOrders = new ArrayList<DBItemHistoryOrder>();
            }
            return childOrders;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getCountOrders(HistoryStateText state) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            filter.setState(state);
            Query<Long> query = createQuery(new StringBuilder().append("select count(id) from ").append(DBLayer.DBITEM_HISTORY_ORDER).append(
                    getOrdersWhere()).toString());
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private String getOrdersWhere() {
        return getWhere(true);
    }

    private String getOrderStepsWhere() {
        return getWhere(false);
    }

    private String getWhere(boolean orderLogs) {
        String where = "";
        String and = "";
        String clause = "";

        if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
            where += and + " jobSchedulerId = :jobschedulerId";
            and = " and";
        }

        if (filter.isMainOrder()) {
            where += and + " parentId = 0";
            and = " and";
        }

        if (orderLogs) {
            where += and + " state > " + OrderStateText.PENDING.intValue();
            and = " and";
        }

        if (filter.getHistoryIds() != null && !filter.getHistoryIds().isEmpty()) {
            where += and + " id in (:historyIds)";
            and = " and";
        } else {

            if (filter.getExecutedFrom() != null) {
                where += and + " startTime >= :startTimeFrom";
                and = " and";
            }

            if (filter.getExecutedTo() != null) {
                where += and + " startTime < :startTimeTo";
                and = " and";
            }

            if (filter.getStates() != null && !filter.getStates().isEmpty()) {
                clause = filter.getStates().stream().map(state -> STATEMAP.get(state)).collect(Collectors.joining(" or "));
                if (filter.getStates().size() > 1) {
                    clause = "(" + clause + ")";
                }
                where += and + " " + clause;
                and = " and";
            }

            if (filter.getCriticalities() != null && !filter.getCriticalities().isEmpty()) {
                where += and + " criticality in (:criticalities)";
                and = " and";
            }

            if (filter.getJobs() != null && !filter.getJobs().isEmpty()) {
                List<String> l = new ArrayList<String>();
                for (Entry<String, Set<String>> entry : filter.getJobs().entrySet()) {
                    String s = "workflowPath = '" + entry.getKey() + "'";
                    if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                        if (entry.getValue().size() == 1) {
                            s += " and jobName = '" + entry.getValue().iterator().next() + "'";
                        } else {
                            s += " and jobName in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(",")) + ")";
                        }
                    }
                    l.add("(" + s + ")");
                }
                if (!l.isEmpty()) {
                    where += and + " (" + String.join(" or ", l) + ")";
                    and = " and";
                }
            } else if (filter.getOrders() != null && !filter.getOrders().isEmpty()) {
                List<String> l = new ArrayList<String>();
                for (Entry<String, Set<String>> entry : filter.getOrders().entrySet()) {
                    String s = "workflowPath = '" + entry.getKey() + "'";
                    if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                        if (entry.getValue().size() == 1) {
                            s += " and orderKey = '" + entry.getValue().iterator().next() + "'";
                        } else {
                            s += " and orderKey in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(",")) + ")";
                        }
                    }
                    l.add("(" + s + ")");
                }
                if (!l.isEmpty()) {
                    where += and + " (" + String.join(" or ", l) + ")";
                    and = " and";
                }

            } else {
                if (filter.getWorkflows() != null && !filter.getWorkflows().isEmpty()) {
                    where += and + " " + SearchStringHelper.getStringListPathSql(filter.getWorkflows(), "workflowPath");
                    and = " and";
                }
                if (filter.getExcludedJobs() != null && !filter.getExcludedJobs().isEmpty()) {
                    List<String> l = new ArrayList<String>();
                    for (Entry<String, Set<String>> entry : filter.getExcludedJobs().entrySet()) {
                        String s = "workflowPath != '" + entry.getKey() + "'";
                        if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                            if (entry.getValue().size() == 1) {
                                s += " or jobName != '" + entry.getValue().iterator().next() + "'";
                            } else {
                                s += " or jobName not in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(","))
                                        + ")";
                            }
                        }
                        l.add("(" + s + ")");
                    }
                    if (!l.isEmpty()) {
                        where += and + " (" + String.join(" and ", l) + ")";
                        and = " and";
                    }
                }
                if (filter.getExcludedOrders() != null && !filter.getExcludedOrders().isEmpty()) {
                    List<String> l = new ArrayList<String>();
                    for (Entry<String, Set<String>> entry : filter.getExcludedOrders().entrySet()) {
                        String s = "workflowPath != '" + entry.getKey() + "'";
                        if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                            if (entry.getValue().size() == 1) {
                                s += " or orderKey != '" + entry.getValue().iterator().next() + "'";
                            } else {
                                s += " or orderKey not in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(","))
                                        + ")";
                            }
                        }
                        l.add("(" + s + ")");
                    }
                    if (!l.isEmpty()) {
                        where += and + " (" + String.join(" and ", l) + ")";
                        and = " and";
                    }
                }
                if (filter.getFolders() != null && !filter.getFolders().isEmpty()) {
                    clause = filter.getFolders().stream().map(folder -> {
                        if (folder.getRecursive()) {
                            return "(workflowFolder = '" + folder.getFolder() + "' or workflowFolder like '" + (folder.getFolder() + "/%").replaceAll(
                                    "//+", "/") + "')";
                        } else {
                            return "workflowFolder = '" + folder.getFolder() + "'";
                        }
                    }).collect(Collectors.joining(" or "));
                    if (filter.getFolders().size() > 1) {
                        clause = "(" + clause + ")";
                    }
                    where += and + " " + clause;
                    and = " and";
                }
            }
        }

        if (!where.trim().isEmpty()) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> createQuery(String hql) throws SOSHibernateException {
        Query<T> query = session.createQuery(hql);
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("jobschedulerId", filter.getSchedulerId());
        }
        if (filter.getHistoryIds() != null && !filter.getHistoryIds().isEmpty()) {
            query.setParameterList("historyIds", filter.getHistoryIds());
        }
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getCriticalities() != null && !filter.getCriticalities().isEmpty()) {
            query.setParameterList("criticalities", filter.getCriticalities());
        }
        return query;
    }

}
