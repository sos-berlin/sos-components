package com.sos.joc.event.bean.deploy;

public class DeployHistoryWorkflowPathEvent extends DeployHistoryEvent {

    /** No args constructor for use in serialization */
    public DeployHistoryWorkflowPathEvent() {
    }
    
    public DeployHistoryWorkflowPathEvent(String name, String path) {
        super(DeployHistoryWorkflowPathEvent.class.getSimpleName(), null, name, null, path, null);
    }

}
