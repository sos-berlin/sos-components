package com.sos.joc.event.bean.history;

public class HistoryOrderStarted extends HistoryEvent {

    public HistoryOrderStarted(String controllerId, String orderId, Long historyId, Long historyParentId) {
        super(HistoryOrderStarted.class.getSimpleName(), controllerId, null);
        putVariable("orderId", orderId);// HISTORY_ORDERS.ORDER_ID
        putVariable("historyId", String.valueOf(historyId));// HISTORY_ORDERS.ID
        putVariable("historyParentId", String.valueOf(historyParentId));// HISTORY_ORDERS.PARENT_ID
    }

    public String getOrderId() {
        return getVariables().get("orderId");
    }

    public Long getHistoryId() {
        try {
            return Long.parseLong(getVariables().get("historyId"));
        } catch (Throwable e) {
            return null;
        }
    }

    // parentId = 0 - mainOrder
    public Long getHistoryParentId() {
        try {
            return Long.parseLong(getVariables().get("historyParentId"));
        } catch (Throwable e) {
            return null;
        }
    }
}
