package com.sos.joc.db.security;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.query.Query;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamHistory;
import com.sos.joc.db.authentication.DBItemIamHistoryDetails;
import com.sos.joc.db.authentication.DBItemIamIdentityService;

public class IamHistoryDbLayer {

    private static final String DBItemIamHistory = com.sos.joc.db.authentication.DBItemIamHistory.class.getSimpleName();
    private static final String DBItemIamHistoryDetails = com.sos.joc.db.authentication.DBItemIamHistoryDetails.class.getSimpleName();;
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
        if (filter.getIdentityServiceId() != null) {
            query.setParameter("identityServiceId", filter.getIdentityServiceId());
        }
        if (filter.getAccountName() != null && !filter.getAccountName().isEmpty()) {
            query.setParameter("accountName", filter.getAccountName());
        }

        if (filter.getDateFrom() != null) {
            query.setParameter("from", filter.getDateFrom());
        }

        if (filter.getDateTo() != null) {
            query.setParameter("to", filter.getDateTo());
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

        if (filter.getIdentityServiceId() != null) {
            where += and + " identityServiceId = :identityServiceId";
            and = " and ";
        }

        if (filter.getDateFrom() != null) {
            where += and + "loginDate >= :from";
            and = " and ";
        }
        if (filter.getDateTo() != null) {
            where += and + "loginDate < :to";
            and = " and ";
        }

        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    public List<DBItemIamHistory> getIamAccountList(IamHistoryFilter filter, final int limit) throws SOSHibernateException {
        filter.setOrderCriteria("loginDate");
        filter.setSortMode("desc");
        Query<DBItemIamHistory> query = sosHibernateSession.createQuery("from " + DBItemIamHistory + getWhere(filter) + filter.getOrderCriteria()
                + filter.getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamHistory> iamHistoryList = query.getResultList();
        return iamHistoryList == null ? Collections.emptyList() : iamHistoryList;
    }

    public List<DBItemIamHistory> getListOfFailedLogins(IamHistoryFilter filter, final int limit) throws SOSHibernateException {
        filter.setLoginSuccess(false);
        return getIamAccountList(filter, limit);
    }

    public void addLoginAttempt(SOSAuthCurrentAccount account, Map<String, String> authenticationResult, boolean loginSuccess) throws SOSHibernateException {

        String accountName = account.getAccountname();
        if (accountName == null || accountName.isEmpty()) {
            accountName = SOSAuthHelper.NONE;
        }
        
       
        DBItemIamHistory dbItemIamHistory = new DBItemIamHistory();
        dbItemIamHistory.setAccountName(accountName);
        dbItemIamHistory.setLoginDate(new Date());
        dbItemIamHistory.setLoginSuccess(loginSuccess);
        dbItemIamHistory.setIdentityServiceId(account.getIdentityService().getIdentityServiceId());
        if (loginSuccess) {
            IamHistoryFilter filter = new IamHistoryFilter();
            filter.setAccountName(accountName);
            filter.setIdentityServiceId(account.getIdentityService().getIdentityServiceId());
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
            for (Entry<String, String> entry : authenticationResult.entrySet()) {
                DBItemIamHistoryDetails dbItemIamHistoryDetails = new DBItemIamHistoryDetails();
                dbItemIamHistoryDetails.setIamHistoryId(dbItemIamHistory.getId());
                dbItemIamHistoryDetails.setIdentityServiceName(entry.getKey());
                dbItemIamHistoryDetails.setMessage(entry.getValue());
                sosHibernateSession.save(dbItemIamHistoryDetails);
            }

        }

    }

    public List<DBItemIamHistoryDetails> getListOfFailedLoginDetails(Long id, int limit) throws SOSHibernateException {
        Query<DBItemIamHistoryDetails> query = sosHibernateSession.createQuery("from " + DBItemIamHistoryDetails
                + " where iamHistoryId=:iamHistoryId");
        query.setParameter("iamHistoryId", id);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamHistoryDetails> iamHistoryDetailsList = query.getResultList();
        return iamHistoryDetailsList == null ? Collections.emptyList() : iamHistoryDetailsList;
    }

}