package com.sos.joc.event.bean.history;

public class HistoryOrderLog extends HistoryLogEvent {

    public HistoryOrderLog(String key, Long historyOrderId, Object orderEntry, String sessionIdentifier) {
        super(key, historyOrderId, orderEntry, sessionIdentifier);
    }
}
