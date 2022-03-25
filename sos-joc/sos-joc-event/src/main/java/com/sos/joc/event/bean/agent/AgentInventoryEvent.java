package com.sos.joc.event.bean.agent;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class AgentInventoryEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AgentInventoryEvent() {
    }
    
    /**
     * @param controllerId
     * @param agentId
     */
    public AgentInventoryEvent(String controllerId) {
        super("AgentInventoryUpdated", controllerId, null);
    }

    /**
     * @param controllerId
     * @param agentId
     */
    public AgentInventoryEvent(String controllerId, String agentId) {
        super("AgentInventoryUpdated", controllerId, null);
        putVariable("agentId", agentId);
    }
    
    /**
     * @param controllerId
     * @param agentIds
     */
    public AgentInventoryEvent(String controllerId, Collection<String> agentIds) {
        super("AgentInventoryUpdated", controllerId, null);
        if (agentIds.size() == 1) {
            putVariable("agentId", agentIds.iterator().next());
        }
    }
    
    @JsonIgnore
    public String getAgentId() {
        return (String) getVariables().get("agentId");
    }
}
