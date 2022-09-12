package com.sos.auth.openid;

import java.io.Serializable;
import java.security.KeyStore;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.keycloak.classes.SOSKeycloakAccountAccessToken;
import com.sos.auth.openid.classes.SOSOpenIdAccountAccessToken;
import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocException;

public class SOSOpenIdSession implements ISOSSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdSession.class);

    private SOSOpenIdAccountAccessToken accessToken;
    private Long startSession;
    private Long lastTouch;
    private Long initSessionTimeout;
    private SOSOpenIdHandler sosOpenIdHandler;

    private Map<String, Object> attributes;

    public SOSOpenIdSession(SOSAuthCurrentAccount currentAccount, SOSIdentityService identityService) {
        super();
        initSession(currentAccount, identityService);
    }

    private Map<String, Object> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        return attributes;
    }

    private void initSession(SOSAuthCurrentAccount currentAccount, SOSIdentityService identityService) {
        if (sosOpenIdHandler == null) {
            KeyStore trustStore = null;
            SOSOpenIdWebserviceCredentials webserviceCredentials = new SOSOpenIdWebserviceCredentials();
            try {
                webserviceCredentials.setValuesFromProfile(identityService);

                trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                        webserviceCredentials.getTruststorePassword());

                webserviceCredentials.setAccount(currentAccount.getAccountname());
                sosOpenIdHandler = new SOSOpenIdHandler(currentAccount);
                startSession = Instant.now().toEpochMilli();

            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        getAttributes().put(key, value);
    }

    @Override
    public Object getAttribute(Object key) {

        return getAttributes().get(key);
    }

    @Override
    public void removeAttribute(Object key) {
        getAttributes().remove(key);
    }

    @Override
    public void stop() {
        lastTouch = 0L;
    }

    @Override
    public Long getTimeout() {
        if (initSessionTimeout < 0L) {
            return -1L;
        } else {
            Long timeout = initSessionTimeout - Instant.now().toEpochMilli() + lastTouch;
            if (timeout < 0) {
                return 0L;
            }
            return timeout;
        }
    }

    @Override
    public Serializable getAccessToken() {
        return accessToken.getAccess_token();

    }

    @Override
    public void touch() {

        lastTouch = Instant.now().toEpochMilli();
        if (initSessionTimeout == null) {
            if (Globals.iamSessionTimeout != null) {
                initSessionTimeout = Globals.iamSessionTimeout * 1000L;
            } else {
                initSessionTimeout = 30 * 60 * 1000L;
            }
        }

    }

    @Override
    public SOSOpenIdAccountAccessToken getSOSOpenIdAccountAccessToken() {
        return accessToken;
    }

    public void setAccessToken(SOSOpenIdAccountAccessToken accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean renew() {
        try {
            if (sosOpenIdHandler.accountAccessTokenIsValid(accessToken)) {
                this.setAccessToken(sosOpenIdHandler.renewAccountAccess(accessToken));
                startSession = Instant.now().toEpochMilli();
                return true;
            } else {
                this.stop();
                return false;
            }
        } catch (JocException e) {
            LOGGER.error("", e);
            return false;
        }
    }

    @Override
    public Long getStartSession() {
        return startSession;
    }

    @Override
    public SOSVaultAccountAccessToken getSOSVaultAccountAccessToken() {
        return null;
    }

    @Override
    public SOSKeycloakAccountAccessToken getSOSKeycloakAccountAccessToken() {
        return null;
    }
}
