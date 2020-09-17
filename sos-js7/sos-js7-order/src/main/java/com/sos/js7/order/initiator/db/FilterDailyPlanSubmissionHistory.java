package com.sos.js7.order.initiator.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;

public class FilterDailyPlanSubmissionHistory extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlanSubmissionHistory.class);

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

    private String controllerId;
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

}