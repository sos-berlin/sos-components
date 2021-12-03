package com.sos.joc.db.inventory;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CLUSTER_NAME]", "[SUB_AGENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS_SEQUENCE, allocationSize = 1)
public class DBItemInventorySubAgentClusterMember extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CLUSTER_NAME]", nullable = false)
    private String clusterName;

    @Column(name = "[SUB_AGENT_ID]", nullable = false)
    private String subAgentId;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }
    
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String val) {
        clusterName = val;
    }
    
    public String getSubAgentId() {
        return subAgentId;
    }

    public void setSubAgentId(String val) {
        subAgentId = val;
    }
    
    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
        ordering = val;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

}