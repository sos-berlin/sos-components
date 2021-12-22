package com.sos.auth.shiro;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import com.sos.auth.interfaces.ISOSAuthorizing;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamPermission;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;

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
        IamAccountDBLayer sosUserDBLayer;
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SimpleAuthorizationInfo");
            sosUserDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(userName);
            List<DBItemIamAccount> sosUserList = null;
            try {
                sosUserList = sosUserDBLayer.getIamAccountList(filter, 0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            for (DBItemIamAccount sosUserDBItem : sosUserList) {

                List<DBItemIamAccount2Roles> sosUserRoles = sosUserDBLayer.getListOfRoles(sosUserDBItem.getId());
                for (DBItemIamAccount2Roles sosUser2RoleDBItem : sosUserRoles) {
                    DBItemIamRole sosUserRoleDBItem = sosUserDBLayer.getIamRole(sosUser2RoleDBItem.getRoleId());
                    authorizationInfo.addRole(sosUserRoleDBItem.getRoleName());
                }

            }
            return authorizationInfo;

        } catch (Exception e1) {
            LOGGER.error("", e1);
            return null;
        } finally {
            Globals.disconnect(sosHibernateSession);
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
        IamAccountDBLayer sosUserDBLayer;
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SimpleAuthorizationInfo");
            sosUserDBLayer = new IamAccountDBLayer(sosHibernateSession);

            IamAccountFilter filter = new IamAccountFilter();

            filter.setAccountName(userName);
            List<DBItemIamAccount> sosUserList = null;
            try {
                sosUserList = sosUserDBLayer.getIamAccountList(filter, 0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            for (DBItemIamAccount sosUserDBItem : sosUserList) {

                // The permission directly assigned
                List<DBItemIamPermission> sosUserPermissions = sosUserDBLayer.getListOfPermissions(sosUserDBItem.getId());

                for (DBItemIamPermission sosUserPermissionDBItem : sosUserPermissions) {
                    authorizationInfo.addStringPermission(sosUserPermissionDBItem.getAccountPermission());
                }

                // The permissions assigned by the roles
                List<DBItemIamAccount2Roles> sosUserRoles = sosUserDBLayer.getListOfRoles(sosUserDBItem.getId());

                for (DBItemIamAccount2Roles sosUser2RoleDBItem : sosUserRoles) {

                    sosUserPermissions = sosUserDBLayer.getListOfRolePermissions(sosUser2RoleDBItem.getRoleId());

                    for (DBItemIamPermission sosUserPermissionDBItemFromRole : sosUserPermissions) {
                        authorizationInfo.addStringPermission(sosUserPermissionDBItemFromRole.getAccountPermission());
                    }
                }
            }
            return authorizationInfo;
        } catch (Exception e1) {
            LOGGER.error("", e1);
            return null;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
