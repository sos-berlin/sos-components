package com.sos.auth.vault;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.Globals;

public class SOSVaultSession implements ISOSSession {

    private SOSVaultAccountAccessToken accessToken;
    private Long startSession;
    private Long lastTouch;
    private Long initSessionTimeout;
    private SOSVaultHandler sosVaultHandler;

    Map<String, Object> attributes;

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
                Date now = new Date();
                startSession = now.getTime();

            } catch (Exception e) {
                e.printStackTrace();
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
            Date now = new Date();
            Long timeout = initSessionTimeout - now.getTime() + lastTouch;
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
  
        try {
            Date now = new Date();
            lastTouch = now.getTime();
            if (initSessionTimeout == null) {
                initSessionTimeout = Long.valueOf(Globals.sosCockpitProperties.getProperty("iam_session_timeout"));
            }
        } catch (NumberFormatException e) {
            initSessionTimeout = 300000L;

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
                startSession = new Date().getTime();
                return true;
            } else {
                return false;
            }
        } catch (SOSException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Long getStartSession() {
        return startSession;
    }
}
