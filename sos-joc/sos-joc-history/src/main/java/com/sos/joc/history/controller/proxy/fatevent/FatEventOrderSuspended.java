package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderSuspended extends AFatEventOrderProcessed {

    public FatEventOrderSuspended(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderSuspended;
    }
}
