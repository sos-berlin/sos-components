package com.sos.joc.event.bean.agent;

public class AgentVersionUpdatedEvent extends AgentVersionEvent {
    
    
    public AgentVersionUpdatedEvent(String agentId, String version) {
        super("AgentVersionUpdated", agentId, version);
    }
    
    public AgentVersionUpdatedEvent(String agentId, String version, String javaVersion) {
        super("AgentVersionUpdated", agentId, version, javaVersion);
    }
    
}
