package com.sos.auth.ldap.classes;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.common.ASOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.sosintern.SOSInternAuthSession;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSLdapSubject extends ASOSAuthSubject {

    private SOSInternAuthSession session;

    public SOSLdapSubject() {
        super();
        setOfRoles = new HashSet<>();
        setOfAccountPermissions = new HashSet<>();
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

    public void setPermissionAndRoles(List<String> listOfLdapRoles, String accountName, SOSIdentityService identityService)
            throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            setOfRoles = new HashSet<>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            if (IdentityServiceTypes.LDAP_JOC == identityService.getIdentyServiceType()) {
                List<DBItemIamPermissionWithName> listOfRoles = iamAccountDBLayer.getListOfRolesForAccountName(accountName, identityService
                        .getIdentityServiceId());
                setOfRoles = listOfRoles.stream().map(DBItemIamPermissionWithName::getRoleName).collect(Collectors.toSet());
            } else {
                if (listOfLdapRoles != null) {
                    setOfRoles.addAll(listOfLdapRoles);
                }
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
