package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public class FatEventEmpty extends AFatEvent {

    public FatEventEmpty() {
        super(null, null);
    }

    public FatEventEmpty(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void set(Object... objects) {

    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.Empty;
    }

}
