package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

// without outcome
public final class FatEventOrderFinished extends AFatEventOrderProcessed {

    public FatEventOrderFinished(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderFinished;
    }

}
