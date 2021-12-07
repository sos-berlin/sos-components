package com.sos.auth.vault.classes;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.util.SOSPrivateConf;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.typesafe.config.ConfigException;

public class SOSVaultWebserviceCredentials {

    private String account = "";
    private String password = "";
    private String accessToken = "";
    private String decodedAccount = "";
    private String serviceUrl;

    private String keyStorePath = "";
    private String keyStorePassword = "";
    private String keyPassword = "";
    private KeyStoreType keyStoreType = KeyStoreType.PKCS12;

    private String trustStorePath = "";
    private String trustStorePassword = "";
    private KeyStoreType trustStoreType = KeyStoreType.PKCS12;

    private String applicationToken;
    private String vaultAccount;
    private String vaultPassword;
    private String vaultPolicy;

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

    public void setPassword(String password) {
        this.password = password;
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

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(KeyStoreType keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public KeyStoreType getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(KeyStoreType trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getPassword() {
        return password;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setValuesFromProfile() throws SOSMissingDataException, UnsupportedEncodingException {

        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }

        if (serviceUrl == null || serviceUrl.isEmpty()) {
            serviceUrl = Globals.sosCockpitProperties.getProperty("iam_url", "https://vault:8200");

        }

        if (keyStorePath.isEmpty()) {
            keyStorePath = Globals.sosCockpitProperties.getProperty("iam_keystorepath");
        }

        if (keyStorePassword.isEmpty()) {
            keyStorePassword = Globals.sosCockpitProperties.getProperty("iam_keystorepassword");
        }

        if (keyPassword.isEmpty()) {
            keyPassword = Globals.sosCockpitProperties.getProperty("iam_keypassword");
        }

        if (keyStoreType == null) {
            keyStoreType = KeyStoreType.fromValue(Globals.sosCockpitProperties.getProperty("iam_keystoretype", "PKCS12"));
        }

        if (trustStorePath.isEmpty()) {
            trustStorePath = Globals.sosCockpitProperties.getProperty("iam_truststorepath");
        }

        if (trustStorePassword.isEmpty()) {
            trustStorePassword = Globals.sosCockpitProperties.getProperty("iam_truststorepassword");
        }

        if (trustStoreType == null) {
            trustStoreType = KeyStoreType.fromValue(Globals.sosCockpitProperties.getProperty("iam_truststoretype"));

        }
        if (applicationToken == null) {
            applicationToken = Globals.sosCockpitProperties.getProperty("iam_applicationtoken");
        }

        if (vaultPolicy == null) {
            vaultPolicy = Globals.sosCockpitProperties.getProperty("iam_vaultPolicy");
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

    public String getVaultPolicy() {
        return vaultPolicy;
    }

    public void setVaultPolicy(String vaultPolicy) {
        this.vaultPolicy = vaultPolicy;
    }


}
