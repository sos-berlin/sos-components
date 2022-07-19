package com.sos.auth.vault.classes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;

public class SOSVaultWebserviceCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultWebserviceCredentials.class);

    private String account = "";
    private String accessToken = "";
    private String decodedAccount = "";
    private String serviceUrl;
    private String authenticationMethodPath;

    private String truststorePath = "";
    private String truststorePassword = "";
    private KeystoreType truststoreType = null;

    private String applicationToken;
    private String vaultAccount;
    private String vaultPassword;

    public String getAccountDecodedAccount() {
        return decodedAccount;
    }

    public void setAccountDecodedAccount(String accountDecodedAccount) {
        this.decodedAccount = accountDecodedAccount;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public SOSVaultWebserviceCredentials() {
        super();
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEncodedAccount() {
        if (decodedAccount == null) {
            decodedAccount = ":";
        }
        byte[] authEncBytes = Base64.encodeBase64(decodedAccount.getBytes());
        return new String(authEncBytes);
    }

    public String getAccount() {
        return account;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public void setTrustStorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public KeystoreType getTrustStoreType() {
        return truststoreType;
    }

    public void setTruststoreType(KeystoreType truststoreType) {
        this.truststoreType = truststoreType;
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
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSVaultWebserviceCredentials");
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

                com.sos.joc.model.security.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.Properties.class);

                if (serviceUrl == null || serviceUrl.isEmpty()) {
                    serviceUrl = getProperty(properties.getVault().getIamVaultUrl(), "https://vault:8200");
                }

                if (authenticationMethodPath == null || authenticationMethodPath.isEmpty()) {
                    authenticationMethodPath = getProperty(properties.getVault().getIamVaultAuthenticationMethodPath(), "");
                }

                String truststorePathGui = getProperty(properties.getVault().getIamVaultTruststorePath(), "");
                String truststorePassGui = getProperty(properties.getVault().getIamVaultTruststorePassword(), "");
                String tTypeGui = getProperty(properties.getVault().getIamVaultTruststoreType(), "");

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

                if (applicationToken == null) {
                    applicationToken = getProperty(properties.getVault().getIamVaultApplicationToken(), "");
                }

            }
        } catch (SOSHibernateException | IOException e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getVaultAccount() {
        return vaultAccount;
    }

    public void setVaultAccount(String vaultAccount) {
        this.vaultAccount = vaultAccount;
    }

    public String getVaultPassword() {
        return vaultPassword;
    }

    public void setVaultPassword(String vaultPassword) {
        this.vaultPassword = vaultPassword;
    }

    @Override
    public String toString() {
        return "SOSVaultWebserviceCredentials [account=" + account + ", accessToken=" + accessToken + ", decodedAccount=" + decodedAccount
                + ", serviceUrl=" + serviceUrl + ", truststorePath=" + truststorePath + ", truststorePassword=" + truststorePassword
                + ", truststoreType=" + truststoreType + ", applicationToken=" + applicationToken + ", vaultAccount=" + vaultAccount
                + ", vaultPassword=" + vaultPassword + "]";
    }

    public String getAuthenticationMethodPath() {
        return authenticationMethodPath;
    }

    public void setAuthenticationMethodPath(String authenticationMethodPath) {
        this.authenticationMethodPath = authenticationMethodPath;
    }

}
