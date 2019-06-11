package com.sos.jobscheduler.event.master.fatevent.bean;

import java.util.LinkedHashMap;

import com.sos.commons.util.SOSString;

public class OrderForkedChild {

    private String branchId;
    private String orderId;
    private LinkedHashMap<String, String> arguments;

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String val) {
        branchId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public LinkedHashMap<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(LinkedHashMap<String, String> val) {
        arguments = val;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }

}
