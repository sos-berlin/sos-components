package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

// with outcome/problem
public final class FatEventOrderBroken extends AFatEventOrderProcessed {

    public FatEventOrderBroken(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderBroken;
    }

}
