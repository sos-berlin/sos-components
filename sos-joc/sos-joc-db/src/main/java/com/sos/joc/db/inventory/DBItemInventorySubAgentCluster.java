package com.sos.joc.db.inventory;

import java.util.Date;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[SUBAGENT_CLUSTER_ID]" }) })
@Proxy(lazy = false)
public class DBItemInventorySubAgentCluster extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_SUBAGENT_CLUSTERS_SEQUENCE)
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

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

}