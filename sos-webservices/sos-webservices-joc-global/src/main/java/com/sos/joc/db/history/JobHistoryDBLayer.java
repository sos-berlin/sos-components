package com.sos.joc.db.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.history.items.HistoryGroupedSummary;
import com.sos.joc.db.history.items.JobsPerAgent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;

public class JobHistoryDBLayer {

    /** result rerun interval in seconds */
    private static final long RERUN_INTERVAL = 1;
    private static final int MAX_RERUNS = 3;

    private SOSHibernateSession session;
    private HistoryFilter filter;
    private static final Map<HistoryStateText, String> STATEMAP = Collections.unmodifiableMap(new HashMap<HistoryStateText, String>() {

        private static final long serialVersionUID = 1L;
        {
            put(HistoryStateText.SUCCESSFUL, "(severity=" + HistorySeverity.SUCCESSFUL + ")");
            put(HistoryStateText.INCOMPLETE, "(severity=" + HistorySeverity.INCOMPLETE + ")");
            put(HistoryStateText.FAILED, "(severity=" + HistorySeverity.FAILED + ")");
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

    public List<DBItemHistoryOrderStep> getOrderSteps(Long historyOrderId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemHistoryOrderStep> query = session.createQuery(new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS)
                    .append(" where historyOrderId = :historyOrderId").toString());
            query.setParameter("historyOrderId", historyOrderId);
            return executeResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getJobs() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemHistoryOrderStep> query = createQuery(new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(
                    getOrderStepsWhere()).append(" order by startTime desc").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }

            return executeScroll(query);
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
                String s = "historyOrderMainParentId = " + entry.getKey();
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
            StringBuilder hql = new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(where).append(
                    " order by startTime desc");
            Query<DBItemHistoryOrderStep> query = session.createQuery(hql.toString());
            return executeScroll(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public long getCountJobs(HistoryStateText state, Collection<Folder> permittedFolders) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            filter.setState(state);
            if (permittedFolders == null || permittedFolders.size() == 0) {
                Query<Long> query = createQuery(new StringBuilder().append("select count(id) from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS)
                        .append(getOrderStepsWhere()).toString());
                return session.getSingleResult(query);
            } else {
                Query<String> query = createQuery(new StringBuilder().append("select workflowFolder from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS)
                        .append(getOrderStepsWhere()).toString());
                List<String> result = executeResultList(query);
                if (result == null) {
                    return 0L;
                } else {
                    return result.stream().filter(folder -> SOSAuthFolderPermissions.isPermittedForFolder(folder, permittedFolders)).count();
                }
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemHistoryOrder> getMainOrdersDeprecated() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            boolean isMainOrder = filter.isMainOrder();
            filter.setMainOrder(true);
            Query<DBItemHistoryOrder> query = createQuery(new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(
                    getOrdersWhere()).append(" order by startTime desc").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            filter.setMainOrder(isMainOrder);
            return executeResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public ScrollableResults getMainOrders() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            boolean isMainOrder = filter.isMainOrder();
            filter.setMainOrder(true);
            Query<DBItemHistoryOrder> query = createQuery(new StringBuilder().append("from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(
                    getOrdersWhere()).append(" order by startTime desc").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            filter.setMainOrder(isMainOrder);

            return executeScroll(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemHistoryOrder> getOrderForkChilds(Long orderId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
            hql.append("where parentId=:orderId");
            Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
            query.setParameter("orderId", orderId);
            return executeResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemHistoryOrderState> getOrderStates(Long historyOrderId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER_STATES).append(" ");
            hql.append("where historyOrderId=:historyOrderId");
            Query<DBItemHistoryOrderState> query = session.createQuery(hql.toString());
            query.setParameter("historyOrderId", historyOrderId);
            return executeResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getCountOrders(HistoryStateText state, Map<String, Set<Folder>> permittedFoldersMap) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            filter.setState(state);
            if (permittedFoldersMap == null || permittedFoldersMap.isEmpty()) {
                Query<Long> query = createQuery(new StringBuilder().append("select count(id) from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(
                        getOrdersWhere()).toString());
                return session.getSingleResult(query);
            } else {
                Query<HistoryGroupedSummary> query = createQuery(new StringBuilder().append("select new ").append(HistoryGroupedSummary.class
                        .getName()).append("(count(id), controllerId, workflowFolder) from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(
                                getOrdersWhere()).append(" group by controllerId, workflowFolder").toString());

                List<HistoryGroupedSummary> result = executeResultList(query);
                if (result != null) {
                    return result.stream().filter(s -> isPermittedForFolder(s.getFolder(), permittedFoldersMap.get(s.getControllerId()))).mapToLong(
                            s -> s.getCount()).sum();
                }
                return 0L;
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private static boolean isPermittedForFolder(String folder, Collection<Folder> permittedFolders) {
        if (folder == null || folder.isEmpty()) {
            return true;
        }
        if (permittedFolders == null || permittedFolders.isEmpty()) {
            return true;
        }
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return permittedFolders.stream().parallel().anyMatch(filter);
    }

    public Map<String, List<JobsPerAgent>> getCountJobs() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<JobsPerAgent> query = createQuery(new StringBuilder().append("select new ").append(JobsPerAgent.class.getName()).append(
                    "(agentId, error, count(id)) from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(getOrderStepsWhere()).append(
                            " group by agentId, error").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            List<JobsPerAgent> result = executeResultList(query);
            if (result == null) {
                return Collections.emptyMap();
            } else {
                return result.stream().collect(Collectors.groupingBy(JobsPerAgent::getAgentId));
            }
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
        StringBuilder where = new StringBuilder();
        String and = "";
        String clause = "";

        if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
            where.append(and);
            if (filter.getControllerIds().size() == 1) {
                where.append(" controllerId = :controllerIds");
            } else {
                where.append(" controllerId in (:controllerIds)");
            }
            and = " and";
        }

        if (filter.getAgentIds() != null && !filter.getAgentIds().isEmpty()) {
            where.append(and).append(" agentId in (:agentIds)");
            and = " and";
        }

        if (filter.isMainOrder()) {
            where.append(and).append(" parentId = 0");
            and = " and";
        }

        if (orderLogs) {
            // TODO ???
            // where += and + " state > " + OrderStateText.PENDING.intValue();
            // and = " and";
        }

        if (filter.getHistoryIds() != null && !filter.getHistoryIds().isEmpty()) {
            where.append(and).append(" id in (:historyIds)");
            and = " and";
        } else {
            if (filter.getExecutedFrom() == null && filter.getExecutedTo() == null) {
                filter.setExecutedFrom(new Date(0)); // set to 1970 to use startTime index
            }
            if (filter.getExecutedFrom() != null) {
                where.append(and).append(" startTime >= :startTimeFrom");
                and = " and";
            }
            if (filter.getExecutedTo() != null) {
                where.append(and).append(" startTime < :startTimeTo");
                and = " and";
            }
            
            if (filter.getEndFrom() != null) {
                where.append(and).append(" endTime >= :endTimeFrom");
                and = " and";
            }
 
            if (filter.getEndTo() != null) {
                where.append(and).append(" endTime < :endTimeTo");
                and = " and";
            }
            
            if (filter.getStates() != null && !filter.getStates().isEmpty()) {
                clause = filter.getStates().stream().map(state -> STATEMAP.get(state)).collect(Collectors.joining(" or "));
                if (filter.getStates().size() > 1) {
                    clause = "(" + clause + ")";
                }
                where.append(and).append(" ").append(clause);
                and = " and";
            }

            if (filter.getCriticalities() != null && !filter.getCriticalities().isEmpty()) {
                where.append(and).append(" criticality in (:criticalities)");
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
                    where.append(and).append(" (" + String.join(" or ", l) + ")");
                    and = " and";
                }
            } else if (filter.getOrders() != null && !filter.getOrders().isEmpty()) {
                List<String> l = new ArrayList<String>();
                for (Entry<String, Set<String>> entry : filter.getOrders().entrySet()) {
                    String s = "workflowPath = '" + entry.getKey() + "'";
                    if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                        if (entry.getValue().size() == 1) {
                            s += " and orderId = '" + entry.getValue().iterator().next() + "'";
                        } else {
                            s += " and orderId in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(",")) + ")";
                        }
                    }
                    l.add("(" + s + ")");
                }
                if (!l.isEmpty()) {
                    where.append(and).append(" (" + String.join(" or ", l) + ")");
                    and = " and";
                }

            } else {
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
                        where.append(and).append(" (" + String.join(" and ", l) + ")");
                        and = " and";
                    }
                }
                // if (filter.getExcludedOrders() != null && !filter.getExcludedOrders().isEmpty()) {
                // List<String> l = new ArrayList<String>();
                // for (Entry<String, Set<String>> entry : filter.getExcludedOrders().entrySet()) {
                // String s = "workflowPath != '" + entry.getKey() + "'";
                // if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                // if (entry.getValue().size() == 1) {
                // s += " or orderId != '" + entry.getValue().iterator().next() + "'";
                // } else {
                // s += " or orderId not in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(","))
                // + ")";
                // }
                // }
                // l.add("(" + s + ")");
                // }
                // if (!l.isEmpty()) {
                // where += and + " (" + String.join(" and ", l) + ")";
                // and = " and";
                // }
                // }
                if (filter.getExcludedWorkflows() != null && !filter.getExcludedWorkflows().isEmpty()) {
                    where.append(and).append(" workflowPath not in (:excludedWorkflows)");
                    and = " and";
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
                    where.append(and).append(" ").append(clause);
                    and = " and";
                }
                if (filter.getOrderId() != null && !filter.getOrderId().isEmpty()) {
                    if (filter.getOrderId().contains("*") || filter.getOrderId().contains("?")) {
                        where.append(and).append(" orderId like :orderId");
                    } else {
                        where.append(and).append(" orderId = :orderId");
                    }
                    and = " and";
                }
                if (filter.getWorkflowPath() != null && !filter.getWorkflowPath().isEmpty()) {
                    if (filter.getWorkflowPath().contains("*") || filter.getWorkflowPath().contains("?")) {
                        where.append(and).append(" workflowPath like :workflowPath");
                    } else {
                        where.append(and).append(" workflowPath = :workflowPath");
                    }
                    and = " and";
                }
                if (filter.getWorkflowName() != null && !filter.getWorkflowName().isEmpty()) {
                    if (filter.getWorkflowName().contains("*") || filter.getWorkflowName().contains("?")) {
                        where.append(and).append(" workflowName like :workflowName");
                    } else {
                        where.append(and).append(" workflowName = :workflowName");
                    }
                    and = " and";
                }
            }
        }

        if (where.length() > 0) {
            return " where " + where.toString();
        }
        return where.toString();
    }

    private <T> Query<T> createQuery(String hql) throws SOSHibernateException {
        Query<T> query = session.createQuery(hql);
        if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
            if (filter.getControllerIds().size() == 1) {
                query.setParameter("controllerIds", filter.getControllerIds());
            } else {
                query.setParameterList("controllerIds", filter.getControllerIds());
            }
        }
        if (filter.getAgentIds() != null && !filter.getAgentIds().isEmpty()) {
            query.setParameterList("agentIds", filter.getAgentIds());
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
        if (filter.getEndFrom() != null) {
            query.setParameter("endTimeFrom", filter.getEndFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getEndTo() != null) {
            query.setParameter("endTimeTo", filter.getEndTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getCriticalities() != null && !filter.getCriticalities().isEmpty()) {
            query.setParameterList("criticalities", filter.getCriticalities());
        }
        if (filter.getExcludedWorkflows() != null && !filter.getExcludedWorkflows().isEmpty()) {
            query.setParameterList("excludedWorkflows", filter.getExcludedWorkflows());
        }
        if (filter.getOrderId() != null && !filter.getOrderId().isEmpty()) {
            if (filter.getOrderId().contains("*") || filter.getOrderId().contains("?")) {
                query.setParameter("orderId", filter.getOrderId().replace('*', '%').replace('?', '_'));
            } else {
                query.setParameter("orderId", filter.getOrderId());
            }
        }
        if (filter.getWorkflowPath() != null && !filter.getWorkflowPath().isEmpty()) {
            if (filter.getWorkflowPath().contains("*") || filter.getWorkflowPath().contains("?")) {
                query.setParameter("workflowPath", filter.getWorkflowPath().replace('*', '%').replace('?', '_'));
            } else {
                query.setParameter("workflowPath", filter.getWorkflowPath());
            }
        }
        if (filter.getWorkflowName() != null && !filter.getWorkflowName().isEmpty()) {
            if (filter.getWorkflowName().contains("*") || filter.getWorkflowName().contains("?")) {
                query.setParameter("workflowName", filter.getWorkflowName().replace('*', '%').replace('?', '_'));
            } else {
                query.setParameter("workflowName", filter.getWorkflowName());
            }
        }
        return query;
    }

    private <T> List<T> executeResultList(Query<T> query) throws SOSHibernateException {
        List<T> result = null;
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                result = session.getResultList(query);
                run = false;
            } catch (Exception e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = SOSHibernate.findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        try {
                            Thread.sleep(RERUN_INTERVAL * 1000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        }
        return result;
    }

    private <T> ScrollableResults executeScroll(Query<T> query) throws SOSHibernateException {
        ScrollableResults result = null;
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                result = session.scroll(query);
                run = false;
            } catch (Exception e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = SOSHibernate.findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        try {
                            Thread.sleep(RERUN_INTERVAL * 1000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        }
        return result;
    }

}
