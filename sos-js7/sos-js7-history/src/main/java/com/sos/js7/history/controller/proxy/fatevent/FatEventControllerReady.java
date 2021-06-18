package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventControllerReady extends AFatEvent {

    private String id;
    private String timezone;
    private Long totalRunningTime;

    public FatEventControllerReady(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length == 3) {
            this.id = (String) objects[0];
            this.timezone = (String) objects[1];
            this.totalRunningTime = (Long) objects[2];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.ControllerReady;
    }

    public String getId() {
        return id;
    }

    public String getTimezone() {
        return timezone;
    }

    public Long getTotalRunningTime() {
        return totalRunningTime;
    }
}
