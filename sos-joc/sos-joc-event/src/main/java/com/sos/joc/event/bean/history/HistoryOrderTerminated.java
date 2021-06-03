package com.sos.joc.event.bean.history;

public class HistoryOrderTerminated extends HistoryOrderEvent {

    public HistoryOrderTerminated(String controllerId, String orderId, String workflowName, String workflowVersion, Object payload) {
        super(HistoryOrderTerminated.class.getSimpleName(), controllerId, orderId, workflowName, workflowVersion, payload);
    }
}
