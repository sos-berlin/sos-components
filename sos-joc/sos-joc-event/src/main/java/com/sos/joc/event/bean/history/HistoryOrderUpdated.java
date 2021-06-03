package com.sos.joc.event.bean.history;

public class HistoryOrderUpdated extends HistoryOrderEvent {

    public HistoryOrderUpdated(String controllerId, String orderId, String workflowName, String workflowVersion, Object payload) {
        super(HistoryOrderUpdated.class.getSimpleName(), controllerId, orderId, workflowName, workflowVersion, payload);
    }
}
