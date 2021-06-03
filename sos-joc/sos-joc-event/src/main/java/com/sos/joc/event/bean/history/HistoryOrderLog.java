package com.sos.joc.event.bean.history;

public class HistoryOrderLog extends HistoryLogEvent {

    public HistoryOrderLog(String key, Long historyOrderId, String content, boolean newline) {
        super(key, historyOrderId, null, content, newline);
    }
}
