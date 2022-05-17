package com.sos.joc.db.security;

public class IamHistoryFilter extends SOSHibernateFilter {

    private Long id;
    private String accountName;
    private Boolean loginSuccess;
    private String loginDateTo;

    public IamHistoryFilter() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginDateTo() {
        return loginDateTo;
    }

    public void setLoginDateTo(String loginDateTo) {
        this.loginDateTo = loginDateTo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setLoginSuccess(Boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }

    public Boolean getLoginSuccess() {
        return loginSuccess;
    }

}
