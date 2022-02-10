package com.sos.joc.db.security;

public class IamAccountFilter extends SOSHibernateFilter {

    private Long id;
    private Long identityServiceId;
    private Long roleId;
    private String accountName; 

    
    public IamAccountFilter() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    
    public String getAccountName() {
        return accountName;
    }

    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    
    public Long getRoleId() {
        return roleId;
    }

    
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

}
