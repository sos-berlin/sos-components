package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryOrderLogArrived extends HistoryLogEvent {

    public HistoryOrderLogArrived(Long historyId, Boolean complete, String sessionIdentifier) {
        super(HistoryOrderLogArrived.class.getSimpleName(), historyId, null, sessionIdentifier);
        putVariable("complete", complete);
    }

    @JsonIgnore
    public Boolean getComplete() {
        return (Boolean) getVariables().get("complete");
    }
}
