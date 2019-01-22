package com.sos.webservices.order.initiator.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.jobscheduler.db.general.SOSFilter;

public class FilterDaysPlanned extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDaysPlanned.class);
    private Integer day;
    private Integer dayFrom;
    private Integer dayTo;
    private Integer year;
    private String masterId;
    
    public Integer getDay() {
        return day;
    }
    
    public void setDay(Integer day) {
        this.day = day;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getMasterId() {
        return masterId;
    }
    
    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }
    
    public static Logger getLogger() {
        return LOGGER;
    }

    
    public Integer getDayFrom() {
        return dayFrom;
    }

    
    public void setDayFrom(Integer dayFrom) {
        this.dayFrom = dayFrom;
    }

    
    public Integer getDayTo() {
        return dayTo;
    }

    
    public void setDayTo(Integer dayTo) {
        this.dayTo = dayTo;
    }

}