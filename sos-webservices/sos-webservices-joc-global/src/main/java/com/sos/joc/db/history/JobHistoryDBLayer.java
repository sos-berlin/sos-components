package com.sos.joc.db.history;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.HistoryStateText;

public class JobHistoryDBLayer {

    private SOSHibernateSession session;
    private HistoryFilter filter;
    private static final Map<String, String> STATEMAP = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;
        {
            put("SUCCESSFUL", "(endTime != null and error != 1)");
            put("INCOMPLETE", "(startTime != null and endTime is null)");
            put("FAILED", "(endTime != null and error = 1)");
        }
    });

    public JobHistoryDBLayer(SOSHibernateSession connection, HistoryFilter filter) {
        this.session = connection;
        this.filter = filter;
    }

    public List<DBItemOrderStep> getJobHistoryFromTo() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemOrderStep> query = createQuery(new StringBuilder().append("from ").append(DBLayer.HISTORY_DBITEM_ORDER_STEP).append(
                    getWhere()).append(" order by startTime desc").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getCountJobHistoryFromTo(HistoryStateText state) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            filter.setState(state);
            Query<Long> query = createQuery(new StringBuilder().append("select count(*) from ").append(DBLayer.HISTORY_DBITEM_ORDER_STEP).append(
                    getWhere()).toString());
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemOrder> getOrderHistoryFromTo() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Query<DBItemOrder> query = createQuery(new StringBuilder().append("from ").append(DBLayer.HISTORY_DBITEM_ORDER).append(getWhere()).append(
                    " order by startTime desc").toString());
            if (filter.getLimit() > 0) {
                query.setMaxResults(filter.getLimit());
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private String getWhere() {
        String where = "";
        String and = "";
        String clause = "";

        if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
            where += and + " masterId =: schedulerId";
            and = " and ";
        }
        // if (filter.getTaskIds() != null && !filter.getTaskIds().isEmpty()) {
        // where += and + " historyId in (:taskIds)";
        // and = " and ";
        // }

        if (filter.getExecutedFrom() != null) {
            where += and + " startTime >= :startTimeFrom";
            and = " and ";
        }

        if (filter.getExecutedTo() != null) {
            where += and + " startTime < :startTimeTo";
            and = " and ";
        }

        if (filter.getStates() != null && !filter.getStates().isEmpty()) {
            clause = filter.getStates().stream().map(state -> STATEMAP.get(state)).collect(Collectors.joining(" or "));
            if (filter.getStates().size() > 1) {
                clause = "(" + clause + ")";
            }
            where += and + clause;
            and = " and ";
        }

        if (filter.getJobs() != null && !filter.getJobs().isEmpty()) {
            where += and + SearchStringHelper.getStringListPathSql(filter.getJobs(), "jobName");
            and = " and ";
        } else if ((filter.getOrders() != null && !filter.getOrders().isEmpty()) || (filter.getWorkflows() != null && !filter.getWorkflows()
                .isEmpty())) { //TODO
            clause = SearchStringHelper.getStringListPathSql(filter.getOrders().stream().map(order -> order.getWorkflow() + "," + order.getOrderId())
                    .collect(Collectors.toSet()), "concat(workflowPath, ',', name)");
            String clause2 = SearchStringHelper.getStringListPathSql(filter.getWorkflows(), "workflowPath");
            clause = "(" + String.join(" or ", Arrays.asList(clause, clause2)) + ")";
            where += and + clause;
            and = " and ";
        } else {
            if (filter.getExcludedJobs() != null && !filter.getExcludedJobs().isEmpty()) {
                clause = filter.getExcludedJobs().stream().map(job -> "jobName != '" + job + "'").collect(Collectors.joining(" and "));
                if (filter.getExcludedJobs().size() > 1) {
                    clause = "(" + clause + ")";
                }
                where += and + clause;
                and = " and ";
            }
            if (filter.getExcludedOrders() != null && !filter.getExcludedOrders().isEmpty()) {
                clause = filter.getExcludedOrders().stream().map(order -> "concat(workflowPath, ',', name) != '" + order.getWorkflow() + "," + order
                        .getOrderId() + "'").collect(Collectors.joining(" and "));
                if (filter.getExcludedJobs().size() > 1) {
                    clause = "(" + clause + ")";
                }
                where += and + clause;
                and = " and ";
            }
            if (filter.getExcludedWorkflows() != null && !filter.getExcludedWorkflows().isEmpty()) {
                clause = filter.getExcludedWorkflows().stream().map(workflow -> "workflowPath != '" + workflow + "'").collect(Collectors.joining(
                        " and "));
                if (filter.getExcludedJobs().size() > 1) {
                    clause = "(" + clause + ")";
                }
                where += and + clause;
                and = " and ";
            }
            // if (filter.getFolders() != null && !filter.getFolders().isEmpty()) { // TODO needs join with orders history
            // clause = filter.getFolders().stream().map(folder -> {
            // if (folder.getRecursive()) {
            // return "(folder = '" + folder.getFolder() + "' or folder like '" + (folder.getFolder() + "/%").replaceAll("//+", "/") + "')";
            // } else {
            // return "folder = '" + folder.getFolder() + "'";
            // }
            // }).collect(Collectors.joining(" or "));
            // if (filter.getFolders().size() > 1) {
            // clause = "(" + clause + ")";
            // }
            // where += and + clause;
            // and = " and ";
            // }
        }

        if (!where.trim().isEmpty()) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> createQuery(String hql) throws SOSHibernateException {
        Query<T> query = session.createQuery(hql);
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        return query;
    }

}
