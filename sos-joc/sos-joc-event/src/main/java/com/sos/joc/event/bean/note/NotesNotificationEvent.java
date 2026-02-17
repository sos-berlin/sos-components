package com.sos.joc.event.bean.note;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class NotesNotificationEvent extends JOCEvent {
    
    public NotesNotificationEvent() {
        super("NotesNotification", null, null);
        putVariables(null, null, false);
    }

    public NotesNotificationEvent(Map<String, Map<String, Long>> requestors, Map<String, Long> approvers) {
        super("NotesNotification", null, null);
        putVariables(requestors, approvers, true);
    }
    
    public NotesNotificationEvent(Map<String, Map<String, Long>> requestors, Map<String, Long> approvers, boolean onlyNotification) {
        super("NotesNotification", null, null);
        putVariables(requestors, approvers, true);
        putVariable("onlyNotification", onlyNotification);
    }
    
    private void putVariables(Map<String, Map<String, Long>> requestors, Map<String, Long> approvers, boolean withNotification) {
        putVariable("requestors", requestors);
        putVariable("approvers", approvers);
        putVariable("withNotification", withNotification);
        putVariable("onlyNotification", false);
    }
    
    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Map<String, Map<String, Long>> getRequestors() {
        return (Map<String, Map<String, Long>>) getVariables().get("requestors");
    }
    
    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Map<String, Long> getApprovers() {
        return (Map<String, Long>) getVariables().get("approvers");
    }
    
    @JsonIgnore
    public Boolean withNotification() {
        return (Boolean) getVariables().get("withNotification");
    }
    
    @JsonIgnore
    public Boolean onlyNotification() {
        return (Boolean) getVariables().get("onlyNotification");
    }
}
