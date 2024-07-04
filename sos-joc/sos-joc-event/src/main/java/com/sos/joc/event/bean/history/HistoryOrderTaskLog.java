package com.sos.joc.event.bean.history;

public class HistoryOrderTaskLog extends HistoryLogEvent {

    public HistoryOrderTaskLog(String key, Long historyOrderId, Long historyOrderStepId, String content, String sessionIdentifier) {
        super(key, historyOrderId, historyOrderStepId, content, sessionIdentifier);
    }

}
