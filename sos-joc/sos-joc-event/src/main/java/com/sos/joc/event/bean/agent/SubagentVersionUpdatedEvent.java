package com.sos.joc.event.bean.agent;

public class SubagentVersionUpdatedEvent extends AgentVersionEvent {
    
    
    public SubagentVersionUpdatedEvent(String controllerId, String agentId, String subagentId, String version) {
        super("SubagentVersionUpdated", controllerId, agentId, version);
        putVariable("subagentId", agentId);
    }
    
    public SubagentVersionUpdatedEvent(String controllerId, String agentId, String subagentId, String version, String javaVersion) {
        super("SubagentVersionUpdated", controllerId, agentId, version, javaVersion);
        putVariable("subagentId", agentId);
    }
    
    public String getSubagentId() {
        return (String) getVariables().get("subagentId");
    }

}
