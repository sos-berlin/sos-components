package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventAgentReady extends AFatEvent {

    private String id;
    private String uri;
    private String timezone;

    public FatEventAgentReady(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length == 3) {
            this.id = (String) objects[0];
            this.uri = (String) objects[1];
            this.timezone = (String) objects[2];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.AgentReady;
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public String getTimezone() {
        return timezone;
    }

}
