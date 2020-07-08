package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_AGENT_CLUSTER_MEMBERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[AGENT_CLUSTER_ID]", "[URI]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_AGENT_CLUSTER_MEMBERS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_AGENT_CLUSTER_MEMBERS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryAgentClusterMember extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_AGENT_CLUSTER_MEMBERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[AGENT_CLUSTER_ID]", nullable = false)
    private Long agentClusterId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[CLUSTER_URI]", nullable = true)
    private String clusterUri;

    @Column(name = "[ORDERING]", nullable = false)
    private Long ordering;

    @Column(name = "[TITLE]", nullable = false)
    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getAgentClusterId() {
        return agentClusterId;
    }

    public void setAgentClusterId(Long val) {
        agentClusterId = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
    }

    public String getClusterUri() {
        return clusterUri;
    }

    public void setClusterUri(String val) {
        clusterUri = val;
    }

    public Long getOrdering() {
        return ordering;
    }

    public void setOrdering(Long val) {
        if (val == null) {
            val = 0L;
        }
        ordering = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }
}
