package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventAgentSubagentDedicated extends AFatEvent {

    public FatEventAgentSubagentDedicated(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {

    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.AgentSubagentDedicated;
    }

}
