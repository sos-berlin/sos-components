package com.sos.auth.sosintern.classes;

import com.sos.auth.classes.SOSIdentityService;

public class SOSInternAuthWebserviceCredentials {

    private SOSIdentityService identityService;
    private String account = "";
    
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public SOSIdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(SOSIdentityService identityService) {
        this.identityService = identityService;
    }

}
