package com.sos.joc.event.bean.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NotificationLogEvent extends MonitoringEvent {

    public NotificationLogEvent(String level, String category, long epochMillis, String loggerName, String markerName, String messsage,
            Throwable thrown) {
        super(NotificationLogEvent.class.getSimpleName(), null, null);
        putVariable("level", level);
        putVariable("category", category);
        putVariable("epochMillis", epochMillis);
        putVariable("loggerName", loggerName);
        putVariable("markerName", markerName);
        putVariable("messsage", messsage);
        putVariable("thrown", thrown);
    }

    @JsonIgnore
    public String getLevel() {
        return (String) getVariables().get("level");
    }
    
    @JsonIgnore
    public String getCategory() {
        return (String) getVariables().get("category");
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
        return (String) getVariables().get("messsage");
    }
    
    @JsonIgnore
    public Throwable getThrown() {
        return (Throwable) getVariables().get("thrown");
    }
    
    @JsonIgnore
    public String toString() {
        String thrown = getThrown() != null ? getThrown().toString() : "";
        String markerName = getMarkerName() != null ? getMarkerName() : "";
        return String.format("level:%s, category:%s, epochMillis:%d, clazz:%s, marker:%s, messsage:%s, thrown:%s", getLevel(), getCategory(), getEpochMillis(),
                getLoggerName(), markerName, getMessage(), thrown);
    }
}
