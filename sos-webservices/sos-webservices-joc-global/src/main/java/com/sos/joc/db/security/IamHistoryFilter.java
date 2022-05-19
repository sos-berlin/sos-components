package com.sos.joc.db.security;

import java.util.Date;

public class IamHistoryFilter extends SOSHibernateFilter {

    private Long id;
    private String accountName;
    private Boolean loginSuccess;
    private Date loginDateTo;

    public IamHistoryFilter() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLoginDateTo() {
        return loginDateTo;
    }

    public void setLoginDateTo(Date loginDateTo) {
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
