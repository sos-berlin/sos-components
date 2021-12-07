package com.sos.joc.classes.security;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.interfaces.ISOSSecurityConfiguration;
import com.sos.auth.vault.SOSVaultHandler;
import com.sos.auth.vault.classes.SOSVaultAccountCredentials;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
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
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.IniPermissions;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.SecurityConfigurationAccount;
import com.sos.joc.model.security.permissions.ControllerFolders;
import com.sos.joc.model.security.permissions.IniControllers;
import com.sos.joc.model.security.permissions.IniPermission;
import com.sos.joc.model.security.permissions.SecurityConfigurationFolders;
import com.sos.joc.model.security.permissions.SecurityConfigurationRole;
import com.sos.joc.model.security.permissions.SecurityConfigurationRoles;

public class SOSSecurityDBConfiguration implements ISOSSecurityConfiguration {

    private void storeInVault(DBItemIamAccount dbItemIamAccount) throws Exception {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
        webserviceCredentials.setValuesFromProfile();
        keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeyStorePath(), webserviceCredentials.getKeyStoreType(), webserviceCredentials
                .getKeyStorePassword());

        trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTrustStorePath(), webserviceCredentials.getTrustStoreType(),
                webserviceCredentials.getTrustStorePassword());

        SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);
        SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
        sosVaultAccountCredentials.setAccount(dbItemIamAccount.getAccountName());
        sosVaultAccountCredentials.setPolicy(webserviceCredentials.getVaultPolicy());
        sosVaultAccountCredentials.setPassword(SOSAuthHelper.getSHA512(dbItemIamAccount.getAccountPassword()));
        sosVaultHandler.storeAccountPassword(sosVaultAccountCredentials);

    }

    private void storeAccounts(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
            DBItemIamIdentityService dbItemIamIdentityService) throws Exception {
        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        IamAccountFilter iamAccountFilter = new IamAccountFilter();
        iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
        iamAccountDBLayer.deleteCascading(iamAccountFilter);
        for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
            iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            DBItemIamAccount dbItemIamAcount = new DBItemIamAccount();
            dbItemIamAcount.setAccountName(securityConfigurationAccount.getAccount());
            dbItemIamAcount.setAccountPassword(SOSAuthHelper.getSHA512(securityConfigurationAccount.getPassword()));
            dbItemIamAcount.setIdentityServiceId(dbItemIamIdentityService.getId());

            sosHibernateSession.save(dbItemIamAcount);
            if (IdentityServiceTypes.VAULT_JOC.name().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                storeInVault(dbItemIamAcount);
            }
        }
    }

    private void storeRoles(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
            DBItemIamIdentityService dbItemIamIdentityService) throws SOSHibernateException {
        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        iamAccountDBLayer.deleteRoles(dbItemIamIdentityService.getId());
        for (String role : securityConfiguration.getRoles().getAdditionalProperties().keySet()) {
            DBItemIamRole dbItemIamRole = new DBItemIamRole();
            dbItemIamRole.setRoleName(role);
            dbItemIamRole.setIdentityServiceId(dbItemIamIdentityService.getId());
            sosHibernateSession.save(dbItemIamRole);
        }

        for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
            
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(securityConfigurationAccount.getAccount());
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);

            if (dbItemIamAccount != null) {
                Long accountId = dbItemIamAccount.getId();
                for (String role : securityConfigurationAccount.getRoles()) {
                    DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(role);
                    if (dbItemIamRole != null) {
                        DBItemIamAccount2Roles dbItemIamAccount2Roles = new DBItemIamAccount2Roles();
                        dbItemIamAccount2Roles.setRoleId(dbItemIamRole.getId());
                        dbItemIamAccount2Roles.setAccountId(accountId);
                        sosHibernateSession.save(dbItemIamAccount2Roles);
                    }
                }
            }
        }
    }

    private void storePermissions(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
            DBItemIamIdentityService dbItemIamIdentityService) throws SOSHibernateException {
        IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
        iamAccountDBLayer.deleteRole2Permissions(dbItemIamIdentityService.getId());

        if (securityConfiguration.getRoles() != null) {
            for (Entry<String, SecurityConfigurationRole> roles : securityConfiguration.getRoles().getAdditionalProperties().entrySet()) {
                DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(roles.getKey());

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
                            dbItemIamPermission.setAccountPermission(iniPermission.getPath());
                            sosHibernateSession.save(dbItemIamPermission);
                        }
                        for (IniPermission iniPermission : roles.getValue().getPermissions().getControllerDefaults()) {

                            DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
                            dbItemIamPermission.setRoleId(roleId);
                            dbItemIamPermission.setAccountPermission(iniPermission.getPath());
                            dbItemIamPermission.setExcluded(iniPermission.getExcluded());
                            sosHibernateSession.save(dbItemIamPermission);
                        }

                        if (roles.getValue().getPermissions().getControllers() != null) {
                            for (Entry<String, List<IniPermission>> controller : roles.getValue().getPermissions().getControllers()
                                    .getAdditionalProperties().entrySet()) {
                                for (IniPermission iniPermission : controller.getValue()) {
                                    DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
                                    dbItemIamPermission.setRoleId(roleId);
                                    dbItemIamPermission.setAccountPermission(iniPermission.getPath());
                                    dbItemIamPermission.setControllerId(controller.getKey());
                                    dbItemIamPermission.setExcluded(iniPermission.getExcluded());
                                    sosHibernateSession.save(dbItemIamPermission);
                                }
                            }
                        }
                    }

                    if (roles.getValue().getFolders() != null) {
                        for (Folder folder : roles.getValue().getFolders().getJoc()) {

                            DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
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
                                    dbItemIamPermission.setControllerId(controller.getKey());
                                    dbItemIamPermission.setFolderPermission(folder.getFolder());
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

    @Override
    public SecurityConfiguration writeConfiguration(SecurityConfiguration securityConfiguration, DBItemIamIdentityService dbItemIamIdentityService)
            throws Exception {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            storeAccounts(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
            storeRoles(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
            storePermissions(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);

            Globals.commit(sosHibernateSession);
            return securityConfiguration;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

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
            securityConfigurationAccount.setAccount(dbItemIamAccount.getAccountName());
            securityConfigurationAccount.setPassword(dbItemIamAccount.getAccountPassword());
            securityConfigurationAccount.setIdentityServiceId(dbItemIamAccount.getIdentityServiceId());
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
                iniPermission.setPath(dbItemSOSPermissionWithName.getAccountPermission());
                iniPermission.setExcluded(dbItemSOSPermissionWithName.getExcluded());
                if (iniPermission.getPath().startsWith("sos:products:controller:")) {
                    securityConfigurationRole.getPermissions().getControllerDefaults().add(iniPermission);
                } else {
                    securityConfigurationRole.getPermissions().getJoc().add(iniPermission);
                }
            }

            if ((dbItemSOSPermissionWithName.getControllerId() != null && !dbItemSOSPermissionWithName.getControllerId().isEmpty())
                    && (dbItemSOSPermissionWithName.getAccountPermission() != null && !dbItemSOSPermissionWithName.getAccountPermission()
                            .isEmpty())) {
                IniPermission iniPermission = new IniPermission();
                iniPermission.setPath(dbItemSOSPermissionWithName.getAccountPermission());
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

    @Override
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
