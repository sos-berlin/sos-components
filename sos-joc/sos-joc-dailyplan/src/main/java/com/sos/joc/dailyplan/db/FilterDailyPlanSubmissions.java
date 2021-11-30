package com.sos.joc.dailyplan.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.DBFilter;

public class FilterDailyPlanSubmissions extends DBFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlanSubmissions.class);

    private String controllerId;
    private Date dateFor;
    private Date dateFrom;
    private Date dateTo;
    private String userAccount;

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public Date getDateFor() {
        return dateFor;
    }

    public void setDateFor(Date dateFor) {
        this.dateFor = dateFor;
    }
}