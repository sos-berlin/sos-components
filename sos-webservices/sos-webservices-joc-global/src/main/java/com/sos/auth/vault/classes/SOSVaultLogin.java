package com.sos.auth.vault.classes;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.vault.SOSVaultHandler;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSVaultLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultLogin.class);

    private String msg = "";
    private SOSIdentityService identityService;

    SOSVaultSubject sosVaultSubject;

    public SOSVaultLogin() {

    }

    public void login(String account, String pwd, HttpServletRequest httpServletRequest) {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        try {
            SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
            webserviceCredentials.setValuesFromProfile(identityService);
            keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeystorePath(), webserviceCredentials.getKeystoreType(),
                    webserviceCredentials.getKeystorePassword());

            trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                    webserviceCredentials.getTruststorePassword());

            webserviceCredentials.setAccount(account);
            SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);

            SOSVaultAccountAccessToken sosVaultAccountAccessToken = sosVaultHandler.login(pwd);
            sosVaultSubject = new SOSVaultSubject(identityService);
             
            if (sosVaultAccountAccessToken.getAuth() == null) {
                sosVaultSubject.setAuthenticated(false);
                setMsg("There is no user with the given account/password combination");
            } else {
                sosVaultSubject.setAuthenticated(true);
                sosVaultSubject.setPermissionAndRoles(sosVaultAccountAccessToken.getAuth().getToken_policies(),account, identityService);
                sosVaultSubject.setAccessToken(sosVaultAccountAccessToken);
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        } catch (SOSException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
