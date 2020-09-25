package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventControllerReady extends AFatEvent {

    private String controllerId;
    private String timezone;

    public FatEventControllerReady(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length == 2) {
            this.controllerId = (String) objects[0];
            this.timezone = (String) objects[1];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.ControllerReady;
    }

    public String getControllerId() {
        return controllerId;
    }

    public String getTimezone() {
        return timezone;
    }

}
