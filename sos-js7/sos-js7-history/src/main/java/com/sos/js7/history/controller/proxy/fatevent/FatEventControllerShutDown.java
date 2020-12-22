package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventControllerShutDown extends AFatEvent {

    private String id;

    public FatEventControllerShutDown(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length > 0) {
            this.id = (String) objects[0];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.ControllerShutDown;
    }

    public String getId() {
        return id;
    }

}
