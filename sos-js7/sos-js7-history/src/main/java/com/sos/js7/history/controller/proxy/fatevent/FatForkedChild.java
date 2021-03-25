package com.sos.js7.history.controller.proxy.fatevent;

import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

public class FatForkedChild {

    private final String orderId;
    private final String branchId;
    private final String position;

    public FatForkedChild(String orderId, String branchId, Position position) {
        this.orderId = orderId;
        this.branchId = branchId;
        this.position = position == null ? null : position.asString();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getPosition() {
        return position;
    }
}
