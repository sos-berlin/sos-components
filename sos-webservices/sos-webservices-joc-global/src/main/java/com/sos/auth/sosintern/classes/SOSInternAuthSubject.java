package com.sos.auth.sosintern.classes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.sosintern.SOSInternAuthSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.security.IamAccountDBLayer;

public class SOSInternAuthSubject implements ISOSAuthSubject {

    private SOSInternAuthSession session;
    private Boolean authenticated;
    private Boolean isForcePasswordChange;
    private Map<String, List<String>> mapOfFolderPermissions;
    private Set<String> setOfAccountPermissions;
    private Set<String> setOfRoles;

    @Override
    public Boolean hasRole(String role) {
        return setOfRoles.contains(role);
    }

    @Override
    public Boolean isPermitted(String permission) {
        permission = permission + ":";
        if (setOfAccountPermissions != null) {
            for (String accountPermission : setOfAccountPermissions) {
                accountPermission = accountPermission + ":";
                if (permission.startsWith(accountPermission)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    private SOSInternAuthSession getInternAuthSession() {
        if (session == null) {
            session = new SOSInternAuthSession();
        }
        return session;
    }

    @Override
    public ISOSSession getSession() {
        return getInternAuthSession();
    }

    public void setAccessToken(String accessToken) {
        getInternAuthSession().setAccessToken(accessToken);
    }

    public void setPermissionAndRoles(String accountName, SOSIdentityService identityServiceId) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            setOfRoles = new HashSet<String>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDbLayer = new IamAccountDBLayer(sosHibernateSession);
            List<DBItemIamPermissionWithName> listOfRoles = iamAccountDbLayer.getListOfRolesForAccountName(accountName, identityServiceId
                    .getIdentityServiceId());
            for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfRoles) {
                setOfRoles.add(dbItemSOSPermissionWithName.getRoleName());
            }
            List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDbLayer.getListOfPermissionsFromRoleNames(setOfRoles, identityServiceId
                    .getIdentityServiceId());
            mapOfFolderPermissions = SOSAuthHelper.getMapOfFolderPermissions(listOfPermissions);
            setOfAccountPermissions = SOSAuthHelper.getSetOfPermissions(listOfPermissions);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    @Override
    public Map<String, List<String>> getMapOfFolderPermissions() {
        return mapOfFolderPermissions;
    }

    @Override
    public Boolean isForcePasswordChange() {
        return isForcePasswordChange;
    }

    public void setIsForcePasswordChange(Boolean isForcePasswordChange) {
        this.isForcePasswordChange = isForcePasswordChange;
    }

    @Override
    public Set<String> getListOfAccountPermissions() {
        return setOfAccountPermissions;
    }

}
