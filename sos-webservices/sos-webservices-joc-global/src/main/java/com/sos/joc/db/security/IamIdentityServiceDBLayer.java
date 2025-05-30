package com.sos.joc.db.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;

public class IamIdentityServiceDBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IamIdentityServiceDBLayer.class);
    private static final String DBItemIamIdentityService = com.sos.joc.db.authentication.DBItemIamIdentityService.class.getSimpleName();
    private static final String DBItemIamRole = com.sos.joc.db.authentication.DBItemIamRole.class.getSimpleName();
    private static final String DBItemIamPermission = com.sos.joc.db.authentication.DBItemIamPermission.class.getSimpleName();

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

        if (filter.getSecondFactor() != null) {
            query.setParameter("secondFactor", filter.getSecondFactor());
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

        if (filter.getSecondFactor() != null) {
            where += and + " secondFactor = :secondFactor";
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

        identityList = sosHibernateSession.getResultList(query);
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
        identityServiceList = sosHibernateSession.getResultList(query);
        return identityServiceList;
    }

    public void rename(String identityServiceOldName, String identityServiceNewName) throws SOSHibernateException {
        String hql = "update " + DBItemIamIdentityService
                + " set identityServiceName=:identityServiceNewName where identityServiceName=:identityServiceOldName";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("identityServiceNewName", identityServiceNewName);
        query.setParameter("identityServiceOldName", identityServiceOldName);
        sosHibernateSession.executeUpdate(query);
    }

    private List<DBItemIamAccount> deleteAccountsByServiceId(Long identityServiceId) throws SOSHibernateException {
        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        IamAccountFilter iamAccountFilter = new IamAccountFilter();
        iamAccountFilter.setIdentityServiceId(identityServiceId);
        List<DBItemIamAccount> listOfDeletedAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
        iamAccountDBLayer.deleteCascading(iamAccountFilter);
        return listOfDeletedAccounts;
    }

    private void deleteRolesByServiceId(Long identityServiceId) throws SOSHibernateException {
        String hql = "delete " + DBItemIamRole + " where identityServiceId=:identityServiceId";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("identityServiceId", identityServiceId);
        sosHibernateSession.executeUpdate(query);
    }

    private void deletePermissionsByServiceId(Long identityServiceId) throws SOSHibernateException {
        String hql = "delete " + DBItemIamPermission + " where identityServiceId=:identityServiceId";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("identityServiceId", identityServiceId);
        sosHibernateSession.executeUpdate(query);
    }

    private void deleteSecondFactorByServiceId(Long identityServiceId) throws SOSHibernateException {
        String hql = "update " + DBItemIamIdentityService + " set secondFactorIsId=null where secondFactorIsId=:identityServiceId";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("identityServiceId", identityServiceId);
        sosHibernateSession.executeUpdate(query);
    }
    
    public int deleteCascading(IamIdentityServiceFilter filter) throws Exception {

        List<DBItemIamIdentityService> identityServices2BeDeleted = this.getIdentityServiceList(filter, 0);
        int count = identityServices2BeDeleted.size();
        if (count > 0) {
            count = this.delete(filter);
            for (DBItemIamIdentityService dbItemIamIdentityService : identityServices2BeDeleted) {
                Long identityServiceId = dbItemIamIdentityService.getId();
                List<DBItemIamAccount> listOfDeletedAccounts = deleteAccountsByServiceId(identityServiceId);
                Set<String> accounts = new HashSet<String>();
                listOfDeletedAccounts.stream().forEach(e -> accounts.add(e.getAccountName()));
                deleteRolesByServiceId(identityServiceId);
                deletePermissionsByServiceId(identityServiceId);
                deleteSecondFactorByServiceId(identityServiceId);

                JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);

                JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
                jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
                jocConfigurationFilter.setName(dbItemIamIdentityService.getIdentityServiceName());

                List<DBItemJocConfiguration> listOfdbItemJocConfiguration = jocConfigurationDBLayer.getJocConfigurations(jocConfigurationFilter, 0);
                if (listOfdbItemJocConfiguration.size() == 1) {
                    sosHibernateSession.delete(listOfdbItemJocConfiguration.get(0));
                }
            }
        }
        return count;
    }

}