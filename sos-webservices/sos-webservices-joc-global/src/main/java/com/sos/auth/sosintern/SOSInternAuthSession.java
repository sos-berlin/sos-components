package com.sos.auth.sosintern;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.sos.auth.interfaces.ISOSSession;
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.joc.Globals;

public class SOSInternAuthSession implements ISOSSession {

    private String accessToken;
    private Long startSession;
    private Long lastTouch;
    private Long initSessionTimeout;

    private Map<String, Object> attributes;

    public SOSInternAuthSession() {
        super();
        startSession = Instant.now().toEpochMilli();
;
    }

    private Map<String, Object> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        return attributes;
    }

    @Override
    public SOSVaultAccountAccessToken getSOSVaultAccountAccessToken() {
        return null;
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
        return accessToken;
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
    public void setAttribute(String key, Object value) {
        getAttributes().put(key, value);
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean renew() {
        if (getTimeout() > 0) {
            touch();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Long getStartSession() {
        return startSession;
    }
}
