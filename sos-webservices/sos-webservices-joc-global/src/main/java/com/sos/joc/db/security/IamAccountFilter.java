package com.sos.joc.db.security;

import java.util.Date;

import com.sos.joc.classes.JobSchedulerDate;

public class IamAccountFilter extends SOSHibernateFilter {

    private Long id;
    private Long identityServiceId;
    private Long roleId;
    private String accountName;
    private Boolean disabled;

    private String dateFrom;
    private String dateTo;
    private String timeZone;

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

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

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

}
