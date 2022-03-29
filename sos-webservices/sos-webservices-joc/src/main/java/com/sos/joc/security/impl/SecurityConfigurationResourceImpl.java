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
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.accounts.AccountRename;
import com.sos.joc.model.security.configuration.Roles;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationAccount;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.identityservice.IdentityServiceFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.roles.RoleRename;
import com.sos.joc.security.resource.ISecurityConfigurationResource;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

@Path("authentication")
public class SecurityConfigurationResourceImpl extends JOCResourceImpl implements ISecurityConfigurationResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfigurationResourceImpl.class);
	private static final String API_CALL_READ = "./authentication/auth";
	private static final String API_CALL_WRITE = "./authentication/auth/store";
	private static final String API_CALL_ACCOUNT_RENAME = "./authentication/auth/account/rename";;

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
				sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
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
	            storeAuditLog(securityConfiguration.getAuditLog(), CategoryType.IDENTITY); 

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


}