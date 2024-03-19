package com.sos.joc.classes.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.classes.SOSInitialPasswordSetting;
import com.sos.auth.classes.SOSPasswordHasher;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamAccount2RoleWithName;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamPermission;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocInfoException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationAccount;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.SecurityConfigurationRoles;
import com.sos.joc.model.security.configuration.permissions.ControllerFolders;
import com.sos.joc.model.security.configuration.permissions.IniControllers;
import com.sos.joc.model.security.configuration.permissions.IniPermission;
import com.sos.joc.model.security.configuration.permissions.IniPermissions;
import com.sos.joc.model.security.configuration.permissions.SecurityConfigurationFolders;

public class SOSSecurityDBConfiguration {

    private void storeAccounts(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
            DBItemIamIdentityService dbItemIamIdentityService, boolean updateAccounts) throws Exception {

        SOSIdentityService sosIdentityService = new SOSIdentityService(dbItemIamIdentityService);
        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

        SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper.getInitialPasswordSettings(sosHibernateSession);
        String initialPassword = sosInitialPasswordSetting.getInitialPassword();

        for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
            String password = null;
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            if ((securityConfigurationAccount.getIdentityServiceId() != null && securityConfigurationAccount.getIdentityServiceId() == 0L)
                    || securityConfigurationAccount.getPassword() == null || securityConfigurationAccount.getPassword().isEmpty()) {
                password = initialPassword;
            } else {
                password = securityConfigurationAccount.getPassword();
            }

            if (!sosInitialPasswordSetting.isMininumPasswordLength(password)) {
                JocError error = new JocError();
                error.setMessage("Password is too short");
                throw new JocInfoException(error);
            }
            iamAccountFilter.setAccountName(securityConfigurationAccount.getAccountName());
            List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
            DBItemIamAccount dbItemIamAcount;
            if (listOfAccounts.size() == 1) {
                dbItemIamAcount = listOfAccounts.get(0);
            } else {
                dbItemIamAcount = new DBItemIamAccount();
            }

            dbItemIamAcount.setAccountName(securityConfigurationAccount.getAccountName());
            if ("JOC".equals(dbItemIamIdentityService.getIdentityServiceType()) || "VAULT-JOC-ACTIVE".equals(dbItemIamIdentityService
                    .getIdentityServiceType())) {
                if (!"********".equals(password)) {
                    dbItemIamAcount.setAccountPassword(SOSPasswordHasher.hash(password));
                }
            } else {
                dbItemIamAcount.setAccountPassword("********");
            }
            dbItemIamAcount.setIdentityServiceId(dbItemIamIdentityService.getId());
            dbItemIamAcount.setForcePasswordChange(securityConfigurationAccount.getForcePasswordChange());
            if (sosInitialPasswordSetting.getInitialPassword().equals(password)) {
                dbItemIamAcount.setForcePasswordChange(true);
            }
            dbItemIamAcount.setDisabled(securityConfigurationAccount.getDisabled());

            if (listOfAccounts.size() == 1) {
                if (updateAccounts) {
                    sosHibernateSession.update(dbItemIamAcount);
                } else {
                    dbItemIamAcount = null;
                }
            } else {
                sosHibernateSession.save(dbItemIamAcount);
            }

            iamAccountFilter = new IamAccountFilter();
            if (dbItemIamAcount != null) {
                Long accountId = dbItemIamAcount.getId();
                List<DBItemIamAccount2Roles> listOfRoles = iamAccountDBLayer.getListOfRoles(accountId);
                for (DBItemIamAccount2Roles entry : listOfRoles) {
                    DBItemIamRole dbItemIamRole = iamAccountDBLayer.getIamRole(entry.getRoleId());

                    if (dbItemIamRole != null && !securityConfigurationAccount.getRoles().contains(dbItemIamRole.getRoleName())) {
                        iamAccountFilter.setId(entry.getAccountId());
                        iamAccountFilter.setRoleId(entry.getRoleId());
                        iamAccountDBLayer.deleteAccount2Role(iamAccountFilter);
                    }
                }
                for (String role : securityConfigurationAccount.getRoles()) {
                    DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(role, dbItemIamIdentityService.getId());
                    if (dbItemIamRole != null) {
                        DBItemIamAccount2Roles dbItemIamAccount2Roles = iamAccountDBLayer.getRoleAssignment(dbItemIamRole.getId(), accountId);

                        if (dbItemIamAccount2Roles == null) {
                            dbItemIamAccount2Roles = new DBItemIamAccount2Roles();
                            dbItemIamAccount2Roles.setRoleId(dbItemIamRole.getId());
                            dbItemIamAccount2Roles.setAccountId(accountId);
                            sosHibernateSession.save(dbItemIamAccount2Roles);
                        }
                    }
                }
            }
        }
    }

    private void storeRoles(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
            DBItemIamIdentityService dbItemIamIdentityService) throws SOSHibernateException {
        if (securityConfiguration.getRoles() != null) {
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            for (String role : securityConfiguration.getRoles().getAdditionalProperties().keySet()) {
                DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(role, dbItemIamIdentityService.getId());
                if (dbItemIamRole == null) {
                    dbItemIamRole = new DBItemIamRole();
                    dbItemIamRole.setRoleName(role);
                    dbItemIamRole.setIdentityServiceId(dbItemIamIdentityService.getId());
                    sosHibernateSession.save(dbItemIamRole);
                }
            }
        }
    }

    private void storePermissions(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
            DBItemIamIdentityService dbItemIamIdentityService) throws SOSHibernateException {
        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

        if (securityConfiguration.getRoles() != null) {
            iamAccountDBLayer.deleteRole2Permissions(dbItemIamIdentityService.getId());
            for (Entry<String, SecurityConfigurationRole> roles : securityConfiguration.getRoles().getAdditionalProperties().entrySet()) {
                DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(roles.getKey(), dbItemIamIdentityService.getId());

                if (dbItemIamRole != null) {
                    Long roleId = dbItemIamRole.getId();

                    if (roles.getValue().getPermissions() != null) {
                        for (IniPermission iniPermission : roles.getValue().getPermissions().getJoc()) {

                            DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
                            if (iniPermission.getExcluded() == null) {
                                dbItemIamPermission.setExcluded(false);
                            } else {
                                dbItemIamPermission.setExcluded(iniPermission.getExcluded());
                            }

                            dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
                            dbItemIamPermission.setRecursive(false);
                            dbItemIamPermission.setRoleId(roleId);
                            dbItemIamPermission.setAccountPermission(iniPermission.getPermissionPath());
                            sosHibernateSession.save(dbItemIamPermission);
                        }
                        for (IniPermission iniPermission : roles.getValue().getPermissions().getControllerDefaults()) {

                            DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
                            dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
                            dbItemIamPermission.setRoleId(roleId);
                            dbItemIamPermission.setAccountPermission(iniPermission.getPermissionPath());
                            dbItemIamPermission.setRecursive(false);
                            dbItemIamPermission.setExcluded(iniPermission.getExcluded());
                            sosHibernateSession.save(dbItemIamPermission);
                        }

                        if (roles.getValue().getPermissions().getControllers() != null) {
                            for (Entry<String, List<IniPermission>> controller : roles.getValue().getPermissions().getControllers()
                                    .getAdditionalProperties().entrySet()) {
                                for (IniPermission iniPermission : controller.getValue()) {
                                    DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
                                    dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
                                    dbItemIamPermission.setRoleId(roleId);
                                    dbItemIamPermission.setAccountPermission(iniPermission.getPermissionPath());
                                    dbItemIamPermission.setControllerId(controller.getKey());
                                    dbItemIamPermission.setExcluded(iniPermission.getExcluded());
                                    dbItemIamPermission.setRecursive(false);
                                    sosHibernateSession.save(dbItemIamPermission);
                                }
                            }
                        }
                    }

                    if (roles.getValue().getFolders() != null) {
                        for (Folder folder : roles.getValue().getFolders().getJoc()) {

                            DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
                            dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
                            dbItemIamPermission.setFolderPermission(folder.getFolder());
                            dbItemIamPermission.setExcluded(false);

                            if (folder.getRecursive() == null) {
                                dbItemIamPermission.setRecursive(false);
                            } else {
                                dbItemIamPermission.setRecursive(folder.getRecursive());
                            }
                            dbItemIamPermission.setRoleId(roleId);
                            sosHibernateSession.save(dbItemIamPermission);
                        }

                        if (roles.getValue().getFolders().getControllers() != null) {
                            for (Entry<String, List<Folder>> controller : roles.getValue().getFolders().getControllers().getAdditionalProperties()
                                    .entrySet()) {

                                for (Folder folder : controller.getValue()) {
                                    DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
                                    dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
                                    dbItemIamPermission.setControllerId(controller.getKey());
                                    dbItemIamPermission.setFolderPermission(folder.getFolder());
                                    dbItemIamPermission.setExcluded(false);
                                    dbItemIamPermission.setRecursive(folder.getRecursive());
                                    dbItemIamPermission.setRoleId(roleId);
                                    sosHibernateSession.save(dbItemIamPermission);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public SecurityConfiguration writeConfiguration(SecurityConfiguration securityConfiguration, DBItemIamIdentityService dbItemIamIdentityService)
            throws Exception {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            storeRoles(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
            storeAccounts(sosHibernateSession, securityConfiguration, dbItemIamIdentityService, true);
            storePermissions(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);

            Globals.commit(sosHibernateSession);
            return securityConfiguration;
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            throw e;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public SecurityConfiguration importConfiguration(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
            DBItemIamIdentityService dbItemIamIdentityService) throws Exception {
        storeRoles(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
        storeAccounts(sosHibernateSession, securityConfiguration, dbItemIamIdentityService, false);
        storePermissions(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
        return securityConfiguration;

    }

    private List<SecurityConfigurationAccount> getAccounts(SOSHibernateSession sosHibernateSession, Long identityServiceId)
            throws SOSHibernateException {

        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        IamAccountFilter iamAccountFilter = new IamAccountFilter();
        iamAccountFilter.setIdentityServiceId(identityServiceId);
        List<SecurityConfigurationAccount> listOfAccountEntries = new ArrayList<SecurityConfigurationAccount>();
        List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
        for (DBItemIamAccount dbItemIamAccount : listOfAccounts) {
            SecurityConfigurationAccount securityConfigurationAccount = new SecurityConfigurationAccount();
            securityConfigurationAccount.setAccountName(dbItemIamAccount.getAccountName());
            securityConfigurationAccount.setPassword("********");
            securityConfigurationAccount.setIdentityServiceId(dbItemIamAccount.getIdentityServiceId());
            securityConfigurationAccount.setForcePasswordChange(dbItemIamAccount.getForcePasswordChange());
            securityConfigurationAccount.setDisabled(dbItemIamAccount.getDisabled());
            List<DBItemIamAccount2RoleWithName> listOfRoles = iamAccountDBLayer.getListOfRolesWithName(dbItemIamAccount);
            securityConfigurationAccount.setRoles(new ArrayList<String>());
            for (DBItemIamAccount2RoleWithName dbItemIamAccount2RoleWithName : listOfRoles) {
                securityConfigurationAccount.getRoles().add(dbItemIamAccount2RoleWithName.getRoleName());
            }
            listOfAccountEntries.add(securityConfigurationAccount);
        }
        return listOfAccountEntries;

    }

    private SecurityConfigurationRoles getRoles(SOSHibernateSession sosHibernateSession, Long identityServiceId) throws SOSHibernateException {

        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        SecurityConfigurationRoles securityConfigurationRoles = new SecurityConfigurationRoles();
        List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDBLayer.getListOfPermissionsWithName(identityServiceId);
        for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfPermissions) {
            SecurityConfigurationRole securityConfigurationRole = new SecurityConfigurationRole();
            IniPermissions permissions = new IniPermissions();
            SecurityConfigurationFolders folders = new SecurityConfigurationFolders();
            securityConfigurationRole.setFolders(folders);
            ControllerFolders controllerFolders = new ControllerFolders();
            securityConfigurationRole.getFolders().setControllers(controllerFolders);
            securityConfigurationRole.setPermissions(permissions);
            permissions.setJoc(new ArrayList<IniPermission>());
            permissions.setControllerDefaults(new ArrayList<IniPermission>());
            permissions.setControllers(new IniControllers());
            securityConfigurationRoles.setAdditionalProperty(dbItemSOSPermissionWithName.getRoleName(), securityConfigurationRole);
        }

        for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfPermissions) {
            SecurityConfigurationRole securityConfigurationRole = securityConfigurationRoles.getAdditionalProperties().get(dbItemSOSPermissionWithName
                    .getRoleName());
            if ((dbItemSOSPermissionWithName.getControllerId() == null || dbItemSOSPermissionWithName.getControllerId().isEmpty())
                    && (dbItemSOSPermissionWithName.getAccountPermission() != null && !dbItemSOSPermissionWithName.getAccountPermission()
                            .isEmpty())) {
                IniPermission iniPermission = new IniPermission();
                iniPermission.setPermissionPath(dbItemSOSPermissionWithName.getAccountPermission());
                iniPermission.setExcluded(dbItemSOSPermissionWithName.getExcluded());
                if (iniPermission.getPermissionPath().startsWith("sos:products:controller:")) {
                    securityConfigurationRole.getPermissions().getControllerDefaults().add(iniPermission);
                } else {
                    securityConfigurationRole.getPermissions().getJoc().add(iniPermission);
                }
            }

            if ((dbItemSOSPermissionWithName.getControllerId() != null && !dbItemSOSPermissionWithName.getControllerId().isEmpty())
                    && (dbItemSOSPermissionWithName.getAccountPermission() != null && !dbItemSOSPermissionWithName.getAccountPermission()
                            .isEmpty())) {
                IniPermission iniPermission = new IniPermission();
                iniPermission.setPermissionPath(dbItemSOSPermissionWithName.getAccountPermission());
                iniPermission.setExcluded(dbItemSOSPermissionWithName.getExcluded());
                if (securityConfigurationRole.getPermissions().getControllers().getAdditionalProperties().get(dbItemSOSPermissionWithName
                        .getControllerId()) == null) {
                    securityConfigurationRole.getPermissions().getControllers().getAdditionalProperties().put(dbItemSOSPermissionWithName
                            .getControllerId(), new ArrayList<IniPermission>());
                }
                securityConfigurationRole.getPermissions().getControllers().getAdditionalProperties().get(dbItemSOSPermissionWithName
                        .getControllerId()).add(iniPermission);
            }

            if ((dbItemSOSPermissionWithName.getControllerId() == null || dbItemSOSPermissionWithName.getControllerId().isEmpty())
                    && (dbItemSOSPermissionWithName.getFolderPermission() != null && !dbItemSOSPermissionWithName.getFolderPermission().isEmpty())) {
                Folder folder = new Folder();
                folder.setFolder(dbItemSOSPermissionWithName.getFolderPermission());
                folder.setRecursive(dbItemSOSPermissionWithName.getRecursive());
                securityConfigurationRole.getFolders().getJoc().add(folder);
            }

            if ((dbItemSOSPermissionWithName.getControllerId() != null && !dbItemSOSPermissionWithName.getControllerId().isEmpty())
                    && (dbItemSOSPermissionWithName.getFolderPermission() != null && !dbItemSOSPermissionWithName.getFolderPermission().isEmpty())) {
                Folder folder = new Folder();
                folder.setFolder(dbItemSOSPermissionWithName.getFolderPermission());
                folder.setRecursive(dbItemSOSPermissionWithName.getRecursive());
                if (securityConfigurationRole.getFolders().getControllers().getAdditionalProperties().get(dbItemSOSPermissionWithName
                        .getControllerId()) == null) {
                    securityConfigurationRole.getFolders().getControllers().getAdditionalProperties().put(dbItemSOSPermissionWithName
                            .getControllerId(), new ArrayList<Folder>());
                }

                securityConfigurationRole.getFolders().getControllers().getAdditionalProperties().get(dbItemSOSPermissionWithName.getControllerId())
                        .add(folder);
            }

            securityConfigurationRoles.setAdditionalProperty(dbItemSOSPermissionWithName.getRoleName(), securityConfigurationRole);

        }

        return securityConfigurationRoles;

    }

    public SecurityConfiguration readConfiguration(Long identityServiceId, String identityServiceName) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        SecurityConfiguration secConfig = new SecurityConfiguration();

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration.readConfiguration");

            if (identityServiceId == null) {
                IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
                IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
                iamIdentityServiceFilter.setIdentityServiceName(identityServiceName);
                DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
                identityServiceId = dbItemIamIdentityService.getId();
            }

            secConfig.setAccounts(getAccounts(sosHibernateSession, identityServiceId));
            secConfig.setRoles(getRoles(sosHibernateSession, identityServiceId));

        } finally {
            Globals.disconnect(sosHibernateSession);
        }

        return secConfig;
    }

}
