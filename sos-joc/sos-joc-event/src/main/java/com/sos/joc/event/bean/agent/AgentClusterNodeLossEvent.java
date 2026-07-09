package com.sos.joc.event.bean.agent;

public class AgentClusterNodeLossEvent extends AgentClusterConfirmEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AgentClusterNodeLossEvent() {
    }

    public AgentClusterNodeLossEvent(String controllerId, String message) {
        super(AgentClusterNodeLossEvent.class.getSimpleName(), controllerId, message);
    }
}
