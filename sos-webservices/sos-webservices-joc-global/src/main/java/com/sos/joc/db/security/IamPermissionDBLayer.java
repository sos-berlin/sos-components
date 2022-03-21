package com.sos.joc.db.security;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamPermission;

public class IamPermissionDBLayer {

    private static final String DBItemIamRole = com.sos.joc.db.authentication.DBItemIamRole.class.getSimpleName();
    private static final String DBItemIamPermission = com.sos.joc.db.authentication.DBItemIamPermission.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public IamPermissionDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    private String getWhere(IamPermissionFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getControllerId() != null && !filter.getControllerId().isEmpty()) {
            where += and + " controllerId = :controllerId";
            and = " and ";
        } else {
            where += and + " (controllerId is null or controllerId = '')";
            and = " and ";
        }
        if (filter.getIdentityServiceId() != null) {
            where += and + " identityServiceId = :identityServiceId";
            and = " and ";
        }

        if (filter.getAccountId() != null) {
            where += and + " accountId = :accountId";
            and = " and ";
        }
        if (filter.getRoleId() != null) {
            where += and + " roleId = :roleId";
            and = " and ";
        }
        if (filter.getPermission() != null && !filter.getPermission().isEmpty()) {
            where += and + " folderPermission is null and accountPermission = :accountPermission";
            and = " and ";
        }
        if (filter.getFolder() != null && !filter.getFolder().isEmpty()) {
            where += and + " accountPermission is null and folderPermission = :folderPermission";
            and = " and ";
        }

        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(IamPermissionFilter filter, Query<T> query) {
        if (filter.getIdentityServiceId() != null) {
            query.setParameter("identityServiceId", filter.getIdentityServiceId());
        }
        if (filter.getControllerId() != null && !filter.getControllerId().isEmpty()) {
            query.setParameter("controllerId", filter.getControllerId());
        }
        if (filter.getRoleId() != null) {
            query.setParameter("roleId", filter.getRoleId());
        }
        if (filter.getAccountId() != null) {
            query.setParameter("accountId", filter.getAccountId());
        }
        if (filter.getPermission() != null && !filter.getPermission().isEmpty()) {
            query.setParameter("accountPermission", filter.getPermission());
        }
        if (filter.getFolder() != null && !filter.getFolder().isEmpty()) {
            query.setParameter("folderPermission", filter.getFolder());
        }

        return query;
    }

    public int delete(IamPermissionFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamPermission + getWhere(filter);
        Query<DBItemIamPermission> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);
        row = query.executeUpdate();
        return row;
    }

    public List<DBItemIamPermission> getIamPermissionList(IamPermissionFilter filter, final int limit) throws SOSHibernateException {
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery("from " + DBItemIamPermission + getWhere(filter)
                + " and folderPermission is null " + filter.getOrderCriteria() + filter.getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamPermission> iamPermissionList = query.getResultList();
        return iamPermissionList == null ? Collections.emptyList() : iamPermissionList;
    }

    public List<DBItemIamPermission> getIamFolderList(IamPermissionFilter filter, final int limit) throws SOSHibernateException {
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery("from " + DBItemIamPermission + getWhere(filter)
                + " and accountPermission is null " + filter.getOrderCriteria() + filter.getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamPermission> iamPermissionList = query.getResultList();
        return iamPermissionList == null ? Collections.emptyList() : iamPermissionList;
    }

    public DBItemIamPermission getIamPermission(Long permissionId) throws SOSHibernateException {
        return (DBItemIamPermission) sosHibernateSession.get(DBItemIamPermission.class, permissionId);
    }

    public int renamePermission(Long identityServiceId, Long roleId, String controllerId, String permissionOldPath, String permissionNewPath,
            boolean newExcluded) throws SOSHibernateException {
        IamPermissionFilter filter = new IamPermissionFilter();
        filter.setIdentityServiceId(identityServiceId);
        filter.setControllerId(controllerId);
        filter.setRoleId(roleId);
        filter.setPermission(permissionOldPath);
        String hql = "update " + DBItemIamPermission + " set accountPermission=:permissionNewPath, excluded=:newExcluded " + getWhere(filter);
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);

        query.setParameter("permissionNewPath", permissionNewPath);
        query.setParameter("newExcluded", newExcluded);
        return sosHibernateSession.executeUpdate(query);
    }

    public int renameFolder(Long identityServiceId, Long roleId, String controllerId, String oldFolder, String newFolder, boolean newRecursive)
            throws SOSHibernateException {
        IamPermissionFilter filter = new IamPermissionFilter();
        filter.setIdentityServiceId(identityServiceId);
        filter.setControllerId(controllerId);
        filter.setFolder(oldFolder);
        filter.setRoleId(roleId);
        String hql = "update " + DBItemIamPermission + " set folderPermission=:newFolder, recursive=:newRecursive " + getWhere(filter);

        Query<DBItemIamPermission> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);

        query.setParameter("newFolder", newFolder);
        query.setParameter("newRecursive", newRecursive);
        return sosHibernateSession.executeUpdate(query);
    }

    public DBItemIamPermission getUniquePermission(IamPermissionFilter filter) throws SOSHibernateException {
        List<DBItemIamPermission> permissionList = null;
        Query<DBItemIamPermission> query = sosHibernateSession.createQuery("from " + DBItemIamPermission + getWhere(filter));
        bindParameters(filter, query);

        permissionList = query.getResultList();
        if (permissionList.size() == 0) {
            return null;
        } else {
            return permissionList.get(0);
        }
    }

}
