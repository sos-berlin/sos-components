package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryOrderTerminated extends HistoryOrderEvent {

    public HistoryOrderTerminated(String controllerId, String orderId, String state, Long historyId, Long historyParentId) {
        super(HistoryOrderStarted.class.getSimpleName(), controllerId, orderId, historyId, historyParentId);
        putVariable("stateText", state);// HISTORY_ORDERS.STATE as text
    }

    @JsonIgnore
    public String getStateText() {
        return getVariables().get("stateText");
    }
}
