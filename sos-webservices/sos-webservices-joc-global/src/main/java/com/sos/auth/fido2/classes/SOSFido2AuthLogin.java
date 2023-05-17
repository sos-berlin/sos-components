package com.sos.auth.fido2.classes;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.fido2.SOSFido2AuthHandler;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.sosintern.SOSInternAuthHandler;
import com.sos.auth.sosintern.classes.SOSInternAuthSubject;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class SOSFido2AuthLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFido2AuthLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;
    private SOSInternAuthSubject sosInternAuthSubject;

    public SOSFido2AuthLogin() {

    }

    public void login(SOSAuthCurrentAccount currentAccount, String pwd) {
 
        try {
            SOSFido2AuthWebserviceCredentials sosFido2AuthWebserviceCredentials = new SOSFido2AuthWebserviceCredentials();
            sosFido2AuthWebserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            sosFido2AuthWebserviceCredentials.setAccount(currentAccount.getAccountname());
            sosFido2AuthWebserviceCredentials.setChallenge(currentAccount.getSosLoginParameters().getFido2Challenge());
            sosFido2AuthWebserviceCredentials.setAlgorithm(currentAccount.getSosLoginParameters().getAlgorithm());
            sosFido2AuthWebserviceCredentials.setSignature(currentAccount.getSosLoginParameters().getSignature());

            SOSFido2AuthHandler sosFido2AuthHandler = new SOSFido2AuthHandler();

            SOSAuthAccessToken sosFido2AuthAccessToken = null;

            boolean disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());
            if (!disabled && !identityService.isSecondFactor()) {
                if (identityService.isSingleFactor()) {
                    sosFido2AuthAccessToken = sosFido2AuthHandler.login(sosFido2AuthWebserviceCredentials);
                } else {
                    if ((identityService.isTwoFactor() && SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount
                            .getAccountname()))) {
                        sosFido2AuthAccessToken = sosFido2AuthHandler.login(sosFido2AuthWebserviceCredentials);
                    }
                }
            }

            sosInternAuthSubject = new SOSInternAuthSubject();
            if (sosFido2AuthAccessToken == null) {
                sosInternAuthSubject.setAuthenticated(false);
                setMsg("There is no FIDO2 account with the given account");
            } else {
                sosInternAuthSubject.setAuthenticated(true);
                sosInternAuthSubject.setIsForcePasswordChange(false);
                sosInternAuthSubject.setPermissionAndRoles(currentAccount.getAccountname(), identityService);
                sosInternAuthSubject.setAccessToken(sosFido2AuthAccessToken.getAccessToken());
            }

        } catch (SOSHibernateException | InvalidKeyException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
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
