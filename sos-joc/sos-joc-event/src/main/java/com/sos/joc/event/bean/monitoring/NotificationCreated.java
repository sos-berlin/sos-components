package com.sos.joc.event.bean.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NotificationCreated extends MonitoringEvent {

    public NotificationCreated(String controllerId, Long notificationId) {
        super(NotificationCreated.class.getSimpleName(), controllerId, null);
        putVariable("notificationId", notificationId);
    }

    @JsonIgnore
    public Long getNotificationId() {
        return (Long) getVariables().get("notificationId");
    }
}
