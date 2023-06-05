package com.sos.joc.db.security;

public class IamFido2DevicesFilter extends SOSHibernateFilter {

    private Long id;
    private Long accountId;
    private String credentialId;
    private String origin;

    public IamFido2DevicesFilter() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    
    public String getOrigin() {
        return origin;
    }

    
    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
