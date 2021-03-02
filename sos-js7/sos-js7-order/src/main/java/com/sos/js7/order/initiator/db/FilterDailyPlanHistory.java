package com.sos.js7.order.initiator.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;

public class FilterDailyPlanHistory extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDailyPlanHistory.class);
    private List<String> listOfControllerIds;
    private Boolean submitted;
    private String orderId;
    private Date dailyPlanDate;
    private Date dailyPlanDateFrom;
    private Date dailyPlanDateTo;

    public void setControllerId(String controllerId) {
        this.addControllerId(controllerId);
    }

    public void addControllerId(String controllerId) {
        if (controllerId != null) {
            if (listOfControllerIds == null) {
                listOfControllerIds = new ArrayList<String>();
            }
            listOfControllerIds.add(controllerId);
        }
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

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public List<String> getListOfControllerIds() {
        return listOfControllerIds;
    }

    public void setListOfControllerIds(List<String> listOfControllerIds) {
        this.listOfControllerIds = listOfControllerIds;
    }

}