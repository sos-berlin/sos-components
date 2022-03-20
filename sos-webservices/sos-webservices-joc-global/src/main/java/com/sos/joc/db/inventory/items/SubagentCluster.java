package com.sos.joc.db.inventory.items;

import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.model.agent.SubAgentId;

public class SubagentCluster {
    
    private DBItemInventorySubAgentCluster sa;
    private SubAgentId subagent = new SubAgentId();
    
    public SubagentCluster(DBItemInventorySubAgentCluster sa, String subAgentId, Integer priority) {
        this.sa = sa;
        this.subagent.setPriority(priority);
        this.subagent.setSubagentId(subAgentId);
    }
    
    public DBItemInventorySubAgentCluster getDBItemInventorySubAgentCluster() {
        return sa;
    }
    
    public SubAgentId getDBItemInventorySubAgentClusterMember() {
        return subagent;
    }
}
