package com.sos.joc.event.bean.history;

public class HistoryOrderTaskLogFirstStderr extends HistoryEvent {

    public HistoryOrderTaskLogFirstStderr(String controllerId, Object payload) {
        super(HistoryOrderTaskLogFirstStderr.class.getSimpleName(), controllerId, null, payload);
    }

}
