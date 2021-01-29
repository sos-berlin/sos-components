package com.sos.js7.order.initiator.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;

public class FilterDailyPlanHistory extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlanHistory.class);
    private String controllerId;
    private String category;
    private String orderId;
    private Date dailyPlanDate;
    private Date dailyPlanDateFrom;
    private Date dailyPlanDateTo;
    
    public String getControllerId() {
        return controllerId;
    }
    
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Date getDailyPlanDate() {
        return dailyPlanDate;
    }
    
    public void setDailyPlanDate(Date dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    
    public Date getDailyPlanDateFrom() {
        return dailyPlanDateFrom;
    }

    
    public void setDailyPlanDateFrom(Date dailyPlanDateFrom) {
        this.dailyPlanDateFrom = dailyPlanDateFrom;
    }

    
    public Date getDailyPlanDateTo() {
        return dailyPlanDateTo;
    }

    
    public void setDailyPlanDateTo(Date dailyPlanDateTo) {
        this.dailyPlanDateTo = dailyPlanDateTo;
    }

    
    public String getOrderId() {
        return orderId;
    }

    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
        
   
    
}