package com.sos.joc.event.bean.history;

public class HistoryOrderTaskTerminated extends HistoryTaskEvent {

    public HistoryOrderTaskTerminated(String controllerId, String orderId, String workflowName, String workflowVersion, Object payload) {
        super(HistoryOrderTaskTerminated.class.getSimpleName(), controllerId, orderId, workflowName, workflowVersion, payload);
    }
}
