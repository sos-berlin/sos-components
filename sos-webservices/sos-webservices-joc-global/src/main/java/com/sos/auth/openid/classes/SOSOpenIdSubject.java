package com.sos.auth.openid.classes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.common.ASOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.openid.SOSOpenIdSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSOpenIdSubject extends ASOSAuthSubject {

    private SOSOpenIdSession session;
    private SOSIdentityService identityService;
    private SOSAuthCurrentAccount currentAccount;

    public SOSOpenIdSubject(SOSAuthCurrentAccount currentAccount, SOSIdentityService identityService) {
        super();
        this.identityService = identityService;
        this.currentAccount = currentAccount;
    }

    private SOSOpenIdSession getOpenIdSession() {
        if (session == null) {
            session = new SOSOpenIdSession(currentAccount, identityService);
        }
        return session;
    }

    @Override
    public ISOSSession getSession() {
        return getOpenIdSession();
    }

    public void setAccessToken(SOSOpenIdAccountAccessToken accessToken) {
        getOpenIdSession().setAccessToken(accessToken);
    }

    public void setPermissionAndRoles(Set<String> setOfTokenRoles, String accountName) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            setOfRoles = new HashSet<>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            if (IdentityServiceTypes.OIDC_JOC == identityService.getIdentyServiceType()) {
                List<DBItemIamPermissionWithName> listOfRoles = iamAccountDBLayer.getListOfRolesForAccountName(accountName, identityService
                        .getIdentityServiceId());
                setOfRoles = listOfRoles.stream().map(DBItemIamPermissionWithName::getRoleName).collect(Collectors.toSet());
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
