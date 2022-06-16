package com.sos.auth.keycloak.classes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;

public class SOSKeycloakWebserviceCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeycloakWebserviceCredentials.class);

    private String account = "";
    private String accessToken = "";
    private String serviceUrl;

    private String truststorePath = "";
    private String truststorePassword = "";
    private KeystoreType truststoreType = null;

    private String adminAccount;
    private String adminPassword;
    private String clientSecret;
    private String clientId;
    private String realm;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public SOSKeycloakWebserviceCredentials() {
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

    public String getTruststorePath() {
        return truststorePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public KeystoreType getTrustStoreType() {
        return truststoreType;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRealm() {
        return realm;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
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
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSKeycloakWebserviceCredentials");
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
                    serviceUrl = getProperty(properties.getKeycloak().getIamKeycloakUrl(), "https://keycloak:8283");
                }

                if (realm == null || realm.isEmpty()) {
                    realm = getProperty(properties.getKeycloak().getIamKeycloakRealm(), "master");
                }

                if (clientSecret == null) {
                    clientSecret = getProperty(properties.getKeycloak().getIamKeycloakClientSecret(), "");
                }

                if (clientId == null) {
                    clientId = getProperty(properties.getKeycloak().getIamKeycloakClientId(), "");
                }

                if (adminAccount == null) {
                    adminAccount = getProperty(properties.getKeycloak().getIamKeycloakAdminAccount(), "");
                }

                if (adminPassword == null) {
                    adminPassword = getProperty(properties.getKeycloak().getIamKeycloakAdminPassword(), "");
                }

                String truststorePathGui = getProperty(properties.getKeycloak().getIamKeycloakTruststorePath(), "");
                String truststorePassGui = getProperty(properties.getKeycloak().getIamKeycloakTruststorePassword(), "");
                String tTypeGui = getProperty(properties.getKeycloak().getIamKeycloakTruststoreType(), "");

                String truststorePathDefault = getProperty(System.getProperty("javax.net.ssl.trustStore"), "");
                String truststoreTypeDefault = getProperty(System.getProperty("javax.net.ssl.trustStoreType"), "");
                String truststorePassDefault = getProperty(System.getProperty("javax.net.ssl.trustStorePassword"), "");
                if (!(truststorePathGui + truststorePassGui + tTypeGui).isEmpty()) {

                    if (truststorePath.isEmpty()) {
                        truststorePath = getProperty(truststorePathGui, truststorePathDefault);
                    }

                    if (truststorePassword.isEmpty()) {
                        truststorePassword = getProperty(truststorePassGui, truststorePassDefault);
                    }

                    if (truststoreType == null) {
                        truststoreType = KeystoreType.valueOf(getProperty(tTypeGui, truststoreTypeDefault));
                    }
                } else {

                    if (truststorePath.isEmpty()) {
                        truststorePath = Globals.sosCockpitProperties.getProperty("truststore_path", truststorePathDefault);
                    }

                    if (truststorePassword.isEmpty()) {
                        truststorePassword = Globals.sosCockpitProperties.getProperty("truststore_password", truststorePassDefault);
                    }

                    if (truststoreType == null) {
                        truststoreType = KeystoreType.valueOf(Globals.sosCockpitProperties.getProperty("truststore_type", truststoreTypeDefault));
                    }
                }

                if (truststorePath != null && !truststorePath.trim().isEmpty()) {
                    Path p = Globals.sosCockpitProperties.resolvePath(truststorePath.trim());
                    truststorePath = p.toString();
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
        return "SOSKeycloakWebserviceCredentials [account=" + account + ", accessToken=" + accessToken + ", serviceUrl=" + serviceUrl
                + ", truststorePath=" + truststorePath + ", truststorePassword=" + truststorePassword + ", truststoreType=" + truststoreType
                + ", adminAccount=" + adminAccount + ", adminPassword=" + adminPassword + ", clientSecret=" + clientSecret + ", clientId="
                + clientId + ", realm=" + realm + "]";
    }

}
