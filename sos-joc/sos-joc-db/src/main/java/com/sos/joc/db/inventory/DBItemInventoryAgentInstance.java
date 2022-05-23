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
@Table(name = DBLayer.TABLE_INV_AGENT_INSTANCES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[AGENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_AGENT_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_AGENT_INSTANCES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryAgentInstance extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_AGENT_INSTANCES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[AGENT_NAME]", nullable = false)
    private String agentName;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;
    
    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;
    
    /* foreign key INVENTORY_OPERTATION_SYSTEM.ID */
    @Column(name = "[OS_ID]", nullable = false)
    private Long osId;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[JAVA_VERSION]", nullable = true)
    private String javaVersion;

    @Column(name = "[STARTED_AT]", nullable = true)
    private Date startedAt;
    
    @Column(name = "[CERTIFICATE]", nullable = true)
    private String certificate;
    
    /* 0=no, 1=yes */
    @Column(name = "[IS_WATCHER]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean isWatcher = false;

    /* 0=no, 1=yes */
    @Column(name = "[DISABLED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean disabled = false;

    /* 0=no, 1=yes */
    @Column(name = "[HIDDEN]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean hidden = false;

    /* 0=no, 1=yes */
    @Column(name = "[DEPLOYED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean deployed = false;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
    }
    
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
    }
    
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String val) {
        agentName = val;
    }
    
    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
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
    
    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long val) {
        if (val == null) {
            val = 0L;
        }
        osId = val;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
        version = val;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String val) {
        javaVersion = val;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date val) {
        startedAt = val;
    }
    
    public boolean getIsWatcher() {
        return isWatcher;
    }

    public void setIsWatcher(boolean val) {
        isWatcher = val;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean val) {
        hidden = val;
    }
    
    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean val) {
        disabled = val;
    }
    
    public boolean getDeployed() {
        return deployed;
    }

    public void setDeployed(boolean val) {
        deployed = val;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }
    
    public String getCertificate() {
        return certificate;
    }
    
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

}