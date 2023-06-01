package com.sos.auth.fido2.classes;

public class SOSFido2AuthWebserviceCredentials {

    private Long identityServiceId;
    private String account = "";
    private String signature = "";
    private String clientDataJson = "";
    private String authenticatorData;
    private Long requestId;

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

    
    public Long getRequestId() {
        return requestId;
    }

    
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

}
