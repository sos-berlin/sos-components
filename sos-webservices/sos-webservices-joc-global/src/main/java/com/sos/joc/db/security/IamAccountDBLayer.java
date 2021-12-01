package com.sos.joc.db.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamAccount2RoleWithName;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamPermission;
import com.sos.joc.db.authentication.DBItemIamRole;

public class IamAccountDBLayer {

    private static final String DBItemIamAccount = com.sos.joc.db.authentication.DBItemIamAccount.class.getSimpleName();
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

    public int deleteRoles() throws SOSHibernateException {
        String hql = "delete from " + DBItemIamRole;
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        row = query.executeUpdate();
        return row;
    }

    public void deleteCascading(IamAccountFilter filter) throws SOSHibernateException {
        List<DBItemIamAccount> iamAccountList = getIamAccountList(filter, 0);
        delete(filter);
        filter.setAccountName(null);
        for (DBItemIamAccount iamAccountDBItem : iamAccountList) {
            filter.setId(iamAccountDBItem.getId());
            deleteAccount2Role(filter);
            deletePermission(filter);
        }
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

    private int deleteAccount2Role(IamAccountFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamAccount2Roles + getWhere(filter);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = query.executeUpdate();
        return row;
    }

    public int deleteRole2Permissions() throws SOSHibernateException {
        String hql = "delete from " + DBItemIamPermission;
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);

        row = query.executeUpdate();
        return row;
    }

    private String getWhere(IamAccountFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getAccountName() != null && !filter.getAccountName().equals("")) {
            where += and + " accountName = :accountName";
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

    public List<DBItemIamAccount> getIamAccountList(IamAccountFilter filter, final int limit) throws SOSHibernateException {
        List<DBItemIamAccount> iamAccountList = null;
        Query<DBItemIamAccount> query = sosHibernateSession.createQuery("from " + DBItemIamAccount + getWhere(filter) + filter.getOrderCriteria() + filter
                .getSortMode());
        if (filter.getAccountName() != null && !filter.getAccountName().equals("")) {
            query.setParameter("accountName", filter.getAccountName());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        iamAccountList = query.getResultList();
        return iamAccountList;
    }

    public List<DBItemIamAccount2Roles> getListOfRoles(Long accountId) throws SOSHibernateException {
        List<DBItemIamAccount2Roles> iamAccount2RoleList = null;
        Query<DBItemIamAccount2Roles> query = sosHibernateSession.createQuery("from " + DBItemIamAccount2Roles + " where accountId=:accountId");

        query.setParameter("accountId", accountId);

        iamAccount2RoleList = query.getResultList();
        return iamAccount2RoleList;
    }

    public List<DBItemIamRole> getListOfAllRoles() throws SOSHibernateException {
        List<DBItemIamRole> iamRolesList = null;
        Query<DBItemIamRole> query = sosHibernateSession.createQuery("from " + DBItemIamRole );
        iamRolesList = query.getResultList();
        return iamRolesList;
    }

    public List<DBItemIamAccount2RoleWithName> getListOfRolesWithName(DBItemIamAccount dbitemIamAccount) throws SOSHibernateException {

        String q = "select u.roleId as roleId,u.accountId as accountId," + "r.roleName as roleName, '" + dbitemIamAccount.getAccountName() + "' as accountName"
                + " from " + DBItemIamAccount2Roles + " u, " + DBItemIamRole + " r where r.id=u.roleId and u.accountId=:accountId";

        Query<DBItemIamAccount2RoleWithName> query = sosHibernateSession.createQuery(q);
        query.setParameter("accountId", dbitemIamAccount.getId());
        query.setResultTransformer(Transformers.aliasToBean(DBItemIamAccount2RoleWithName.class));
        return sosHibernateSession.getResultList(query);

    }

    public List<DBItemIamPermissionWithName> getListOfPermissionsWithName() throws SOSHibernateException {

        String q = "select p.controllerId as controllerId,p.accountId as accountId,p.roleId as roleId,"
                + "p.accountPermission as accountPermission,p.folderPermission as folderPermission,"
                + "p.excluded as excluded,p.recursive as recursive,r.roleName as roleName" + " from " + DBItemIamPermission + " p, "
                + DBItemIamRole + " r where r.id=p.roleId";

        Query<DBItemIamPermissionWithName> query = sosHibernateSession.createQuery(q);
        query.setResultTransformer(Transformers.aliasToBean(DBItemIamPermissionWithName.class));
        return sosHibernateSession.getResultList(query);

    }

    public List<DBItemIamPermissionWithName> getListOfRolesForAccountName(String accountName) throws SOSHibernateException {

        String q = "select a.id as accountId,r.id as roleId," + "a.accountName as accountName," + "r.roleName as roleName" + " from " + DBItemIamAccount + " a, "
                + DBItemIamAccount2Roles + " ar," + DBItemIamRole + " r" + " where a.accountName=:accountName" + " and ar.accountId=a.id and r.id=ar.roleId";

        Query<DBItemIamPermissionWithName> query = sosHibernateSession.createQuery(q);
        query.setParameter("accountName", accountName);

        query.setResultTransformer(Transformers.aliasToBean(DBItemIamPermissionWithName.class));
        return sosHibernateSession.getResultList(query);

    }

    public DBItemIamRole getRoleByName(String roleName) throws SOSHibernateException {
        List<DBItemIamRole> iamRoleList = null;
        Query<DBItemIamRole> query = sosHibernateSession.createQuery("from " + DBItemIamRole + " where roleName=:roleName");

        query.setParameter("roleName", roleName);

        iamRoleList = query.getResultList();
        if (iamRoleList.size() > 0) {
            return iamRoleList.get(0);
        }
        return null;
    }

    public DBItemIamRole getIamRole(Long roleId) throws SOSHibernateException {
        return (DBItemIamRole) sosHibernateSession.get(DBItemIamRole.class, roleId);
    }

    public List<DBItemIamPermission> getListOfRolePermissions(Long roleId) throws SOSHibernateException {
        List<DBItemIamPermission> iamPermissionList = null;
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery("from " + DBItemIamPermission + " where roleId=:roleId");

        query.setParameter("roleId", roleId);

        iamPermissionList = query.getResultList();
        return iamPermissionList;
    }

    public List<DBItemIamPermission> getListOfPermissions(Long accountId) throws SOSHibernateException {
        List<DBItemIamPermission> iamPermissionList = null;
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery("from " + DBItemIamPermission + " where accountId=:accountId");

        query.setParameter("accountId", accountId);

        iamPermissionList = query.getResultList();
        return iamPermissionList;
    }

    public com.sos.joc.db.authentication.DBItemIamAccount getIamAccountByName(String accountName) throws SOSHibernateException {
        List<DBItemIamAccount> iamAccountList = null;
        Query<DBItemIamAccount> query = sosHibernateSession.createQuery("from " + DBItemIamAccount + " where accountName=:accountName");

        query.setParameter("accountName", accountName);

        iamAccountList = query.getResultList();
        if (iamAccountList.size() > 0) {
            return iamAccountList.get(0);
        }
        return null;
    }

    private String getOrderListSql(Collection<String> list) {
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

    private List<DBItemIamPermissionWithName> getListOfPermissionsFromRoles(Set<String> setOfRoles,String accountName) throws SOSHibernateException {
        String q = "select a.id as accountId, a.accountName as accountName, p.controllerId as controllerId,p.accountId as accountId,p.roleId as roleId,p.accountPermission as accountPermission,"
                + "p.folderPermission as folderPermission,p.excluded as excluded,p.recursive as recursive,r.roleName as roleName " + "from "
                + DBItemIamAccount + " a, " + DBItemIamPermission + " p," + DBItemIamRole + " r" + " where " + getOrderListSql(setOfRoles)
                + " and p.roleId=r.id and a.accountName = :accountName";

        Query<DBItemIamPermissionWithName> query = sosHibernateSession.createQuery(q);
        query.setParameter("accountName", accountName);

        query.setResultTransformer(Transformers.aliasToBean(DBItemIamPermissionWithName.class));
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemIamPermissionWithName> getListOfPermissionsFromRoleNames(Set<String> setOfRoles,String accountName) throws SOSHibernateException {
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
                resultList.addAll(getListOfPermissionsFromRoles(s,accountName));
            }
            return resultList;
        } else {
            return getListOfPermissionsFromRoles(setOfRoles,accountName);
        }
    }

}