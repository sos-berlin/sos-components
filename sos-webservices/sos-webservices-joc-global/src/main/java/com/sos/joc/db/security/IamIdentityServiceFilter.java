package com.sos.joc.db.security;

import com.sos.joc.model.security.idendityservice.IdentityServiceTypes;

public class IamIdentityServiceFilter extends SOSHibernateFilter {

    private Long id;
    private IdentityServiceTypes iamIdentityServiceType; 
    private String identityServiceName;
    private Boolean disabled;
    private Boolean required;

    public IamIdentityServiceFilter() {
        super.setOrderCriteria("ordering");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
    public Boolean getDisabled() {
        return disabled;
    }

    
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    
    public IdentityServiceTypes getIamIdentityServiceType() {
        return iamIdentityServiceType;
    }

    
    public void setIamIdentityServiceType(IdentityServiceTypes iamIdentityServiceType) {
        this.iamIdentityServiceType = iamIdentityServiceType;
    }

    
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    
    public Boolean getRequired() {
        return required;
    }

    
    public void setRequired(Boolean required) {
        this.required = required;
    }

}
