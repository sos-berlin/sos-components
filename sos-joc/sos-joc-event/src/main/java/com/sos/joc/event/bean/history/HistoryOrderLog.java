package com.sos.joc.event.bean.history;

public class HistoryOrderLog extends HistoryLogEvent {

    public HistoryOrderLog(String key, Long historyOrderId, String content) {
        super(key, historyOrderId, null, content);
    }
}
