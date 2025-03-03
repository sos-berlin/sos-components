package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventAgentCoupled extends AFatEvent {

    private String id;

    public FatEventAgentCoupled(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length > 0) {
            this.id = (String) objects[0];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.AgentCoupled;
    }

    public String getId() {
        return id;
    }

}
