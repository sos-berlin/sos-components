package com.sos.auth.classes;

import jakarta.servlet.http.HttpServletRequest;

public class SOSLoginParameters {

    private HttpServletRequest request;
    private String basicAuthorization;
    private String clientCertCN;
    private String identityService;
    private String accessToken;
    private String account;

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getBasicAuthorization() {
        return basicAuthorization;
    }

    public void setBasicAuthorization(String basicAuthorization) {
        this.basicAuthorization = basicAuthorization;
    }

    public String getClientCertCN() {
        return clientCertCN;
    }

    public void setClientCertCN(String clientCertCN) {
        this.clientCertCN = clientCertCN;
    }

    public String getIdentityService() {
        return identityService;
    }

    public void setIdentityService(String identityService) {
        this.identityService = identityService;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}
