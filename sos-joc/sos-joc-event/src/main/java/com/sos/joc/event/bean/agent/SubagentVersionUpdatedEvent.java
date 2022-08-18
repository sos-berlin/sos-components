package com.sos.joc.event.bean.agent;

public class SubagentVersionUpdatedEvent extends AgentVersionEvent {
    
    
    public SubagentVersionUpdatedEvent(String agentId, String subagentId, String version) {
        super("SubagentVersionUpdated", agentId, version);
        putVariable("subagentId", agentId);
    }
    
    public SubagentVersionUpdatedEvent(String agentId, String subagentId, String version, String javaVersion) {
        super("SubagentVersionUpdated", agentId, version, javaVersion);
        putVariable("subagentId", agentId);
    }
    
    public String getSubagentId() {
        return (String) getVariables().get("subagentId");
    }

}
