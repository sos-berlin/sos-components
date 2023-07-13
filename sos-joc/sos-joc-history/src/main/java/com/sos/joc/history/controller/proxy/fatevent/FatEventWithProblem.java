package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventEntry;
import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventWithProblem extends AFatEvent {

    private final Throwable error;
    private final HistoryEventType eventType;
    private final String orderId;

    public FatEventWithProblem(HistoryEventEntry entry, String orderId, Throwable error) {
        this(entry, orderId, error, -1L, new Date());
    }

    public FatEventWithProblem(HistoryEventEntry entry, String orderId, Throwable error, Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
        this.error = error;
        this.eventType = entry == null ? null : entry.getEventType();
        this.orderId = orderId;
    }

    @Override
    public void set(Object... objects) {
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.EventWithProblem;
    }

    public HistoryEventType getEventType() {
        return eventType;
    }

    public String getError() {
        return error == null ? "" : error.toString();
    }

    public String getOrderId() {
        return orderId;
    }
}
