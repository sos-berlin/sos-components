package com.sos.joc.event.bean.agent;

import com.sos.joc.event.bean.JOCEvent;

public class AgentVersionEvent extends JOCEvent {
    
    public AgentVersionEvent(String jocEventName, String controllerId, String agentId, String version) {
        super(jocEventName, controllerId, null);
        putVariable("agentId", agentId);
        putVariable("version", version);
    }
    
    public String getAgentId() {
        return (String) getVariables().get("agentId");
    }

    public String getVersion() {
        return (String) getVariables().get("version");
    }
}
