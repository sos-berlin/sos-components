package com.sos.joc.db.inventory.agent;

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

import com.sos.joc.db.inventory.InventoryDBItemConstants;

@Entity
@Table(name = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTER)
@SequenceGenerator(name = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTER_SEQUENCE, sequenceName = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTER_SEQUENCE, allocationSize = 1)
public class DBItemInventoryAgentCluster implements Serializable {

    private static final long serialVersionUID = 2550971072531081059L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign Key INVENTORY_PROCESS_CLASSES.ID */
    private Long processClassId;

    /** Others */
    private String schedulingType;
    private Integer numberOfAgents;
    private Date created;
    private Date modified;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTER_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_AGENT_CLUSTER_SEQUENCE)
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
    @Column(name = "`PROCESS_CLASS_ID`", nullable = false)
    public Long getProcessClassId() {
        return processClassId;
    }

    /** Foreign Key */
    @Column(name = "`PROCESS_CLASS_ID`", nullable = false)
    public void setProcessClassId(Long processClassId) {
        if (processClassId == null) {
            processClassId = InventoryDBItemConstants.DEFAULT_ID;
        }
        this.processClassId = processClassId;
    }

    @Column(name = "`SCHEDULING_TYPE`", nullable = false)
    public String getSchedulingType() {
        return schedulingType;
    }

    @Column(name = "`SCHEDULING_TYPE`", nullable = false)
    public void setSchedulingType(String schedulingType) {
        this.schedulingType = schedulingType;
    }

    @Column(name = "`NUMBER_OF_AGENTS`", nullable = false)
    public Integer getNumberOfAgents() {
        return numberOfAgents;
    }

    @Column(name = "`NUMBER_OF_AGENTS`", nullable = false)
    public void setNumberOfAgents(Integer numberOfAgents) {
        this.numberOfAgents = numberOfAgents;
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
        return new HashCodeBuilder().append(processClassId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryAgentCluster)) {
            return false;
        }
        DBItemInventoryAgentCluster rhs = ((DBItemInventoryAgentCluster) other);
        return new EqualsBuilder().append(processClassId, rhs.processClassId).isEquals();
    }

}