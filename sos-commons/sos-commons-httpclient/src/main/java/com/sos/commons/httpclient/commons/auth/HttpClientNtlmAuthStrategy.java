package com.sos.commons.httpclient.commons.auth;

import java.net.Authenticator;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;

/** TODO - not implemented yet - NTLM Auth with Java requires external lib like SMBJ or JCIFS */
public class HttpClientNtlmAuthStrategy implements IHttpClientAuthStrategy {

    @SuppressWarnings("unused")
    private final String password;
    @SuppressWarnings("unused")
    private final String workstation;
    private final boolean hasAuthenticator;

    @SuppressWarnings("unused")
    private String username;
    @SuppressWarnings("unused")
    private String domain;

    private String token;
    private Map<String, String> authHeaders;

    public HttpClientNtlmAuthStrategy(ISOSLogger logger, String username, String password, String workstation, String domain) {
        this.username = username;
        this.password = password;
        this.workstation = getWorkstation(logger, workstation);
        this.hasAuthenticator = false;

        if (SOSString.isEmpty(domain) && username.contains("\\")) {
            String[] parts = username.split("\\\\");
            if (parts.length > 1) {
                this.domain = parts[0];
                this.username = parts[1];
            } else {
                this.domain = "";
            }
        } else {
            this.domain = domain;
        }
        // TODO
        this.token = null;
    }

    @Override
    public boolean hasAuthenticator() {
        return hasAuthenticator;
    }

    @Override
    public Authenticator toAuthenticator() {
        // NTLM Auth with Java requires external lib like SMBJ or JCIFS
        return null;
    }

    @Override
    public Map<String, String> getAuthHeaders() {
        // setNtlmHeader();
        return authHeaders;
    }

    public void setAuthHeaders(Map<String, String> headers) {
        authHeaders = headers;
    }

    @SuppressWarnings("unused")
    private void setNtlmHeader() {
        if (authHeaders == null) {
            authHeaders = new LinkedHashMap<>();
        }
        if (!authHeaders.containsKey(HttpUtils.HEADER_AUTHORIZATION)) {
            authHeaders.put(HttpUtils.HEADER_AUTHORIZATION, "NTLM " + token);
        }
    }

    private String getWorkstation(ISOSLogger logger, String workstation) {
        if (SOSString.isEmpty(workstation)) {
            try {
                return SOSShell.getLocalHostName();
            } catch (UnknownHostException e) {
                logger.warn("[HttpClientNtlmAuthStrategy][workstation][getLocalHostName]" + e.toString());
            }
        }
        return workstation;
    }
}
