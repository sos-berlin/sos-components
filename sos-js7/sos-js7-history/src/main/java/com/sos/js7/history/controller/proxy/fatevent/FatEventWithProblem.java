package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventEntry;
import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventWithProblem extends AFatEvent {

    private final HistoryEventEntry entry;
    private final Throwable error;

    public FatEventWithProblem(HistoryEventEntry entry, Throwable error) {
        this(entry, error, -1L, new Date());
    }

    public FatEventWithProblem(HistoryEventEntry entry, Throwable error, Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
        this.entry = entry;
        this.error = error;
    }

    @Override
    public void set(Object... objects) {
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.EventWithProblem;
    }

    public HistoryEventEntry getEntry() {
        return entry;
    }

    public Throwable getError() {
        return error;
    }

}
