package com.sos.joc.db.inventory.deprecated.agent;

import java.io.Serializable;
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.joc.db.inventory.deprecated.InventoryDBItemConstants;

@Entity
@Table(name = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTERMEMBERS)
@SequenceGenerator(name = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTERMEMBERS_SEQUENCE, sequenceName = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTERMEMBERS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryAgentClusterMember implements Serializable {

    private static final long serialVersionUID = 8059333159913852093L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign Key INVENTORY_AGENT_CLUSTER.ID */
    private Long agentClusterId;
    /** Foreign Key INVENTORY_AGENT_INSTANCES.ID */
    private Long agentInstanceId;
    /** Foreign Key INVENTORY_AGENT_INSTANCES.URL */
    private String url;

    /** Others */
    private Integer ordering;
    private Date created;
    private Date modified;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTERMEMBERS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTERMEMBERS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }

    /** Foreign Key */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return instanceId;
    }

    /** Foreign Key */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long instanceId) {
        if (instanceId == null) {
            instanceId = InventoryDBItemConstants.DEFAULT_ID;
        }
        this.instanceId = instanceId;
    }

    /** Foreign Key */
    @Column(name = "`AGENT_CLUSTER_ID`", nullable = false)
    public Long getAgentClusterId() {
        return agentClusterId;
    }

    /** Foreign Key */
    @Column(name = "`AGENT_CLUSTER_ID`", nullable = false)
    public void setAgentClusterId(Long agentClusterId) {
        if (agentClusterId == null) {
            agentClusterId = InventoryDBItemConstants.DEFAULT_ID;
        }
        this.agentClusterId = agentClusterId;
    }

    /** Foreign Key */
    @Column(name = "`AGENT_INSTANCE_ID`", nullable = false)
    public Long getAgentInstanceId() {
        return agentInstanceId;
    }

    /** Foreign Key */
    @Column(name = "`AGENT_INSTANCE_ID`", nullable = false)
    public void setAgentInstanceId(Long agentInstanceId) {
        if (agentInstanceId == null) {
            agentInstanceId = InventoryDBItemConstants.DEFAULT_ID;
        }
        this.agentInstanceId = agentInstanceId;
    }

    /** Foreign Key */
    @Column(name = "`URL`", nullable = false)
    public String getUrl() {
        return url;
    }

    /** Foreign Key */
    @Column(name = "`URL`", nullable = false)
    public void setUrl(String url) {
        if (url == null || url.isEmpty()) {
            url = InventoryDBItemConstants.DEFAULT_NAME;
        }
        this.url = url;
    }

    @Column(name = "`ORDERING`", nullable = false)
    public Integer getOrdering() {
        return ordering;
    }

    @Column(name = "`ORDERING`", nullable = false)
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(instanceId).append(agentClusterId).append(agentInstanceId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryAgentClusterMember)) {
            return false;
        }
        DBItemInventoryAgentClusterMember rhs = ((DBItemInventoryAgentClusterMember) other);
        return new EqualsBuilder().append(instanceId, rhs.instanceId).append(agentClusterId, rhs.agentClusterId).append(agentInstanceId,
                rhs.agentInstanceId).isEquals();
    }

}