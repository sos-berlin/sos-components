package com.sos.js7.order.initiator.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;

public class FilterDailyPlanSubmissionHistory extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlanSubmissionHistory.class);
    private String controllerId;
    private Date dailyPlanDate;
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
    
    public Date getDailyPlanDate() {
        return dailyPlanDate;
    }
    
    public void setDailyPlanDate(Date dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }
    
 

}