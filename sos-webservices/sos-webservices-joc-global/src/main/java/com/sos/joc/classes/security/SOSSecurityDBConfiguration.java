package com.sos.joc.classes.security;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.classes.SOSInitialPasswordSetting;
import com.sos.auth.classes.SOSPasswordHasher;
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
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInfoException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.IniPermissions;
import com.sos.joc.model.security.Role;
import com.sos.joc.model.security.Roles;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.SecurityConfigurationAccount;
import com.sos.joc.model.security.permissions.ControllerFolders;
import com.sos.joc.model.security.permissions.IniControllers;
import com.sos.joc.model.security.permissions.IniPermission;
import com.sos.joc.model.security.permissions.SecurityConfigurationFolders;
import com.sos.joc.model.security.permissions.SecurityConfigurationRole;
import com.sos.joc.model.security.permissions.SecurityConfigurationRoles;

public class SOSSecurityDBConfiguration implements ISOSSecurityConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SOSSecurityDBConfiguration.class);

	private void storeInVault(SOSVaultWebserviceCredentials webserviceCredentials,
			SecurityConfigurationAccount securityConfigurationAccount, String password,
			IdentityServiceTypes identityServiceTypes) throws Exception {
		KeyStore trustStore = null;

		if ((webserviceCredentials.getTruststorePath() != null)
				&& (webserviceCredentials.getTrustStoreType() != null)) {
			trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(),
					webserviceCredentials.getTrustStoreType(), webserviceCredentials.getTruststorePassword());

			SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, trustStore);
			SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
			sosVaultAccountCredentials.setUsername(securityConfigurationAccount.getAccount());

			List<String> tokenPolicies = new ArrayList<String>();
			for (String role : securityConfigurationAccount.getRoles()) {
				tokenPolicies.add(role);
			}

			sosVaultAccountCredentials.setTokenPolicies(tokenPolicies);

			if (!"********".equals(password) && IdentityServiceTypes.VAULT_JOC_ACTIVE.equals(identityServiceTypes)) {
				sosVaultHandler.storeAccountPassword(sosVaultAccountCredentials, password);
			}

			sosVaultHandler.updateTokenPolicies(sosVaultAccountCredentials);
		} else {
			JocError error = new JocError();
			error.setMessage("Configuration for VAULT missing");
			throw new JocException(error);
		}

	}

	private void storeAccounts(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
			DBItemIamIdentityService dbItemIamIdentityService, boolean updateAccounts) throws Exception {

		SOSIdentityService sosIdentityService = new SOSIdentityService(dbItemIamIdentityService);
		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

		SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
		if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString()
				.equals(dbItemIamIdentityService.getIdentityServiceType())) {
			webserviceCredentials.setValuesFromProfile(sosIdentityService);
		}

		SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper
				.getInitialPasswordSettings(sosHibernateSession);
		String initialPassword = sosInitialPasswordSetting.getInitialPassword();

		for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
			String password = null;
			IamAccountFilter iamAccountFilter = new IamAccountFilter();
			iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

			if ((securityConfigurationAccount.getIdentityServiceId() != null
					&& securityConfigurationAccount.getIdentityServiceId() == 0L)
					|| securityConfigurationAccount.getPassword() == null
					|| securityConfigurationAccount.getPassword().isEmpty()) {
				password = initialPassword;
			} else {
				password = securityConfigurationAccount.getPassword();
			}

			if (!sosInitialPasswordSetting.isMininumPasswordLength(password)) {
				JocError error = new JocError();
				error.setMessage("Password is too short");
				throw new JocInfoException(error);
			}
			iamAccountFilter.setAccountName(securityConfigurationAccount.getAccount());
			List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
			DBItemIamAccount dbItemIamAcount;
			if (listOfAccounts.size() == 1) {
				dbItemIamAcount = listOfAccounts.get(0);
			} else {
				dbItemIamAcount = new DBItemIamAccount();
			}

			dbItemIamAcount.setAccountName(securityConfigurationAccount.getAccount());
			if ("JOC".equals(dbItemIamIdentityService.getIdentityServiceType())
					|| "VAULT-JOC-ACTIVE".equals(dbItemIamIdentityService.getIdentityServiceType())) {
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

					if (dbItemIamRole != null
							&& !securityConfigurationAccount.getRoles().contains(dbItemIamRole.getRoleName())) {
						iamAccountFilter.setId(entry.getAccountId());
						iamAccountFilter.setRoleId(entry.getRoleId());
						iamAccountDBLayer.deleteAccount2Role(iamAccountFilter);
					}
				}
				for (String role : securityConfigurationAccount.getRoles()) {
					DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(role,
							dbItemIamIdentityService.getId());
					if (dbItemIamRole != null) {
						DBItemIamAccount2Roles dbItemIamAccount2Roles = iamAccountDBLayer
								.getRoleAssignment(dbItemIamRole.getId(), accountId);

						if (dbItemIamAccount2Roles == null) {
							dbItemIamAccount2Roles = new DBItemIamAccount2Roles();
							dbItemIamAccount2Roles.setRoleId(dbItemIamRole.getId());
							dbItemIamAccount2Roles.setAccountId(accountId);
							sosHibernateSession.save(dbItemIamAccount2Roles);
						}
					}
				}
			}

			if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString()
					.equals(dbItemIamIdentityService.getIdentityServiceType())) {
				storeInVault(webserviceCredentials, securityConfigurationAccount, password,
						IdentityServiceTypes.fromValue(dbItemIamIdentityService.getIdentityServiceType()));
			}
		}
	}

	private void deleteAccounts(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
			DBItemIamIdentityService dbItemIamIdentityService) throws Exception {

		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
		IamAccountFilter iamAccountFilter = new IamAccountFilter();
		iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

		for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
			iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
			iamAccountFilter.setAccountName(securityConfigurationAccount.getAccount());
			int count = iamAccountDBLayer.deleteCascading(iamAccountFilter);
			if (count == 0) {
				throw new JocObjectNotExistException("Object <" + securityConfigurationAccount.getAccount() + "> not found" );
			}
		}
	}

	private void changePasswordAccounts(SOSHibernateSession sosHibernateSession, boolean withPasswordCheck,
			SecurityConfiguration securityConfiguration, DBItemIamIdentityService dbItemIamIdentityService)
			throws Exception {

		SOSIdentityService sosIdentityService = new SOSIdentityService(dbItemIamIdentityService);
		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
		IamAccountFilter iamAccountFilter = new IamAccountFilter();

		if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString().equals(dbItemIamIdentityService.getIdentityServiceType())
				|| IdentityServiceTypes.JOC.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
			SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
			if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString()
					.equals(dbItemIamIdentityService.getIdentityServiceType())) {
				webserviceCredentials.setValuesFromProfile(sosIdentityService);
			}

			String initialPassword = null;
			SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper
					.getInitialPasswordSettings(sosHibernateSession);

			for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
				if (securityConfigurationAccount.getPassword() != null && securityConfigurationAccount.getPassword()
						.equals(securityConfigurationAccount.getRepeatedPassword())) {
					iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
					iamAccountFilter.setAccountName(securityConfigurationAccount.getAccount());

					List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
					if (listOfAccounts.size() == 1) {

						if (withPasswordCheck) {
							if (!SOSPasswordHasher.verify(securityConfigurationAccount.getOldPassword(),
									listOfAccounts.get(0).getAccountPassword())) {
								continue;
							}
						}

						String password;
						if (securityConfigurationAccount.getPassword() == null
								|| securityConfigurationAccount.getPassword().isEmpty()) {
							if (initialPassword == null) {
								initialPassword = sosInitialPasswordSetting.getInitialPassword();
							}
							password = initialPassword;
							listOfAccounts.get(0).setForcePasswordChange(true);

						} else {
							password = securityConfigurationAccount.getPassword();
							listOfAccounts.get(0).setForcePasswordChange(false);
						}
						listOfAccounts.get(0).setAccountPassword(SOSPasswordHasher.hash(password));
						if (!sosInitialPasswordSetting.isMininumPasswordLength(password)) {
							JocError error = new JocError();
							error.setMessage("Password is too short");
							throw new JocInfoException(error);
						}

						if (sosInitialPasswordSetting.getInitialPassword().equals(password)) {
							listOfAccounts.get(0).setForcePasswordChange(true);
						}

						sosHibernateSession.update(listOfAccounts.get(0));

						if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString()
								.equals(dbItemIamIdentityService.getIdentityServiceType())) {
							storeInVault(webserviceCredentials, securityConfigurationAccount, password,
									IdentityServiceTypes.fromValue(dbItemIamIdentityService.getIdentityServiceType()));
						}
					} else {
						JocError error = new JocError();
						error.setMessage("Unknown account or password is wrong");
						throw new JocInfoException(error);
					}

				} else {
					JocError error = new JocError();
					error.setMessage("Password does not match repeated password");
					throw new JocInfoException(error);
				}
			}
		}

	}

	private void forcePasswordChangeAccounts(SOSHibernateSession sosHibernateSession,
			SecurityConfiguration securityConfiguration, DBItemIamIdentityService dbItemIamIdentityService)
			throws Exception {

		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
		IamAccountFilter iamAccountFilter = new IamAccountFilter();

		if (IdentityServiceTypes.VAULT_JOC_ACTIVE.toString().equals(dbItemIamIdentityService.getIdentityServiceType())
				|| IdentityServiceTypes.JOC.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {

			for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
				iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
				iamAccountFilter.setAccountName(securityConfigurationAccount.getAccount());
				List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
				if (listOfAccounts.size() == 1) {
					listOfAccounts.get(0).setForcePasswordChange(true);
					sosHibernateSession.update(listOfAccounts.get(0));
				} else {
					JocError error = new JocError();
					error.setMessage("Unknown account:" + securityConfigurationAccount.getAccount());
					throw new JocInfoException(error);
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

	private void deleteRoles(SOSHibernateSession sosHibernateSession, Roles roles,
			DBItemIamIdentityService dbItemIamIdentityService) throws SOSHibernateException {
		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
		for (Role role : roles.getRoles()) {
			if (!iamAccountDBLayer.deleteRoleCascading(role.getRole(), dbItemIamIdentityService.getId())) {
				throw new JocObjectNotExistException("Object <" + role.getRole() + " not found");
			}

		}
	}

	private void storePermissions(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration,
			DBItemIamIdentityService dbItemIamIdentityService) throws SOSHibernateException {
		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

		if (securityConfiguration.getRoles() != null) {
			iamAccountDBLayer.deleteRole2Permissions(dbItemIamIdentityService.getId());
			for (Entry<String, SecurityConfigurationRole> roles : securityConfiguration.getRoles()
					.getAdditionalProperties().entrySet()) {
				DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(roles.getKey(),
						dbItemIamIdentityService.getId());

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
							dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
							dbItemIamPermission.setRoleId(roleId);
							dbItemIamPermission.setAccountPermission(iniPermission.getPath());
							dbItemIamPermission.setRecursive(false);
							dbItemIamPermission.setExcluded(iniPermission.getExcluded());
							sosHibernateSession.save(dbItemIamPermission);
						}

						if (roles.getValue().getPermissions().getControllers() != null) {
							for (Entry<String, List<IniPermission>> controller : roles.getValue().getPermissions()
									.getControllers().getAdditionalProperties().entrySet()) {
								for (IniPermission iniPermission : controller.getValue()) {
									DBItemIamPermission dbItemIamPermission = new DBItemIamPermission();
									dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
									dbItemIamPermission.setRoleId(roleId);
									dbItemIamPermission.setAccountPermission(iniPermission.getPath());
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
							for (Entry<String, List<Folder>> controller : roles.getValue().getFolders().getControllers()
									.getAdditionalProperties().entrySet()) {

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

	@Override
	public SecurityConfiguration writeConfiguration(SecurityConfiguration securityConfiguration,
			DBItemIamIdentityService dbItemIamIdentityService) throws Exception {
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

	public SecurityConfiguration deleteAccounts(SecurityConfiguration securityConfiguration,
			DBItemIamIdentityService dbItemIamIdentityService) throws Exception {
		SOSHibernateSession sosHibernateSession = null;
		try {
			sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);
			deleteAccounts(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
			Globals.commit(sosHibernateSession);
			return securityConfiguration;
		} catch (Exception e) {
			Globals.rollback(sosHibernateSession);
			throw e;
		} finally {
			Globals.disconnect(sosHibernateSession);
		}

	}

	public void deleteRoles(Roles roles, DBItemIamIdentityService dbItemIamIdentityService) throws Exception {
		SOSHibernateSession sosHibernateSession = null;
		try {
			sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);
			deleteRoles(sosHibernateSession, roles, dbItemIamIdentityService);
			Globals.commit(sosHibernateSession);
		} catch (Exception e) {
			Globals.rollback(sosHibernateSession);
			throw e;
		} finally {
			Globals.disconnect(sosHibernateSession);
		}

	}

	public SecurityConfiguration importConfiguration(SOSHibernateSession sosHibernateSession,
			SecurityConfiguration securityConfiguration, DBItemIamIdentityService dbItemIamIdentityService)
			throws Exception {
		storeRoles(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
		storeAccounts(sosHibernateSession, securityConfiguration, dbItemIamIdentityService, false);
		storePermissions(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
		return securityConfiguration;

	}

	private List<SecurityConfigurationAccount> getAccounts(SOSHibernateSession sosHibernateSession,
			Long identityServiceId) throws SOSHibernateException {

		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
		IamAccountFilter iamAccountFilter = new IamAccountFilter();
		iamAccountFilter.setIdentityServiceId(identityServiceId);
		List<SecurityConfigurationAccount> listOfAccountEntries = new ArrayList<SecurityConfigurationAccount>();
		List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
		for (DBItemIamAccount dbItemIamAccount : listOfAccounts) {
			SecurityConfigurationAccount securityConfigurationAccount = new SecurityConfigurationAccount();
			securityConfigurationAccount.setAccount(dbItemIamAccount.getAccountName());
			securityConfigurationAccount.setPassword("********");
			securityConfigurationAccount.setHashedPassword(dbItemIamAccount.getAccountPassword());
			securityConfigurationAccount.setIdentityServiceId(dbItemIamAccount.getIdentityServiceId());
			securityConfigurationAccount.setForcePasswordChange(dbItemIamAccount.getForcePasswordChange());
			securityConfigurationAccount.setDisabled(dbItemIamAccount.getDisabled());
			List<DBItemIamAccount2RoleWithName> listOfRoles = iamAccountDBLayer
					.getListOfRolesWithName(dbItemIamAccount);
			securityConfigurationAccount.setRoles(new ArrayList<String>());
			for (DBItemIamAccount2RoleWithName dbItemIamAccount2RoleWithName : listOfRoles) {
				securityConfigurationAccount.getRoles().add(dbItemIamAccount2RoleWithName.getRoleName());
			}
			listOfAccountEntries.add(securityConfigurationAccount);
		}
		return listOfAccountEntries;

	}

	private SecurityConfigurationRoles getRoles(SOSHibernateSession sosHibernateSession, Long identityServiceId)
			throws SOSHibernateException {

		IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
		SecurityConfigurationRoles securityConfigurationRoles = new SecurityConfigurationRoles();
		List<DBItemIamPermissionWithName> listOfPermissions = iamAccountDBLayer
				.getListOfPermissionsWithName(identityServiceId);
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
			securityConfigurationRoles.setAdditionalProperty(dbItemSOSPermissionWithName.getRoleName(),
					securityConfigurationRole);
		}

		for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfPermissions) {
			SecurityConfigurationRole securityConfigurationRole = securityConfigurationRoles.getAdditionalProperties()
					.get(dbItemSOSPermissionWithName.getRoleName());
			if ((dbItemSOSPermissionWithName.getControllerId() == null
					|| dbItemSOSPermissionWithName.getControllerId().isEmpty())
					&& (dbItemSOSPermissionWithName.getAccountPermission() != null
							&& !dbItemSOSPermissionWithName.getAccountPermission().isEmpty())) {
				IniPermission iniPermission = new IniPermission();
				iniPermission.setPath(dbItemSOSPermissionWithName.getAccountPermission());
				iniPermission.setExcluded(dbItemSOSPermissionWithName.getExcluded());
				if (iniPermission.getPath().startsWith("sos:products:controller:")) {
					securityConfigurationRole.getPermissions().getControllerDefaults().add(iniPermission);
				} else {
					securityConfigurationRole.getPermissions().getJoc().add(iniPermission);
				}
			}

			if ((dbItemSOSPermissionWithName.getControllerId() != null
					&& !dbItemSOSPermissionWithName.getControllerId().isEmpty())
					&& (dbItemSOSPermissionWithName.getAccountPermission() != null
							&& !dbItemSOSPermissionWithName.getAccountPermission().isEmpty())) {
				IniPermission iniPermission = new IniPermission();
				iniPermission.setPath(dbItemSOSPermissionWithName.getAccountPermission());
				iniPermission.setExcluded(dbItemSOSPermissionWithName.getExcluded());
				if (securityConfigurationRole.getPermissions().getControllers().getAdditionalProperties()
						.get(dbItemSOSPermissionWithName.getControllerId()) == null) {
					securityConfigurationRole.getPermissions().getControllers().getAdditionalProperties()
							.put(dbItemSOSPermissionWithName.getControllerId(), new ArrayList<IniPermission>());
				}
				securityConfigurationRole.getPermissions().getControllers().getAdditionalProperties()
						.get(dbItemSOSPermissionWithName.getControllerId()).add(iniPermission);
			}

			if ((dbItemSOSPermissionWithName.getControllerId() == null
					|| dbItemSOSPermissionWithName.getControllerId().isEmpty())
					&& (dbItemSOSPermissionWithName.getFolderPermission() != null
							&& !dbItemSOSPermissionWithName.getFolderPermission().isEmpty())) {
				Folder folder = new Folder();
				folder.setFolder(dbItemSOSPermissionWithName.getFolderPermission());
				folder.setRecursive(dbItemSOSPermissionWithName.getRecursive());
				securityConfigurationRole.getFolders().getJoc().add(folder);
			}

			if ((dbItemSOSPermissionWithName.getControllerId() != null
					&& !dbItemSOSPermissionWithName.getControllerId().isEmpty())
					&& (dbItemSOSPermissionWithName.getFolderPermission() != null
							&& !dbItemSOSPermissionWithName.getFolderPermission().isEmpty())) {
				Folder folder = new Folder();
				folder.setFolder(dbItemSOSPermissionWithName.getFolderPermission());
				folder.setRecursive(dbItemSOSPermissionWithName.getRecursive());
				if (securityConfigurationRole.getFolders().getControllers().getAdditionalProperties()
						.get(dbItemSOSPermissionWithName.getControllerId()) == null) {
					securityConfigurationRole.getFolders().getControllers().getAdditionalProperties()
							.put(dbItemSOSPermissionWithName.getControllerId(), new ArrayList<Folder>());
				}

				securityConfigurationRole.getFolders().getControllers().getAdditionalProperties()
						.get(dbItemSOSPermissionWithName.getControllerId()).add(folder);
			}

			securityConfigurationRoles.setAdditionalProperty(dbItemSOSPermissionWithName.getRoleName(),
					securityConfigurationRole);

		}

		return securityConfigurationRoles;

	}

	@Override
	public SecurityConfiguration readConfiguration(Long identityServiceId, String identityServiceName)
			throws SOSHibernateException {
		SOSHibernateSession sosHibernateSession = null;
		SecurityConfiguration secConfig = new SecurityConfiguration();

		try {
			sosHibernateSession = Globals
					.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration.readConfiguration");

			if (identityServiceId == null) {
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
				iamIdentityServiceFilter.setIdentityServiceName(identityServiceName);
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(iamIdentityServiceFilter);
				identityServiceId = dbItemIamIdentityService.getId();
			}

			secConfig.setAccounts(getAccounts(sosHibernateSession, identityServiceId));
			secConfig.setRoles(getRoles(sosHibernateSession, identityServiceId));

		} finally {
			Globals.disconnect(sosHibernateSession);
		}

		return secConfig;
	}

	public void changePassword(boolean withPasswordCheck, SecurityConfiguration securityConfiguration,
			DBItemIamIdentityService dbItemIamIdentityService) throws Exception {
		SOSHibernateSession sosHibernateSession = null;
		try {
			sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);
			changePasswordAccounts(sosHibernateSession, withPasswordCheck, securityConfiguration,
					dbItemIamIdentityService);
			Globals.commit(sosHibernateSession);
		} catch (JocInfoException e) {
			Globals.rollback(sosHibernateSession);
			LOGGER.info(e.getError().getMessage());
		} catch (Exception e) {
			Globals.rollback(sosHibernateSession);
			throw e;
		} finally {
			Globals.disconnect(sosHibernateSession);
		}

	}

	public void forcePasswordChange(SecurityConfiguration securityConfiguration,
			DBItemIamIdentityService dbItemIamIdentityService) throws Exception {
		SOSHibernateSession sosHibernateSession = null;
		try {
			sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);
			forcePasswordChangeAccounts(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
			Globals.commit(sosHibernateSession);
		} catch (JocInfoException e) {
			Globals.rollback(sosHibernateSession);
			LOGGER.info(e.getError().getMessage());
		} catch (Exception e) {
			Globals.rollback(sosHibernateSession);
			throw e;
		} finally {
			Globals.disconnect(sosHibernateSession);
		}
	}

}
