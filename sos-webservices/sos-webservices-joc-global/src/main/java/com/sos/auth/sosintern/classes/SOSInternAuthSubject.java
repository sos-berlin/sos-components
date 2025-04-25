package com.sos.auth.sosintern.classes;

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

public class SOSInternAuthSubject extends ASOSAuthSubject {

    private SOSInternAuthSession session;
    
    
    public SOSInternAuthSubject() {
        super();
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
            setOfRoles = new HashSet<>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDbLayer = new IamAccountDBLayer(sosHibernateSession);
            List<DBItemIamPermissionWithName> listOfRoles = iamAccountDbLayer.getListOfRolesForAccountName(accountName, identityServiceId
                    .getIdentityServiceId());
            setOfRoles = listOfRoles.stream().map(DBItemIamPermissionWithName::getRoleName).collect(Collectors.toSet());
            List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDbLayer.getListOfPermissionsFromRoleNames(setOfRoles, identityServiceId
                    .getIdentityServiceId());
            mapOfFolderPermissions = SOSAuthHelper.getMapOfFolderPermissions(listOfPermissions);
            setOfAccountPermissions = SOSAuthHelper.getSetOfPermissions(listOfPermissions);
            setOf4EyesRolePermissions = SOSAuthHelper.getSetOf4EyesRolePermissions(listOfPermissions);
            
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }
    
    public void setIsForcePasswordChange(Boolean isForcePasswordChange) {
        this.isForcePasswordChange = isForcePasswordChange;
    }
    
}
