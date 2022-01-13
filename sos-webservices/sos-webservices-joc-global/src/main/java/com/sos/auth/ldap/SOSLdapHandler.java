package com.sos.auth.ldap;

import java.util.Hashtable;
import java.util.UUID;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.ldap.classes.SOSLdapWebserviceCredentials;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class SOSLdapHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapHandler.class);
    private String msg = "";

    public SOSLdapHandler() {
    }

    public SOSAuthAccessToken login(SOSLdapWebserviceCredentials sosInternAuthWebserviceCredentials, String password)
            throws SOSHibernateException {

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, SOSLdapWebserviceCredentials.INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, sosInternAuthWebserviceCredentials.getLdapServerUrl());
        env.put(Context.SECURITY_AUTHENTICATION, SOSLdapWebserviceCredentials.SECURITY_AUTHENTICATION);
        env.put(Context.SECURITY_PRINCIPAL, sosInternAuthWebserviceCredentials.getSecurityPrincipal());
        env.put(Context.SECURITY_CREDENTIALS, password);

        SOSAuthAccessToken sosAuthAccessToken = null;
        try {
            DirContext ctx = new InitialDirContext(env);
            ctx.close();
            sosAuthAccessToken = new SOSAuthAccessToken();
            sosAuthAccessToken.setAccessToken(UUID.randomUUID().toString());

        } catch (AuthenticationNotSupportedException ex) {
            msg = "AuthenticationNotSupportedException: The authentication is not supported by the server";
        } catch (AuthenticationException ex) {
            msg = "There is no account with the given accountname/password combination";
        } catch (NamingException ex) {
            msg = "NamingException: error when trying to create the ldap context >> " + ex.getCause();
        }
        return sosAuthAccessToken;
    }

    public String getMsg() {
        return msg;
    }

}
