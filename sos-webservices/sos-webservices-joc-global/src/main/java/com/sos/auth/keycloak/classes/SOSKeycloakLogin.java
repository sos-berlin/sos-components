package com.sos.auth.keycloak.classes;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.keycloak.SOSKeycloakHandler;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSKeycloakLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeycloakLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;

    SOSKeycloakSubject sosKeycloakSubject;

    public SOSKeycloakLogin() {

    }

    public void login(SOSAuthCurrentAccount currentAccount, String pwd) {
        KeyStore trustStore = null;

        try {
            SOSKeycloakWebserviceCredentials webserviceCredentials = new SOSKeycloakWebserviceCredentials();
            webserviceCredentials.setValuesFromProfile(identityService);

            trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                    webserviceCredentials.getTruststorePassword());

            webserviceCredentials.setAccount(currentAccount.getAccountname());
            SOSKeycloakHandler sosKeycloakHandler = new SOSKeycloakHandler(webserviceCredentials, trustStore);

            SOSKeycloakAccountAccessToken sosKeycloakAccountAccessToken = null;

            boolean disabled;

            disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());

            if (!disabled && (!identityService.isTwoFactor() || (SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount
                    .getAccountname())))) {
                sosKeycloakAccountAccessToken = sosKeycloakHandler.login(pwd);
            }

            sosKeycloakSubject = new SOSKeycloakSubject(currentAccount.getAccountname(), identityService);

            if (sosKeycloakAccountAccessToken.getAccess_token() == null || sosKeycloakAccountAccessToken.getAccess_token().isEmpty()) {
                sosKeycloakSubject.setAuthenticated(false);
                setMsg("There is no user with the given account/password combination");
            } else {
                sosKeycloakSubject.setAuthenticated(true);
                sosKeycloakSubject.setAccessToken(sosKeycloakAccountAccessToken);

                if (IdentityServiceTypes.KEYCLOAK_JOC == identityService.getIdentyServiceType()) {
                    sosKeycloakSubject.setPermissionAndRoles(null, currentAccount.getAccountname());
                } else {
                    sosKeycloakSubject.setPermissionAndRoles(sosKeycloakHandler.getTokenRoles(), currentAccount.getAccountname());
                }
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
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
            sosKeycloakSubject = new SOSKeycloakSubject(account, identityService);
            sosKeycloakSubject.setAuthenticated(true);

            if (IdentityServiceTypes.KEYCLOAK_JOC == identityService.getIdentyServiceType()) {
                sosKeycloakSubject.setPermissionAndRoles(new HashSet<String>(), account);
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
        return sosKeycloakSubject;
    }

    @Override
    public void setIdentityService(SOSIdentityService sosIdentityService) {
        identityService = sosIdentityService;
    }

}
