package com.sos.commons.httpclient.commons.auth;

import java.net.Authenticator;
import java.util.Map;

public interface IHttpClientAuthStrategy {

    public boolean hasAuthenticator();

    public Map<String,String> getAuthHeaders();
    
    public Authenticator toAuthenticator();

}
