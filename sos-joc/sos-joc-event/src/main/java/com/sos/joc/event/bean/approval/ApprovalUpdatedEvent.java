package com.sos.joc.event.bean.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class ApprovalUpdatedEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ApprovalUpdatedEvent() {
    }

    /**
     * @param requestor
     * @param approver
     */
    public ApprovalUpdatedEvent(String requestor, String approver) {
        super("ApprovalUpdated", null, null);
        putVariable("requestor", requestor);
        putVariable("approver", approver);
    }
    
    @JsonIgnore
    public String getRequestor() {
        return (String) getVariables().get("requestor");
    }
    
    @JsonIgnore
    public String getApprover() {
        return (String) getVariables().get("approver");
    }
}
