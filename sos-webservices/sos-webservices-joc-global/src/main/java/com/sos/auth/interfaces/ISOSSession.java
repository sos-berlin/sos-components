package com.sos.auth.interfaces;

import java.io.Serializable;

public interface ISOSSession {

    public void setAttribute(String key, Object value);

    public Object getAttribute(Object key);

    public void removeAttribute(Object key);

    public void stop();

    public Long getTimeout();

    public Serializable getAccessToken();

    public void touch();

}
