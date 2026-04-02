package com.sos.joc.history.controller.proxy.fatevent;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.helper.IOriginalOrderIdProvider;

public class FatForkedChild implements IOriginalOrderIdProvider {

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

    @Override
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
