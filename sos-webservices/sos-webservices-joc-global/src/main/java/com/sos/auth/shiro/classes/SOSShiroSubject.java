package com.sos.auth.shiro.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSPermissionsCreator;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSShiroSubject implements ISOSAuthSubject {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShiroSubject.class);

    private Subject subject;
    private SOSShiroSession session;
    private Set<String> setOfAccountPermissions;
    private Set<String> roles;
    private String accountName;

    @Override
    public Boolean hasRole(String role) {
        if (subject == null) {
            if (roles == null) {
                roles = new HashSet<String>();
                readAccountPermissions();
            }
            return roles.contains(role);
        } else {
            return subject.hasRole(role);
        }

    }

    @Override
    public Boolean isPermitted(String permission) {
        if (subject == null) {
            if (setOfAccountPermissions == null) {
                readAccountPermissions();
            }
            permission = permission + ":";
            for (String accountPermission : setOfAccountPermissions) {
                accountPermission = accountPermission + ":";
                if (permission.startsWith(accountPermission)) {
                    return true;
                }
            }
            return false;
        } else {
            return subject.isPermitted(permission);
        }
    }

    @Override
    public Boolean isAuthenticated() {
        if (subject == null) {
            return false;
        } else {
            return subject.isAuthenticated();
        }
    }

    @Override
    public ISOSSession getSession() {
        if (subject == null) {
            throw new InvalidSessionException();
        }
        if (session == null) {
            session = new SOSShiroSession();
        }
        session.setShiroSession(subject.getSession(false));
        return session;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public Map<String, List<String>> getMapOfFolderPermissions() {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
        return sosPermissionsCreator.getMapOfFolder();
    }

    @Override
    public Boolean isForcePasswordChange() {
        return false;
    }

    private void readAccountPermissions() {
        setOfAccountPermissions = new HashSet<String>();
        roles = new HashSet<String>();
        SOSHibernateSession sosHibernateSession = null;
        try {

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
            iamIdentityServiceFilter.setIamIdentityServiceType(IdentityServiceTypes.SHIRO);
            DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
            if (dbItemIamIdentityService == null) {
                throw new JocObjectNotExistException("Object Identity Service <" + IdentityServiceTypes.SHIRO + "> not found");
            }

            Long identityServiceId = dbItemIamIdentityService.getId();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDbLayer = new IamAccountDBLayer(sosHibernateSession);
            String account;
            if (subject == null) {
                account = this.accountName;
            } else {
                account = (String) subject.getPrincipal();
            }
            List<DBItemIamPermissionWithName> listOfRoles = iamAccountDbLayer.getListOfRolesForAccountName(account, identityServiceId);
            for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfRoles) {
                roles.add(dbItemSOSPermissionWithName.getRoleName());
            }

            List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDBLayer.getListOfPermissionsFromRoleNames(roles, dbItemIamIdentityService
                    .getId());
            setOfAccountPermissions = SOSAuthHelper.getSetOfPermissions(listOfPermissions);

        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    @Override
    public Set<String> getListOfAccountPermissions() {

        if (setOfAccountPermissions == null) {
            readAccountPermissions();
        }
        return setOfAccountPermissions;

    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

}
