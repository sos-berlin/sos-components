package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderResumeMarked extends AFatEventOrderProcessed {

    public FatEventOrderResumeMarked(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderResumeMarked;
    }
}
