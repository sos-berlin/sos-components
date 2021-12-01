package com.sos.auth.sosintern;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sos.auth.interfaces.ISOSSession;
import com.sos.joc.Globals;

public class SOSInternAuthSession implements ISOSSession {

    String accessToken;
    Long startSession;
    Long sessionTimeout;
    Long initSessionTimeout;

    Map<String, Object> attributes;

    private Map<String, Object> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        return attributes;
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
        startSession = 0L;
    }

    @Override
    public Long getTimeout() {
        if (initSessionTimeout < 0L) {
            return -1L;
        } else {
            Date now = new Date();
            Long timeout = initSessionTimeout - now.getTime() + startSession;
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
        try {
            if (initSessionTimeout == null) {
                initSessionTimeout = Long.valueOf(Globals.sosCockpitProperties.getProperty("iam_session_timeout"));
            }
            Date now = new Date();
            startSession = now.getTime();
            sessionTimeout = initSessionTimeout;
        } catch (NumberFormatException e) {
            initSessionTimeout = 90000L;
            sessionTimeout = initSessionTimeout;
        }

    }

 
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
