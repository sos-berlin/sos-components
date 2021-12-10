package com.sos.joc.db.inventory.items;

import com.sos.joc.model.agent.SubagentDirectorType;

public class SubAgentItem {

    private String agentId;
    private String subAgentId;
    private Integer isDirector;
    private Integer ordering;
    
    public SubAgentItem(String agentId, Integer isDirector, String subAgentId, Integer ordering) {
        this.agentId = agentId;
        this.isDirector = isDirector;
        this.subAgentId = subAgentId;
        this.ordering = ordering;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getSubAgentId() {
        return subAgentId;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public Integer getIsDirector() {
        return isDirector;
    }
    
    public boolean isPrimaryDirector() {
        return isDirector == SubagentDirectorType.PRIMARY_DIRECTOR.intValue();
    }
    
    public boolean isStandbyDirector() {
        return isDirector == SubagentDirectorType.STANDBY_DIRECTOR.intValue();
    }
}
