package com.sos.joc.event.bean.proxy;

public class FailoverConfirmEvent extends ConfirmEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public FailoverConfirmEvent() {
    }
    
    public FailoverConfirmEvent(String controllerId, String nodeId, String message) {
        super(FailoverConfirmEvent.class.getSimpleName(), controllerId, nodeId, message);
    }
    
    public FailoverConfirmEvent(String controllerId, String nodeId, String message, boolean onlyProblem) {
        super(FailoverConfirmEvent.class.getSimpleName(), controllerId, nodeId, message, onlyProblem);
    }
}
