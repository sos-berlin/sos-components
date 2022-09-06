package com.sos.auth.sosintern.classes;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.sosintern.SOSInternAuthHandler;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class SOSInternAuthLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSInternAuthLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;
    private SOSInternAuthSubject sosInternAuthSubject;

    public SOSInternAuthLogin() {

    }

    public void login(SOSAuthCurrentAccount currentAccount, String pwd) {
        try {
            SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials = new SOSInternAuthWebserviceCredentials();
            sosInternAuthWebserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            sosInternAuthWebserviceCredentials.setAccount(currentAccount.getAccountname());
            SOSInternAuthHandler sosInternAuthHandler = new SOSInternAuthHandler();

            SOSAuthAccessToken sosInternAuthAccessToken = null;

            boolean disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());
            if (!disabled) {
                if (identityService.isSingleFactor()) {
                    if (identityService.isSingleFactorCert() && SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount
                            .getAccountname())) {

                        sosInternAuthAccessToken = new SOSAuthAccessToken();
                        sosInternAuthAccessToken.setAccessToken(UUID.randomUUID().toString());

                    } else {
                        if (identityService.isSingleFactorPwd()) {
                            sosInternAuthAccessToken = sosInternAuthHandler.login(sosInternAuthWebserviceCredentials, pwd);
                        }
                    }
                } else {
                    if ((identityService.isTwoFactor() && SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount
                            .getAccountname()))) {
                        sosInternAuthAccessToken = sosInternAuthHandler.login(sosInternAuthWebserviceCredentials, pwd);
                    }
                }
            }

            sosInternAuthSubject = new SOSInternAuthSubject();
            if (sosInternAuthAccessToken == null) {
                sosInternAuthSubject.setAuthenticated(false);
                setMsg("There is no account with the given account name/password combination");
            } else {
                sosInternAuthSubject.setAuthenticated(true);
                sosInternAuthSubject.setIsForcePasswordChange(sosInternAuthHandler.getForcePasswordChange());
                sosInternAuthSubject.setPermissionAndRoles(currentAccount.getAccountname(), identityService);
                sosInternAuthSubject.setAccessToken(sosInternAuthAccessToken.getAccessToken());
            }

        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        }

    }

    public void simulateLogin(String account) {
        try {
            sosInternAuthSubject = new SOSInternAuthSubject();
            sosInternAuthSubject.setAuthenticated(true);
            sosInternAuthSubject.setPermissionAndRoles(account, identityService);
        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
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
