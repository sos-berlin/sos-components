package com.sos.auth.fido.classes;

public class SOSFidoAuthWebserviceCredentials {

    private Long identityServiceId;
    private String account = "";
    private String signature = "";
    private String clientDataJson = "";
    private String authenticatorData;
    private String requestId;
    private String credentialId;

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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

    
    public String getRequestId() {
        return requestId;
    }

    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    
    public String getCredentialId() {
        return credentialId;
    }

    
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

 
}
