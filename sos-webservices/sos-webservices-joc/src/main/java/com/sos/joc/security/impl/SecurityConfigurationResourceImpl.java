package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.Path;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.interfaces.ISOSSecurityConfiguration;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.permissions.SecurityConfigurationRole;
import com.sos.joc.security.resource.ISecurityConfigurationResource;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

@Path("authentication")
public class SecurityConfigurationResourceImpl extends JOCResourceImpl implements ISecurityConfigurationResource {

    private static final String API_CALL_READ = "./authentication/shiro";
    private static final String API_CALL_WRITE = "./authentication/shiro/store";

    @Override
    public JOCDefaultResponse postAuthRead(String accessToken) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL_READ, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            ISOSSecurityConfiguration sosSecurityConfiguration = null;
            if (SOSAuthHelper.isShiro()) {
                sosSecurityConfiguration = new SOSSecurityConfiguration();
            } else {
                sosSecurityConfiguration = new SOSSecurityDBConfiguration();
            }

            SecurityConfiguration securityConfiguration = sosSecurityConfiguration.readConfiguration();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType("PROFILE");

            securityConfiguration.setProfiles(jocConfigurationDBLayer.getJocConfigurationProfiles(filter));

            securityConfiguration.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(securityConfiguration));
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
            if (SOSAuthHelper.isShiro()) {
                sosSecurityConfiguration = new SOSSecurityConfiguration();
            } else {
                sosSecurityConfiguration = new SOSSecurityDBConfiguration();
            }
            SecurityConfiguration s = sosSecurityConfiguration.writeConfiguration(securityConfiguration);

            s.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(s));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}