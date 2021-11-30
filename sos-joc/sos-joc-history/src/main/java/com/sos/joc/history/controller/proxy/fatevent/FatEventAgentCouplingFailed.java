package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventAgentCouplingFailed extends AFatEvent {

    private String id;
    private String message;

    public FatEventAgentCouplingFailed(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length > 1) {
            this.id = (String) objects[0];
            this.message = (String) objects[1];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.AgentCouplingFailed;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

}
