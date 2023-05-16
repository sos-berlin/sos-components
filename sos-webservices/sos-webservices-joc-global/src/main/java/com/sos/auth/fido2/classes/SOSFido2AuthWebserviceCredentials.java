package com.sos.auth.fido2.classes;

public class SOSFido2AuthWebserviceCredentials {

    private Long identityServiceId;
    private String account = "";
    private String challenge = "";
    private String signature = "";
    private String algorithm = "";

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

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }

    
    public String getAlgorithm() {
        return algorithm;
    }

    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

}
