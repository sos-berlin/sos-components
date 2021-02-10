package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryOrderTerminated extends HistoryEvent {

    public HistoryOrderTerminated(String controllerId, String orderId, String state, Long historyId, Long historyParentId) {
        super(HistoryOrderTerminated.class.getSimpleName(), controllerId, null);
        putVariable("orderId", orderId);// HISTORY_ORDERS.ORDER_ID
        putVariable("stateText", state);// HISTORY_ORDERS.STATE as text
        putVariable("historyId", String.valueOf(historyId)); // HISTORY_ORDERS.ID
        putVariable("historyParentId", String.valueOf(historyParentId));// HISTORY_ORDERS.PARENT_ID
    }

    @JsonIgnore
    public String getOrderId() {
        return getVariables().get("orderId");
    }

    @JsonIgnore
    public String getStateText() {
        return getVariables().get("stateText");
    }

    @JsonIgnore
    public Long getHistoryId() {
        try {
            return Long.parseLong(getVariables().get("historyId"));
        } catch (Throwable e) {
            return null;
        }
    }

    // parentId = 0 - mainOrder
    @JsonIgnore
    public Long getHistoryParentId() {
        try {
            return Long.parseLong(getVariables().get("historyParentId"));
        } catch (Throwable e) {
            return null;
        }
    }
}
