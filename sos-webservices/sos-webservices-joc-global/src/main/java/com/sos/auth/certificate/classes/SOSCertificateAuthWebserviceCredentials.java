package com.sos.auth.certificate.classes;

import jakarta.servlet.http.HttpServletRequest;

public class SOSCertificateAuthWebserviceCredentials {

    private Long identityServiceId;
    private String account = "";
    private HttpServletRequest httpRequest;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

}
