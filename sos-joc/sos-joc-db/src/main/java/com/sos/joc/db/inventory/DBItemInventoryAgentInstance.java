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
@Table(name = DBLayer.TABLE_INV_AGENT_INSTANCES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[SECURITY_LEVEL]", "[AGENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_AGENT_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_AGENT_INSTANCES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryAgentInstance extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_AGENT_INSTANCES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[SECURITY_LEVEL]", nullable = false)
    private Integer securityLevel;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[AGENT_NAME]", nullable = false)
    private String agentName;

    /* foreign key INVENTORY_OPERTATION_SYSTEM.ID */
    @Column(name = "[OS_ID]", nullable = false)
    private Long osId;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[STARTED_AT]", nullable = true)
    private Date startedAt;
    
    /* 0=no, 1=yes */
    @Column(name = "[DISABLED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean disabled;

    /* 0=no, 1=yes */
    @Column(name = "[IS_WATCHER]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean isWatcher;

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
    
    public Integer getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(Integer val) {
        securityLevel = val;
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
    
    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long val) {
        if (val == null) {
            val = 0L;
        }
        osId = val;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
        version = val;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date val) {
        startedAt = val;
    }
    
    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean val) {
        disabled = val;
    }

    public boolean getIsWatcher() {
        return isWatcher;
    }

    public void setIsWatcher(boolean val) {
        isWatcher = val;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

}