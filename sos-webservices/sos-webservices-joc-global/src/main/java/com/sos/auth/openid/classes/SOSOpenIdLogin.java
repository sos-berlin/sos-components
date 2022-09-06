package com.sos.auth.openid.classes;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.openid.SOSOpenIdHandler;
import com.sos.commons.exception.SOSException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSOpenIdLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;

    SOSOpenIdSubject sosOpenIdSubject;

    public SOSOpenIdLogin() {

    }

    public void login(SOSAuthCurrentAccount currentAccount, String pwd) {

        try {
            SOSOpenIdHandler sosOpenIdHandler = new SOSOpenIdHandler(currentAccount);
            SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken = null;

            boolean disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());

            if (!disabled && (!identityService.isTwoFactor() || (SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount
                    .getAccountname())))) {
                sosOpenIdAccountAccessToken = sosOpenIdHandler.login(currentAccount);
            }

            sosOpenIdSubject = new SOSOpenIdSubject(currentAccount.getAccountname(), identityService);

            if (sosOpenIdAccountAccessToken.getAccess_token() == null || sosOpenIdAccountAccessToken.getAccess_token().isEmpty()) {
                sosOpenIdSubject.setAuthenticated(false);
                setMsg("The access token is not valid");
            } else {
                sosOpenIdSubject.setAuthenticated(true);
                sosOpenIdSubject.setAccessToken(sosOpenIdAccountAccessToken);
                sosOpenIdSubject.setPermissionAndRoles(currentAccount.getAccountname());
            }

        } catch (IOException e) {
            msg = e.toString();
            LOGGER.error("", e);
        } catch (SOSException e) {
            msg = e.toString();
            LOGGER.error("", e);
        } catch (JocException e) {
            msg = e.toString();
            LOGGER.info("KEYCLOAK:" + e.getMessage());
        } catch (Exception e) {
            msg = e.toString();
            LOGGER.error("", e);
        }
    }

    public void simulateLogin(String account) {

        try {
            sosOpenIdSubject = new SOSOpenIdSubject(account, identityService);
            sosOpenIdSubject.setAuthenticated(true);

            if (IdentityServiceTypes.KEYCLOAK_JOC == identityService.getIdentyServiceType()) {
                sosOpenIdSubject.setPermissionAndRoles(account);
            }

        } catch (SOSException e) {
            LOGGER.error("", e);
        } catch (JocException e) {
            LOGGER.info("VAULT:" + e.getMessage());
        } catch (Exception e) {
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
        return sosOpenIdSubject;
    }

    @Override
    public void setIdentityService(SOSIdentityService sosIdentityService) {
        identityService = sosIdentityService;
    }

}
