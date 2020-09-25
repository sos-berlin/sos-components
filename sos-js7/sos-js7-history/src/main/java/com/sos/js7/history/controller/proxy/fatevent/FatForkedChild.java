package com.sos.js7.history.controller.proxy.fatevent;

public class FatForkedChild {

    private final String orderId;
    private final String branchId;

    public FatForkedChild(String orderId, String branchId) {
        this.orderId = orderId;
        this.branchId = branchId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBranchId() {
        return branchId;
    }

}
