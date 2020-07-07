package com.sos.js7.order.initiator.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.joc.db.SOSFilter;

public class FilterDailyPlan extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlan.class);
    private Long planId;
    
    public Long getPlanId() {
        return planId;
    }

    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    private Integer day;
    private Integer dayFrom;
    private Integer dayTo;
    private Integer year;
    private Integer yearFrom;
    private Integer yearTo;
    private String jobschedulerId;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(Integer yearFrom) {
        this.yearFrom = yearFrom;
    }

    public Integer getYearTo() {
        return yearTo;
    }

    public void setYearTo(Integer yearTo) {
        this.yearTo = yearTo;
    }

    public String getJobschedulerId() {
        return jobschedulerId;
    }

    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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