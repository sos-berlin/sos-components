package com.sos.joc.event.bean.history;

public class HistoryOrderTaskTerminated extends HistoryTaskEvent {

    public HistoryOrderTaskTerminated(String controllerId, String orderId, String workflowName, String workflowVersion) {
        super(HistoryOrderTaskTerminated.class.getSimpleName(), controllerId, orderId, workflowName, workflowVersion);
    }
}
