package com.sos.joc.db.security;

public class IamFidoRegistrationFilter extends SOSHibernateFilter {

    private Long id;
    private Long identityServiceId;
    private String accountName;
    private String origin;
    private String email;
    private String token;
    private Boolean completed;
    private Boolean confirmed;
    private Boolean deferred;

    public IamFidoRegistrationFilter() {

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    
    public String getOrigin() {
        return origin;
    }

    
    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
