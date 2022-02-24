package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSInitialPasswordSetting;
import com.sos.auth.interfaces.ISOSSecurityConfiguration;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.AccountRename;
import com.sos.joc.model.security.IdentityServiceFilter;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.RoleRename;
import com.sos.joc.model.security.Roles;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.SecurityConfigurationAccount;
import com.sos.joc.model.security.permissions.SecurityConfigurationRole;
import com.sos.joc.security.resource.ISecurityConfigurationResource;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

@Path("authentication")
public class SecurityConfigurationResourceImpl extends JOCResourceImpl implements ISecurityConfigurationResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfigurationResourceImpl.class);
	private static final String API_CALL_READ = "./authentication/auth";
	private static final String API_CALL_WRITE = "./authentication/auth/store";
	private static final String API_CALL_ACCOUNT_RENAME = "./authentication/auth/account/rename";;
	private static final String API_CALL_ACCOUNT_DELETE = "./authentication/auth/account/delete";;
	private static final String API_CALL_ROLE_RENAME = "./authentication/auth/role/rename";;
	private static final String API_CALL_ROLE_DELETE = "./authentication/auth/role/delete";;
	private static final String API_CALL_CHANGE_PASSWORD = "./authentication/auth/changepassword";;
	private static final String API_CALL_RESET_PASSWORD = "./authentication/auth/resetpassword";;
	private static final String API_CALL_FORCE_PASSWORD_CHANGE = "./authentication/auth/forcepasswordchange";;

	@Override
	public JOCDefaultResponse postAuthRead(String accessToken, byte[] body) {
		SOSHibernateSession sosHibernateSession = null;
		try {
			initLogging(API_CALL_READ, null, accessToken);
			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getView());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			IdentityServiceFilter identityServiceFilter = null;
			if (body.length > 0) {
				JsonValidator.validate(body, IdentityServiceFilter.class);
				identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);
			} else {
				identityServiceFilter = new IdentityServiceFilter();
			}
			SecurityConfiguration securityConfiguration = null;
			try {
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
				iamIdentityServiceFilter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(iamIdentityServiceFilter);
				if (dbItemIamIdentityService == null) {
					throw new JocObjectNotExistException("Object Identity Service <"
							+ identityServiceFilter.getIdentityServiceName() + "> not found");
				}

				ISOSSecurityConfiguration sosSecurityConfiguration = null;
				if (dbItemIamIdentityService == null || IdentityServiceTypes.SHIRO.name()
						.equals(dbItemIamIdentityService.getIdentityServiceType())) {
					sosSecurityConfiguration = new SOSSecurityConfiguration();
				} else {
					sosSecurityConfiguration = new SOSSecurityDBConfiguration();
				}

				securityConfiguration = sosSecurityConfiguration.readConfiguration(dbItemIamIdentityService.getId(),
						dbItemIamIdentityService.getIdentityServiceName());

				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
				JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
				JocConfigurationFilter filter = new JocConfigurationFilter();
				filter.setConfigurationType("PROFILE");

				securityConfiguration.setProfiles(jocConfigurationDBLayer.getJocConfigurationProfiles(filter));

				securityConfiguration.setDeliveryDate(Date.from(Instant.now()));

				return JOCDefaultResponse
						.responseStatus200(Globals.objectMapper.writeValueAsBytes(securityConfiguration));
			} finally {
				identityServiceFilter = null;
				securityConfiguration = null;
				Globals.disconnect(sosHibernateSession);
			}

		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		} finally {
			Globals.disconnect(sosHibernateSession);
		}
	}

	@Override
	public JOCDefaultResponse postAuthStore(String accessToken, byte[] body) {
		try {
			SecurityConfiguration securityConfigurationMasked = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			for (SecurityConfigurationAccount securityConfigurationAccount : securityConfigurationMasked
					.getAccounts()) {
				securityConfigurationAccount.setHashedPassword("********");
				securityConfigurationAccount.setOldPassword("********");
				securityConfigurationAccount.setPassword("********");
				securityConfigurationAccount.setRepeatedPassword("********");
			}

			initLogging(API_CALL_WRITE, Globals.objectMapper.writeValueAsBytes(securityConfigurationMasked),
					accessToken);
			JsonValidator.validate(body, SecurityConfiguration.class);
			SecurityConfiguration securityConfiguration = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			String identityServiceName = securityConfiguration.getIdentityServiceName();
			if (securityConfiguration.getRoles() != null) {
				for (Map.Entry<String, SecurityConfigurationRole> entry : securityConfiguration.getRoles()
						.getAdditionalProperties().entrySet()) {
					try {
						JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(entry.getValue()),
								SecurityConfigurationRole.class);
					} catch (SOSJsonSchemaException e) {
						throw new SOSJsonSchemaException(
								e.getMessage().replaceFirst("(\\[\\$\\.)", "$1roles[" + entry.getKey() + "]."));
					}
				}
			}
			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			ISOSSecurityConfiguration sosSecurityConfiguration = null;

			SOSHibernateSession sosHibernateSession = null;
			try {
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
				filter.setIdentityServiceName(identityServiceName);
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(filter);

				if (dbItemIamIdentityService == null) {
					throw new JocObjectNotExistException(
							"Object Identity Service <" + identityServiceName + "> not found");
				}

				sosSecurityConfiguration = new SOSSecurityDBConfiguration();
				sosSecurityConfiguration.writeConfiguration(securityConfiguration, dbItemIamIdentityService);

				if (IdentityServiceTypes.SHIRO.name().equals(dbItemIamIdentityService.getIdentityServiceType())) {
					sosSecurityConfiguration = new SOSSecurityConfiguration();
					sosSecurityConfiguration.writeConfiguration(securityConfiguration, dbItemIamIdentityService);
				}
	            DBItemJocAuditLog dbAuditLog =  storeAuditLog(securityConfiguration.getAuditLog(), CategoryType.IDENTITY); 

				return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

			} finally {
				securityConfiguration = null;
				Globals.disconnect(sosHibernateSession);
			}
		} catch (

		JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

	@Override
	public JOCDefaultResponse postAuthAcountsDelete(String accessToken, byte[] body) {
		try {
			SecurityConfiguration securityConfigurationMasked = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			for (SecurityConfigurationAccount securityConfigurationAccount : securityConfigurationMasked
					.getAccounts()) {
				securityConfigurationAccount.setHashedPassword("********");
				securityConfigurationAccount.setOldPassword("********");
				securityConfigurationAccount.setPassword("********");
				securityConfigurationAccount.setRepeatedPassword("********");
			}

			initLogging(API_CALL_ACCOUNT_DELETE, Globals.objectMapper.writeValueAsBytes(securityConfigurationMasked),
					accessToken);
			JsonValidator.validate(body, SecurityConfiguration.class);
			SecurityConfiguration securityConfiguration = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			String identityServiceName = securityConfiguration.getIdentityServiceName();
			if (securityConfiguration.getRoles() != null) {
				for (Map.Entry<String, SecurityConfigurationRole> entry : securityConfiguration.getRoles()
						.getAdditionalProperties().entrySet()) {
					try {
						JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(entry.getValue()),
								SecurityConfigurationRole.class);
					} catch (SOSJsonSchemaException e) {
						throw new SOSJsonSchemaException(
								e.getMessage().replaceFirst("(\\[\\$\\.)", "$1roles[" + entry.getKey() + "]."));
					}
				}
			}
			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			SOSSecurityDBConfiguration sosSecurityConfiguration = null;

			SOSHibernateSession sosHibernateSession = null;
			try {
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
				filter.setIdentityServiceName(identityServiceName);
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(filter);

				if (dbItemIamIdentityService == null) {
					throw new JocObjectNotExistException(
							"Object Identity Service <" + identityServiceName + "> not found");
				}

				sosSecurityConfiguration = new SOSSecurityDBConfiguration();
				sosSecurityConfiguration.deleteAccounts(securityConfiguration, dbItemIamIdentityService);
				
	            DBItemJocAuditLog dbAuditLog =  storeAuditLog(securityConfiguration.getAuditLog(), CategoryType.IDENTITY); 

				return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

			} finally {
				securityConfiguration = null;
				Globals.disconnect(sosHibernateSession);
			}
		} catch (

		JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

	@Override
	public JOCDefaultResponse postAuthRolesDelete(String accessToken, byte[] body) {
		try {
			initLogging(API_CALL_ROLE_DELETE, body, accessToken);
			JsonValidator.validate(body, Roles.class);
			Roles roles = Globals.objectMapper.readValue(body, Roles.class);
			String identityServiceName = roles.getIdentityServiceName();

			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			SOSSecurityDBConfiguration sosSecurityConfiguration = null;

			SOSHibernateSession sosHibernateSession = null;
			try {
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
				filter.setIdentityServiceName(identityServiceName);
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(filter);

				if (dbItemIamIdentityService == null) {
					throw new JocObjectNotExistException(
							"Object Identity Service <" + identityServiceName + "> not found");
				}

				sosSecurityConfiguration = new SOSSecurityDBConfiguration();
				sosSecurityConfiguration.deleteRoles(roles, dbItemIamIdentityService);
	            DBItemJocAuditLog dbAuditLog =  storeAuditLog(roles.getAuditLog(), CategoryType.IDENTITY); 


				return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

			} finally {
				roles = null;
				Globals.disconnect(sosHibernateSession);
			}
		} catch (

		JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

	@Override
	public JOCDefaultResponse changePassword(String accessToken, byte[] body) {
		try {
			SecurityConfiguration securityConfigurationMasked = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			for (SecurityConfigurationAccount securityConfigurationAccount : securityConfigurationMasked
					.getAccounts()) {
				securityConfigurationAccount.setHashedPassword("********");
				securityConfigurationAccount.setOldPassword("********");
				securityConfigurationAccount.setPassword("********");
				securityConfigurationAccount.setRepeatedPassword("********");
			}

			initLogging(API_CALL_CHANGE_PASSWORD, Globals.objectMapper.writeValueAsBytes(securityConfigurationMasked),
					accessToken);

			JsonValidator.validate(body, SecurityConfiguration.class);
			SecurityConfiguration securityConfiguration = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			String identityServiceName = securityConfiguration.getIdentityServiceName();

			JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			SOSSecurityDBConfiguration sosSecurityConfiguration = null;

			SOSHibernateSession sosHibernateSession = null;
			try {
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
				filter.setIdentityServiceName(identityServiceName);
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(filter);

				if (dbItemIamIdentityService == null) {
					throw new JocObjectNotExistException(
							"Object Identity Service <" + identityServiceName + "> not found");
				}

				sosSecurityConfiguration = new SOSSecurityDBConfiguration();
				sosSecurityConfiguration.changePassword(true, securityConfiguration, dbItemIamIdentityService);
	            DBItemJocAuditLog dbAuditLog =  storeAuditLog(securityConfiguration.getAuditLog(), CategoryType.IDENTITY); 

				return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

			} finally {
				securityConfiguration = null;
				Globals.disconnect(sosHibernateSession);
			}
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

	@Override
	public JOCDefaultResponse forcePasswordChange(String accessToken, byte[] body) {
		try {
			SecurityConfiguration securityConfigurationMasked = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			for (SecurityConfigurationAccount securityConfigurationAccount : securityConfigurationMasked
					.getAccounts()) {
				securityConfigurationAccount.setHashedPassword("********");
				securityConfigurationAccount.setOldPassword("********");
				securityConfigurationAccount.setPassword("********");
				securityConfigurationAccount.setRepeatedPassword("********");
			}

			initLogging(API_CALL_FORCE_PASSWORD_CHANGE,
					Globals.objectMapper.writeValueAsBytes(securityConfigurationMasked), accessToken);
			JsonValidator.validate(body, SecurityConfiguration.class);
			SecurityConfiguration securityConfiguration = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			String identityServiceName = securityConfiguration.getIdentityServiceName();

			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			SOSSecurityDBConfiguration sosSecurityConfiguration = null;

			SOSHibernateSession sosHibernateSession = null;
			try {
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
				filter.setIdentityServiceName(identityServiceName);
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(filter);

				if (dbItemIamIdentityService == null) {
					throw new JocObjectNotExistException(
							"Object Identity Service <" + identityServiceName + "> not found");
				}

				sosSecurityConfiguration = new SOSSecurityDBConfiguration();
				sosSecurityConfiguration.forcePasswordChange(securityConfiguration, dbItemIamIdentityService);
	            DBItemJocAuditLog dbAuditLog =  storeAuditLog(securityConfiguration.getAuditLog(), CategoryType.IDENTITY); 

				return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

			} finally {
				securityConfiguration = null;
				Globals.disconnect(sosHibernateSession);
			}
		} catch (

		JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

	@Override
	public JOCDefaultResponse resetPassword(String accessToken, byte[] body) {
		try {
			SecurityConfiguration securityConfigurationMasked = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			for (SecurityConfigurationAccount securityConfigurationAccount : securityConfigurationMasked
					.getAccounts()) {
				securityConfigurationAccount.setHashedPassword("********");
				securityConfigurationAccount.setOldPassword("********");
				securityConfigurationAccount.setPassword("********");
				securityConfigurationAccount.setRepeatedPassword("********");
			}

			initLogging(API_CALL_RESET_PASSWORD, Globals.objectMapper.writeValueAsBytes(securityConfigurationMasked),
					accessToken);
			JsonValidator.validate(body, SecurityConfiguration.class);
			SecurityConfiguration securityConfiguration = Globals.objectMapper.readValue(body,
					SecurityConfiguration.class);
			String identityServiceName = securityConfiguration.getIdentityServiceName();

			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			SOSSecurityDBConfiguration sosSecurityConfiguration = null;

			SOSHibernateSession sosHibernateSession = null;
			try {
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
				IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(
						sosHibernateSession);
				IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
				filter.setIdentityServiceName(identityServiceName);
				DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
						.getUniqueIdentityService(filter);

				if (dbItemIamIdentityService == null) {
					throw new JocObjectNotExistException(
							"Object Identity Service <" + identityServiceName + "> not found");
				}

				SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper
						.getInitialPasswordSettings(sosHibernateSession);

				String initialPassword = sosInitialPasswordSetting.getInitialPassword();
				if (!sosInitialPasswordSetting.isMininumPasswordLength(initialPassword)) {
					JocError error = new JocError();
					error.setMessage("Password is too short");
					throw new JocException(error);
				}

				for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
					securityConfigurationAccount.setPassword(initialPassword);
					securityConfigurationAccount.setRepeatedPassword(initialPassword);
				}

				sosSecurityConfiguration = new SOSSecurityDBConfiguration();
				sosSecurityConfiguration.changePassword(false, securityConfiguration, dbItemIamIdentityService);
	            DBItemJocAuditLog dbAuditLog =  storeAuditLog(securityConfiguration.getAuditLog(), CategoryType.IDENTITY); 

				return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

			} finally {
				securityConfiguration = null;
				Globals.disconnect(sosHibernateSession);
			}
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

	@Override
	public JOCDefaultResponse postAuthAcountRename(String accessToken, byte[] body) {

		SOSHibernateSession sosHibernateSession = null;

		try {
			initLogging(API_CALL_ACCOUNT_RENAME, body, accessToken);
			JsonValidator.validate(body, AccountRename.class);
			AccountRename accountRename = Globals.objectMapper.readValue(body, AccountRename.class);
			String identityServiceName = accountRename.getIdentityServiceName();

			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_RENAME);
			sosHibernateSession.setAutoCommit(false);
			sosHibernateSession.beginTransaction();

			IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
			IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
			filter.setIdentityServiceName(identityServiceName);
			DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
					.getUniqueIdentityService(filter);

			if (dbItemIamIdentityService == null) {
				throw new JocObjectNotExistException("Object Identity Service <" + identityServiceName + "> not found");
			}

			IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

			IamAccountFilter iamAccountFilter = new IamAccountFilter();
			iamAccountFilter.setAccountName(accountRename.getAccountNewName());
			iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
			DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
			if (dbItemIamAccount != null) {
				JocError error = new JocError();
				error.setMessage("Account " + accountRename.getAccountNewName() + " already exists");
				throw new JocException(error);
			}

			int count = iamAccountDBLayer.renameAccount(dbItemIamIdentityService.getId(),
					accountRename.getAccountOldName(), accountRename.getAccountNewName());
			if (count == 0) {
				throw new JocObjectNotExistException(
						"Object account <" + accountRename.getAccountOldName() + "> not found");
			}
			
            DBItemJocAuditLog dbAuditLog =  storeAuditLog(accountRename.getAuditLog(), CategoryType.IDENTITY); 


			Globals.commit(sosHibernateSession);

			return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

		} catch (

		JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		} finally {
			Globals.disconnect(sosHibernateSession);
		}

	}

	@Override
	public JOCDefaultResponse postAuthRoleRename(String accessToken, byte[] body) {

		SOSHibernateSession sosHibernateSession = null;

		try {
			initLogging(API_CALL_ROLE_RENAME, body, accessToken);
			JsonValidator.validate(body, RoleRename.class);
			RoleRename roleRename = Globals.objectMapper.readValue(body, RoleRename.class);
			String identityServiceName = roleRename.getIdentityServiceName();

			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ACCOUNT_RENAME);
			sosHibernateSession.setAutoCommit(false);
			sosHibernateSession.beginTransaction();

			IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
			IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
			filter.setIdentityServiceName(identityServiceName);
			DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer
					.getUniqueIdentityService(filter);

			if (dbItemIamIdentityService == null) {
				throw new JocObjectNotExistException("Object Identity Service <" + identityServiceName + "> not found");
			}

			IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

			DBItemIamRole dbItemIamRole = iamAccountDBLayer.getRoleByName(roleRename.getRoleNewName(),
					dbItemIamIdentityService.getId());
			if (dbItemIamRole != null) {
				JocError error = new JocError();
				error.setMessage("Role " + roleRename.getRoleNewName() + " already exists");
				throw new JocException(error);
			}

			int count = iamAccountDBLayer.renameRole(dbItemIamIdentityService.getId(), roleRename.getRoleOldName(),
					roleRename.getRoleNewName());
			if (count == 0) {
				throw new JocObjectNotExistException("Object role <" + roleRename.getRoleOldName() + "> not found");
			}

            DBItemJocAuditLog dbAuditLog =  storeAuditLog(roleRename.getAuditLog(), CategoryType.IDENTITY); 

			Globals.commit(sosHibernateSession);

			return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

		} catch (

		JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		} finally {
			Globals.disconnect(sosHibernateSession);
		}

	}
}