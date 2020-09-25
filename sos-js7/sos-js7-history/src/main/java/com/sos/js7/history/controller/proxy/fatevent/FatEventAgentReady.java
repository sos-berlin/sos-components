package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventAgentReady extends AFatEvent {

    private String path;
    private String uri;
    private String timezone;

    public FatEventAgentReady(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length == 3) {
            this.path = (String) objects[0];
            this.uri = (String) objects[1];
            this.timezone = (String) objects[2];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.AgentReady;
    }

    public String getPath() {
        return path;
    }

    public String getUri() {
        return uri;
    }

    public String getTimezone() {
        return timezone;
    }

}
