package com.sos.auth.interfaces;

import java.io.Serializable;

import com.sos.auth.keycloak.classes.SOSKeycloakAccountAccessToken;
import com.sos.auth.openid.classes.SOSOpenIdAccountAccessToken;

public interface ISOSSession {

    public void setAttribute(String key, Object value);

    public Object getAttribute(Object key);

    public void removeAttribute(Object key);

    public void stop();

    public boolean renew();

    public Long getTimeout();

    public Long getStartSession();

    public Serializable getAccessToken();

    public SOSKeycloakAccountAccessToken getSOSKeycloakAccountAccessToken();

    public SOSOpenIdAccountAccessToken getSOSOpenIdAccountAccessToken();

    public void touch();

}
