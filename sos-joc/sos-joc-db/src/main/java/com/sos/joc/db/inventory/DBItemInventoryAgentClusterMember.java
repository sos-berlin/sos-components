package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_AGENT_CLUSTER_MEMBERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CID_AGENT_CLUSTER]", "[URI]" }) })
public class DBItemInventoryAgentClusterMember extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id // fake id for annotation
    @Column(name = "[CID_AGENT_CLUSTER]", nullable = false)
    private Long cidAgentCluster;

    @Id // fake id for annotation
    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[CLUSTER_URI]", nullable = true)
    private String clusterUri;

    @Column(name = "[ORDERING]", nullable = false)
    private Long ordering;

    @Column(name = "[TITLE]", nullable = false)
    private String title;

    public Long getCidAgentCluster() {
        return cidAgentCluster;
    }

    public void setCidAgentCluster(Long val) {
        cidAgentCluster = val;
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
