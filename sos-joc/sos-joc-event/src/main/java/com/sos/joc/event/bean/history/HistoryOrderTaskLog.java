package com.sos.joc.event.bean.history;

public class HistoryOrderTaskLog extends HistoryLogEvent {

    public HistoryOrderTaskLog(String key, Long historyOrderId, Long historyOrderStepId, String content, boolean newline) {
        super(key, historyOrderId, historyOrderStepId, content, newline);
    }
}
