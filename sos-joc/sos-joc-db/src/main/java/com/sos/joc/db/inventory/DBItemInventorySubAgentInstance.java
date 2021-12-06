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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.agent.SubagentDirectorType;

@Entity
@Table(name = DBLayer.TABLE_INV_SUBAGENT_INSTANCES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SUB_AGENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE, allocationSize = 1)
public class DBItemInventorySubAgentInstance extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE)
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

    @Column(name = "[CERTIFICATE]", nullable = true)
    private String certificate;
    
    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

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