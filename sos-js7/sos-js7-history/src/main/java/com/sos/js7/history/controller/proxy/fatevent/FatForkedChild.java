package com.sos.js7.history.controller.proxy.fatevent;

import java.util.List;

public class FatForkedChild {

    private final String orderId;
    private final String branchId;
    private final List<Object> position;

    public FatForkedChild(String orderId, String branchId, List<Object> position) {
        this.orderId = orderId;
        this.branchId = branchId;
        this.position = position;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBranchId() {
        return branchId;
    }

    public List<Object> getPosition() {
        return position;
    }
}
