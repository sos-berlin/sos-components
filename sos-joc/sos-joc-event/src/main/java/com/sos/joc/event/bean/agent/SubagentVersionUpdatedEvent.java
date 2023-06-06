package com.sos.joc.event.bean.agent;

public class SubagentVersionUpdatedEvent extends AgentVersionEvent {
    
    
    public SubagentVersionUpdatedEvent(String agentId, String subagentId, String version) {
        super("SubagentVersionUpdated", agentId, version);
        putVariable("subagentId", subagentId);
    }
    
    public SubagentVersionUpdatedEvent(String agentId, String subagentId, String version, String javaVersion) {
        super("SubagentVersionUpdated", agentId, version, javaVersion);
        putVariable("subagentId", subagentId);
    }
    
    public String getSubagentId() {
        return (String) getVariables().get("subagentId");
    }

}
