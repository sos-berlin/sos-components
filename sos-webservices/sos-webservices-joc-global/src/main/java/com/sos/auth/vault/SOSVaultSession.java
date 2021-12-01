package com.sos.auth.vault;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sos.auth.interfaces.ISOSSession;

public class SOSVaultSession implements ISOSSession {

    String accessToken;
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
    }

    @Override
    public Long getTimeout() {
        return 90000L;
    }

    @Override
    public Serializable getAccessToken() {
        return accessToken;

    }

    @Override
    public void touch() {
    }

 
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
