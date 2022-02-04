package com.sos.auth.sosintern.classes;

public class SOSInternAuthWebserviceCredentials {

    private Long identityServiceId;
    private String account = "";
    
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

}
