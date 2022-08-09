package com.sos.joc.security.classes;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.db.security.IamRoleDBLayer;
import com.sos.joc.db.security.IamRoleFilter;
import com.sos.joc.exceptions.JocObjectNotExistException;

public class SecurityHelper {
    
    public static DBItemIamIdentityService getIdentityService(SOSHibernateSession sosHibernateSession, String identityServiceName)
            throws SOSHibernateException {
        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
        iamIdentityServiceFilter.setIdentityServiceName(identityServiceName);
        DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
        if (dbItemIamIdentityService == null) {
            throw new JocObjectNotExistException("Couldn't find the Identity Service <" + identityServiceName + ">");
        }
        return dbItemIamIdentityService;
    }

    public static DBItemIamRole getRole(SOSHibernateSession sosHibernateSession, Long identityServiceId, String roleName) throws SOSHibernateException {
        IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);
        IamRoleFilter iamRoleFilter = new IamRoleFilter();
        iamRoleFilter.setIdentityServiceId(identityServiceId);
        iamRoleFilter.setRoleName(roleName);
        DBItemIamRole dbItemIamRole = iamRoleDBLayer.getUniqueRole(iamRoleFilter);
        if (dbItemIamRole == null) {
            throw new JocObjectNotExistException("Couldn't find the role <" + roleName + ">");
        }
        return dbItemIamRole;
    }

}
