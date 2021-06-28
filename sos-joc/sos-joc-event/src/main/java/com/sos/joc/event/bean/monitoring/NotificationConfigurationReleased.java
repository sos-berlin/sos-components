package com.sos.joc.event.bean.monitoring;

public class NotificationConfigurationReleased extends MonitoringEvent {

    public NotificationConfigurationReleased(String controllerId) {
        super(NotificationConfigurationReleased.class.getSimpleName(), controllerId, null);
    }
}
