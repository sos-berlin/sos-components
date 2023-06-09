package com.sos.auth.classes;

import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;

import jakarta.servlet.http.HttpServletRequest;

public class SOSLoginParameters {

    private HttpServletRequest request;
    private String basicAuthorization;
    private String firstAuthorization;
    private String clientCertCN;
    private String identityService;
    private String firstIdentityService;
    private String idToken;
    private String account;
    private String authenticatorData;
    private String clientDataJson;
    private String signature;
    private String credentialId;
    private String requestId;
    private SOSOpenIdWebserviceCredentials sosOpenIdWebserviceCredentials;

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

    public SOSOpenIdWebserviceCredentials getSOSOpenIdWebserviceCredentials() {
        return sosOpenIdWebserviceCredentials;
    }

    public void setSOSOpenIdWebserviceCredentials(SOSOpenIdWebserviceCredentials sosOpenIdWebserviceCredentials) {
        this.sosOpenIdWebserviceCredentials = sosOpenIdWebserviceCredentials;
    }

    public boolean isOIDCLogin() {
        return (getIdentityService() != null && !getIdentityService().equals("") && getIdToken() != null && !getIdToken().isEmpty());
    }

    public boolean isFIDO2Login() {
        return (getIdentityService() != null && !getIdentityService().equals("") && getSignature() != null && !getAuthenticatorData().equals(""));
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getClientDataJson() {
        return clientDataJson;
    }

    public void setClientDataJson(String clientDataJson) {
        this.clientDataJson = clientDataJson;
    }

    public String getAuthenticatorData() {
        return authenticatorData;
    }

    public void setAuthenticatorData(String authenticatorData) {
        this.authenticatorData = authenticatorData;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean authorizationHeaderIsEmpty() {
        return (basicAuthorization == null || basicAuthorization.isEmpty() || basicAuthorization.equals("Basic"));
    }

    
    public String getFirstAuthorization() {
        return firstAuthorization;
    }

    
    public void setFirstAuthorization(String firstAuthorization) {
        this.firstAuthorization = firstAuthorization;
    }

    
    public String getFirstIdentityService() {
        return firstIdentityService;
    }

    
    public void setFirstIdentityService(String firstIdentityService) {
        this.firstIdentityService = firstIdentityService;
    }

    public boolean isSecondPathOfTwoFactor() {
        return firstAuthorization != null && firstIdentityService != null && !firstAuthorization.isEmpty() && !firstIdentityService.isEmpty();
    }

}
