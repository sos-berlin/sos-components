package com.sos.joc.db.security;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamFido2Requests;

public class IamFido2RequestDBLayer {

    private static final String DBItemIamFido2Requests = com.sos.joc.db.authentication.DBItemIamFido2Requests.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public IamFido2RequestDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    private <T> Query<T> bindParameters(IamFido2RequestsFilter filter, Query<T> query) {
        if (filter.getId() != null ) {
            query.setParameter("id", filter.getId());
        }
        if (filter.getIdentityServiceId() != null) {
            query.setParameter("identityServiceId", filter.getIdentityServiceId());
        }
        if (filter.getRequestId() != null && !filter.getRequestId().isEmpty()) {
            query.setParameter("requestId", filter.getRequestId());
        }

        return query;

    }

 

    private String getWhere(IamFido2RequestsFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getIdentityServiceId() != null) {
            where += and + " identityServiceId = :identityServiceId";
            and = " and ";
        }
        if (filter.getRequestId() != null && !filter.getRequestId().isEmpty()) {
            where += and + " requestId = :requestId";
            and = " and ";
        }
        if (filter.getId() != null) {
            where += and + " accountId = :accountId";
            and = " and ";
        }
        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

 

    public DBItemIamFido2Requests getFido2Request(IamFido2RequestsFilter filter) throws SOSHibernateException {
        List<DBItemIamFido2Requests> requestList = null;
        Query<DBItemIamFido2Requests> query = sosHibernateSession.createQuery("from " + DBItemIamFido2Requests + getWhere(filter));
        bindParameters(filter, query);

        requestList = query.getResultList();
        if (requestList.size() == 0) {
            return null;
        } else {
            return requestList.get(0);
        }
    }

    public int deleteFido2Request(IamFido2RequestsFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamFido2Requests + getWhere(filter);
        Query<DBItemIamFido2Requests> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = query.executeUpdate();
        return row;
    }

 
}