package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderSuspended extends AFatEventOrderProcessed {

    private boolean isStarted;

    public FatEventOrderSuspended(Long eventId, Date eventDatetime, boolean isStarted) {
        super(eventId, eventDatetime);
        this.isStarted = isStarted;
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderSuspended;
    }
}
