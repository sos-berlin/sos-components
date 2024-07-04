package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryOrderTaskLogArrived extends HistoryLogEvent {

    public HistoryOrderTaskLogArrived(Long historyOrderStepId, Boolean complete, String sessionIdentifier) {
        super(HistoryOrderTaskLogArrived.class.getSimpleName(), null, historyOrderStepId, "", sessionIdentifier);
        putVariable("complete", complete);
    }

    @JsonIgnore
    public Boolean getComplete() {
        return (Boolean) getVariables().get("complete");
    }
}
