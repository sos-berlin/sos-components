package com.sos.auth.vault;

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
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.Globals;

public class SOSVaultSession implements ISOSSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultSession.class);

    private SOSVaultAccountAccessToken accessToken;
    private Long startSession;
    private Long lastTouch;
    private Long initSessionTimeout;
    private SOSVaultHandler sosVaultHandler;

    private Map<String, Object> attributes;

    public SOSVaultSession(SOSIdentityService identityService) {
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
        if (sosVaultHandler == null) {
            KeyStore keyStore = null;
            KeyStore trustStore = null;
            SOSVaultWebserviceCredentials webserviceCredentials = new SOSVaultWebserviceCredentials();
            try {
                webserviceCredentials.setValuesFromProfile(identityService);

                keyStore = KeyStoreUtil.readKeyStore(webserviceCredentials.getKeystorePath(), webserviceCredentials.getKeystoreType(),
                        webserviceCredentials.getKeystorePassword());

                trustStore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials.getTrustStoreType(),
                        webserviceCredentials.getTruststorePassword());

                webserviceCredentials.setAccount("");
                sosVaultHandler = new SOSVaultHandler(webserviceCredentials, keyStore, trustStore);
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
        return accessToken.getAuth().getClient_token();

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
    public SOSVaultAccountAccessToken getSOSVaultAccountAccessToken() {
        return accessToken;
    }

    public void setAccessToken(SOSVaultAccountAccessToken accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean renew() {
        try {
            if (sosVaultHandler.accountAccessTokenIsValid(accessToken)) {
                sosVaultHandler.renewAccountAccess(accessToken);
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
}
