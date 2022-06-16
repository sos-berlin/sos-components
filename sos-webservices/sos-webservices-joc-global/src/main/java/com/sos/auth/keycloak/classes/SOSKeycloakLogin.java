package com.sos.auth.keycloak.classes;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void login(String account, String pwd, HttpServletRequest httpServletRequest) {
        KeyStore trustStore = null;

        try {
            SOSKeycloakWebserviceCredentials webserviceCredentials = new SOSKeycloakWebserviceCredentials();
            webserviceCredentials.setValuesFromProfile(identityService);

            trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                    webserviceCredentials.getTruststorePassword());

            webserviceCredentials.setAccount(account);
            SOSKeycloakHandler sosKeycloakHandler = new SOSKeycloakHandler(webserviceCredentials, trustStore, identityService);

            SOSKeycloakAccountAccessToken sosKeycloakAccountAccessToken = null;

            boolean disabled;

            disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), account);

            if (!disabled && (!identityService.isTwoFactor() || (SOSAuthHelper.checkCertificate(httpServletRequest, account)))) {
                sosKeycloakAccountAccessToken = sosKeycloakHandler.login(pwd);
            }

            sosKeycloakSubject = new SOSKeycloakSubject(account, identityService);

            if (sosKeycloakAccountAccessToken.getAccess_token() == null || sosKeycloakAccountAccessToken.getAccess_token().isEmpty()) {
                sosKeycloakSubject.setAuthenticated(false);
                setMsg("There is no user with the given account/password combination");
            } else {
                sosKeycloakSubject.setAuthenticated(true);
                sosKeycloakSubject.setAccessToken(sosKeycloakAccountAccessToken);

                if (IdentityServiceTypes.KEYCLOAK_JOC == identityService.getIdentyServiceType()
                        || IdentityServiceTypes.KEYCLOAK_JOC_ACTIVE == identityService.getIdentyServiceType()) {
                    sosKeycloakSubject.setPermissionAndRoles(null, account);
                } else {
                    sosKeycloakSubject.setPermissionAndRoles(sosKeycloakHandler.getTokenRoles(), account);
                }
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOGGER.error("", e);
        } catch (SOSException e) {
            LOGGER.error("", e);
        } catch (JocException e) {
            LOGGER.info("KEYCLOAK:" + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public void simulateLogin(String account) {

        try {
            sosKeycloakSubject = new SOSKeycloakSubject(account, identityService);
            sosKeycloakSubject.setAuthenticated(true);

            if (IdentityServiceTypes.VAULT_JOC == identityService.getIdentyServiceType() || IdentityServiceTypes.VAULT_JOC_ACTIVE == identityService
                    .getIdentyServiceType()) {
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
