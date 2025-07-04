package com.sos.auth.classes;

import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;

import jakarta.servlet.http.HttpServletRequest;

public class SOSLoginParameters {

    private HttpServletRequest request;
    private String basicAuthorization;
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
    private String openidConfiguration;
    private String lockerKey;
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

    public String getMainIdentitySercic() {
        if (isSecondPathOfTwoFactor()) {
            return getFirstIdentityService();
        } else {
            return getIdentityService();
        }
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
        return (getIdentityService() != null && !getIdentityService().isEmpty() && getIdToken() != null && !getIdToken().isEmpty());
    }

    public boolean isFIDO2Login() {
        return (getIdentityService() != null && !getIdentityService().isEmpty() && getSignature() != null && !getAuthenticatorData().isEmpty());
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

    public boolean basicAuthorizationHeaderIsEmpty() {
        return (basicAuthorization == null || basicAuthorization.isEmpty() || basicAuthorization.equals("Basic"));
    }

    public String getFirstIdentityService() {
        return firstIdentityService;
    }

    public void setFirstIdentityService(String firstIdentityService) {
        this.firstIdentityService = firstIdentityService;
    }

    public boolean isSecondPathOfTwoFactor() {
        return firstIdentityService != null && !firstIdentityService.isEmpty();
    }

    public boolean isFirstPathOfTwoFactor() {
        return !isSecondPathOfTwoFactor();
    }

    public String getOpenidConfiguration() {
        return openidConfiguration;
    }

    public void setOpenidConfiguration(String openidConfiguration) {
        this.openidConfiguration = openidConfiguration;
    }

    public SOSOpenIdWebserviceCredentials getSosOpenIdWebserviceCredentials() {
        return sosOpenIdWebserviceCredentials;
    }

    public void setSosOpenIdWebserviceCredentials(SOSOpenIdWebserviceCredentials sosOpenIdWebserviceCredentials) {
        this.sosOpenIdWebserviceCredentials = sosOpenIdWebserviceCredentials;
    }
    
    public String getLockerKey() {
        return lockerKey;
    }

    public void setLockerKey(String lockerKey) {
        this.lockerKey = lockerKey;
    }
}
