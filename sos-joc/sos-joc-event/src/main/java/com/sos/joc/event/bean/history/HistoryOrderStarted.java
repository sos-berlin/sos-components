package com.sos.joc.event.bean.history;

public class HistoryOrderStarted extends HistoryOrderEvent {

    public HistoryOrderStarted(String controllerId, String orderId, String workflowName, String workflowVersion, Object payload) {
        super(HistoryOrderStarted.class.getSimpleName(), controllerId, orderId, workflowName, workflowVersion, payload);
    }
}
