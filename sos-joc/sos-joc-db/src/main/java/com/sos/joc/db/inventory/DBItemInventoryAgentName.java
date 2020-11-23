package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_AGENT_NAMES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[AGENT_ID]", "[AGENT_NAME]" }) })
public class DBItemInventoryAgentName extends DBItem {

    private static final long serialVersionUID = 1L;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[AGENT_NAME]", nullable = false)
    private String agentName;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
    }
    
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String val) {
        agentName = val;
    }
}