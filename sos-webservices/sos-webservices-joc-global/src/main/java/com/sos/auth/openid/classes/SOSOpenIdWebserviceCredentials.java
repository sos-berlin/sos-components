package com.sos.auth.openid.classes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.sos.joc.model.security.properties.oidc.OidcGroupRolesMappingItem;

public class SOSOpenIdWebserviceCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdWebserviceCredentials.class);

    private String account = "";
    private String idToken = "";
    private String authenticationUrl;

    private String clientSecret;
    private String clientId;
    private String providerName;
    private String userAttribute;
    private String openidConfiguration;

    private String truststorePath = "";
    private String truststorePassword = "";
    private KeystoreType truststoreType = null;
    private Map<String, List<String>> groupRolesMap;
    private List<String> claims;

    public String getAuthenticationUrl() {
        return authenticationUrl;
    }

    public String getUserAttribute() {
        return userAttribute;
    }

    public SOSOpenIdWebserviceCredentials() {
        super();
    }

    public void setAccount(String account) {
        this.account = account;
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

    public String getTruststorePath() {
        return truststorePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public KeystoreType getTrustStoreType() {
        return truststoreType;
    }

    public String getProviderName() {
        return providerName;
    }

    public Map<String, List<String>> getGroupRolesMap() {
        return groupRolesMap;
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

                if (authenticationUrl == null || authenticationUrl.isEmpty()) {
                    authenticationUrl = getProperty(properties.getOidc().getIamOidcAuthenticationUrl(), "");
                }

                if (clientSecret == null) {
                    clientSecret = getProperty(properties.getOidc().getIamOidcClientSecret(), "");
                }

                if (clientId == null) {
                    clientId = getProperty(properties.getOidc().getIamOidcClientId(), "");
                }

                if (providerName == null) {
                    providerName = getProperty(properties.getOidc().getIamOidcName(), "");
                }

                if (userAttribute == null || userAttribute.isEmpty()) {
                    userAttribute = getProperty(properties.getOidc().getIamOidcUserAttribute(), "");
                }

                if (groupRolesMap == null) {
                    if (properties.getOidc().getIamOidcGroupRolesMap() != null && properties.getOidc().getIamOidcGroupRolesMap().getItems() != null) {
                        groupRolesMap = new HashMap<String, List<String>>();
                        for (OidcGroupRolesMappingItem entry : properties.getOidc().getIamOidcGroupRolesMap().getItems()) {
                            groupRolesMap.put(entry.getOidcGroup(), entry.getRoles());
                        }
                    }
                }
                if (claims == null) {
                    if (properties.getOidc().getIamOidcGroupClaims() != null) {
                        claims = new ArrayList<String>();
                        for (String claim : properties.getOidc().getIamOidcGroupClaims()) {
                            claims.add(claim);
                        }
                    }
                }

                String truststorePathGui = getProperty(properties.getOidc().getIamOidcTruststorePath(), "");
                String truststorePassGui = getProperty(properties.getOidc().getIamOidcTruststorePassword(), "");
                String tTypeGui = getProperty(properties.getOidc().getIamOidcTruststoreType(), "");

                String truststorePathDefault = getProperty(System.getProperty("javax.net.ssl.trustStore"), Globals.sosCockpitProperties.getProperty(
                        "truststore_path", ""));
                String truststoreTypeDefault = getProperty(System.getProperty("javax.net.ssl.trustStoreType"), Globals.sosCockpitProperties
                        .getProperty("truststore_type", "PKCS12"));
                String truststorePassDefault = getProperty(System.getProperty("javax.net.ssl.trustStorePassword"), Globals.sosCockpitProperties
                        .getProperty("truststore_password", ""));

                truststorePath = getProperty(truststorePathGui, truststorePathDefault);
                truststorePassword = getProperty(truststorePassGui, truststorePassDefault);
                truststoreType = KeystoreType.valueOf(getProperty(tTypeGui, truststoreTypeDefault));

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
        return "SOSOpenIdWebserviceCredentials [account=" + account + ",  serviceUrl=" + authenticationUrl + ", clientSecret=" + clientSecret
                + ", clientId=" + clientId + "]";
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getOpenidConfiguration() {
        return openidConfiguration;
    }

    public void setOpenidConfiguration(String openidConfiguration) {
        this.openidConfiguration = openidConfiguration;
    }

    public List<String> getClaims() {
        return claims;
    }
}
