package com.sos.jobscheduler.event.master.fatevent.bean;

import java.util.LinkedHashMap;

import com.sos.commons.util.SOSString;

public class OrderForkedChild {

    private String branchId;
    private String orderId;
    private LinkedHashMap<String, String> variables;

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

    public LinkedHashMap<String, String> getVariables() {
        return variables;
    }

    public void setVariables(LinkedHashMap<String, String> val) {
        variables = val;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }

}
