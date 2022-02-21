package com.sos.auth.ldap.classes;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.ldap.SOSLdapHandler;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSLdapLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;
    private SOSLdapSubject sosLdapSubject;

    public SOSLdapLogin() {

    }

    public void login(String account, String pwd, HttpServletRequest httpServletRequest) {

        SOSLdapHandler sosLdapHandler = new SOSLdapHandler();
        try {

            SOSLdapWebserviceCredentials sosLdapWebserviceCredentials = new SOSLdapWebserviceCredentials();
            sosLdapWebserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            sosLdapWebserviceCredentials.setAccount(account);
            sosLdapWebserviceCredentials.setValuesFromProfile(identityService);

            SOSAuthAccessToken sosAuthAccessToken = null;

            boolean disabled;
            if (identityService.getIdentyServiceType() == IdentityServiceTypes.LDAP_JOC) {
                disabled = SOSAuthHelper.accountIsDisable(identityService.getIdentityServiceId(), account);
            } else {
                disabled = false;
            }

            if (!disabled && (!identityService.isTwoFactor() || (SOSAuthHelper.checkCertificate(httpServletRequest, account)))) {
                sosAuthAccessToken = sosLdapHandler.login(sosLdapWebserviceCredentials, pwd);
            }

            sosLdapSubject = new SOSLdapSubject();

            if (sosAuthAccessToken == null) {
                sosLdapSubject.setAuthenticated(false);
                setMsg(sosLdapHandler.getMsg());
            } else {
                sosLdapSubject.setAuthenticated(true);
                sosLdapSubject.setAccessToken(sosAuthAccessToken.getAccessToken());
                if (IdentityServiceTypes.LDAP_JOC == identityService.getIdentyServiceType()) {
                    sosLdapSubject.setPermissionAndRoles(null,account, identityService);
                }else {
                    sosLdapSubject.setPermissionAndRoles(sosLdapHandler.getGroupRolesMapping(sosLdapWebserviceCredentials), account, identityService);
                }
                	
            }

        } catch (SOSHibernateException e) {
            setMsg(e.getMessage());
            LOGGER.error("", e);
        } catch (NamingException e) {
            setMsg(e.getMessage());
            LOGGER.error("", e);
        } finally {
            sosLdapHandler.close();
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
