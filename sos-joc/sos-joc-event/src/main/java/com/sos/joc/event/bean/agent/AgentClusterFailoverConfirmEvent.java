package com.sos.joc.event.bean.agent;

public class AgentClusterFailoverConfirmEvent extends AgentClusterConfirmEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AgentClusterFailoverConfirmEvent() {
    }

    public AgentClusterFailoverConfirmEvent(String controllerId, String message) {
        super(AgentClusterFailoverConfirmEvent.class.getSimpleName(), controllerId, message);
    }
}
