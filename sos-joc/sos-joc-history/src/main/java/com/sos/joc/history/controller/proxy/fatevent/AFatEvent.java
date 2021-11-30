package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public abstract class AFatEvent {

    private final Long eventId;
    private final Date eventDatetime;

    public AFatEvent(Long eventId, Date eventDatetime) {
        this.eventId = eventId;
        this.eventDatetime = eventDatetime;
    }

    public Long getEventId() {
        return eventId;
    }

    public Date getEventDatetime() {
        return eventDatetime;
    }

    public abstract void set(Object... objects);

    public abstract HistoryEventType getType();

}
