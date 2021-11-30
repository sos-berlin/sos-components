package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderResumed extends AFatEventOrderProcessed {

    public FatEventOrderResumed(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderResumed;
    }
}
