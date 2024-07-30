package com.sos.auth.openid.classes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
            SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken = null;
            Set<String> setOfTokenRoles = new HashSet<String>();
            if (currentAccount.getSosLoginParameters().getIdToken() != null) {

                SOSOpenIdWebserviceCredentials webserviceCredentials = currentAccount.getSosLoginParameters().getSOSOpenIdWebserviceCredentials();
                if (webserviceCredentials == null) {
                    LOGGER.error("Wrong Headers for OIDC Login");
                } else {
                    webserviceCredentials.setOpenidConfiguration(currentAccount.getSosLoginParameters().getOpenidConfiguration());

                    SOSOpenIdHandler sosOpenIdHandler = new SOSOpenIdHandler(webserviceCredentials);
                    String accountName = sosOpenIdHandler.decodeIdToken(webserviceCredentials.getIdToken());
                    currentAccount.setAccountName(accountName);
                    webserviceCredentials.setAccount(accountName);

                    boolean disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());

                    if (!disabled) {
                        sosOpenIdAccountAccessToken = sosOpenIdHandler.login();
                        if (sosOpenIdAccountAccessToken != null) {
                            setOfTokenRoles = sosOpenIdHandler.getTokenRoles();
                            currentAccount.setKid(sosOpenIdHandler.getKid());

                        }
                    }

                }
            }
            sosOpenIdSubject = new SOSOpenIdSubject(currentAccount, identityService);
            if (sosOpenIdAccountAccessToken == null || sosOpenIdAccountAccessToken.getAccessToken() == null || sosOpenIdAccountAccessToken
                    .getAccessToken().isEmpty()) {
                sosOpenIdSubject.setAuthenticated(false);
                setMsg("The access token is not valid");
            } else {
                sosOpenIdSubject.setAuthenticated(true);
                sosOpenIdSubject.setAccessToken(sosOpenIdAccountAccessToken);
                if (IdentityServiceTypes.OIDC_JOC == identityService.getIdentyServiceType()) {
                    sosOpenIdSubject.setPermissionAndRoles(null, currentAccount.getAccountname());
                } else {
                    if (IdentityServiceTypes.OIDC == identityService.getIdentyServiceType()) {
                        sosOpenIdSubject.setPermissionAndRoles(setOfTokenRoles, currentAccount.getAccountname());
                    }
                }

            }

        } catch (IOException e) {
            msg = e.toString();
            LOGGER.error("", e);
        } catch (SOSException e) {
            msg = e.toString();
            LOGGER.error("", e);
        } catch (JocException e) {
            msg = e.toString();
            LOGGER.info("OIDC:" + e.getMessage());
        } catch (Exception e) {
            msg = e.toString();
            LOGGER.error("", e);
        }
    }

    public void simulateLogin(String account) {

    }

    public void logout() {

    }

    public String getMsg() {
        return msg;
    }

    @Override
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
