package com.sos.auth.sosintern.classes;

public class SOSInternAuthWebserviceCredentials {

    private Long identityServiceId;
    private String account = "";
    private String password = "";
    private String accessToken = "";
    
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    
    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    
    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }
 
}
