package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventOrderAdded extends AFatEventOrder {

    private Date planned;

    public FatEventOrderAdded(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        super.set(objects);
        this.planned = (Date) objects[objects.length - 1];
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderAdded;
    }

    public Date getPlanned() {
        return planned;
    }
}
