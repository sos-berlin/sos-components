package com.sos.joc.db.security;

import com.sos.commons.hibernate.SOSHibernateFilter;
import com.sos.joc.model.security.IdentityServiceTypes;

public class IamIdentityServiceFilter extends SOSHibernateFilter {

    private Long id;
    private IdentityServiceTypes iamIdentityService; 
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

    
    public IdentityServiceTypes getIamIdentityService() {
        return iamIdentityService;
    }

    
    public void setIamIdentityService(IdentityServiceTypes iamIdentityService) {
        this.iamIdentityService = iamIdentityService;
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
