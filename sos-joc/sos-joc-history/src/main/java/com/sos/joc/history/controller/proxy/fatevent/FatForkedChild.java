package com.sos.joc.history.controller.proxy.fatevent;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

public class FatForkedChild {

    private final String orderId;
    private final String branchIdOrName;
    private final FatPosition position;

    public FatForkedChild(String orderId, String branchIdOrName, Position position) {
        this.orderId = orderId;
        this.branchIdOrName = branchIdOrName;
        if (position == null) {
            this.position = null;
        } else {
            this.position = new FatPosition(position);
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBranchIdOrName() {
        return branchIdOrName;
    }

    public FatPosition getPosition() {
        return position;
    }

}
