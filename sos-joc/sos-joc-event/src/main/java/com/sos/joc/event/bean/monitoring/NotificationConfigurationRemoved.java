package com.sos.joc.event.bean.monitoring;

public class NotificationConfigurationRemoved extends MonitoringEvent {

    public NotificationConfigurationRemoved(String controllerId) {
        super(NotificationConfigurationReleased.class.getSimpleName(), controllerId, null);
    }
}
