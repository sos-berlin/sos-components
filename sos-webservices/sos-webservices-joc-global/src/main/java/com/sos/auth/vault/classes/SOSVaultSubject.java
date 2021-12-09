package com.sos.auth.vault.classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.vault.SOSVaultSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.security.IamAccountDBLayer;

public class SOSVaultSubject implements ISOSAuthSubject {

    private SOSVaultSession session;
    private Boolean authenticated;
    private String account;
    private Map<String, List<String>> mapOfFolderPermissions;
    private Set<String> setOfAccountPermissions;
    private Set<String> setOfRoles;
    private Set<DBItemIamPermissionWithName> setOfPermissions;

    @Override
    public Boolean hasRole(String role) {
        return setOfRoles.contains(role);
    }

    @Override
    public Boolean isPermitted(String permission) {
        permission = permission + ":";
        for (String accountPermission:setOfAccountPermissions) {
            accountPermission = accountPermission + ":";
            if (permission.startsWith(accountPermission)){
                return true;
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

    private SOSVaultSession getVaultSession() {
        if (session == null) {
            session = new SOSVaultSession();
        }
        return session;
    }

    @Override
    public ISOSSession getSession() {
        return getVaultSession();
    }

    public void setAccessToken(SOSVaultAccountAccessToken accessToken) {
        getVaultSession().setAccessToken(accessToken);
    }

    public void setPermissionAndRoles(String userName) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            List<DBItemIamPermissionWithName> listOfRoles = iamAccountDBLayer.getListOfRolesForAccountName(userName);
            setOfRoles = new HashSet<String>();
            setOfAccountPermissions = new HashSet<String>();
            for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfRoles) {
                setOfRoles.add(dbItemSOSPermissionWithName.getRoleName());
            }
            List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDBLayer.getListOfPermissionsFromRoleNames(setOfRoles,account);
            setOfPermissions = new HashSet<DBItemIamPermissionWithName>();
            for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfPermissions) {
                setOfPermissions.add(dbItemSOSPermissionWithName);
                if (dbItemSOSPermissionWithName.getAccountPermission() != null && !dbItemSOSPermissionWithName.getAccountPermission().isEmpty()) {
                    setOfAccountPermissions.add(dbItemSOSPermissionWithName.getAccountPermission());
                }
                if (dbItemSOSPermissionWithName.getFolderPermission() != null && !dbItemSOSPermissionWithName.getFolderPermission().isEmpty()) {
                    if (mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()) == null) {
                        mapOfFolderPermissions.put(dbItemSOSPermissionWithName.getRoleName(), new ArrayList<String>());
                    }
                    mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()).add(dbItemSOSPermissionWithName.getFolderPermission());
                }
            }

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public Map<String, List<String>> getMapOfFolderPermissions() {
        return this.mapOfFolderPermissions;
    }

}
