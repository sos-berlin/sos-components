package com.sos.js7.order.initiator.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.DBFilter;

public class FilterOrderVariables extends DBFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterOrderVariables.class);
    private Long plannedOrderId;
    private String variableName;
    private String orderId;

    public Long getPlannedOrderId() {
        return plannedOrderId;
    }

    public void setPlannedOrderId(Long plannedOrderId) {
        this.plannedOrderId = plannedOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    
    public String getVariableName() {
        return variableName;
    }

    
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

}