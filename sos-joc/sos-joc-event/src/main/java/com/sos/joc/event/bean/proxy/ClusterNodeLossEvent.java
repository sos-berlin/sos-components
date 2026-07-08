package com.sos.joc.event.bean.proxy;

public class ClusterNodeLossEvent extends ConfirmEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterNodeLossEvent() {
    }

    public ClusterNodeLossEvent(String controllerId, String nodeId, String message) {
        super(ClusterNodeLossEvent.class.getSimpleName(), controllerId, nodeId, message);
    }
    
    public ClusterNodeLossEvent(String controllerId, String nodeId, String message, boolean onlyProblem) {
        super(ClusterNodeLossEvent.class.getSimpleName(), controllerId, nodeId, message, onlyProblem);
    }
}
