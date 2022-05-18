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

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SUBAGENT_CLUSTER_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS_SEQUENCE, allocationSize = 1)
public class DBItemInventorySubAgentCluster extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[SUBAGENT_CLUSTER_ID]", nullable = false)
    private String subAgentClusterId;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[DEPLOYED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean deployed;

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
    
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
    }
    
    public String getSubAgentClusterId() {
        return subAgentClusterId;
    }

    public void setSubAgentClusterId(String val) {
        subAgentClusterId = val;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }
    
    public boolean getDeployed() {
        return deployed;
    }

    public void setDeployed(boolean val) {
        deployed = val;
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