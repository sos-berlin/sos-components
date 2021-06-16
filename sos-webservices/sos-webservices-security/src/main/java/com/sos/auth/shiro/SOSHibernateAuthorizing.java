package com.sos.auth.shiro;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import com.sos.auth.shiro.db.SOSUserDBLayer;
import com.sos.auth.shiro.db.SOSUserFilter;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.SOSUser2RoleDBItem;
import com.sos.joc.db.authentication.SOSUserDBItem;
import com.sos.joc.db.authentication.SOSUserPermissionDBItem;
import com.sos.joc.db.authentication.SOSUserRoleDBItem;

public class SOSHibernateAuthorizing implements ISOSAuthorizing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateAuthorizing.class);
    private SimpleAuthorizationInfo authorizationInfo = null;

    @Override
    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo_ == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }
        String userName = (String) principalCollection.getPrimaryPrincipal();
        SOSUserDBLayer sosUserDBLayer;
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SimpleAuthorizationInfo");
            sosUserDBLayer = new SOSUserDBLayer(sosHibernateSession);
            SOSUserFilter filter = new SOSUserFilter();
            filter.setUserName(userName);
            List<SOSUserDBItem> sosUserList = null;
            try {
                sosUserList = sosUserDBLayer.getSOSUserList(filter, 0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            for (SOSUserDBItem sosUserDBItem : sosUserList) {

                List<SOSUser2RoleDBItem> sosUserRoles = sosUserDBLayer.getListOfUserRoles(sosUserDBItem);
                for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserRoles) {
                    SOSUserRoleDBItem sosUserRoleDBItem = sosUserDBLayer.getSosUserRole(sosUser2RoleDBItem.getRoleId());
                    authorizationInfo.addRole(sosUserRoleDBItem.getSosUserRole());
                }

            }
            return authorizationInfo;

        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        } finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }
    }

    @Override
    public SimpleAuthorizationInfo setPermissions(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }
        String userName = (String) principalCollection.getPrimaryPrincipal();
        SOSUserDBLayer sosUserDBLayer;
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SimpleAuthorizationInfo");
            sosUserDBLayer = new SOSUserDBLayer(sosHibernateSession);

            SOSUserFilter filter = new SOSUserFilter();

            filter.setUserName(userName);
            List<SOSUserDBItem> sosUserList = null;
            try {
                sosUserList = sosUserDBLayer.getSOSUserList(filter, 0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            for (SOSUserDBItem sosUserDBItem : sosUserList) {

                // The permission directly assigned
                List<SOSUserPermissionDBItem> sosUserPermissions = sosUserDBLayer.getListOfUserPermissions(sosUserDBItem.getId());

                for (SOSUserPermissionDBItem sosUserPermissionDBItem : sosUserPermissions) {
                    authorizationInfo.addStringPermission(sosUserPermissionDBItem.getSosUserPermission());
                }

                // The permissions assigned by the roles
                List<SOSUser2RoleDBItem> sosUserRoles = sosUserDBLayer.getListOfUserRoles(sosUserDBItem);

                for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserRoles) {

                    sosUserPermissions = sosUserDBLayer.getListOfRolePermissions(sosUser2RoleDBItem.getRoleId());

                    for (SOSUserPermissionDBItem sosUserPermissionDBItemFromRole : sosUserPermissions) {
                        authorizationInfo.addStringPermission(sosUserPermissionDBItemFromRole.getSosUserPermission());
                    }
                }
            }
            return authorizationInfo;
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        } finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }
    }

}
