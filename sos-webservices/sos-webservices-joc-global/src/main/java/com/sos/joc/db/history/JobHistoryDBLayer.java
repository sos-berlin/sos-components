package com.sos.joc.db.history;

import java.util.List;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class JobHistoryDBLayer {

    private SOSHibernateSession session;

    public JobHistoryDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public List<DBItemOrderStep> getJobHistoryFromTo(OrderStepFilter filter) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.HISTORY_DBITEM_ORDER_STEP).append(getWhereFromTo(filter));
            sql.append(" order by startTime desc");
            Query<DBItemOrderStep> query = session.createQuery(sql.toString());
            if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
                query.setParameter("schedulerId", filter.getSchedulerId());
            }
            if (filter.getExecutedFrom() != null) {
                query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
            }
            if (filter.getExecutedTo() != null) {
                query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
            }
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

    private String getWhereFromTo(OrderStepFilter filter) {
        String where = "";
        String and = "";

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

        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where += getStatusClause(state) + " or";
            }
            where += " 1=0)";
            and = " and ";
        }

        if (filter.getJobs() != null && filter.getJobs().size() > 0) {
            where += and + SearchStringHelper.getStringListPathSql(filter.getJobs(), "jobName");
            and = " and ";
        } else {
            if (filter.getExcludedJobs() != null && filter.getExcludedJobs().size() > 0) {
                where += and + "(";
                for (String job : filter.getExcludedJobs()) {
                    where += " jobname <> '" + job + "' and";
                }
                where += " 1=1)";
                and = " and ";
            }
            // if (filter.getFolders() != null && filter.getFolders().size() > 0) { //TODO needs join with orders history
            // where += and + "(";
            // for (Folder filterFolder : filter.getFolders()) {
            // if (filterFolder.getRecursive()) {
            // String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
            // where += " (folder = '" + filterFolder.getFolder() + "' or folder like '" + likeFolder + "')";
            // } else {
            // where += " folder = '" + filterFolder.getFolder() + "'";
            // }
            // where += " or ";
            // }
            // where += " 0=1)";
            // and = " and ";
            // }
        }

        if (!where.trim().isEmpty()) {
            where = " where " + where;
        }
        return where;
    }

    private String getStatusClause(String status) {
        if ("SUCCESSFUL".equals(status)) {
            return "(endTime != null and error != 1)";
        }

        if ("INCOMPLETE".equals(status)) {
            return "(startTime != null and endTime is null)";
        }

        if ("FAILED".equals(status)) {
            return "(endTime != null and error = 1)";
        }
        return "";
    }

}
