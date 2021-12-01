package com.sos.auth.vault;

import java.security.KeyStore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.auth.vault.classes.SOSVaultAccountCredentials;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.auth.vault.pojo.sys.auth.SOSVaultAuthenticationMethods;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;

public class TestSOSVaultHandler {

    @BeforeClass
    public static void setUp() throws Exception {
    }

    @AfterClass
    public static void testIsAuthenticated() {
    }

    @Test
    public void testVaultGetStatus() throws Exception {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
        webserviceCredentials.setValuesFromProfile();
        keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeyStorePath(), webserviceCredentials.getKeyStoreType());
        trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTrustStorePath(), webserviceCredentials.getTrustStoreType());

        SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);

        String response = sosVaultHandler.getVaultStatus();

        System.out.println(response);

    }

    @Test
    public void testStoreUserPassword() throws Exception {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
        webserviceCredentials.setValuesFromProfile();
        keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeyStorePath(), webserviceCredentials.getKeyStoreType());
        trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTrustStorePath(), webserviceCredentials.getTrustStoreType());

        SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);

        SOSVaultAccountCredentials sosVaultUserCredentials = new SOSVaultAccountCredentials();
        sosVaultUserCredentials.setAccount(webserviceCredentials.getVaultAccount());
        sosVaultUserCredentials.setPolicy(webserviceCredentials.getVaultPolicy());
        sosVaultUserCredentials.setPassword(webserviceCredentials.getVaultPassword());
        String response = sosVaultHandler.storeAccountPassword(sosVaultUserCredentials);

        System.out.println(response);

    }

    @Test
    public void testGetUserToken() throws Exception {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
        webserviceCredentials.setValuesFromProfile();
        keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeyStorePath(), webserviceCredentials.getKeyStoreType());
        trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTrustStorePath(), webserviceCredentials.getTrustStoreType());
        webserviceCredentials.setAccount("ur");
        webserviceCredentials.setPassword("urpass");
        SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);
 

        SOSVaultAccountAccessToken sosVaultUserAccessToken = sosVaultHandler.login();

        System.out.println(sosVaultUserAccessToken.getAuth().getClient_token());
        sosVaultHandler.userAccessTokenIsValid(sosVaultUserAccessToken);
    }

    @Test
    public void testVaultGetAuthenticationMethods() throws Exception {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
        webserviceCredentials.setValuesFromProfile();
        keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeyStorePath(), webserviceCredentials.getKeyStoreType());
        trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTrustStorePath(), webserviceCredentials.getTrustStoreType());

        SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);

        SOSVaultAuthenticationMethods response = sosVaultHandler.getVaultAuthenticationMethods();

        System.out.println(response.userpass.uuid);

    }

}