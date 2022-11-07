package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderSuspensionMarked extends AFatEventOrderProcessed {

    private boolean isStarted;

    public FatEventOrderSuspensionMarked(Long eventId, Date eventDatetime, boolean isStarted) {
        super(eventId, eventDatetime);
        this.isStarted = isStarted;
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderSuspensionMarked;
    }
}
