package com.sos.joc.db.inventory;

import java.util.Date;

import org.hibernate.annotations.Proxy;

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

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]",
        "[SUBAGENT_CLUSTER_ID]", "[SUBAGENT_ID]" }) })
@Proxy(lazy = false)
public class DBItemInventorySubAgentClusterMember extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_SUBAGENT_CLUSTER_MEMBERS_SEQUENCE)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[SUBAGENT_CLUSTER_ID]", nullable = false)
    private String subAgentClusterId;

    @Column(name = "[SUBAGENT_ID]", nullable = false)
    private String subAgentId;

    @Column(name = "[PRIORITY]", nullable = false)
    private String priority;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String val) {
        priority = val;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

}