package com.sos.auth.keycloak;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.keycloak.classes.SOSKeycloakAccountAccessToken;
import com.sos.auth.keycloak.classes.SOSKeycloakWebserviceCredentials;
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.Globals;

public class SOSKeycloakSession implements ISOSSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeycloakSession.class);

    private SOSKeycloakAccountAccessToken accessToken;
    private Long startSession;
    private Long lastTouch;
    private Long initSessionTimeout;
    private SOSKeycloakHandler sosKeycloakHandler;

    private Map<String, Object> attributes;

    public SOSKeycloakSession(SOSIdentityService identityService) {
        super();
        initSession(identityService);
    }

    private Map<String, Object> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        return attributes;
    }

    private void initSession(SOSIdentityService identityService) {
        if (sosKeycloakHandler == null) {
            KeyStore trustStore = null;
            SOSKeycloakWebserviceCredentials webserviceCredentials = new SOSKeycloakWebserviceCredentials();
            try {
                webserviceCredentials.setValuesFromProfile(identityService);

                trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                        webserviceCredentials.getTruststorePassword());

                webserviceCredentials.setAccount("");
                sosKeycloakHandler = new SOSKeycloakHandler(webserviceCredentials, trustStore);
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
    public SOSKeycloakAccountAccessToken getSOSKeycloakAccountAccessToken() {
        return accessToken;
    }

    public void setAccessToken(SOSKeycloakAccountAccessToken accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean renew() {
        try {
            if (sosKeycloakHandler.accountAccessTokenIsValid(accessToken)) {
                sosKeycloakHandler.renewAccountAccess(accessToken);
                startSession = Instant.now().toEpochMilli();
                return true;
            } else {
                return false;
            }
        } catch (SOSException | IOException e) {
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
}
