package com.sos.joc.db.history;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.persistence.TemporalType;

import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.reporting.ReportingLoader;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.history.items.CSVItem;
import com.sos.joc.db.history.items.HistoryGroupedSummary;
import com.sos.joc.db.history.items.JobsPerAgent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderStateText;

public class JobHistoryDBLayer {

    /** result rerun interval in seconds */
    private static final long RERUN_INTERVAL = 1;
    private static final int MAX_RERUNS = 3;

    private SOSHibernateSession session;
    private HistoryFilter filter;
    private static final Map<HistoryStateText, Integer> STATEMAP = Collections.unmodifiableMap(new HashMap<HistoryStateText, Integer>() {

        private static final long serialVersionUID = 1L;
        {
            put(HistoryStateText.SUCCESSFUL, HistorySeverity.SUCCESSFUL);
            put(HistoryStateText.INCOMPLETE, HistorySeverity.INCOMPLETE);
            put(HistoryStateText.FAILED, HistorySeverity.FAILED);
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

    public ScrollableResults<DBItemHistoryOrderStep> getJobs() throws DBConnectionRefusedException, DBInvalidDataException {
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

    public ScrollableResults<String> getCSV(ReportingLoader loader, LocalDateTime month) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            filter.setExecutedFrom(Date.from(month.atZone(ZoneId.systemDefault()).toInstant()));
            filter.setExecutedTo(Date.from(month.plusMonths(1).atZone(ZoneId.systemDefault()).toInstant()));
            filter.setMainOrder(loader.withoutChildOrders());

            Query<String> query = createQuery(new StringBuilder().append("select ").append(loader.getColumnHql()).append(" as csv from ").append(
                    loader.getDbTable()).append(getOrderStepsWhere()).append(" order by startTime").toString());
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

    public ScrollableResults<DBItemHistoryOrderStep> getJobsFromHistoryIdAndPosition(Map<Long, Set<String>> mapOfHistoryIdAndPosition)
            throws DBConnectionRefusedException, DBInvalidDataException {
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

    public ScrollableResults<CSVItem> getCSVJobsFromHistoryIdAndPosition(Stream<String> columns, Map<Long, Set<String>> mapOfHistoryIdAndPosition)
            throws DBConnectionRefusedException, DBInvalidDataException {
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
            StringBuilder hql = new StringBuilder().append("select workflowFolder as folder, concat(coalesce(").append(columns.collect(Collectors
                    .joining(",''),';',coalesce("))).append(",'')) as csv from ").append(DBLayer.DBITEM_HISTORY_ORDER_STEPS).append(where).append(
                            " order by startTime desc");
            Query<CSVItem> query = session.createQuery(hql.toString(), CSVItem.class);
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

    public Long getHistoryId() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<Long> query = createQuery(new StringBuilder().append("select id from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(
                    getOrdersWhere()).append(" order by startTime desc").toString());
            query.setMaxResults(1);
            return session.getSingleResult(query);
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

    public ScrollableResults<DBItemHistoryOrder> getMainOrders() throws DBConnectionRefusedException, DBInvalidDataException {
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

    public List<String> getOrderIds() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<String> query = createQuery(new StringBuilder().append("select orderId from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(
                    getOrdersWhere()).toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            return executeResultList(query);
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
        return getWhere("", true);
    }

    private String getOrderStepsWhere() {
        return getWhere("", false);
    }
    
//    private String getOrdersWhere(String tableAlias) {
//        return getWhere(tableAlias, true);
//    }
//
//    private String getOrderStepsWhere(String tableAlias) {
//        return getWhere(tableAlias, false);
//    }

    private String getWhere(String tableAlias, boolean orderLogs) {
        String clause = "";
        String alias = (tableAlias != null && !tableAlias.isBlank()) ? tableAlias + "." : "";
        
        List<String> clauses = new ArrayList<>(); 

        if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
            if (filter.getControllerIds().size() == 1) {
                clauses.add(alias + "controllerId = :controllerIds");
            } else {
                clauses.add(alias + "controllerId in (:controllerIds)");
            }
        }

        if (filter.getAgentIds() != null && !filter.getAgentIds().isEmpty()) {
            clauses.add(alias + "agentId in (:agentIds)");
        }

        if (filter.isMainOrder()) {
            clauses.add(alias + "parentId = 0");
        }

        if (orderLogs) {
            // TODO ???
            // where += and + " state > " + OrderStateText.PENDING.intValue();
            // and = " and";
        }

        if (filter.getHistoryIds() != null && !filter.getHistoryIds().isEmpty()) {
            clauses.add(alias + "id in (:historyIds)");
        } else {
            if (filter.getExecutedFrom() == null && filter.getExecutedTo() == null) {
                filter.setExecutedFrom(new Date(0)); // set to 1970 to use startTime index
            }
            if (filter.getExecutedFrom() != null) {
                clauses.add(alias + "startTime >= :startTimeFrom");
            }
            if (filter.getExecutedTo() != null) {
                clauses.add(alias + "startTime < :startTimeTo");
            }

            if (filter.getEndFrom() != null) {
                clauses.add(alias + "endTime >= :endTimeFrom");
            }

            if (filter.getEndTo() != null) {
                clauses.add(alias + "endTime < :endTimeTo");
            }

            if (filter.getStateFrom() != null) {
                clauses.add(alias + "stateTime >= :stateTimeFrom");
            }

            if (filter.getStateTo() != null) {
                clauses.add(alias + "stateTime < :stateTimeTo");
            }

            if (filter.getStates() != null && !filter.getStates().isEmpty()) {
                clause = filter.getStates().stream().map(state -> alias + "severity=" + STATEMAP.get(state)).collect(Collectors.joining(" or "));
                if (filter.getStates().size() > 1) {
                    clause = "(" + clause + ")";
                }
                clauses.add(clause);
            }

            if (filter.getOrderStates() != null && !filter.getOrderStates().isEmpty()) {
                clauses.add(alias + "state in (:orderStates)");
            }

            if (filter.getCriticalities() != null && !filter.getCriticalities().isEmpty()) {
                clauses.add(alias + "criticality in (:criticalities)");
            }

            if (filter.getJobs() != null && !filter.getJobs().isEmpty()) {
                List<String> l = new ArrayList<String>();
                for (Entry<String, Set<String>> entry : filter.getJobs().entrySet()) {
                    String s = alias + "workflowName = '" + entry.getKey() + "'";
                    if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                        if (entry.getValue().size() == 1) {
                            s += " and " + alias + "jobName = '" + entry.getValue().iterator().next() + "'";
                        } else {
                            s += " and " + alias + "jobName in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(
                                    ",")) + ")";
                        }
                    }
                    l.add("(" + s + ")");
                }
                if (!l.isEmpty()) {
                    clauses.add("(" + String.join(" or ", l) + ")");
                }
            } else if (filter.getOrders() != null && !filter.getOrders().isEmpty()) {
                List<String> l = new ArrayList<String>();
                for (Entry<String, Set<String>> entry : filter.getOrders().entrySet()) {
                    String s = alias + "workflowName = '" + entry.getKey() + "'";
                    if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                        if (entry.getValue().size() == 1) {
                            s += " and " + alias + "orderId = '" + entry.getValue().iterator().next() + "'";
                        } else {
                            s += " and " + alias + "orderId in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors.joining(
                                    ",")) + ")";
                        }
                    }
                    l.add("(" + s + ")");
                }
                if (!l.isEmpty()) {
                    clauses.add("(" + String.join(" or ", l) + ")");
                }

            } else {
                if (filter.getExcludedJobs() != null && !filter.getExcludedJobs().isEmpty()) {
                    List<String> l = new ArrayList<String>();
                    for (Entry<String, Set<String>> entry : filter.getExcludedJobs().entrySet()) {
                        String s = alias + "workflowName != '" + entry.getKey() + "'";
                        if (!entry.getValue().isEmpty() && !entry.getValue().contains(null)) {
                            if (entry.getValue().size() == 1) {
                                s += " or " + alias + "jobName != '" + entry.getValue().iterator().next() + "'";
                            } else {
                                s += " or " + alias + "jobName not in (" + entry.getValue().stream().map(val -> "'" + val + "'").collect(Collectors
                                        .joining(",")) + ")";
                            }
                        }
                        l.add("(" + s + ")");
                    }
                    if (!l.isEmpty()) {
                        clauses.add("(" + String.join(" and ", l) + ")");
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
                    clauses.add(alias + "workflowPath not in (:excludedWorkflows)");
                }
                if (filter.getFolders() != null && !filter.getFolders().isEmpty()) {
                    clause = filter.getFolders().stream().map(folder -> {
                        if (folder.getRecursive()) {
                            return "(" + alias + "workflowFolder = '" + folder.getFolder() + "' or " + alias + "workflowFolder like '" + (folder
                                    .getFolder() + "/%").replaceAll("//+", "/") + "')";
                        } else {
                            return alias + "workflowFolder = '" + folder.getFolder() + "'";
                        }
                    }).collect(Collectors.joining(" or "));
                    if (filter.getFolders().size() > 1) {
                        clause = "(" + clause + ")";
                    }
                    clauses.add(clause);
                }
                if (filter.getOrderId() != null && !filter.getOrderId().isEmpty()) {
                    if (filter.getOrderId().contains("*") || filter.getOrderId().contains("?")) {
                        clauses.add(alias + "orderId like :orderId");
                    } else {
                        clauses.add(alias + "orderId = :orderId");
                    }
                }
                if (filter.getWorkflowPath() != null && !filter.getWorkflowPath().isEmpty()) {
                    if (filter.getWorkflowPath().contains("*") || filter.getWorkflowPath().contains("?")) {
                        clauses.add(alias + "workflowPath like :workflowPath");
                    } else {
                        clauses.add(alias + "workflowPath = :workflowPath");
                    }
                }
                if (filter.getWorkflowName() != null && !filter.getWorkflowName().isEmpty()) {
                    if (filter.getWorkflowName().contains("*") || filter.getWorkflowName().contains("?")) {
                        clauses.add(alias + "workflowName like :workflowName");
                    } else {
                        clauses.add(alias + "workflowName = :workflowName");
                    }
                }
                if (filter.getJobName() != null && !filter.getJobName().isEmpty()) {
                    if (filter.getJobName().contains("*") || filter.getJobName().contains("?")) {
                        clauses.add(alias + "jobName like :jobName");
                    } else {
                        clauses.add(alias + "jobName = :jobName");
                    }
                }
                if (filter.getWorkflowNames() != null && !filter.getWorkflowNames().isEmpty()) {
                    clause = IntStream.range(0, filter.getWorkflowNames().size()).mapToObj(i -> alias + "workflowName in (:workflowNames" + i + ")")
                            .collect(Collectors.joining(" or "));
                    if (filter.getWorkflowNames().size() > 1) {
                        clause = "(" + clause + ")";
                    }
                    clauses.add(clause);
                }
                
            }
        }
        
        if (clauses.isEmpty()) {
            return "";
        }
        
        return clauses.stream().collect(Collectors.joining(" and ", " where ", ""));
    }

    private <T> Query<T> createQuery(String hql) throws SOSHibernateException {
        return createQuery(hql, null);
    }

    private <T> Query<T> createQuery(String hql, Class<T> clazz) throws SOSHibernateException {
        Query<T> query = null;
        if (clazz == null) {
            query = session.createQuery(hql);
        } else {
            query = session.createQuery(hql, clazz);
        }
        if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
            if (filter.getControllerIds().size() == 1) {
                query.setParameter("controllerIds", filter.getControllerIds().iterator().next());
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
        if (filter.getStateFrom() != null) {
            query.setParameter("stateTimeFrom", filter.getStateFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getStateTo() != null) {
            query.setParameter("stateTimeTo", filter.getStateTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getOrderStates() != null && !filter.getOrderStates().isEmpty()) {
            query.setParameterList("orderStates", filter.getOrderStates().stream().map(OrderStateText::intValue).collect(Collectors.toSet()));
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
        if (filter.getJobName() != null && !filter.getJobName().isEmpty()) {
            if (filter.getJobName().contains("*") || filter.getJobName().contains("?")) {
                query.setParameter("jobName", filter.getJobName().replace('*', '%').replace('?', '_'));
            } else {
                query.setParameter("jobName", filter.getJobName());
            }
        }
        if (filter.getWorkflowNames() != null && !filter.getWorkflowNames().isEmpty()) {
            AtomicInteger counter = new AtomicInteger();
            for (List<String> chunk : filter.getWorkflowNames()) {
                query.setParameterList("workflowNames" + counter.getAndIncrement(), chunk);
            };
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

    private <T> ScrollableResults<T> executeScroll(Query<T> query) throws SOSHibernateException {
        ScrollableResults<T> result = null;
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
