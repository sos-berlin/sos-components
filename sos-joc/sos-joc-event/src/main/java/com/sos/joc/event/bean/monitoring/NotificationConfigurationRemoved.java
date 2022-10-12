package com.sos.joc.event.bean.monitoring;

public class NotificationConfigurationRemoved extends MonitoringEvent {

    public NotificationConfigurationRemoved() {
        super(NotificationConfigurationReleased.class.getSimpleName(), null, null);
    }
}
