package com.sos.auth.shiro.classes;

import java.io.Serializable;

import org.apache.shiro.session.Session;

import com.sos.auth.interfaces.ISOSSession;

public class SOSShiroSession implements ISOSSession {

    private Session shiroSession;

    @Override
    public void setAttribute(String key, Object value) {
        shiroSession.setAttribute(key, value);
    }

    @Override
    public Object getAttribute(Object key) {
        return shiroSession.getAttribute(key);
    }

    @Override
    public void removeAttribute(Object key) {
        shiroSession.removeAttribute(key);
    }

    @Override
    public void stop() {
        shiroSession.stop();
    }

    @Override
    public Long getTimeout() {
        return shiroSession.getTimeout();
    }

    @Override
    public Serializable getAccessToken() {
        return shiroSession.getId();
    }

    @Override
    public void touch() {
        shiroSession.touch();
    }

    public Session getShiroSession() {
        return shiroSession;
    }

    public void setShiroSession(Session shiroSession) {
        this.shiroSession = shiroSession;
    }

}
