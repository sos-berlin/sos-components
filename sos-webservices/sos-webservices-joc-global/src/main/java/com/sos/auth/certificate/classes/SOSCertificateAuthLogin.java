package com.sos.auth.certificate.classes;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.certificate.SOSCertificateAuthHandler;
import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthSubject;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class SOSCertificateAuthLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSCertificateAuthLogin.class);

    private String msg = "";

    @Override
    public ISOSAuthSubject login(SOSAuthCurrentAccount currentAccount, String pwd, SOSIdentityService identityService) {
        SOSInternAuthSubject sosInternAuthSubject = null;
        try {
            SOSCertificateAuthWebserviceCredentials sosCertificateAuthWebserviceCredentials = new SOSCertificateAuthWebserviceCredentials();
            sosCertificateAuthWebserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            sosCertificateAuthWebserviceCredentials.setAccount(currentAccount.getAccountname());
            sosCertificateAuthWebserviceCredentials.setHttpRequest(currentAccount.getHttpServletRequest());
            SOSCertificateAuthHandler sosCertificateAuthHandler = new SOSCertificateAuthHandler();

            SOSAuthAccessToken sosCertificateAuthAccessToken = null;

            boolean disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());
            if (!disabled) {
                if (identityService.isSingleFactor()) {
                    sosCertificateAuthAccessToken = sosCertificateAuthHandler.login(sosCertificateAuthWebserviceCredentials);
                    if (SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount.getAccountname())) {
                        sosCertificateAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
                        sosCertificateAuthAccessToken = new SOSAuthAccessToken();
                        sosCertificateAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
                    }
                }
            }

            sosInternAuthSubject = new SOSInternAuthSubject();
            if (sosCertificateAuthAccessToken == null) {
                sosInternAuthSubject.setAuthenticated(false);
                setMsg("There is no account with a valid certificate");
            } else {
                sosInternAuthSubject.setAuthenticated(true);
                sosInternAuthSubject.setIsForcePasswordChange(false);
                sosInternAuthSubject.setPermissionAndRoles(currentAccount.getAccountname(), identityService);
                sosInternAuthSubject.setAccessToken(sosCertificateAuthAccessToken.getAccessToken());
            }
            
        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        }
        return sosInternAuthSubject;
    }

    @Override
    public ISOSAuthSubject simulateLogin(String account, SOSIdentityService identityService) {
        SOSInternAuthSubject sosInternAuthSubject = null;
        try {
            sosInternAuthSubject = new SOSInternAuthSubject();
            sosInternAuthSubject.setAuthenticated(true);
            sosInternAuthSubject.setPermissionAndRoles(account, identityService);
        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        }
        return sosInternAuthSubject;
    }

    @Override
    public void logout() {
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public void setMsg(String msg) {
        LOGGER.debug("sosLogin: setMsg=" + msg);
        this.msg = msg;
    }

}
