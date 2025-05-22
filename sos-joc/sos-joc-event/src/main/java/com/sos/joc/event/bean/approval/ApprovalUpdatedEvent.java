package com.sos.joc.event.bean.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class ApprovalUpdatedEvent extends JOCEvent {
    
    public ApprovalUpdatedEvent() {
        super("ApprovalUpdated", null, null);
        putVariables(null, null, false, 0L, null);
    }

    public ApprovalUpdatedEvent(String requestor, String approverAction) {
        super("ApprovalUpdated", null, null);
        putVariables(requestor, null, true, 0L, approverAction);
    }

    public ApprovalUpdatedEvent(String approver, Long numOfPending) {
        super("ApprovalUpdated", null, null);
        putVariables(null, approver, true, numOfPending, null);
    }

    public ApprovalUpdatedEvent(String requestor, String approver, Long numOfPending, String approverAction) {
        super("ApprovalUpdated", null, null);
        putVariables(requestor, approver, true, numOfPending, approverAction);
    }

    private void putVariables(String requestor, String approver, boolean withNotification, Long numOfPending, String approverAction) {
        putVariable("requestor", requestor);
        putVariable("approver", approver);
        putVariable("withNotification", withNotification);
        putVariable("numOfPending", numOfPending);
        putVariable("approverAction", approverAction);
    }
    
    @JsonIgnore
    public String getRequestor() {
        return (String) getVariables().get("requestor");
    }
    
    @JsonIgnore
    public String getApprover() {
        return (String) getVariables().get("approver");
    }
    
    @JsonIgnore
    public Boolean withNotification() {
        return (Boolean) getVariables().get("withNotification");
    }
    
    @JsonIgnore
    public Long numOfPending() {
        return (Long) getVariables().get("numOfPending");
    }
    
    @JsonIgnore
    public String getApproverAction() {
        return (String) getVariables().get("approverAction");
    }
}
