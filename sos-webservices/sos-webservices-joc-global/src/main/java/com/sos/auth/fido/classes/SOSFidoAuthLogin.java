package com.sos.auth.fido.classes;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.fido.SOSFidoAuthHandler;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthSubject;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class SOSFidoAuthLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFidoAuthLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;
    private SOSInternAuthSubject sosInternAuthSubject;

    public SOSFidoAuthLogin() {

    }

    public void login(SOSAuthCurrentAccount currentAccount, String pwd) {

        try {
            SOSFidoAuthWebserviceCredentials sosFidoAuthWebserviceCredentials = new SOSFidoAuthWebserviceCredentials();
            sosFidoAuthWebserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            sosFidoAuthWebserviceCredentials.setAccount(currentAccount.getAccountname());
            sosFidoAuthWebserviceCredentials.setClientDataJson(currentAccount.getSosLoginParameters().getClientDataJson());
            sosFidoAuthWebserviceCredentials.setSignature(currentAccount.getSosLoginParameters().getSignature());
            sosFidoAuthWebserviceCredentials.setAuthenticatorData(currentAccount.getSosLoginParameters().getAuthenticatorData());
            sosFidoAuthWebserviceCredentials.setRequestId(currentAccount.getSosLoginParameters().getRequestId());
            sosFidoAuthWebserviceCredentials.setCredentialId(currentAccount.getSosLoginParameters().getCredentialId());

            SOSFidoAuthHandler sosFidoAuthHandler = new SOSFidoAuthHandler();

            SOSAuthAccessToken sosFidoAuthAccessToken = null;

            if ((currentAccount.getSosLoginParameters().getClientDataJson() != null) && (currentAccount.getSosLoginParameters()
                    .getSignature() != null) && (currentAccount.getSosLoginParameters().getAuthenticatorData() != null)) {

                boolean disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());
                if (!disabled) {
                    sosFidoAuthAccessToken = sosFidoAuthHandler.login(sosFidoAuthWebserviceCredentials);
                } else {
                    LOGGER.debug(identityService.getIdentityServiceName() + " is disabled");
                }
            } else {
                if (currentAccount.getSosLoginParameters().getClientDataJson() != null) {
                    LOGGER.info("-- getClientDataJson is null");
                }
                if (currentAccount.getSosLoginParameters().getSignature() != null) {
                    LOGGER.info("-- getSignature is null");
                }
                if (currentAccount.getSosLoginParameters().getAuthenticatorData() != null) {
                    LOGGER.info("-- getAuthenticatorData is null");
                }
            }

            sosInternAuthSubject = new SOSInternAuthSubject();
            if (sosFidoAuthAccessToken == null) {
                sosInternAuthSubject.setAuthenticated(false);
                setMsg("There is no FIDO2 account with the given account");
            } else {
                sosInternAuthSubject.setAuthenticated(true);
                sosInternAuthSubject.setIsForcePasswordChange(false);
                sosInternAuthSubject.setPermissionAndRoles(currentAccount.getAccountname(), identityService);
                sosInternAuthSubject.setAccessToken(sosFidoAuthAccessToken.getAccessToken());
            }

        } catch (SOSHibernateException | InvalidKeyException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException
                | InvalidKeySpecException |

                IOException e) {
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
