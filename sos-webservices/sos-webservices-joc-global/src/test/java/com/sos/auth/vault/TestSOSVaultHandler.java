package com.sos.auth.vault;

import java.security.KeyStore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.auth.vault.classes.SOSVaultAccountCredentials;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;

public class TestSOSVaultHandler {

    @BeforeClass
    public static void setUp() throws Exception {
    }

    @AfterClass
    public static void testIsAuthenticated() {
    }

     
    @Ignore
    @Test
    public void testStoreUserPassword() throws Exception {
        KeyStore trustStore = null;

        SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
        SOSIdentityService sosIdentityService = new SOSIdentityService(null, null, null);
        webserviceCredentials.setValuesFromProfile(sosIdentityService);
        trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType());

        SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, trustStore);

        SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
        sosVaultAccountCredentials.setUsername(webserviceCredentials.getVaultAccount());
        String response = sosVaultHandler.storeAccountPassword(sosVaultAccountCredentials,"test");

        System.out.println(response);

    }

    @Ignore
    @Test
    public void testGetUserToken() throws Exception {
        KeyStore trustStore = null;

        SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
        SOSIdentityService sosIdentityService = new SOSIdentityService(null, null, null);
        webserviceCredentials.setValuesFromProfile(sosIdentityService);
        trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType());
        webserviceCredentials.setAccount("ur");
        SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, trustStore);
 

        SOSVaultAccountAccessToken sosVaultUserAccessToken = sosVaultHandler.login(sosIdentityService.getIdentyServiceType(),"urpass");

        System.out.println(sosVaultUserAccessToken.getAuth().getClient_token());
        sosVaultHandler.accountAccessTokenIsValid(sosVaultUserAccessToken);
    }

 

}