package com.sos.joc.event.bean.deploy;

public class DeployHistoryWorkflowEvent extends DeployHistoryEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public DeployHistoryWorkflowEvent() {
    }
    
    public DeployHistoryWorkflowEvent(String controllerId, String name, String commitId, String path) {
        super(DeployHistoryWorkflowEvent.class.getSimpleName(), controllerId, name, commitId, path, 1);
    }
}
