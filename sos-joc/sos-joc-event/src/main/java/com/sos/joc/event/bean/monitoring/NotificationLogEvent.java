package com.sos.joc.event.bean.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NotificationLogEvent extends MonitoringEvent {

    public NotificationLogEvent(String level, long epochMillis, String loggerName, String markerName, String message, Throwable thrown) {
        super(NotificationLogEvent.class.getSimpleName(), null, null);
        putVariable("level", level);
        putVariable("epochMillis", epochMillis);
        putVariable("loggerName", loggerName);
        putVariable("markerName", markerName);
        putVariable("message", message);
        putVariable("thrown", thrown);
    }

    @JsonIgnore
    public String getLevel() {
        return (String) getVariables().get("level");
    }

    @JsonIgnore
    public Long getEpochMillis() {
        return (Long) getVariables().get("epochMillis");
    }

    @JsonIgnore
    public String getLoggerName() {
        return (String) getVariables().get("loggerName");
    }

    @JsonIgnore
    public String getMarkerName() {
        return (String) getVariables().get("markerName");
    }

    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("message");
    }

    @JsonIgnore
    public Throwable getThrown() {
        return (Throwable) getVariables().get("thrown");
    }

    @JsonIgnore
    public String toString() {
        String thrown = getThrown() != null ? getThrown().toString() : "";
        String markerName = getMarkerName() != null ? getMarkerName() : "";
        return String.format("level:%s,epochMillis:%d, clazz:%s, marker:%s, messsage:%s, thrown:%s", getLevel(), getEpochMillis(), getLoggerName(),
                markerName, getMessage(), thrown);
    }
}
