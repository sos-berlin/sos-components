package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryOrderTaskLogArrived extends HistoryLogEvent {

    public HistoryOrderTaskLogArrived(Long historyOrderStepId, Boolean complete) {
        super(HistoryOrderTaskLogArrived.class.getSimpleName(), null, historyOrderStepId, "", false);
        putVariable("complete", complete);
    }
    
    @JsonIgnore
    public Boolean getComplete() {
        return (Boolean) getVariables().get("complete");
    }
}
