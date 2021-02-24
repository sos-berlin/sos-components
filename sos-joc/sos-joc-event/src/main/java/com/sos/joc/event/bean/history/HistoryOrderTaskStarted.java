package com.sos.joc.event.bean.history;

public class HistoryOrderTaskStarted extends HistoryTaskEvent {

    public HistoryOrderTaskStarted(String controllerId, String orderId, String jobName, Long historyId, Long historyOrderId) {
        super(HistoryOrderTaskStarted.class.getSimpleName(), controllerId, orderId, jobName, historyId,  historyOrderId);
    }
}
