package com.sos.joc.db.inventory;

import java.beans.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryMeta.AgentClusterSchedulingType;

@Entity
@Table(name = DBLayer.TABLE_INV_AGENT_CLUSTERS)
public class DBItemInventoryAgentCluster extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[SCHEDULING_TYPE]", nullable = false)
    private Integer schedulingType;

    @Column(name = "[NUMBER_OF_AGENTS]", nullable = false)
    private Long numberOfAgents;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public Integer getSchedulingType() {
        return schedulingType;
    }

    @Transient
    public AgentClusterSchedulingType getSchedulingTypeAsEnum() {
        return AgentClusterSchedulingType.fromValue(schedulingType);
    }

    public void setSchedulingType(Integer val) {
        schedulingType = val;
    }

    @Transient
    public void setSchedulingType(AgentClusterSchedulingType val) {
        setSchedulingType(val == null ? null : val.value());
    }

    public Long getNumberOfAgents() {
        return numberOfAgents;
    }

    public void setNumberOfAgents(Long val) {
        if (val == null) {
            val = 0L;
        }
        numberOfAgents = val;
    }

}
