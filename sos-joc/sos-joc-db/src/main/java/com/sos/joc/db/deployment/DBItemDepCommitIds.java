package com.sos.joc.db.deployment;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_DEP_COMMIT_IDS)
@Proxy(lazy = false)
public class DBItemDepCommitIds extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_DEP_COMMIT_IDS_SEQUENCE)
    private Long id;

    @Column(name = "[INV_CID]", nullable = false)
    private Long invConfigurationId;

    @Column(name = "[DEP_HID]", nullable = true)
    private Long depHistoryId;

    @Column(name = "[CFG_PATH]", nullable = false)
    private String configPath;

    @Column(name = "[COMMIT_ID]", nullable = false)
    private String commitId;

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

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

}