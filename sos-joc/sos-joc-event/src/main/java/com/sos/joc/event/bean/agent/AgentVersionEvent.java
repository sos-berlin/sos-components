package com.sos.joc.event.bean.agent;

import com.sos.joc.event.bean.JOCEvent;

public class AgentVersionEvent extends JOCEvent {
    
    public AgentVersionEvent(String jocEventName, String controllerId, String agentId, String version) {
        super(jocEventName, controllerId, null);
        putVariable("agentId", agentId);
        putVariable("version", version);
    }
    
    public AgentVersionEvent(String jocEventName, String controllerId, String agentId, String version, String javaVersion) {
        super(jocEventName, controllerId, null);
        putVariable("agentId", agentId);
        putVariable("version", version);
        if(javaVersion.length() > 30) {
            putVariable("javaVersion", javaVersion.substring(0, 30));
        } else {
            putVariable("javaVersion", javaVersion);
        }
    }
    
    public String getAgentId() {
        return (String) getVariables().get("agentId");
    }

    public String getVersion() {
        return (String) getVariables().get("version");
    }

    public String getJavaVersion() {
        return (String) getVariables().get("javaVersion");
    }
}
