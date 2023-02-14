package com.sos.joc.event.bean.monitoring;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MonitoringGuiEvent extends MonitoringEvent {

    public MonitoringGuiEvent(Integer type, String category, String source, String request, long epochMillis, String messsage) {
        super(MonitoringGuiEvent.class.getSimpleName(), null, null);
        putVariables(type, category, source, request, epochMillis, messsage);
    }
    
    @JsonIgnore
    private void putVariables(Integer type, String category, String source, String request, long epochMillis, String messsage) {
        putVariable("level", type);
        putVariable("category", category);
        putVariable("epochMillis", epochMillis);
        putVariable("source", source);
        putVariable("request", request);
        putVariable("messsage", messsage);
    }

    @JsonIgnore
    public Integer getLevel() {
        return (Integer) getVariables().get("level");
    }
    
    @JsonIgnore
    public String getCategory() {
        return (String) getVariables().get("category");
    }
    
    @JsonIgnore
    public String getSource() {
        return (String) getVariables().get("source");
    }
    
    @JsonIgnore
    public String getRequest() {
        return (String) getVariables().get("request");
    }
    
    @JsonIgnore
    public Long getEpochMillis() {
        return (Long) getVariables().get("epochMillis");
    }
    
    @JsonIgnore
    public Date getDate() {
        try {
            return Date.from(Instant.ofEpochMilli(getEpochMillis()));
        } catch (Exception e) {
            return null;
        }
    }
    
    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("messsage");
    }
}
