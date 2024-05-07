package com.sos.joc.db.deployment;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_VERSIONS)
@SequenceGenerator(name = DBLayer.TABLE_DEP_VERSIONS_SEQUENCE, sequenceName = DBLayer.TABLE_DEP_VERSIONS_SEQUENCE, allocationSize = 1)
public class DBItemDepVersions extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DEP_VERSIONS_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_DEP_VERSIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[INV_CID]", nullable = true)
    private Long invConfigurationId;

    @Column(name = "[DEP_HID]", nullable = true)
    private Long depHistoryId;

    @Column(name = "[VERSION]", nullable = false)
    private String version;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvConfigurationId() {
        return invConfigurationId;
    }

    public void setInvConfigurationId(Long invConfigurationId) {
        this.invConfigurationId = invConfigurationId;
    }

    public Long getDepHistoryId() {
        return depHistoryId;
    }

    public void setDepHistoryId(Long depHistoryId) {
        this.depHistoryId = depHistoryId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

}