package com.sos.auth.openid.classes;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;

public class SOSOpenIdWebserviceCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdWebserviceCredentials.class);

    private String account = "";
    private String accessToken = "";
    private String serviceUrl;

    private String clientSecret;
    private String clientId;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public SOSOpenIdWebserviceCredentials() {
        super();
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccount() {
        return account;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    private String getProperty(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }

    }

    public void setValuesFromProfile(SOSIdentityService sosIdentityService) {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSOpenIdWebserviceCredentials");
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
            filter.setName(sosIdentityService.getIdentityServiceName());
            filter.setObjectType(sosIdentityService.getIdentyServiceType().value());
            List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(filter, 0);
            DBItemJocConfiguration dbItem = null;
            if (listOfJocConfigurations.size() == 1) {
                dbItem = listOfJocConfigurations.get(0);
            }

            if (dbItem != null) {

                if (Globals.sosCockpitProperties == null) {
                    Globals.sosCockpitProperties = new JocCockpitProperties();
                }

                com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.properties.Properties.class);

                if (serviceUrl == null || serviceUrl.isEmpty()) {
                    serviceUrl = getProperty(properties.getOidc().getIamOidcdUrl(), "https://keycloak:8283");
                }

                if (clientSecret == null) {
                    clientSecret = getProperty(properties.getOidc().getIamOidcClientSecret(), "");
                }

                if (clientId == null) {
                    clientId = getProperty(properties.getOidc().getIamOidcClientId(), "");
                }

            }
        } catch (SOSHibernateException | IOException e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    @Override
    public String toString() {
        return "SOSOpenIdWebserviceCredentials [account=" + account + ", accessToken=" + accessToken + ", serviceUrl=" + serviceUrl
                + ", clientSecret=" + clientSecret + ", clientId=" + clientId + "]";
    }

}
