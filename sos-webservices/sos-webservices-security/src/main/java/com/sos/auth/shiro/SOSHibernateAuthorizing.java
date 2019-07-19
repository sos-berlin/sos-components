package com.sos.auth.shiro;

import java.util.List;

import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.shiro.db.SOSUser2RoleDBItem;
import com.sos.auth.shiro.db.SOSUserDBItem;
import com.sos.auth.shiro.db.SOSUserDBLayer;
import com.sos.auth.shiro.db.SOSUserPermissionDBItem;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;

public class SOSHibernateAuthorizing implements ISOSAuthorizing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateAuthorizing.class);
    private SimpleAuthorizationInfo authorizationInfo = null;


    @Override
    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection)
            throws JocConfigurationException, DBConnectionRefusedException, DBOpenSessionException {
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
        try {
            if (authorizationInfo_ == null) {
                SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
                authorizationInfo = simpleAuthorizationInfo;
            } else {
                authorizationInfo = authorizationInfo_;
            }
            String userName = (String) principalCollection.getPrimaryPrincipal();
            SOSUserDBLayer sosUserDBLayer;
            try {
                sosUserDBLayer = new SOSUserDBLayer(sosHibernateSession);
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
            sosUserDBLayer.getFilter().setUserName(userName);
            List<SOSUserDBItem> sosUserList = null;
            try {
                sosUserList = sosUserDBLayer.getSOSUserList(0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            for (SOSUserDBItem sosUserDBItem : sosUserList) {
                for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserDBItem.getSOSUserRoleDBItems()) {
                    if (sosUser2RoleDBItem.getSosUserRoleDBItem() != null) {
                        authorizationInfo.addRole(sosUser2RoleDBItem.getSosUserRoleDBItem().getSosUserRole());
                    }
                }
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        ;
        return authorizationInfo;
    }

    @Override
    public SimpleAuthorizationInfo setPermissions(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection)
            throws JocConfigurationException, DBConnectionRefusedException, DBOpenSessionException {
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");

        try {
            if (authorizationInfo == null) {
                SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
                authorizationInfo = simpleAuthorizationInfo;
            } else {
                authorizationInfo = authorizationInfo_;
            }
            String userName = (String) principalCollection.getPrimaryPrincipal();
            SOSUserDBLayer sosUserDBLayer;
            try {
                sosUserDBLayer = new SOSUserDBLayer(sosHibernateSession);
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
            sosUserDBLayer.getFilter().setUserName(userName);
            List<SOSUserDBItem> sosUserList = null;
            try {
                sosUserList = sosUserDBLayer.getSOSUserList(0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            for (SOSUserDBItem sosUserDBItem : sosUserList) {
                // Die direkt zugewiesenen Rechte.
                for (SOSUserPermissionDBItem sosUserPermissionDBItem : sosUserDBItem.getSOSUserPermissionDBItems()) {
                    authorizationInfo.addStringPermission(sosUserPermissionDBItem.getSosUserPermission());
                }
                // Die �ber die Rollen zugewiesenen Rechte
                for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserDBItem.getSOSUserRoleDBItems()) {
                    for (SOSUserPermissionDBItem sosUserPermissionDBItemFromRole : sosUser2RoleDBItem.getSosUserRoleDBItem()
                            .getSOSUserPermissionDBItems()) {
                        authorizationInfo.addStringPermission(sosUserPermissionDBItemFromRole.getSosUserPermission());
                    }
                }
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return authorizationInfo;
    }

}
