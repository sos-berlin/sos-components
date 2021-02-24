package com.sos.joc.event.bean.history;

public class HistoryOrderStarted extends HistoryOrderEvent {

    public HistoryOrderStarted(String controllerId, String orderId, Long historyId, Long historyParentId) {
        super(HistoryOrderStarted.class.getSimpleName(), controllerId, orderId, historyId, historyParentId);
    }
}
