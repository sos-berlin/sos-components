package com.sos.js7.order.initiator.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;

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