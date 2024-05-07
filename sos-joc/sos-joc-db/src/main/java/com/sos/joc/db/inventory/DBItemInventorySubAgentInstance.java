package com.sos.joc.db.inventory;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.agent.SubagentDirectorType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_INSTANCES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SUB_AGENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE, allocationSize = 1)
public class DBItemInventorySubAgentInstance extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[SUB_AGENT_ID]", nullable = false)
    private String subAgentId;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    /* 0=no, 1=primary, 2=standby */
    @Column(name = "[IS_DIRECTOR]", nullable = false)
    private Integer isDirector;

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    /* 0=no, 1=yes */
    @Column(name = "[IS_WATCHER]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean isWatcher;

    /* 0=no, 1=yes */
    @Column(name = "[DISABLED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean disabled;

    /* 0=no, 1=yes */
    @Column(name = "[DEPLOYED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean deployed;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    /* foreign key INVENTORY_OPERTATION_SYSTEM.ID */
    @Column(name = "[OS_ID]", nullable = false)
    private Long osId;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[JAVA_VERSION]", nullable = true)
    private String javaVersion;

    @Column(name = "[CERTIFICATE]", nullable = true)
    private String certificate;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Transient
    private String transaction = "none";

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getSubAgentId() {
        return subAgentId;
    }

    public void setSubAgentId(String val) {
        subAgentId = val;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        if (val != null && !"/".equals(val)) {
            val = val.replaceFirst("/$", "");
        }
        uri = val;
    }

    public Integer getIsDirector() {
        return isDirector;
    }

    public void setIsDirector(Integer val) {
        isDirector = val;
    }

    @Transient
    public SubagentDirectorType getDirectorAsEnum() {
        try {
            return SubagentDirectorType.fromValue(isDirector);
        } catch (Exception e) {
            return null;
        }
    }

    @Transient
    public void setIsDirector(SubagentDirectorType val) {
        setIsDirector(val == null ? null : val.intValue());
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
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

    public void setModified(Date val) {
        modified = val;
    }

    public boolean getIsWatcher() {
        return isWatcher;
    }

    public void setIsWatcher(boolean val) {
        isWatcher = val;
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

    public Date getModified() {
        return modified;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @Transient
    public String getTransaction() {
        return transaction;
    }

    @Transient
    public void setTransaction(String val) {
        transaction = val == null ? "none" : val;
    }

}