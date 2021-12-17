package com.sos.auth.vault.classes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;

public class SOSVaultWebserviceCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultWebserviceCredentials.class);

    private String account = "";
    private String accessToken = "";
    private String decodedAccount = "";
    private String serviceUrl;

    private String keystorePath = "";
    private String keystorePassword = "";
    private String keyPassword = "";
    private KeystoreType keystoreType = null;

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

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public KeystoreType getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(KeystoreType keystoreType) {
        this.keystoreType = keystoreType;
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

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
    
    private String getProperty(String value,String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }else {
            return value;
        }
        
    }
    
    public void setValuesFromProfile() throws SOSMissingDataException, UnsupportedEncodingException {
    }

    public void setValuesFromProfile(SOSIdentityService sosIdentityService) throws SOSMissingDataException, UnsupportedEncodingException {

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

                com.sos.joc.model.security.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.Properties.class);

                if (serviceUrl == null || serviceUrl.isEmpty()) {
                    serviceUrl = getProperty(properties.getVault().getIamVaultUrl(),"https://vault:8200"); 
                }

                if (keystorePath.isEmpty()) {
                    keystorePath = getProperty(properties.getVault().getIamVaultKeystorePath(),""); 
                }

                if (keystorePassword.isEmpty()) {
                    keystorePassword = getProperty(properties.getVault().getIamVaultKeystorePassword(),""); 
                }

                if (keyPassword.isEmpty()) {
                    keyPassword = getProperty(properties.getVault().getIamVaultKeyPassword(),""); 
                }

                if (keystoreType == null) {
                    keystoreType = KeystoreType.fromValue(getProperty(properties.getVault().getIamVaultKeystoreType(),"PKCS12")); 
                }

                if (truststorePath.isEmpty()) {
                    truststorePath = getProperty(properties.getVault().getIamVaultTruststorePath(),""); 
                }

                if (truststorePassword.isEmpty()) {
                    truststorePassword = getProperty(properties.getVault().getIamVaultTruststorePassword(),""); 
                }

                if (truststoreType == null) {
                    truststoreType = KeystoreType.fromValue(getProperty(properties.getVault().getIamVaultTruststoreType(),"PKCS12")); 

                }
                if (applicationToken == null) {
                    applicationToken = getProperty(properties.getVault().getIamVaultApplicationToken(),""); 
                }
 
            }
        } catch (SOSHibernateException | IOException e) {
            LOGGER.error(e.getMessage(), e);
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
                + ", serviceUrl=" + serviceUrl + ", keystorePath=" + keystorePath + ", keystorePassword=" + keystorePassword + ", keyPassword="
                + keyPassword + ", keystoreType=" + keystoreType + ", truststorePath=" + truststorePath + ", truststorePassword=" + truststorePassword
                + ", truststoreType=" + truststoreType + ", applicationToken=" + applicationToken + ", vaultAccount=" + vaultAccount
                + ", vaultPassword=" + vaultPassword + "]";
    }

}
