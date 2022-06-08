package com.sos.auth.classes;

import java.io.Serializable;

import com.sos.auth.interfaces.ISOSSession;
import com.sos.joc.exceptions.SessionNotExistException;

public class SOSSessionHandler {

    private SOSAuthCurrentAccount sosCurrentUser;
    private ISOSSession sosSession;

    public SOSSessionHandler(SOSAuthCurrentAccount sosCurrentUser) {
        super();
        this.sosCurrentUser = sosCurrentUser;
    }

    private void initSession() throws SessionNotExistException {
        if (sosSession == null) {
            sosSession = sosCurrentUser.getCurrentSubject().getSession();
            sosSession.touch();
        }
        if (sosSession == null) {
            throw new SessionNotExistException();
        }

    }

    public void setAttribute(String key, Object value) throws SessionNotExistException {
        initSession();
        sosSession.setAttribute(key, value);
    }

    public Object getAttribute(Object key) throws SessionNotExistException {
        initSession();
        return sosSession.getAttribute(key);

    }

    public String getStringAttribute(Object key) throws SessionNotExistException {
        initSession();
        return (String) sosSession.getAttribute(key);

    }

    public Boolean getBooleanAttribute(Object key) throws SessionNotExistException {
        initSession();
        return (Boolean) sosSession.getAttribute(key);
    }

    public void removeAttribute(Object key) throws SessionNotExistException {
        initSession();
        sosSession.removeAttribute(key);
    }

    public void stop() throws SessionNotExistException {
        initSession();
        sosSession.stop();
    }

    public long getTimeout() throws SessionNotExistException {
        initSession();
        return sosSession.getTimeout();
    }

    public Serializable getAccessToken() throws SessionNotExistException {
        initSession();
        return sosSession.getAccessToken();
    }

    public void touch() throws SessionNotExistException {
        initSession();
        sosSession.touch();

    }

}
