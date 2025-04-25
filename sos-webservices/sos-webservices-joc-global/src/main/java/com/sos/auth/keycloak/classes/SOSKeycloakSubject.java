package com.sos.auth.keycloak.classes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.common.ASOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.keycloak.SOSKeycloakSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSKeycloakSubject extends ASOSAuthSubject {

    private SOSKeycloakSession session;
    private SOSIdentityService identityService;
    private String account;

    public SOSKeycloakSubject(String account, SOSIdentityService identityService) {
        super();
        this.identityService = identityService;
        this.account = account;
    }

    private SOSKeycloakSession getKeycloakSession() {
        if (session == null) {
            session = new SOSKeycloakSession(identityService);
        }
        return session;
    }

    @Override
    public ISOSSession getSession() {
        return getKeycloakSession();
    }

    public void setAccessToken(SOSKeycloakAccountAccessToken accessToken) {
        getKeycloakSession().setAccessToken(accessToken);
    }

    public void setPermissionAndRoles(Set<String> setOfTokenRoles, String accountName) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            setOfRoles = new HashSet<>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            if (IdentityServiceTypes.KEYCLOAK_JOC == identityService.getIdentyServiceType()) {
                List<DBItemIamPermissionWithName> listOfRoles = iamAccountDBLayer.getListOfRolesForAccountName(accountName, identityService
                        .getIdentityServiceId());
                for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfRoles) {
                    setOfRoles.add(dbItemSOSPermissionWithName.getRoleName());
                }
            } else {
                setOfRoles.addAll(setOfTokenRoles);
            }

            List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDBLayer.getListOfPermissionsFromRoleNames(setOfRoles, identityService
                    .getIdentityServiceId());
            mapOfFolderPermissions = SOSAuthHelper.getMapOfFolderPermissions(listOfPermissions);
            setOfAccountPermissions = SOSAuthHelper.getSetOfPermissions(listOfPermissions);
            setOf4EyesRolePermissions = SOSAuthHelper.getSetOf4EyesRolePermissions(listOfPermissions);

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
