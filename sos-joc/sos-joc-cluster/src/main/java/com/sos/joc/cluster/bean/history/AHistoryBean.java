package com.sos.joc.cluster.bean.history;

import java.io.Serializable;

import com.sos.controller.model.event.EventType;
import com.sos.joc.cluster.common.JocClusterUtil;

public abstract class AHistoryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EventType eventType;
    private final Long eventId;
    private final Long eventIdInSeconds;
    private final String controllerId;
    private final Long historyId;

    public AHistoryBean(EventType eventType, Long eventId, String controllerId, Long historyId) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.eventIdInSeconds = this.eventId == null ? null : JocClusterUtil.eventId2Instant(this.eventId).getEpochSecond();
        this.controllerId = controllerId;
        this.historyId = historyId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getEventIdInSeconds() {
        return eventIdInSeconds;
    }

    public String getControllerId() {
        return controllerId;
    }

    public Long getHistoryId() {
        return historyId;
    }
}
