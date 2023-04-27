package com.sos.joc.db.security;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamFido2Registration;

public class IamFido2DBLayer {

    private static final String DBItemIamFido2Registration = com.sos.joc.db.authentication.DBItemIamFido2Registration.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public IamFido2DBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    private <T> Query<T> bindParameters(IamFido2RegistrationFilter filter, Query<T> query) {
        if (filter.getIdentityServiceId() != null) {
            query.setParameter("identityServiceId", filter.getIdentityServiceId());
        }

        if (filter.getApproved() != null) {
            query.setParameter("approved", filter.getApproved());
        }

        if (filter.getConfirmed() != null) {
            query.setParameter("confirmed", filter.getConfirmed());
        }

        if (filter.getRejected() != null) {
            query.setParameter("rejected", filter.getRejected());
        }

        if (filter.getRpName() != null && !filter.getRpName().isEmpty()) {
            query.setParameter("rpName", filter.getRpName());
        }

        if (filter.getAccountName() != null && !filter.getAccountName().isEmpty()) {
            query.setParameter("accountName", filter.getAccountName());
        }

        return query;
    }

    public int delete(IamFido2RegistrationFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamFido2Registration + getWhere(filter);
        Query<DBItemIamFido2Registration> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);
        row = query.executeUpdate();
        return row;
    }

    private String getWhere(IamFido2RegistrationFilter filter) {
        String where = " ";
        String and = "";

        if (filter.getRpName() != null && !filter.getRpName().isEmpty()) {
            where += and + " rpName = :rpName";
            and = " and ";
        }
        if (filter.getAccountName() != null && !filter.getAccountName().isEmpty()) {
            where += and + " accountName = :accountName";
            and = " and ";
        }
        if (filter.getIdentityServiceId() != null) {
            where += and + " identityServiceId = :identityServiceId";
            and = " and ";
        }
        if (filter.getApproved() != null) {
            where += and + " approved = :approved";
            and = " and ";
        }
        if (filter.getConfirmed() != null) {
            where += and + " confirmed = :confirmed";
            and = " and ";
        }
        if (filter.getRejected() != null) {
            where += and + " rejected = :rejected";
            and = " and ";
        }

        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    public List<DBItemIamFido2Registration> getIamAccountList(IamFido2RegistrationFilter filter, final int limit) throws SOSHibernateException {
        Query<DBItemIamFido2Registration> query = sosHibernateSession.createQuery("from " + DBItemIamFido2Registration + getWhere(filter) + filter
                .getOrderCriteria() + filter.getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamFido2Registration> iamFido2RegistrationList = query.getResultList();
        return iamFido2RegistrationList == null ? Collections.emptyList() : iamFido2RegistrationList;
    }

    public com.sos.joc.db.authentication.DBItemIamFido2Registration getIamFido2RegistrationtByName(IamFido2RegistrationFilter filter)
            throws SOSHibernateException {

        if ((filter.getRpName() == null) || (filter.getRpName().isEmpty())) {
            return null;
        }

        Query<DBItemIamFido2Registration> query = sosHibernateSession.createQuery("from " + DBItemIamFido2Registration + getWhere(filter));

        bindParameters(filter, query);

        List<DBItemIamFido2Registration> iamFido2RegistrationList = query.getResultList();
        if (iamFido2RegistrationList.size() > 0) {
            return iamFido2RegistrationList.get(0);
        }
        return null;
    }

    public DBItemIamFido2Registration getUniqueFido2Registration(IamFido2RegistrationFilter filter) throws SOSHibernateException {
        if ((filter.getAccountName() == null) || (filter.getAccountName().isEmpty())) {
            return null;
        }
        if ((filter.getIdentityServiceId() == null)) {
            return null;
        }
        List<DBItemIamFido2Registration> fido2RegistrationList = null;
        Query<DBItemIamFido2Registration> query = sosHibernateSession.createQuery("from " + DBItemIamFido2Registration + getWhere(filter) + filter
                .getOrderCriteria() + filter.getSortMode());
        bindParameters(filter, query);

        fido2RegistrationList = query.getResultList();
        if (fido2RegistrationList.size() == 0) {
            return null;
        } else {
            return fido2RegistrationList.get(0);
        }
    }

}