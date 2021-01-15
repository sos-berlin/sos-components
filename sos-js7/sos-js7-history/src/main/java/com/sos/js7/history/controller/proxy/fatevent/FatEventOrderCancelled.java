package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderCancelled extends AFatEventOrderProcessed {

    private Boolean started;

    public FatEventOrderCancelled(Long eventId, Date eventDatetime, Boolean started) {
        super(eventId, eventDatetime);
        this.started = started;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderCancelled;
    }

    public Boolean isStarted() {
        return started;
    }
}
