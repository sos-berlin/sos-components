package com.sos.joc.event.bean.history;

public class HistoryOrderTaskStarted extends HistoryTaskEvent {

    public HistoryOrderTaskStarted(String controllerId, String orderId, String workflowName, String workflowVersion) {
        super(HistoryOrderTaskStarted.class.getSimpleName(), controllerId, orderId, workflowName, workflowVersion);
    }
}
