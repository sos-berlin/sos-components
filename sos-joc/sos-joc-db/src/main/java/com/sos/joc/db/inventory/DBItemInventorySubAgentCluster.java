package com.sos.joc.db.inventory;

import java.time.LocalDateTime;

import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.annotations.SOSCurrentTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]",
        "[SUBAGENT_CLUSTER_ID]" }) })
public class DBItemInventorySubAgentCluster extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSIdGenerator(sequenceName = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS_SEQUENCE)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[SUBAGENT_CLUSTER_ID]", nullable = false)
    private String subAgentClusterId;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[DEPLOYED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean deployed;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[MODIFIED]", nullable = false)
    @SOSCurrentTimestampUtc
    private LocalDateTime modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
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
        if (val == null) {
            val = 0;
        }
        ordering = val;
    }

    public LocalDateTime getModified() {
        return modified;
    }

}