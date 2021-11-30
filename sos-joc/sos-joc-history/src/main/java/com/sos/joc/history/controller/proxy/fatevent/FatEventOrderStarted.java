package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventOrderStarted extends AFatEventOrder {

    private Date scheduledFor;

    public FatEventOrderStarted(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        super.set(objects);
        this.scheduledFor = (Date) objects[objects.length - 1];
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderStarted;
    }

    public Date getScheduledFor() {
        return scheduledFor;
    }
}
