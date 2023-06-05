package com.sos.joc.db.security;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Devices;

public class IamFido2DevicesDBLayer {

    private static final String DBItemIamFido2Devices = com.sos.joc.db.authentication.DBItemIamFido2Devices.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public IamFido2DevicesDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    private <T> Query<T> bindParameters(IamFido2DevicesFilter filter, Query<T> query) {
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }
        if (filter.getAccountId() != null) {
            query.setParameter("accountId", filter.getAccountId());
        }
        if (filter.getOrigin() != null && !filter.getOrigin().isEmpty()) {
            query.setParameter("origin", filter.getOrigin());
        }
        if (filter.getCredentialId() != null && !filter.getCredentialId().isEmpty()) {
            query.setParameter("credentialId", filter.getCredentialId());
        }

        return query;

    }

    public int delete(IamFido2DevicesFilter filter) throws SOSHibernateException {
        IamFido2DevicesFilter filterDelete = new IamFido2DevicesFilter();
        filterDelete.setId(filter.getId());
        String hql = "delete from " + DBItemIamFido2Devices + getWhere(filterDelete);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterDelete, query);

        row = query.executeUpdate();
        return row;
    }

    private String getWhere(IamFido2DevicesFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getId() != null) {
            where += and + " id = :id";
            and = " and ";
        }
        if (filter.getAccountId() != null) {
            where += and + " accountId = :accountId";
            and = " and ";
        }
        if (filter.getCredentialId() != null && !filter.getCredentialId().isEmpty()) {
            where += and + " credentialId = :credentialId";
            and = " and ";
        }
        if (filter.getOrigin() != null && !filter.getOrigin().isEmpty()) {
            where += and + " origin = :origin";
            and = " and ";
        }

        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    public List<DBItemIamFido2Devices> getListOfFido2Devices(IamFido2DevicesFilter filter) throws SOSHibernateException {
        Query<DBItemIamFido2Devices> query = sosHibernateSession.createQuery("from " + DBItemIamFido2Devices + getWhere(filter));

        bindParameters(filter, query);

        List<DBItemIamFido2Devices> iamFido2Devices = sosHibernateSession.getResultList(query);
        return iamFido2Devices == null ? Collections.emptyList() : iamFido2Devices;
    }

}