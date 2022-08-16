package com.sos.joc.event.bean.agent;

public class AgentVersionUpdatedEvent extends AgentVersionEvent {
    
    
    public AgentVersionUpdatedEvent(String controllerId, String agentId, String version) {
        super("AgentVersionUpdated", controllerId, agentId, version);
    }
    
}
