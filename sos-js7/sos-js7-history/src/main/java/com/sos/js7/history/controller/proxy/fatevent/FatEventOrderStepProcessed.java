package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

// with outcome
public final class FatEventOrderStepProcessed extends AFatEventOrderProcessed {

    public FatEventOrderStepProcessed(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderStepProcessed;
    }
}
