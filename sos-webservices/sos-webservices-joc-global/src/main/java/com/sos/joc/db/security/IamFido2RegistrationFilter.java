package com.sos.joc.db.security;

public class IamFido2RegistrationFilter extends SOSHibernateFilter {

    private Long id;
    private Long identityServiceId;
    private String accountName;
    private String token;
    private Boolean confirmed;
    private Boolean deferred;

    public IamFido2RegistrationFilter() {

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

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    
    public Boolean getDeferred() {
        return deferred;
    }

    
    public void setDeferred(Boolean deferred) {
        this.deferred = deferred;
    }

    
    public String getToken() {
        return token;
    }

    
    public void setToken(String token) {
        this.token = token;
    }

}
