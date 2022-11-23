package com.sos.joc.event.bean.monitoring;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NotificationLogEvent extends MonitoringEvent {

    public NotificationLogEvent(String level, String category, long epochMillis, String fqcn, String messsage, Throwable thrown) {
        super(NotificationLogEvent.class.getSimpleName(), null, null);
        putVariable("level", level);
        putVariable("category", category);
        putVariable("epochMillis", epochMillis);
        putVariable("fqcn", fqcn);
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
    public Long getInstant() {
        return (Long) getVariables().get("epochMillis");
    }
    
    /**
     * @return full qualified class name
     */
    @JsonIgnore
    public String getFqcn() {
        return (String) getVariables().get("fqcn");
    }
    
    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("messsage");
    }
    
    @JsonIgnore
    public Throwable getThrown() {
        return (Throwable) getVariables().get("thrown");
    }
}
