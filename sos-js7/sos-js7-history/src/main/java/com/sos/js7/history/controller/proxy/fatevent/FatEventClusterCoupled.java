package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventClusterCoupled extends AFatEvent {

    private String id;
    private String activeId;
    private boolean isPrimary;

    public FatEventClusterCoupled(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length == 3) {
            this.id = (String) objects[0];
            this.activeId = (String) objects[1];
            this.isPrimary = (Boolean) objects[2];
        }
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.ClusterCoupled;
    }

    public String getId() {
        return id;
    }

    public String getActiveId() {
        return activeId;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}
