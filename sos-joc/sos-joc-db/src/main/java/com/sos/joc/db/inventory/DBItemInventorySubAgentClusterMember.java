package com.sos.joc.db.inventory;

import java.util.Date;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SUBAGENT_CLUSTER_ID]",
        "[SUBAGENT_ID]" }) })
public class DBItemInventorySubAgentClusterMember extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS_SEQUENCE)
    private Long id;

    @Column(name = "[SUBAGENT_CLUSTER_ID]", nullable = false)
    private String subAgentClusterId;

    @Column(name = "[SUBAGENT_ID]", nullable = false)
    private String subAgentId;

    @Column(name = "[PRIORITY]", nullable = false)
    private Integer priority;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getSubAgentClusterId() {
        return subAgentClusterId;
    }

    public void setSubAgentClusterId(String val) {
        subAgentClusterId = val;
    }

    public String getSubAgentId() {
        return subAgentId;
    }

    public void setSubAgentId(String val) {
        subAgentId = val;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer val) {
        priority = val;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

}