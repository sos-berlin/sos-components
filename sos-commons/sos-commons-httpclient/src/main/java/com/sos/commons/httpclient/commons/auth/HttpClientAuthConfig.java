package com.sos.commons.httpclient.commons.auth;

import com.sos.commons.util.loggers.base.ISOSLogger;

public class HttpClientAuthConfig {

    private final HttpClientAuthMethod method;
    private final IHttpClientAuthStrategy strategy;

    public HttpClientAuthConfig(String username, String password) {
        this.method = HttpClientAuthMethod.BASIC;
        this.strategy = new HttpClientBasicAuthStrategy(username, password);
    }

    public HttpClientAuthConfig(ISOSLogger logger, String username, String password, String workstation, String domain) {
        this.method = HttpClientAuthMethod.NTLM;
        this.strategy = new HttpClientNtlmAuthStrategy(logger, username, password, workstation, domain);
    }

    public HttpClientAuthMethod getMethod() {
        return method;
    }

    public IHttpClientAuthStrategy getStrategy() {
        return strategy;
    }
}
