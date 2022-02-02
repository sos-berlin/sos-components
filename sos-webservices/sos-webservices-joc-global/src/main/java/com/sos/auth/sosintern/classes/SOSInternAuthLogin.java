package com.sos.auth.sosintern.classes;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.sosintern.SOSInternAuthHandler;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;

public class SOSInternAuthLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSInternAuthLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;
    private SOSInternAuthSubject sosInternAuthSubject;

    public SOSInternAuthLogin() {

    }

    public void login(String account, String pwd, HttpServletRequest httpServletRequest) {
        SOSHibernateSession sosHibernateSession = null;

        try {

            SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials = new SOSInternAuthWebserviceCredentials();
            sosInternAuthWebserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            sosInternAuthWebserviceCredentials.setAccount(account);
            SOSInternAuthHandler sosInternAuthHandler = new SOSInternAuthHandler();

            SOSAuthAccessToken sosInternAuthAccessToken = null;

            if (identityService.isSingleFactor()) {
                if (identityService.isSingleFactorCert() && SOSAuthHelper.checkCertificate(httpServletRequest, account)) {
                    sosInternAuthAccessToken = new SOSAuthAccessToken();
                    sosInternAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
                } else {
                    if (identityService.isSingleFactorPwd()) {
                        sosInternAuthAccessToken = sosInternAuthHandler.login(sosInternAuthWebserviceCredentials, pwd);
                    }
                }
            } else {
                if ((identityService.isTwoFactor() && SOSAuthHelper.checkCertificate(httpServletRequest, ""))) {
                    sosInternAuthAccessToken = sosInternAuthHandler.login(sosInternAuthWebserviceCredentials, pwd);
                }
            }

            sosInternAuthSubject = new SOSInternAuthSubject();
            if (sosInternAuthAccessToken == null) {
                sosInternAuthSubject.setAuthenticated(false);
                setMsg("There is no account with the given accountname/password combination");
            } else {
                sosInternAuthSubject.setAuthenticated(true);
                sosInternAuthSubject.setIsForcePasswordChange(sosInternAuthHandler.getForcePasswordChange());
                sosInternAuthSubject.setPermissionAndRoles(account, identityService);
                sosInternAuthSubject.setAccessToken(sosInternAuthAccessToken.getAccessToken());
            }

        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        } finally {
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
        return sosInternAuthSubject;
    }

    public void setIdentityService(SOSIdentityService identityService) {
        this.identityService = identityService;
    }

}
