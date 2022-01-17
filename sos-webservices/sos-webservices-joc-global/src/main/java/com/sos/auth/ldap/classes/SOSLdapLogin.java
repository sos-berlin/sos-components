package com.sos.auth.ldap.classes;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.ldap.SOSLdapHandler;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;

public class SOSLdapLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;
    private SOSLdapSubject sosLdapSubject;

    public SOSLdapLogin() {

    }

    public void login(String account, String pwd, HttpServletRequest httpServletRequest) {
        SOSHibernateSession sosHibernateSession = null;

        SOSLdapHandler sosLdapHandler = new SOSLdapHandler();
        try {

            SOSLdapWebserviceCredentials sosLdapWebserviceCredentials = new SOSLdapWebserviceCredentials();
            sosLdapWebserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            sosLdapWebserviceCredentials.setAccount(account);
            sosLdapWebserviceCredentials.setValuesFromProfile(identityService);

            SOSAuthAccessToken sosAuthAccessToken = sosLdapHandler.login(sosLdapWebserviceCredentials, pwd);
            sosLdapSubject = new SOSLdapSubject();
            if (sosAuthAccessToken == null) {
                sosLdapSubject.setAuthenticated(false);
                setMsg(sosLdapHandler.getMsg());
            } else {
                sosLdapSubject.setPermissionAndRoles(sosLdapHandler.getGroupRolesMapping(sosLdapWebserviceCredentials), account, identityService);
                sosLdapSubject.setAuthenticated(true);
                sosLdapSubject.setAccessToken(sosAuthAccessToken.getAccessToken());
            }

        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        } catch (NamingException e) {
            LOGGER.error("", e);
        } finally {
            sosLdapHandler.close();
            Globals.disconnect(sosHibernateSession);
        }

    }

    public void logout() {

    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        LOGGER.debug("sosLogin: setMsg=" + msg);
        this.msg = msg;
    }

    @Override
    public ISOSAuthSubject getCurrentSubject() {
        return sosLdapSubject;
    }

    public void setIdentityService(SOSIdentityService identityService) {
        this.identityService = identityService;
    }

}
