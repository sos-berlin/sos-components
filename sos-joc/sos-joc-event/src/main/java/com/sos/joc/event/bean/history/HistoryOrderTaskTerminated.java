package com.sos.joc.event.bean.history;

public class HistoryOrderTaskTerminated extends HistoryEvent {

    public HistoryOrderTaskTerminated(String controllerId, String orderId, String severity, Long historyId, Long historyOrderId) {
        super(HistoryOrderTaskTerminated.class.getSimpleName(), controllerId, null);
        putVariable("orderId", orderId); // HISTORY_ORDER_STEPS.ORDER_ID
        putVariable("severityText", severity); // HISTORY_ORDER_STEPS.SEVERITY as text
        putVariable("historyId", String.valueOf(historyId)); // HISTORY_ORDER_STEPS.ID
        putVariable("historyOrderId", String.valueOf(historyOrderId));// HISTORY_ORDER_STEPS.HO_ID
    }

    public String getOrderId() {
        return getVariables().get("orderId");
    }

    public String getSeverityText() {
        return getVariables().get("severityText");
    }

    public Long getHistoryId() {
        try {
            return Long.parseLong(getVariables().get("historyId"));
        } catch (Throwable e) {
            return null;
        }
    }

    public Long getHistoryOrderId() {
        try {
            return Long.parseLong(getVariables().get("historyOrderId"));
        } catch (Throwable e) {
            return null;
        }
    }
}
