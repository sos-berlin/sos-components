package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

// with outcome
public final class FatEventOrderOutcomeAdded extends AFatEventOrderProcessed {

    public FatEventOrderOutcomeAdded(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderOutcomeAdded;
    }

}
