package com.sos.auth.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.ldap.classes.SOSLdapGroupRolesMapping;
import com.sos.auth.ldap.classes.SOSLdapWebserviceCredentials;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;

public class SOSLdapHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapHandler.class);
    private String msg = "";
    InitialLdapContext ldapContext;
    StartTlsResponse startTls;

    private void startTls(SOSLdapWebserviceCredentials sosInternAuthWebserviceCredentials) throws IOException, NamingException {
        if (sosInternAuthWebserviceCredentials.getUseStartTls()) {
            LOGGER.debug("using StartTls for authentication");
            startTls = (StartTlsResponse) ldapContext.extendedOperation(new StartTlsRequest());
            if (Globals.withHostnameVerification) {
                if (sosInternAuthWebserviceCredentials.isHostnameVerification()) {
                    LOGGER.debug("HostNameVerification=true");
                } else {
                    LOGGER.debug("HostNameVerification=false");
                    startTls.setHostnameVerifier(new DummyVerifier());
                }
            } else {
                LOGGER.debug("HostNameVerification=false");
                startTls.setHostnameVerifier(new DummyVerifier());
            }
            startTls.negotiate();
        }
    }

    private void createDirContext(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials, String password) throws NamingException, IOException {
        if (ldapContext == null) {

            if (Globals.sosCockpitProperties != null) {
               
                if (sosLdapWebserviceCredentials.getTruststorePath() != null && !sosLdapWebserviceCredentials.getTruststorePath().isEmpty()) {
                    Globals.sosCockpitProperties.getProperties().put("ldap_truststore_path", sosLdapWebserviceCredentials.getTruststorePath());
                }
                if (sosLdapWebserviceCredentials.getTruststorePassword() != null && !sosLdapWebserviceCredentials.getTruststorePassword().isEmpty()) {
                    Globals.sosCockpitProperties.getProperties().put("ldap_truststore_password", sosLdapWebserviceCredentials
                            .getTruststorePassword());
                }
                if (sosLdapWebserviceCredentials.getTruststoreType() != null) {
                    Globals.sosCockpitProperties.getProperties().put("ldap_truststore_type", sosLdapWebserviceCredentials.getTruststoreType());
                }
            }

            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, SOSLdapWebserviceCredentials.INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, sosLdapWebserviceCredentials.getLdapServerUrl());
            env.put(Context.SECURITY_AUTHENTICATION, SOSLdapWebserviceCredentials.SECURITY_AUTHENTICATION);
            env.put(Context.SECURITY_PRINCIPAL, sosLdapWebserviceCredentials.getSecurityPrincipal());
            env.put(Context.SECURITY_CREDENTIALS, password);
            startTls(sosLdapWebserviceCredentials);
            if (sosLdapWebserviceCredentials.isSSL()) {
                env.put("java.naming.ldap.factory.socket", "com.sos.auth.ldap.classes.SOSLdapSSLSocketFactory");
            }

            ldapContext = new InitialLdapContext(env, null);
        }
    }

    public SOSAuthAccessToken login(SOSLdapWebserviceCredentials sosInternAuthWebserviceCredentials, String password) throws SOSHibernateException {

        SOSAuthAccessToken sosAuthAccessToken = null;
        try {
            createDirContext(sosInternAuthWebserviceCredentials, password);
            sosAuthAccessToken = new SOSAuthAccessToken();
            sosAuthAccessToken.setAccessToken(UUID.randomUUID().toString());

        } catch (AuthenticationNotSupportedException ex) {
            msg = "AuthenticationNotSupportedException: The authentication is not supported by the server";
        } catch (AuthenticationException ex) {
            msg = "There is no account with the given accountname/password combination";
        } catch (NamingException ex) {
            msg = "NamingException: error when trying to create the ldap context >> " + ex.getCause();
        } catch (IOException ex) {
            msg = "IOException: error when trying to create the ldap context with startTls >> " + ex.getCause();
        }
        return sosAuthAccessToken;
    }

    public String getMsg() {
        return msg;
    }

    public List<String> getGroupRolesMapping(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) throws NamingException {
        SOSLdapGroupRolesMapping sosLdapGroupRolesMapping = new SOSLdapGroupRolesMapping(ldapContext, sosLdapWebserviceCredentials);
        return sosLdapGroupRolesMapping.getGroupRolesMapping(sosLdapWebserviceCredentials);
    }

    public void close() {
        try {
            if (startTls != null) {
                startTls.close();
            }
        } catch (IOException e) {
            LOGGER.error("IOException: error when trying to close the startTlsResponse >> " + e.getCause(), e);
        }
        try {
            if (ldapContext != null) {
                ldapContext.close();

            }
        } catch (NamingException e) {
            LOGGER.error("NamingException: error when trying to close the ldap context >> " + e.getCause(), e);
        }
    }

    /** The hostname verifier always return true */
    final static class DummyVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
