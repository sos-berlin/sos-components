package com.sos.js7.order.initiator.db;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.joc.DBItemJocAuditLogDetails;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
import com.sos.joc.model.common.Folder;

public class DBLayerDailyPlanHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDailyPlanHistory.class);
    private static final String DBItemDailyPlanHistory = DBItemDailyPlanHistory.class.getSimpleName();
    private static final String DBItemJocAuditLogDetails = DBItemJocAuditLogDetails.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerDailyPlanHistory(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public FilterDailyPlanHistory resetFilter() {
        FilterDailyPlanHistory filter = new FilterDailyPlanHistory();
        filter.setControllerId("");
        return filter;
    }

    private String getWhere(FilterDailyPlanHistory filter) {
        String where = "";
        String and = "";

        if (filter.getDailyPlanDate() != null) {
            where += and + " dailyPlanDate = :dailyPlanDate";
            and = " and ";
        }

        if (filter.getDailyPlanDateFrom() != null) {
            where += and + " dailyPlanDate >= :dailyPlanDateFrom";
            and = " and ";
        }

        if (filter.getDailyPlanDateTo() != null) {
            where += and + " dailyPlanDate < :dailyPlanDateTo";
            and = " and ";
        }

        if (filter.getSubmitted() != null) {
            where += and + " submitted = :submitted";
            and = " and ";
        }

        if (filter.getListOfOrderIds() != null && filter.getListOfOrderIds().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfOrderIds(), "orderId");
            and = " and ";
        }

        if (filter.getListOfControllerIds() != null && filter.getListOfControllerIds().size() > 0) {
            where += and + SearchStringHelper.getStringListSql(filter.getListOfControllerIds(), "controllerId");
            and = " and ";
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += and + " orderId = :orderId";
            and = " and ";
        }

        if (filter.getSetOfWorkflowFolders() != null && filter.getSetOfWorkflowFolders().size() > 0) {
            where += and + "(";
            for (Folder filterFolder : filter.getSetOfWorkflowFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where += " (" + "workflowFolder" + " = '" + filterFolder.getFolder() + "' or " + "workflowFolder" + " like '" + likeFolder + "')";
                } else {
                    where += String.format("workflowFolder" + " %s '" + filterFolder.getFolder() + "'", SearchStringHelper.getSearchOperator(
                            filterFolder.getFolder()));
                }
                where += " or ";
            }
            where += " 0=1)";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterDailyPlanHistory filter, Query<T> query) {

        if (filter.getDailyPlanDate() != null) {
            query.setParameter("dailyPlanDate", filter.getDailyPlanDate());
        }
        if (filter.getDailyPlanDateFrom() != null) {
            query.setParameter("dailyPlanDateFrom", filter.getDailyPlanDateFrom());
        }
        if (filter.getDailyPlanDateTo() != null) {
            query.setParameter("dailyPlanDateTo", filter.getDailyPlanDateTo());
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }

        if (filter.getSubmitted() != null) {
            query.setParameter("submitted", filter.getSubmitted());
        }

        return query;

    }

    public List<DBItemDailyPlanHistory> getDailyPlanHistory(FilterDailyPlanHistory filter, final int limit) throws SOSHibernateException {
        String q = "from " + DBItemDailyPlanHistory + getWhere(filter) + filter.getOrderCriteria() + filter.getSortMode();
        Query<DBItemDailyPlanHistory> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<String> getOrderIdsByAuditLog(Long auditLogId) throws SOSHibernateException {
        String q = "from " + DBItemJocAuditLogDetails + " where auditLogId=:auditLogId";
        Query<DBItemJocAuditLogDetails> query = sosHibernateSession.createQuery(q);
        query.setParameter("auditLogId", auditLogId);
        List<String> orderIds = new ArrayList<String>();
        for (DBItemJocAuditLogDetails dbItemJocAuditLogDetails : sosHibernateSession.getResultList(query)) {
            if (dbItemJocAuditLogDetails.getOrderId() != null) {
                orderIds.add(dbItemJocAuditLogDetails.getOrderId());
            }
        }
        return orderIds;
    }

    public void storeDailyPlanHistory(DBItemDailyPlanHistory dbItemDailyPlanHistory) throws SOSHibernateException {
        sosHibernateSession.save(dbItemDailyPlanHistory);
    }

    public void updateDailyPlanHistory(DBItemDailyPlanHistory dbItemDailyPlanHistory) throws SOSHibernateException {
        sosHibernateSession.update(dbItemDailyPlanHistory);
    }
}