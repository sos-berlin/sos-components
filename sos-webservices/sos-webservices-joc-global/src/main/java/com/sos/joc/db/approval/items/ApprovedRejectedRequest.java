package com.sos.joc.db.approval.items;

import com.sos.joc.model.security.foureyes.ApproverState;

public class ApprovedRejectedRequest {
    
    private Integer approverState;
    private Long numOf;

    public String getApproverState() {
        try {
            return ApproverState.fromValue(approverState).value();
        } catch (Exception e) {
            return ApproverState.PENDING.value();
        }
    }
    
    public void setApproverState(Integer approverState) {
        this.approverState = approverState;
    }
    
    public Long getNumOf() {
        return numOf;
    }
    
    public void setNumOf(Long numOf) {
        this.numOf = numOf;
    }
}
