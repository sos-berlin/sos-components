package com.sos.webservices.order.initiator.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.jobscheduler.db.general.SOSFilter;

public class FilterOrderVariables extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterOrderVariables.class);
    private String plannedOrderId;
    
    public String getPlannedOrderId() {
        return plannedOrderId;
    }
    
    public void setPlannedOrderId(String plannedOrderId) {
        this.plannedOrderId = plannedOrderId;
    }
    

}