package com.sos.joc.event.bean.approval;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class ApprovalUpdatedEvent extends JOCEvent {
    
    public ApprovalUpdatedEvent() {
        super("ApprovalUpdated", null, null);
        putVariables(null, null, false);
    }

    public ApprovalUpdatedEvent(Map<String, Map<String, Long>> requestors, Map<String, Long> approvers) {
        super("ApprovalUpdated", null, null);
        putVariables(requestors, approvers, true);
    }

    private void putVariables(Map<String, Map<String, Long>> requestors, Map<String, Long> approvers, boolean withNotification) {
        putVariable("requestors", requestors);
        putVariable("approvers", approvers);
        putVariable("withNotification", withNotification);
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
}
