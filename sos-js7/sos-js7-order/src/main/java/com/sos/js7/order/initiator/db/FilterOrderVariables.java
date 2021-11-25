package com.sos.js7.order.initiator.db;

import com.sos.joc.db.DBFilter;

public class FilterOrderVariables extends DBFilter {

    private Long plannedOrderId;
    private String variableName;
    private String orderId;

    public Long getPlannedOrderId() {
        return plannedOrderId;
    }

    public void setPlannedOrderId(Long val) {
        plannedOrderId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String val) {
        variableName = val;
    }

}