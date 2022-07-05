package com.sos.joc.db.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamAccount2RoleWithName;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamHistory;
import com.sos.joc.db.authentication.DBItemIamPermission;
import com.sos.joc.db.authentication.DBItemIamRole;

public class IamAccountDBLayer {

    private static final String DBItemIamAccount = com.sos.joc.db.authentication.DBItemIamAccount.class.getSimpleName();
    private static final String DBItemIamHistory = com.sos.joc.db.authentication.DBItemIamHistory.class.getSimpleName();
    private static final String DBItemIamRole = com.sos.joc.db.authentication.DBItemIamRole.class.getSimpleName();
    private static final String DBItemIamAccount2Roles = com.sos.joc.db.authentication.DBItemIamAccount2Roles.class.getSimpleName();
    private static final String DBItemIamPermission = com.sos.joc.db.authentication.DBItemIamPermission.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public IamAccountDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    private <T> Query<T> bindParameters(IamAccountFilter filter, Query<T> query) {
        if (filter.getAccountName() != null && !filter.getAccountName().equals("")) {
            query.setParameter("accountName", filter.getAccountName());
        }
        if (filter.getId() != null) {
            query.setParameter("accountId", filter.getId());
        }
        if (filter.getIdentityServiceId() != null) {
            query.setParameter("identityServiceId", filter.getIdentityServiceId());
        }
        if (filter.getRoleId() != null) {
            query.setParameter("roleId", filter.getRoleId());
        }
        if (filter.getDisabled() != null) {
            query.setParameter("disabled", filter.getDisabled());
        }
        if (filter.getAccountName() != null && !filter.getAccountName().isEmpty()) {
            query.setParameter("accountName", filter.getAccountName());
        }

        return query;

    }

    public int delete(IamAccountFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamAccount + getWhere(filter);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);
        row = query.executeUpdate();
        return row;
    }

    public int deleteCascading(IamAccountFilter filter) throws SOSHibernateException {
        IamAccountFilter filterCascade = new IamAccountFilter();
        List<DBItemIamAccount> iamAccountList = getIamAccountList(filter, 0);
        if (iamAccountList.size() > 0) {
            delete(filter);
            for (DBItemIamAccount iamAccountDBItem : iamAccountList) {
                filterCascade.setId(iamAccountDBItem.getId());
                deleteAccount2Role(filterCascade);
                deletePermission(filterCascade);
            }
        }
        return iamAccountList.size();
    }

    private int deletePermission(IamAccountFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamPermission + getWhere(filter);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = query.executeUpdate();
        return row;
    }

    public int deleteAccount2Role(IamAccountFilter filter) throws SOSHibernateException {
        filter.setIdentityServiceId(null);
        String hql = "delete from " + DBItemIamAccount2Roles + getWhere(filter);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = query.executeUpdate();
        return row;
    }

    public int deleteRole2Permissions(Long identityServiceId) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamPermission + " where identityServiceId=:identityServiceId";
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query.setParameter("identityServiceId", identityServiceId);

        row = query.executeUpdate();
        return row;
    }

    private String getWhere(IamAccountFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getIdentityServiceId() != null) {
            where += and + " identityServiceId = :identityServiceId";
            and = " and ";
        }
        if (filter.getRoleId() != null) {
            where += and + " roleId = :roleId";
            and = " and ";
        }
        if (filter.getId() != null) {
            where += and + " accountId = :accountId";
            and = " and ";
        }
        if (filter.getAccountName() != null && !filter.getAccountName().isEmpty()) {
            where += and + " accountName = :accountName";
            and = " and ";
        }
        if (filter.getDisabled() != null) {
            where += and + " disabled = :disabled";
            and = " and ";
        }
        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    public List<DBItemIamAccount> getIamAccountList(IamAccountFilter filter, final int limit) throws SOSHibernateException {
        Query<DBItemIamAccount> query = sosHibernateSession.createQuery("from " + DBItemIamAccount + getWhere(filter) + filter.getOrderCriteria()
                + filter.getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamAccount> iamAccountList = query.getResultList();
        return iamAccountList == null ? Collections.emptyList() : iamAccountList;
    }

    public List<DBItemIamAccount2Roles> getListOfRoles(Long accountId) throws SOSHibernateException {
        Query<DBItemIamAccount2Roles> query = sosHibernateSession.createQuery("from " + DBItemIamAccount2Roles + " where accountId=:accountId");

        query.setParameter("accountId", accountId);

        List<DBItemIamAccount2Roles> iamAccount2RoleList = sosHibernateSession.getResultList(query);
        return iamAccount2RoleList == null ? Collections.emptyList() : iamAccount2RoleList;
    }

    public List<DBItemIamRole> getListOfAllRoles(Long identityServiceId) throws SOSHibernateException {
        Query<DBItemIamRole> query = sosHibernateSession.createQuery("from " + DBItemIamRole + " where identityServiceId=:identityServiceId");
        query.setParameter("identityServiceId", identityServiceId);

        List<DBItemIamRole> iamRolesList = sosHibernateSession.getResultList(query);
        return iamRolesList == null ? Collections.emptyList() : iamRolesList;
    }

    public List<DBItemIamAccount2RoleWithName> getListOfRolesWithName(DBItemIamAccount dbitemIamAccount) throws SOSHibernateException {

        String q = "select u.roleId as roleId,u.accountId as accountId," + "r.roleName as roleName, '" + dbitemIamAccount.getAccountName()
                + "' as accountName" + " from " + DBItemIamAccount2Roles + " u, " + DBItemIamRole
                + " r where r.id=u.roleId and u.accountId=:accountId and r.identityServiceId=:identityServiceId";

        Query<DBItemIamAccount2RoleWithName> query = sosHibernateSession.createQuery(q);
        query.setParameter("accountId", dbitemIamAccount.getId());
        query.setParameter("identityServiceId", dbitemIamAccount.getIdentityServiceId());
        query.setResultTransformer(Transformers.aliasToBean(DBItemIamAccount2RoleWithName.class));
        List<DBItemIamAccount2RoleWithName> listOfRolesWithName = sosHibernateSession.getResultList(query);
        return listOfRolesWithName == null ? Collections.emptyList() : listOfRolesWithName;

    }

    public List<DBItemIamPermissionWithName> getListOfPermissionsWithName(Long identityServiceId) throws SOSHibernateException {

        String q = "select p.controllerId as controllerId,p.roleId as roleId,"
                + "p.accountPermission as accountPermission,p.folderPermission as folderPermission,"
                + "p.excluded as excluded,p.recursive as recursive,r.roleName as roleName" + " from " + DBItemIamPermission + " p, " + DBItemIamRole
                + " r where r.identityServiceId=p.identityServiceId and r.id=p.roleId and r.identityServiceId=:identityServiceId";

        Query<DBItemIamPermissionWithName> query = sosHibernateSession.createQuery(q);
        query.setParameter("identityServiceId", identityServiceId);
        query.setResultTransformer(Transformers.aliasToBean(DBItemIamPermissionWithName.class));
        List<DBItemIamPermissionWithName> listOfPermissionsWithName = sosHibernateSession.getResultList(query);
        return listOfPermissionsWithName == null ? Collections.emptyList() : listOfPermissionsWithName;

    }

    public List<DBItemIamPermissionWithName> getListOfRolesForAccountName(String accountName, Long identityServiceId) throws SOSHibernateException {

        String q = "select r.id as roleId," + "r.roleName as roleName" + " from " + DBItemIamAccount + " a, " + DBItemIamAccount2Roles + " ar,"
                + DBItemIamRole + " r" + " where a.accountName=:accountName"
                + " and ar.accountId=a.id and r.id=ar.roleId and r.identityServiceId=:identityServiceId";

        Query<DBItemIamPermissionWithName> query = sosHibernateSession.createQuery(q);
        query.setParameter("accountName", accountName);
        query.setParameter("identityServiceId", identityServiceId);

        query.setResultTransformer(Transformers.aliasToBean(DBItemIamPermissionWithName.class));

        List<DBItemIamPermissionWithName> listOfRolesForAccountName = sosHibernateSession.getResultList(query);
        return listOfRolesForAccountName == null ? Collections.emptyList() : listOfRolesForAccountName;

    }

    public DBItemIamRole getRoleByName(String roleName, Long identityServiceId) throws SOSHibernateException {
        List<DBItemIamRole> iamRoleList = null;
        Query<DBItemIamRole> query = sosHibernateSession.createQuery("from " + DBItemIamRole
                + " where roleName=:roleName and identityServiceId=:identityServiceId");

        query.setParameter("roleName", roleName);
        query.setParameter("identityServiceId", identityServiceId);

        iamRoleList = query.getResultList();
        if (iamRoleList.size() > 0) {
            return iamRoleList.get(0);
        }
        return null;
    }

    public DBItemIamAccount2Roles getRoleAssignment(Long roleId, Long accountId) throws SOSHibernateException {
        List<DBItemIamAccount2Roles> iamAccount2RoleList = null;
        Query<DBItemIamAccount2Roles> query = sosHibernateSession.createQuery("from " + DBItemIamAccount2Roles
                + " where roleId=:roleId and accountId=:accountId");

        query.setParameter("roleId", roleId);
        query.setParameter("accountId", accountId);

        iamAccount2RoleList = query.getResultList();
        if (iamAccount2RoleList.size() > 0) {
            return iamAccount2RoleList.get(0);
        }
        return null;
    }

    public DBItemIamRole getIamRole(Long roleId) throws SOSHibernateException {
        return (DBItemIamRole) sosHibernateSession.get(DBItemIamRole.class, roleId);
    }

    public List<DBItemIamPermission> getListOfRolePermissions(Long roleId) throws SOSHibernateException {
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery("from " + DBItemIamPermission + " where roleId=:roleId");

        query.setParameter("roleId", roleId);

        List<DBItemIamPermission> iamPermissionList = sosHibernateSession.getResultList(query);
        return iamPermissionList == null ? Collections.emptyList() : iamPermissionList;
    }

    public List<DBItemIamPermission> getListOfPermissions(Long accountId) throws SOSHibernateException {
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery("from " + DBItemIamPermission + " where accountId=:accountId");

        query.setParameter("accountId", accountId);

        List<DBItemIamPermission> iamPermissionList = sosHibernateSession.getResultList(query);
        return iamPermissionList == null ? Collections.emptyList() : iamPermissionList;
    }

    public com.sos.joc.db.authentication.DBItemIamAccount getIamAccountByName(IamAccountFilter filter) throws SOSHibernateException {

        if ((filter.getAccountName() == null) || (filter.getAccountName().isEmpty())) {
            return null;
        }

        Query<DBItemIamAccount> query = sosHibernateSession.createQuery("from " + DBItemIamAccount + getWhere(filter));

        bindParameters(filter, query);

        List<DBItemIamAccount> iamAccountList = query.getResultList();
        if (iamAccountList.size() > 0) {
            return iamAccountList.get(0);
        }
        return null;
    }

    private String getRoleListSql(Collection<String> list) {
        if (list.size() == 0) {
            return "1=0";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("r.roleName in (");
        for (String s : list) {
            sql.append("'" + s + "',");
        }
        String s = sql.toString();
        s = s.substring(0, s.length() - 1);
        s = s + ")";

        return " (" + s + ") ";
    }

    private List<DBItemIamPermissionWithName> getListOfPermissionsFromRoles(Set<String> setOfRoles, Long identityServiceId)
            throws SOSHibernateException {
        if (setOfRoles.size() == 0) {
            return new ArrayList<DBItemIamPermissionWithName>();
        }

        String q = "select  p.controllerId as controllerId,p.roleId as roleId,p.accountPermission as accountPermission,"
                + "p.folderPermission as folderPermission,p.excluded as excluded,p.recursive as recursive,r.roleName as roleName " + "from "
                + DBItemIamPermission + " p," + DBItemIamRole + " r" + " where " + getRoleListSql(setOfRoles)
                + " and p.roleId=r.id and p.identityServiceId = :identityServiceId";

        Query<DBItemIamPermissionWithName> query = sosHibernateSession.createQuery(q);

        query.setParameter("identityServiceId", identityServiceId);

        query.setResultTransformer(Transformers.aliasToBean(DBItemIamPermissionWithName.class));

        List<DBItemIamPermissionWithName> listOfPermissionsFromRoles = sosHibernateSession.getResultList(query);
        return listOfPermissionsFromRoles == null ? Collections.emptyList() : listOfPermissionsFromRoles;

    }

    public List<DBItemIamPermissionWithName> getListOfPermissionsFromRoleNames(Set<String> setOfRoles, Long identityServiceId)
            throws SOSHibernateException {
        List<DBItemIamPermissionWithName> resultList = new ArrayList<DBItemIamPermissionWithName>();
        int size = setOfRoles.size();
        if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
            ArrayList<String> copy = (ArrayList<String>) setOfRoles.stream().collect(Collectors.toList());
            for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                Set<String> s = null;
                if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                    s = copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)).stream().collect(Collectors.toSet());

                } else {
                    s = copy.subList(i, size).stream().collect(Collectors.toSet());
                }
                resultList.addAll(getListOfPermissionsFromRoles(s, identityServiceId));
            }
            return resultList;
        } else {
            return getListOfPermissionsFromRoles(setOfRoles, identityServiceId);
        }
    }

    public int renameAccount(Long identityServiceId, String accountOldName, String accountNewName) throws SOSHibernateException {
        String hql = "update " + DBItemIamAccount
                + " set accountName=:accountNewName where accountName=:accountOldName and identityServiceId=:identityServiceId";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("accountOldName", accountOldName);
        query.setParameter("accountNewName", accountNewName);
        query.setParameter("identityServiceId", identityServiceId);
        return sosHibernateSession.executeUpdate(query);
    }

    public int renameRole(Long identityServiceId, String roleOldName, String roleNewName) throws SOSHibernateException {
        String hql = "update " + DBItemIamRole + " set roleName=:roleNewName where roleName=:roleOldName and identityServiceId=:identityServiceId";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("roleOldName", roleOldName);
        query.setParameter("roleNewName", roleNewName);
        query.setParameter("identityServiceId", identityServiceId);
        return sosHibernateSession.executeUpdate(query);
    }

    public DBItemIamAccount getUniqueAccount(IamAccountFilter filter) throws SOSHibernateException {
        if ((filter.getAccountName() == null) || (filter.getAccountName().isEmpty())) {
            return null;
        }
        List<DBItemIamAccount> accountList = null;
        Query<DBItemIamAccount> query = sosHibernateSession.createQuery("from " + DBItemIamAccount + getWhere(filter) + filter.getOrderCriteria()
                + filter.getSortMode());
        bindParameters(filter, query);

        accountList = query.getResultList();
        if (accountList.size() == 0) {
            return null;
        } else {
            return accountList.get(0);
        }
    }

    public boolean deleteRoleCascading(String role, Long identityServiceId) throws SOSHibernateException {
        DBItemIamRole dbItemIamRole = this.getRoleByName(role, identityServiceId);
        if (dbItemIamRole != null) {
            IamAccountFilter filter = new IamAccountFilter();
            filter.setRoleId(dbItemIamRole.getId());
            filter.setIdentityServiceId(identityServiceId);
            deletePermission(filter);
            deleteAccount2Role(filter);
            sosHibernateSession.delete(dbItemIamRole);
            return true;
        } else {
            return false;
        }
    }

  

}