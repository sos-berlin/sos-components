package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.Path;

import com.sos.auth.interfaces.ISOSSecurityConfiguration;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.SecurityConfigurationAccount;
import com.sos.joc.model.security.permissions.SecurityConfigurationRole;
import com.sos.joc.security.resource.ISecurityConfigurationResource;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

@Path("authentication")
public class SecurityConfigurationResourceImpl extends JOCResourceImpl implements ISecurityConfigurationResource {

    private static final String API_CALL_READ = "./authentication/auth";
    private static final String API_CALL_WRITE = "./authentication/auth/store";

    @Override
    public JOCDefaultResponse postAuthRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL_READ, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            SecurityConfiguration securityConfiguration = null;
            if (body.length > 0) {
                JsonValidator.validate(body, SecurityConfiguration.class);
                securityConfiguration = Globals.objectMapper.readValue(body, SecurityConfiguration.class);
            } else {
                securityConfiguration = new SecurityConfiguration();
            }

            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
                IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
                IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
                iamIdentityServiceFilter.setIdentityServiceName(securityConfiguration.getIdentityServiceName());
                DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
                if (dbItemIamIdentityService == null) {
                    throw new SOSMissingDataException("No identity service found for: " + securityConfiguration.getIdentityServiceName());
                }

                ISOSSecurityConfiguration sosSecurityConfiguration = null;
                if (dbItemIamIdentityService == null || IdentityServiceTypes.SHIRO.name().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                    sosSecurityConfiguration = new SOSSecurityConfiguration();
                } else {
                    sosSecurityConfiguration = new SOSSecurityDBConfiguration();
                }

                securityConfiguration = sosSecurityConfiguration.readConfiguration(dbItemIamIdentityService.getId(), dbItemIamIdentityService
                        .getIdentityServiceName());

                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
                JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
                JocConfigurationFilter filter = new JocConfigurationFilter();
                filter.setConfigurationType("PROFILE");

                securityConfiguration.setProfiles(jocConfigurationDBLayer.getJocConfigurationProfiles(filter));

                securityConfiguration.setDeliveryDate(Date.from(Instant.now()));

                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(securityConfiguration));
            } finally {
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
            initLogging(API_CALL_WRITE, body, accessToken);
            JsonValidator.validate(body, SecurityConfiguration.class);
            SecurityConfiguration securityConfiguration = Globals.objectMapper.readValue(body, SecurityConfiguration.class);
            String identityServiceName = securityConfiguration.getIdentityServiceName();
            if (securityConfiguration.getRoles() != null) {
                for (Map.Entry<String, SecurityConfigurationRole> entry : securityConfiguration.getRoles().getAdditionalProperties().entrySet()) {
                    try {
                        JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(entry.getValue()), SecurityConfigurationRole.class);
                    } catch (SOSJsonSchemaException e) {
                        throw new SOSJsonSchemaException(e.getMessage().replaceFirst("(\\[\\$\\.)", "$1roles[" + entry.getKey() + "]."));
                    }
                }
            }
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            ISOSSecurityConfiguration sosSecurityConfiguration = null;

            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_WRITE);
                IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
                IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
                filter.setIdentityServiceName(identityServiceName);
                DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(filter);

                if (dbItemIamIdentityService == null) {
                    throw new SOSMissingDataException("No identity service found for: " + identityServiceName);
                }

                sosSecurityConfiguration = new SOSSecurityDBConfiguration();
                SecurityConfiguration s = sosSecurityConfiguration.writeConfiguration(securityConfiguration, dbItemIamIdentityService);

                if (IdentityServiceTypes.SHIRO.name().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                    sosSecurityConfiguration = new SOSSecurityConfiguration();
                    s = sosSecurityConfiguration.writeConfiguration(securityConfiguration, dbItemIamIdentityService);
                }else {
                    
                }
                s.setDeliveryDate(Date.from(Instant.now()));
                for (SecurityConfigurationAccount securityConfigurationAccount : s.getAccounts()) {
                    securityConfigurationAccount.setPassword("********");
                }

                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(s));

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