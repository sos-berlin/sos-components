package com.sos.js7.history.controller.proxy.fatevent;

import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

public class FatForkedChild {

    private final String orderId;
    private final String branchIdOrName;
    private final String position;

    public FatForkedChild(String orderId, String branchIdOrName, Position position) {
        this.orderId = orderId;
        this.branchIdOrName = branchIdOrName;
        this.position = position == null ? null : position.asString();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBranchIdOrName() {
        return branchIdOrName;
    }

    public String getPosition() {
        return position;
    }
}
