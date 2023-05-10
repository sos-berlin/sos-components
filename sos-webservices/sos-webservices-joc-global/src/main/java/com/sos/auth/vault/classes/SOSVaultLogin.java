package com.sos.auth.vault.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.vault.SOSVaultHandler;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSVaultLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;

    SOSVaultSubject sosVaultSubject;

    public SOSVaultLogin() {

    }

    public void login(SOSAuthCurrentAccount currentAccount, String pwd) {
        KeyStore truststore = null;

        try {
            SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
            webserviceCredentials.setIdentityServiceId(identityService.getIdentityServiceId());
            webserviceCredentials.setValuesFromProfile(identityService);

            if (Files.exists(Paths.get(webserviceCredentials.getTruststorePath()))) {
                truststore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                        webserviceCredentials.getTruststorePassword());
            } else {
                LOGGER.warn("Truststore file " + webserviceCredentials.getTruststorePath() + " not existing");
            }

            webserviceCredentials.setAccount(currentAccount.getAccountname());
            SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, truststore);

            SOSVaultAccountAccessToken sosVaultAccountAccessToken = null;

            boolean disabled;

            disabled = SOSAuthHelper.accountIsDisabled(identityService.getIdentityServiceId(), currentAccount.getAccountname());

            if (!disabled && (identityService.isSingleFactor() || (SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount
                    .getAccountname())))) {
                sosVaultAccountAccessToken = sosVaultHandler.login(identityService.getIdentyServiceType(), pwd);
            }

            sosVaultSubject = new SOSVaultSubject(currentAccount.getAccountname(), identityService);

            if (sosVaultAccountAccessToken == null || sosVaultAccountAccessToken.getAuth() == null) {
                sosVaultSubject.setAuthenticated(false);
                if (sosVaultAccountAccessToken == null) {
                    setMsg("Account has no roles. Login skipped.");
                } else {
                    setMsg("There is no user with the given account/password combination");
                }
            } else {
                sosVaultSubject.setAuthenticated(true);
                sosVaultSubject.setAccessToken(sosVaultAccountAccessToken);
                sosVaultSubject.setPermissionAndRoles(sosVaultAccountAccessToken.getAuth().getToken_policies(), currentAccount.getAccountname());
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            msg = e.toString();
            LOGGER.error("", e);
        } catch (SOSException e) {
            msg = e.toString();
            LOGGER.error("", e);
        } catch (JocException e) {
            msg = e.toString();
            LOGGER.info("VAULT:" + e.getMessage());
        } catch (Exception e) {
            msg = e.toString();
            LOGGER.error("", e);
        }
    }

    public void simulateLogin(String account) {

        try {
            sosVaultSubject = new SOSVaultSubject(account, identityService);
            sosVaultSubject.setAuthenticated(true);

            if (IdentityServiceTypes.VAULT_JOC == identityService.getIdentyServiceType() || IdentityServiceTypes.VAULT_JOC_ACTIVE == identityService
                    .getIdentyServiceType()) {
                sosVaultSubject.setPermissionAndRoles(new ArrayList<String>(), account);
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
        return sosVaultSubject;
    }

    @Override
    public void setIdentityService(SOSIdentityService sosIdentityService) {
        identityService = sosIdentityService;
    }

}
