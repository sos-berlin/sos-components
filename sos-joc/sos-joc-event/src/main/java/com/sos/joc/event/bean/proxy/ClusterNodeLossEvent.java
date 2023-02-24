package com.sos.joc.event.bean.proxy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class ClusterNodeLossEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterNodeLossEvent() {
    }

    public ClusterNodeLossEvent(String controllerId, String nodeId, String message) {
        super(ClusterNodeLossEvent.class.getSimpleName(), controllerId, null);
        putVariable("nodeId", nodeId);
        putVariable("message", message);
        putVariable("onlyProblem", false);
    }
    
    public ClusterNodeLossEvent(String controllerId, String nodeId, String message, boolean onlyProblem) {
        super(ClusterNodeLossEvent.class.getSimpleName(), controllerId, null);
        putVariable("nodeId", nodeId);
        putVariable("message", message);
        putVariable("onlyProblem", onlyProblem);
    }
    
    @JsonIgnore
    public String getNodeId() {
        return (String) getVariables().get("nodeId");
    }
    
    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("message");
    }
    
    @JsonIgnore
    public Boolean onlyProblem() {
        return (Boolean) getVariables().get("onlyProblem");
    }
}
