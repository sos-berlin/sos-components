package com.sos.auth.ldap;

import java.io.IOException;
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
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
 
public class SOSLdapHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapHandler.class);
    private String msg = "";
    InitialLdapContext ldapContext;
    StartTlsResponse startTls;

    private void startTls(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) throws IOException, NamingException {
        if (sosLdapWebserviceCredentials.getUseStartTls()) {
            LOGGER.debug("using StartTls for authentication");
            startTls = (StartTlsResponse) ldapContext.extendedOperation(new StartTlsRequest());
            boolean isHostNameVerification = false;
            if (sosLdapWebserviceCredentials.getHostnameVerification() != null && !sosLdapWebserviceCredentials.getHostnameVerification()
                    .isEmpty()) {
                isHostNameVerification = sosLdapWebserviceCredentials.isHostnameVerification();
            }
            if (isHostNameVerification) {
                LOGGER.debug("HostNameVerification=true");
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
            if (sosLdapWebserviceCredentials.isSSL()) {
                env.put("java.naming.ldap.factory.socket", "com.sos.auth.ldap.classes.SOSLdapSSLSocketFactory");
            }

            ldapContext = new InitialLdapContext(env, null);
            startTls(sosLdapWebserviceCredentials);
        }
    }

    public SOSAuthAccessToken login(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials, IdentityServiceTypes identityServiceType, String password) throws SOSHibernateException {
 		if (Globals.withHostnameVerification) {
			System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "false");
			LOGGER.info("hostname verification is enabled");
		}else {
			System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
			LOGGER.info("hostname verification is disabled");
		}

 
        SOSAuthAccessToken sosAuthAccessToken = null;
        try {
            if (identityServiceType == IdentityServiceTypes.LDAP && sosLdapWebserviceCredentials.getSearchBaseNotNull().isEmpty() && "memberOf".equals(sosLdapWebserviceCredentials.getGroupNameAttribute())) {
                msg = "LDAP configuration is not valid: Missing setting 'searchBase'";
            }
            if (sosLdapWebserviceCredentials.getGroupSearchBaseNotNull().isEmpty() && !sosLdapWebserviceCredentials.getGroupSearchFilterNotNull().isEmpty()) {
                msg = "LDAP configuration is not valid: Missing setting 'groupSearchBase'";
            }
            if (sosLdapWebserviceCredentials.getUserDnTemplateNotNull().isEmpty()) {
                msg = "LDAP configuration is not valid: Missing setting 'userDnTemplate'";
            }
            if (sosLdapWebserviceCredentials.getLdapServerUrlNotNull().isEmpty()) {
                msg = ("LDAP configuration is not valid: Missing setting ldapUrl");
            }
            if (password == null || password.isEmpty()) {
            	msg = "Password is empty";
            }
            if (msg.isEmpty()) {
                createDirContext(sosLdapWebserviceCredentials, password);
                sosAuthAccessToken = new SOSAuthAccessToken();
                sosAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
            }

        } catch (AuthenticationNotSupportedException ex) {
            msg = "AuthenticationNotSupportedException: The authentication is not supported by the server";
        } catch (AuthenticationException ex) {
        	   String s = ex.getMessage();
               if (ex.getCause() != null) {
                   s = s + ex.getCause();
               }
            LOGGER.info(s);
            msg = "There is no account with the given accountname/password combination. " + s;
        } catch (NamingException ex) {
            String s = ex.getMessage();
            if (ex.getCause() != null) {
                s = s + ex.getCause();
            }
            msg = "NamingException: error when trying to create the ldap context >> " + s;
            LOGGER.info(msg);
        } catch (IOException ex) {
            String s = ex.getMessage();
            if (ex.getCause() != null) {
                s = s + ex.getCause();
            }
            msg = "IOException: error when trying to create the ldap context >> " + s;
            LOGGER.info(msg);
        }
        return sosAuthAccessToken;
    }

    public String getMsg() {
        return msg;
    }

    public List<String> getGroupRolesMapping(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) throws NamingException {
        SOSLdapGroupRolesMapping sosLdapGroupRolesMapping = new SOSLdapGroupRolesMapping(ldapContext, sosLdapWebserviceCredentials);
        return sosLdapGroupRolesMapping.getGroupRolesMapping();
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
