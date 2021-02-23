package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryOrderTaskStarted extends HistoryEvent {

    public HistoryOrderTaskStarted(String controllerId, String orderId, Long historyId, Long historyOrderId) {
        super(HistoryOrderTaskStarted.class.getSimpleName(), controllerId, null);
        putVariable("orderId", orderId); // HISTORY_ORDER_STEPS.ORDER_ID
        putVariable("historyId", String.valueOf(historyId)); // HISTORY_ORDER_STEPS.ID
        putVariable("historyOrderId", String.valueOf(historyOrderId));// HISTORY_ORDER_STEPS.HO_ID
    }

    @JsonIgnore
    public String getOrderId() {
        return getVariables().get("orderId");
    }

    @JsonIgnore
    public Long getHistoryId() {
        try {
            return Long.parseLong(getVariables().get("historyId"));
        } catch (Throwable e) {
            return null;
        }
    }

    @JsonIgnore
    public Long getHistoryOrderId() {
        try {
            return Long.parseLong(getVariables().get("historyOrderId"));
        } catch (Throwable e) {
            return null;
        }
    }
}
