package com.sos.joc.db.security;

import java.util.Date;

import com.sos.joc.classes.JobSchedulerDate;

public class IamHistoryFilter extends SOSHibernateFilter {

    private Long id;
    private String accountName;
    private Boolean loginSuccess;
    private String dateFrom;
    private String dateTo;
    private String timeZone;

    public Date getDateFrom() {
        return JobSchedulerDate.getDateFrom(this.dateFrom, this.getTimeZone());
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return JobSchedulerDate.getDateTo(this.dateTo, this.getTimeZone());
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public IamHistoryFilter() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
