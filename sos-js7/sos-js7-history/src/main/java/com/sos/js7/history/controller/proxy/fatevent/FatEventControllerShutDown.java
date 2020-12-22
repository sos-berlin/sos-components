package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventControllerShutDown extends AFatEvent {

    private String controllerId;

    public FatEventControllerShutDown(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length >= 1) {
            this.controllerId = (String) objects[0];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.ControllerShutDown;
    }

    public String getControllerId() {
        return controllerId;
    }

}
