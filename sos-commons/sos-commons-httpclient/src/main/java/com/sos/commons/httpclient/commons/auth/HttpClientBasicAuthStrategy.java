package com.sos.commons.httpclient.commons.auth;

import java.net.Authenticator;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sos.commons.util.http.HttpUtils;

public class HttpClientBasicAuthStrategy implements IHttpClientAuthStrategy {

    // Header order does not matter in HTTP, but LinkedHashMap preserves insertion order
    // for consistent debug output instead of random order
    private Map<String, String> authHeaders = new LinkedHashMap<>();

    public HttpClientBasicAuthStrategy(String username, String password) {
        if (username != null && password != null) {
            authHeaders.put(HttpUtils.HEADER_AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        }
    }

    @Override
    public boolean hasAuthenticator() {
        return false;
    }

    @Override
    // We do NOT set an Authenticator here, because:
    // 1) Many servers don't send a proper WWW-Authenticate header, causing Java's Authenticator to fail.
    // 2) The built-in Authenticator tries up to 3 authentication attempts per request and throws exceptions on failure.
    // 3) Redirects may not preserve authentication when using Authenticator, leading to unexpected 401 errors.
    // By manually adding the Authorization header, we have full control over authentication.
    public Authenticator toAuthenticator() {
        // return new Authenticator() {
        //
        // @Override
        // protected PasswordAuthentication getPasswordAuthentication() {
        // return new PasswordAuthentication(username, password.toCharArray());
        // }
        // };
        return null;
    }

    @Override
    public Map<String, String> getAuthHeaders() {
        return authHeaders;
    }

    public void setAuthHeaders(Map<String, String> headers) {
        authHeaders = headers;
    }
}
