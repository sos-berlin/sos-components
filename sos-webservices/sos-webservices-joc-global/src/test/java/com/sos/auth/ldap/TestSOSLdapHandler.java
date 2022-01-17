package com.sos.auth.ldap;

import java.security.KeyStore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.auth.classes.SOSAuthAccessToken;

public class TestSOSLdapHandler {

    @BeforeClass
    public static void setUp() throws Exception {
    }

    @AfterClass
    public static void testIsAuthenticated() {
    }

    @Test
    public void testGetUserToken() throws Exception {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        SOSLdapHandler sosLdapHandler = new SOSLdapHandler();

        SOSAuthAccessToken sosVaultUserAccessToken = sosLdapHandler.login(null, "urpass");

    }

}