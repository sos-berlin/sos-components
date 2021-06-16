package com.sos.auth.shiro.db;

import java.util.List;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.SOSUser2RoleDBItem;
import com.sos.joc.db.authentication.SOSUserDBItem;
import com.sos.joc.db.authentication.SOSUserPermissionDBItem;
import com.sos.joc.db.authentication.SOSUserRoleDBItem;

public class SOSUserDBLayer {

    private static final String SOSUserDBItem = com.sos.joc.db.authentication.SOSUserDBItem.class.getSimpleName();
    private static final String SOSUser2RoleDBItem = com.sos.joc.db.authentication.SOSUser2RoleDBItem.class.getSimpleName();
    private static final String SOSUserPermissionDBItem = com.sos.joc.db.authentication.SOSUserPermissionDBItem.class.getSimpleName();

    private final SOSHibernateSession sosHibernateSession;

    public SOSUserDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public int delete(SOSUserFilter filter) throws Exception {
        String hql = "delete from " + SOSUserDBItem + getWhere(filter);
        Query<SOSUserDBItem> query = null;
        int row = 0;
        sosHibernateSession.beginTransaction();
        query = sosHibernateSession.createQuery(hql);
        if (filter.getUserName() != null && !filter.getUserName().equals("")) {
            query.setParameter("sosUserName", filter.getUserName());
        }
        row = query.executeUpdate();
        return row;
    }

    private String getWhere(SOSUserFilter filter) {
        String where = " ";
        String and = "";
        if (filter.getUserName() != null && !filter.getUserName().equals("")) {
            where += and + " sosUserName = :sosUserName";
            and = " and ";
        }
        if (!where.trim().equals("")) {
            where = " where " + where;
        }
        return where;
    }

    public List<SOSUserDBItem> getSOSUserList(SOSUserFilter filter, final int limit) throws Exception {
        List<SOSUserDBItem> sosUserList = null;
        sosHibernateSession.beginTransaction();
        Query<SOSUserDBItem> query = sosHibernateSession.createQuery("from " + SOSUserDBItem + getWhere(filter) + filter.getOrderCriteria() + filter
                .getSortMode());
        if (filter.getUserName() != null && !filter.getUserName().equals("")) {
            query.setParameter("sosUserName", filter.getUserName());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        sosUserList = query.getResultList();
        return sosUserList;
    }

    public List<SOSUser2RoleDBItem> getListOfUserRoles(SOSUserDBItem sosUserDBItem) throws SOSHibernateException {
        List<SOSUser2RoleDBItem> sosUser2RoleList = null;
        sosHibernateSession.beginTransaction();
        Query<SOSUser2RoleDBItem> query = sosHibernateSession.createQuery("from " + SOSUser2RoleDBItem + " where userId=:userId");

        query.setParameter("userId", sosUserDBItem.getId());

        sosUser2RoleList = query.getResultList();
        return sosUser2RoleList;
    }

    public SOSUserRoleDBItem getSosUserRole(Long roleId) throws SOSHibernateException {
        return (SOSUserRoleDBItem) sosHibernateSession.get(SOSUserRoleDBItem.class, roleId);
    }

    public List<SOSUserPermissionDBItem> getListOfRolePermissions(Long roleId) throws SOSHibernateException {
        List<SOSUserPermissionDBItem> sosUserPermissionList = null;
        sosHibernateSession.beginTransaction();
        Query<SOSUserPermissionDBItem> query = sosHibernateSession.createQuery("from " + SOSUserPermissionDBItem + " where roleId=:roleId");

        query.setParameter("roleId", roleId);

        sosUserPermissionList = query.getResultList();
        return sosUserPermissionList;
    }

    public List<SOSUserPermissionDBItem> getListOfUserPermissions(Long userId) throws SOSHibernateException {
        List<SOSUserPermissionDBItem> sosUserPermissionList = null;
        sosHibernateSession.beginTransaction();
        Query<SOSUserPermissionDBItem> query = sosHibernateSession.createQuery("from " + SOSUserPermissionDBItem + " where userId=:userId");

        query.setParameter("userId", userId);

        sosUserPermissionList = query.getResultList();
        return sosUserPermissionList;
    }

}