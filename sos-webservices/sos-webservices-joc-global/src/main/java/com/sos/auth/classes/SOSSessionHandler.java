package com.sos.auth.classes;

import java.io.Serializable;

import org.apache.shiro.session.InvalidSessionException;

import com.sos.auth.interfaces.ISOSSession;
import com.sos.joc.exceptions.SessionNotExistException;

public class SOSSessionHandler {

    private SOSAuthCurrentAccount sosCurrentUser;
    private ISOSSession sosSession;

    public SOSSessionHandler(SOSAuthCurrentAccount sosCurrentUser) {
        super();
        this.sosCurrentUser = sosCurrentUser;
    }

    private void initSession() {
        if (sosSession == null) {
            sosSession = sosCurrentUser.getCurrentSubject().getSession();
            sosSession.touch();
        }
    }

    public void setAttribute(String key, Object value) throws SessionNotExistException {
        try {
            initSession();
            if (sosSession != null) {
                sosSession.setAttribute(key, value);
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }
    }

    public Object getAttribute(Object key) throws SessionNotExistException {
        initSession();
        ISOSSession shiroSession = sosCurrentUser.getCurrentSubject().getSession();
        try {
            if (shiroSession != null) {
                return shiroSession.getAttribute(key);
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }

        return null;
    }

    public String getStringAttribute(Object key) throws SessionNotExistException {
        initSession();
        try {
            if (sosSession != null) {
                return (String) sosSession.getAttribute(key);
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }

        return null;
    }

    public Boolean getBooleanAttribute(Object key) throws SessionNotExistException {
        initSession();
        try {
            if (sosSession != null) {
                return (Boolean) sosSession.getAttribute(key);
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }

        return null;
    }

    public void removeAttribute(Object key) throws SessionNotExistException {
        initSession();
        try {
            if (sosSession != null) {
                sosSession.removeAttribute(key);
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }

    }

    public void stop() throws SessionNotExistException {
        initSession();
        try {
            if (sosSession != null) {
                sosSession.stop();
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }

    }

    public long getTimeout() throws SessionNotExistException {
        initSession();

        if (sosSession != null) {
            return sosSession.getTimeout();
        } else {
            throw new SessionNotExistException("Session has expired");
        }

    }

    public Serializable getAccessToken() throws SessionNotExistException {
        initSession();
        try {
            if (sosSession != null) {
                return sosSession.getAccessToken();
            } else {
                return null;
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }

    }

    public void touch() throws SessionNotExistException {
        initSession();
        try {
            if (sosSession != null) {
                sosSession.touch();
            }
        } catch (InvalidSessionException e1) {
            throw new SessionNotExistException(e1);
        }

    }

}
