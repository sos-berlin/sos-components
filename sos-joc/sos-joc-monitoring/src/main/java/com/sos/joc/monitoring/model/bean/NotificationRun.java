package com.sos.joc.monitoring.model.bean;

import java.io.Serializable;

import com.sos.joc.monitoring.configuration.Notification;

public class NotificationRun implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Notification configuredNotification;
    private final boolean hasConfiguredWorkflowJobs;

    public NotificationRun(final Notification configuredNotification, final boolean hasConfiguredWorkflowJobs) {
        this.configuredNotification = configuredNotification;
        this.hasConfiguredWorkflowJobs = hasConfiguredWorkflowJobs;
    }

    public Notification getConfiguredNotification() {
        return configuredNotification;
    }

    public boolean hasConfiguredWorkflowJobs() {
        return hasConfiguredWorkflowJobs;
    }

}
