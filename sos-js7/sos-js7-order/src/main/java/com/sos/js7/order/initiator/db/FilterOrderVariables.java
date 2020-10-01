package com.sos.js7.order.initiator.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;

public class FilterOrderVariables extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterOrderVariables.class);
    private Long plannedOrderId;
    private String plannedOrderKey;

    public Long getPlannedOrderId() {
        return plannedOrderId;
    }

    public void setPlannedOrderId(Long plannedOrderId) {
        this.plannedOrderId = plannedOrderId;
    }

    public String getPlannedOrderKey() {
        return plannedOrderKey;
    }

    public void setPlannedOrderKey(String plannedOrderKey) {
        this.plannedOrderKey = plannedOrderKey;
    }

}