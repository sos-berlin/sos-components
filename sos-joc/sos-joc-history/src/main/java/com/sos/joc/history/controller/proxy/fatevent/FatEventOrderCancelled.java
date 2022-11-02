package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderCancelled extends AFatEventOrderProcessed {

    public FatEventOrderCancelled() {
        super(null, null);
    }

    public FatEventOrderCancelled(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderCancelled;
    }

}
