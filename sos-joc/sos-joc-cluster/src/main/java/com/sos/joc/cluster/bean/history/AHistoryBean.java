package com.sos.joc.cluster.bean.history;

import java.io.Serializable;

import com.sos.controller.model.event.EventType;

public abstract class AHistoryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EventType eventType;
    private final long eventId;
    private final String controllerId;
    private final Long historyId;

    public AHistoryBean(EventType eventType, Long eventId, String controllerId, Long historyId) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.controllerId = controllerId;
        this.historyId = historyId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getControllerId() {
        return controllerId;
    }

    public Long getHistoryId() {
        return historyId;
    }
}
