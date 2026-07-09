package com.sos.joc.event.bean.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public abstract class AgentClusterConfirmEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AgentClusterConfirmEvent() {
    }
    
    public AgentClusterConfirmEvent(String key, String controllerId, String message) {
        super(key, controllerId, null);
        putVariable("message", message);
        putVariable("onlyProblem", true);
    }
    
    @JsonIgnore
    public String getAgentId() {
        return (String) getVariables().get("agentId");
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
