package com.sos.auth.vault.classes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.vault.SOSVaultSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSVaultSubject implements ISOSAuthSubject {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultSubject.class);

    private SOSVaultSession session;
    private Boolean authenticated;
    private Map<String, List<String>> mapOfFolderPermissions;
    private Set<String> setOfAccountPermissions;
    private Set<String> setOfRoles;
    private SOSIdentityService identityService;
    private String account;

    public SOSVaultSubject(String account, SOSIdentityService identityService) {
        super();
        this.identityService = identityService;
        this.account = account;
    }

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

    private SOSVaultSession getVaultSession() {
        if (session == null) {
            session = new SOSVaultSession(identityService);
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

    public void setPermissionAndRoles(List<String> listOfTokenRoles, String accountName) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            setOfRoles = new HashSet<String>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            if (IdentityServiceTypes.VAULT_JOC == identityService.getIdentyServiceType() || IdentityServiceTypes.VAULT_JOC_ACTIVE == identityService
                    .getIdentyServiceType()) {
                List<DBItemIamPermissionWithName> listOfRoles = iamAccountDBLayer.getListOfRolesForAccountName(accountName, identityService
                        .getIdentityServiceId());
                for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfRoles) {
                    setOfRoles.add(dbItemSOSPermissionWithName.getRoleName());
                }
            } else {
                setOfRoles.addAll(listOfTokenRoles);
            }

            List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDBLayer.getListOfPermissionsFromRoleNames(setOfRoles, identityService
                    .getIdentityServiceId());
            mapOfFolderPermissions = SOSAuthHelper.getMapOfFolderPermissions(listOfPermissions);
            setOfAccountPermissions = SOSAuthHelper.getSetOfPermissions(listOfPermissions);

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public Map<String, List<String>> getMapOfFolderPermissions() {
        return this.mapOfFolderPermissions;
    }

    @Override
    public Boolean isForcePasswordChange() {
        if (identityService.getIdentyServiceType() == IdentityServiceTypes.VAULT_JOC_ACTIVE) {
            try {
                return SOSAuthHelper.getForcePasswordChange(account, identityService);
            } catch (SOSHibernateException e) {
                LOGGER.error("", e);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public Set<String> getListOfAccountPermissions() {
        return setOfAccountPermissions;
    }

    @Override
    public Set<String> getListOfAccountRoles() {
        return this.setOfRoles;
    }
}