package com.sos.joc.event.bean.history;


public class HistoryOrderLog extends HistoryLogEvent {

    public HistoryOrderLog(String key, Long historyOrderId, Object orderEntry) {
        super(key, historyOrderId, orderEntry);
    }
}
