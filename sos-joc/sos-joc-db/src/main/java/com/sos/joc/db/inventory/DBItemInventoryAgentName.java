package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_AGENT_NAMES)
public class DBItemInventoryAgentName extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Id
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