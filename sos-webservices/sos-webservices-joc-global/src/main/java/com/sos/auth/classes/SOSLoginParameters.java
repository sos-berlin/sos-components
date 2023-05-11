package com.sos.auth.classes;

import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;

import jakarta.servlet.http.HttpServletRequest;

public class SOSLoginParameters {

    private HttpServletRequest request;
    private String basicAuthorization;
    private String clientCertCN;
    private String identityService;
    private String idToken;
    private String account;
    private String fido2Challenge;
    private SOSOpenIdWebserviceCredentials webserviceCredentials;

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public SOSOpenIdWebserviceCredentials getWebserviceCredentials() {
        return webserviceCredentials;
    }

    public void setWebserviceCredentials(SOSOpenIdWebserviceCredentials webserviceCredentials) {
        this.webserviceCredentials = webserviceCredentials;
    }

    public boolean isOIDCLogin() {
        return (getIdentityService() != null && !getIdentityService().equals("") && getIdToken() != null && !getIdToken().isEmpty());
    }

    public boolean isFIDO2Login() {
        return (getIdentityService() != null && !getIdentityService().equals("") && getFido2Challenge() != null && !getFido2Challenge().equals(""));
    }

    public String getFido2Challenge() {
        return fido2Challenge;
    }

    public void setFido2Challenge(String fido2Challenge) {
        this.fido2Challenge = fido2Challenge;
    }

}
