package com.sos.auth.vault.classes;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.vault.SOSVaultHandler;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;

public class SOSVaultLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultLogin.class);

    private String msg="";
    SOSVaultSubject sosVaultSubject;

    public SOSVaultLogin() {

    }

    public void login(String user, String pwd, HttpServletRequest httpServletRequest) {
        KeyStore keyStore = null;
        KeyStore trustStore = null;

        try {
            SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
            webserviceCredentials.setValuesFromProfile();
            keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeyStorePath(), webserviceCredentials.getKeyStoreType(),
                    webserviceCredentials.getKeyStorePassword());

            trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTrustStorePath(), webserviceCredentials.getTrustStoreType(),
                    webserviceCredentials.getTrustStorePassword());

            webserviceCredentials.setAccount(user);
            webserviceCredentials.setPassword(pwd);
            SOSVaultHandler sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);
            
            SOSVaultAccountAccessToken sosVaultUserAccessToken = sosVaultHandler.login();
            sosVaultSubject = new SOSVaultSubject();
            if (sosVaultUserAccessToken.getAuth() == null) {
                sosVaultSubject.setAuthenticated(false);
                setMsg("There is no user with the given username/password combination");
            } else {
                sosVaultSubject.setAccount(user);
                sosVaultSubject.setAuthenticated(true);
                sosVaultSubject.setPermissionAndRoles(user);
                sosVaultSubject.setAccessToken(sosVaultUserAccessToken);
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
    public void setIdentityServiceId(Long value) {
      // Not needed for vault
    }

}
