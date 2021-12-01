package com.sos.auth.classes;

import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSIdentityService {

    private String identityServiceName;
    private IdentityServiceTypes identyServiceType;

    public SOSIdentityService(String identityServiceName, IdentityServiceTypes identyServiceType) {
        super();
        this.identityServiceName = identityServiceName;
        this.identyServiceType = identyServiceType;
    }

    public String getIdentityServiceName() {
        return identityServiceName;
    }

    public IdentityServiceTypes getIdentyServiceType() {
        return identyServiceType;
    }

    
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    
    public void setIdentyServiceType(IdentityServiceTypes identyServiceType) {
        this.identyServiceType = identyServiceType;
    }

}
