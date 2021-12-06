package com.sos.auth.classes;

import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSIdentityService {

    private Long identityServiceId;
    private String identityServiceName;
    private IdentityServiceTypes identyServiceType;

    public SOSIdentityService(Long identityServiceId, String identityServiceName, IdentityServiceTypes identyServiceType) {
        super();
        this.identityServiceName = identityServiceName;
        this.identyServiceType = identyServiceType;
        this.identityServiceId = identityServiceId;
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

    
    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    
    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

}
