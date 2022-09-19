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
    private String refreshToken = "";
    private String idToken = "";
    private String authenticationUrl;

    private String clientSecret;
    private String clientId;
    private String providerName;
    private String tokenVerificationUrl;
    private String logoutUrl;
    private String profileInformationUrl;
    private String sessionRenewalUrl;
    private String certificateUrl;
    private String certificateIssuer;
    private String certificateExpirationDate;
    private Boolean isJwtToken;
    private String jwtEmailField;
    private String jwtClientIdField;
    private String jwtUrlField;
    private String jwtAlgorithmField;
    private String jwtPublicKeyField;
    private String jwtExpirationField;
    private String expiresInField;

    public String getAuthenticationUrl() {
        return authenticationUrl;
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

    
    public String getRefreshToken() {
        return refreshToken;
    }

    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getTokenVerificationUrl() {
        String s = tokenVerificationUrl.replaceAll("<access_token>", accessToken);
        s = s.replaceAll("<id_token>", idToken);
        return s;
    }

    public String getOriginalTokenVerificationUrl() {
        return tokenVerificationUrl;
    }

    public String getLogoutUrl() {
        String s = logoutUrl.replaceAll("<access_token>", accessToken);
        s = s.replaceAll("<id_token>", idToken);
        return s;
    }

    public String getOriginalLogoutUrl() {
        return logoutUrl;
    }

    public String getProfileInformationUrl() {
        return profileInformationUrl;
    }

    public String getSessionRenewalUrl() {
        return sessionRenewalUrl;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public String getCertificateIssuer() {
        return certificateIssuer;
    }

    public String getCertificateExpirationDate() {
        return certificateExpirationDate;
    }

    public Boolean getIsJwtToken() {
        return isJwtToken;
    }

    public String getJwtEmailField() {
        return jwtEmailField;
    }

    public String getJwtClientIdField() {
        return jwtClientIdField;
    }

    public String getJwtUrlField() {
        return jwtUrlField;
    }

    public String getJwtAlgorithmField() {
        return jwtAlgorithmField;
    }

    public String getJwtPublicKeyField() {
        return jwtPublicKeyField;
    }

    public String getJwtExpirationField() {
        return jwtExpirationField;
    }

    public String getExpiresInField() {
        return expiresInField;
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

                if (tokenVerificationUrl == null) {
                    tokenVerificationUrl = getProperty(properties.getOidc().getIamOidcTokenVerificationUrl(), "");
                }

                if (logoutUrl == null) {
                    logoutUrl = getProperty(properties.getOidc().getIamOidcLogoutUrl(), "");
                }

                if (profileInformationUrl == null) {
                    profileInformationUrl = getProperty(properties.getOidc().getIamOidcProfileInformationUrl(), "");
                }

                if (sessionRenewalUrl == null) {
                    sessionRenewalUrl = getProperty(properties.getOidc().getIamOidcSessionRenewalUrl(), "");
                }

                if (certificateUrl == null) {
                    certificateUrl = getProperty(properties.getOidc().getIamOidcCertificateUrl(), "");
                }

                if (certificateIssuer == null) {
                    certificateIssuer = getProperty(properties.getOidc().getIamOidcCertificateIssuer(), "");
                }

                if (certificateExpirationDate == null) {
                    certificateExpirationDate = getProperty(properties.getOidc().getIamOidcCertificateExpirationDate(), "");
                }

                if (isJwtToken == null) {
                    isJwtToken = true;// getProperty(properties.getOidc().g(), "");
                }

                if (jwtEmailField == null) {
                    jwtEmailField = getProperty(properties.getOidc().getIamOidcJwtEmailField(), "email");
                }

                if (jwtClientIdField == null) {
                    jwtClientIdField = getProperty(properties.getOidc().getIamOidcJwtClientIdField(), "aud");
                }

                if (jwtUrlField == null) {
                    jwtUrlField = getProperty(properties.getOidc().getIamOidcJwtUrlField(), "iss");
                }

                if (jwtAlgorithmField == null) {
                    jwtAlgorithmField = getProperty(properties.getOidc().getIamOidcJwtAlgorithmField(), "alg");
                }

                if (jwtPublicKeyField == null) {
                    jwtPublicKeyField = getProperty(properties.getOidc().getIamOidcJwtPublicKeyField(), "");
                }

                if (jwtExpirationField == null) {
                    jwtExpirationField = getProperty(properties.getOidc().getIamOidcJwtExpirationField(), "exp");
                }
                if (expiresInField == null) {
                    expiresInField = getProperty(properties.getOidc().getIamOidcExpiresInField(), "expires_in");
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
        return "SOSOpenIdWebserviceCredentials [account=" + account + ", accessToken=" + accessToken + ", serviceUrl=" + authenticationUrl
                + ", clientSecret=" + clientSecret + ", clientId=" + clientId + "]";
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
