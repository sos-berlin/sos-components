package com.sos.joc.db.security;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamIdentityService;

public class IamIdentityServiceDBLayer {

    private static final String DBItemIamIdentityService = com.sos.joc.db.authentication.DBItemIamIdentityService.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public IamIdentityServiceDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemIamIdentityService getIamRole(Long identityServiceId) throws SOSHibernateException {
        return (DBItemIamIdentityService) sosHibernateSession.get(DBItemIamIdentityService.class, identityServiceId);
    }

    private <T> Query<T> bindParameters(IamIdentityServiceFilter filter, Query<T> query) {

        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }

        if (filter.getDisabled() != null) {
            query.setParameter("disabled", filter.getDisabled());
        }

        if (filter.getRequired() != null) {
            query.setParameter("required", filter.getRequired());
        }

        if (filter.getIamIdentityServiceType() != null) {
            query.setParameter("identityServiceType", filter.getIamIdentityServiceType().value());
        }
        if (filter.getIdentityServiceName() != null && !filter.getIdentityServiceName().isEmpty()) {
            query.setParameter("identityServiceName", filter.getIdentityServiceName());
        }

        return query;

    }

    private String getWhere(IamIdentityServiceFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getId() != null) {
            where += and + " id = :id";
            and = " and ";
        }

        if (filter.getIdentityServiceName() != null && !filter.getIdentityServiceName().isEmpty()) {
            where += and + " identityServiceName = :identityServiceName";
            and = " and ";
        }

        if (filter.getIamIdentityServiceType() != null) {
            where += and + " identityServiceType = :identityServiceType";
            and = " and ";
        }

        if (filter.getDisabled() != null) {
            where += and + " disabled = :disabled";
            and = " and ";
        }

        if (filter.getRequired() != null) {
            where += and + " required = :required";
            and = " and ";
        }

        if (filter.getIamIdentityServiceType() != null) {
            where += and + " identityServiceType = :identityServiceType";
            and = " and ";
        }

        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    public DBItemIamIdentityService getUniqueIdentityService(IamIdentityServiceFilter filter) throws SOSHibernateException {
        List<DBItemIamIdentityService> identityList = null;
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery("from " + DBItemIamIdentityService + getWhere(filter) + filter
                .getOrderCriteria() + filter.getSortMode());
        bindParameters(filter, query);

        identityList = query.getResultList();
        if (identityList.size() == 0) {
            return null;
        } else {
            return identityList.get(0);
        }
    }

    public int delete(IamIdentityServiceFilter filter) throws SOSHibernateException {
        String hql = "delete " + DBItemIamIdentityService + getWhere(filter);
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;

    }

    public List<DBItemIamIdentityService> getIdentityServiceList(IamIdentityServiceFilter filter, final int limit) throws SOSHibernateException {
        List<DBItemIamIdentityService> identityServiceList = null;
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery("from " + DBItemIamIdentityService + getWhere(filter) + filter
                .getOrderCriteria() + filter.getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        identityServiceList = query.getResultList();
        return identityServiceList;
    }

    public void  rename(String identityServiceOldName, String identityServiceNewName) throws SOSHibernateException {
        String hql = "update " + DBItemIamIdentityService + " set identityServiceName=:identityServiceNewName where identityServiceName=:identityServiceOldName";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("identityServiceNewName", identityServiceNewName);
        query.setParameter("identityServiceOldName", identityServiceOldName);
        sosHibernateSession.executeUpdate(query);
    }

}