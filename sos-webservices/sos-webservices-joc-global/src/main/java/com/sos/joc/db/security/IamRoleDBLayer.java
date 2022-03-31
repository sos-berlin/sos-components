package com.sos.joc.db.security;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamRole;

public class IamRoleDBLayer {

    private static final String DBItemIamRole = com.sos.joc.db.authentication.DBItemIamRole.class.getSimpleName();
    private static final String DBItemIamAccount2Roles = com.sos.joc.db.authentication.DBItemIamAccount2Roles.class.getSimpleName();
    private static final String DBItemIamPermission = com.sos.joc.db.authentication.DBItemIamPermission.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public IamRoleDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    private String getWhere(IamRoleFilter filter) {
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

        if (filter.getRoleName() != null && !filter.getRoleName().isEmpty()) {
            where += and + " roleName = :roleName";
            and = " and ";
        }
        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(IamRoleFilter filter, Query<T> query) {
        if (filter.getRoleName() != null && !filter.getRoleName().equals("")) {
            query.setParameter("roleName", filter.getRoleName());
        }
        if (filter.getIdentityServiceId() != null) {
            query.setParameter("identityServiceId", filter.getIdentityServiceId());
        }
        if (filter.getRoleId() != null) {
            query.setParameter("roleId", filter.getRoleId());
        }
        return query;
    }

    public int delete(IamRoleFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamRole + getWhere(filter);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);
        row = query.executeUpdate();
        return row;
    }

    public int deleteCascading(IamRoleFilter filter) throws SOSHibernateException {
        IamRoleFilter filterCascade = new IamRoleFilter();
        List<DBItemIamRole> iamRoleList = getIamRoleList(filter, 0);
        if (iamRoleList.size() > 0) {
            delete(filter);
            for (DBItemIamRole iamRoleDBItem : iamRoleList) {
                filterCascade.setRoleId(iamRoleDBItem.getId());
                deleteAccount2Role(filterCascade);
                deletePermission(filterCascade);
            }
        }
        return iamRoleList.size();
    }

    private int deletePermission(IamRoleFilter filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemIamPermission + getWhere(filter);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = query.executeUpdate();
        return row;
    }

    public int deleteAccount2Role(IamRoleFilter filter) throws SOSHibernateException {
        filter.setIdentityServiceId(null);
        String hql = "delete from " + DBItemIamAccount2Roles + getWhere(filter);
        Query<DBItemIamAccount> query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = query.executeUpdate();
        return row;
    }

    public List<DBItemIamRole> getIamRoleList(IamRoleFilter filter, final int limit) throws SOSHibernateException {
        Query<DBItemIamRole> query = sosHibernateSession.createQuery("from " + DBItemIamRole + getWhere(filter) + filter.getOrderCriteria() + filter
                .getSortMode());
        bindParameters(filter, query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<DBItemIamRole> iamRoleList = query.getResultList();
        return iamRoleList == null ? Collections.emptyList() : iamRoleList;
    }

    public List<String> getIamControllersForRole(IamRoleFilter filter) throws SOSHibernateException {
        filter.setRoleName(null);
        Query<String> query = sosHibernateSession.createQuery("select DISTINCT controllerId from " + DBItemIamPermission + getWhere(filter)
                + " and  controllerId is not null ");
        bindParameters(filter, query);

        List<String> iamRoleList = query.getResultList();
        if (!iamRoleList.contains("")) {
            iamRoleList.add("");
        }
        return iamRoleList == null ? Collections.emptyList() : iamRoleList;

    }

    public DBItemIamRole getIamRole(Long roleId) throws SOSHibernateException {
        return (DBItemIamRole) sosHibernateSession.get(DBItemIamRole.class, roleId);
    }

    public int renameRole(Long identityServiceId, String roleOldName, String roleNewName) throws SOSHibernateException {
        String hql = "update " + DBItemIamRole + " set roleName=:roleNewName where roleName=:roleOldName and identityServiceId=:identityServiceId";
        Query<DBItemIamIdentityService> query = sosHibernateSession.createQuery(hql);
        query.setParameter("roleOldName", roleOldName);
        query.setParameter("roleNewName", roleNewName);
        query.setParameter("identityServiceId", identityServiceId);
        return sosHibernateSession.executeUpdate(query);
    }

    public DBItemIamRole getUniqueRole(IamRoleFilter filter) throws SOSHibernateException {
        List<DBItemIamRole> roleList = null;
        Query<DBItemIamRole> query = sosHibernateSession.createQuery("from " + DBItemIamRole + getWhere(filter) + filter.getOrderCriteria() + filter
                .getSortMode());
        bindParameters(filter, query);

        roleList = query.getResultList();
        if (roleList.size() == 0) {
            return null;
        } else {
            return roleList.get(0);
        }
    }

    public boolean deleteRoleCascading(String role, Long identityServiceId) throws SOSHibernateException {
        IamRoleFilter filter = new IamRoleFilter();
        filter.setRoleName(role);
        filter.setIdentityServiceId(identityServiceId);
        DBItemIamRole dbItemIamRole = this.getUniqueRole(filter);
        if (dbItemIamRole != null) {
            filter.setRoleName(null);
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
