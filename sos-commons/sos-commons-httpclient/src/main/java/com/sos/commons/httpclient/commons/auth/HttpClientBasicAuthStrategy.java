package com.sos.commons.httpclient.commons.auth;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;

public class HttpClientBasicAuthStrategy implements IHttpClientAuthStrategy {

    private final String username;
    private final String password;
    private final boolean hasAuthenticator;

    private Map<String, String> authHeaders;

    public HttpClientBasicAuthStrategy(String username, String password) {
        this.username = username;
        this.password = password;
        this.hasAuthenticator = username != null && password != null;
    }

    @Override
    public boolean hasAuthenticator() {
        return hasAuthenticator;
    }

    @Override
    public Authenticator toAuthenticator() {
        if (!hasAuthenticator) {
            return null;
        }

        return new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        };
    }

    @Override
    public Map<String, String> getAuthHeaders() {
        return authHeaders;
    }

    public void setAuthHeaders(Map<String, String> headers) {
        authHeaders = headers;
    }
}
