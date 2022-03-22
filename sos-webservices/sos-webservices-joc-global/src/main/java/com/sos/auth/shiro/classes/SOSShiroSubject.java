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
    private Set<String> setOfAccountPermissions = new HashSet<String>();

    @Override
    public Boolean hasRole(String role) {
        return subject.hasRole(role);
    }

    @Override
    public Boolean isPermitted(String permission) {
        return subject.isPermitted(permission);
    }

    @Override
    public Boolean isAuthenticated() {
        return subject.isAuthenticated();
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
            Set<String> setOfRoles = new HashSet<String>();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            IamAccountDBLayer iamAccountDbLayer = new IamAccountDBLayer(sosHibernateSession);
            String account = (String) subject.getPrincipal();
            List<DBItemIamPermissionWithName> listOfRoles = iamAccountDbLayer.getListOfRolesForAccountName(account, identityServiceId);
            for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfRoles) {
                setOfRoles.add(dbItemSOSPermissionWithName.getRoleName());
            }

            List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDBLayer.getListOfPermissionsFromRoleNames(setOfRoles,
                    dbItemIamIdentityService.getId());
            Map<String, List<String>> mapOfFolderPermissions = new HashMap<String, List<String>>();
            Set<DBItemIamPermissionWithName> setOfPermissions = new HashSet<DBItemIamPermissionWithName>();
            for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfPermissions) {
                setOfPermissions.add(dbItemSOSPermissionWithName);
                if (dbItemSOSPermissionWithName.getAccountPermission() != null && !dbItemSOSPermissionWithName.getAccountPermission().isEmpty()) {
                    String permission = "";
                    if (dbItemSOSPermissionWithName.getExcluded()) {
                        permission = "-" + dbItemSOSPermissionWithName.getAccountPermission();
                    } else {
                        permission = dbItemSOSPermissionWithName.getAccountPermission();
                    }
                    setOfAccountPermissions.add(permission);
                }
                if (dbItemSOSPermissionWithName.getFolderPermission() != null && !dbItemSOSPermissionWithName.getFolderPermission().isEmpty()) {
                    if (mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()) == null) {
                        mapOfFolderPermissions.put(dbItemSOSPermissionWithName.getRoleName(), new ArrayList<String>());
                    }
                    if (dbItemSOSPermissionWithName.getRecursive()) {
                        mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()).add(dbItemSOSPermissionWithName.getFolderPermission()
                                + "/*");
                    } else {
                        mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()).add(dbItemSOSPermissionWithName.getFolderPermission());
                    }
                }
            }
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

}
