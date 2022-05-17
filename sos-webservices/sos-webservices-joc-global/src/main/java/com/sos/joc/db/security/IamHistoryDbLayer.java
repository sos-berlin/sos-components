package com.sos.joc.db.security;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamHistory;

public class IamHistoryDbLayer {

    private static final String DBItemIamHistory = com.sos.joc.db.authentication.DBItemIamHistory.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public IamHistoryDbLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    private <T> Query<T> bindParameters(IamHistoryFilter filter, Query<T> query) {
        if (filter.getAccountName() != null && !filter.getAccountName().equals("")) {
            query.setParameter("accountName", filter.getAccountName());
        }
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }
        if (filter.getLoginSuccess() != null) {
            query.setParameter("loginSuccess", filter.getLoginSuccess());
        }
        if (filter.getAccountName() != null && !filter.getAccountName().isEmpty()) {
            query.setParameter("accountName", filter.getAccountName());
        }

        return query;

    }

    private String getWhere(IamHistoryFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getId() != null) {
            where += and + " id = :id";
            and = " and ";
        }
        if (filter.getAccountName() != null && !filter.getAccountName().isEmpty()) {
            where += and + " accountName = :accountName";
            and = " and ";
        }
        if (filter.getLoginSuccess() != null) {
            where += and + " loginSuccess = :loginSuccess";
            and = " and ";
        }
        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    public List<DBItemIamHistory> getIamAccountList(IamHistoryFilter filter, final int limit) throws SOSHibernateException {
        Query<DBItemIamHistory> query = sosHibernateSession.createQuery("from " + DBItemIamHistory + getWhere(filter) + filter.getOrderCriteria()
                + filter.getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamHistory> iamHistoryList = query.getResultList();
        return iamHistoryList == null ? Collections.emptyList() : iamHistoryList;
    }

    public void addLoginAttempt(String accountName, boolean loginSuccess) throws SOSHibernateException {

        DBItemIamHistory dbItemIamHistory = new DBItemIamHistory();
        dbItemIamHistory.setAccountName(accountName);
        dbItemIamHistory.setLoginDate(new Date());
        dbItemIamHistory.setLoginSuccess(loginSuccess);
        if (loginSuccess) {
            IamHistoryFilter filter = new IamHistoryFilter();
            filter.setAccountName(accountName);
            filter.setLoginSuccess(true);
            List<DBItemIamHistory> l = getIamAccountList(filter, 1);
            if (l.size() > 0) {
                dbItemIamHistory = l.get(0);
                dbItemIamHistory.setLoginDate(new Date());
                sosHibernateSession.update(dbItemIamHistory);
            } else {
                sosHibernateSession.save(dbItemIamHistory);
            }
        } else {
            sosHibernateSession.save(dbItemIamHistory);

        }

    }

}