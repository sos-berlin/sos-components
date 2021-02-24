package com.sos.joc.event.bean.history;

public class HistoryOrderUpdated extends HistoryOrderEvent {

    public HistoryOrderUpdated(String controllerId, String orderId, Long historyId, Long historyParentId) {
        super(HistoryOrderStarted.class.getSimpleName(), controllerId, orderId, historyId, historyParentId);
    }
}
