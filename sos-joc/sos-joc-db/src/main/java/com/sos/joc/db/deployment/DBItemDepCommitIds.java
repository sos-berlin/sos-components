package com.sos.joc.db.deployment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_COMMIT_IDS)
@SequenceGenerator(name = DBLayer.TABLE_DEP_COMMIT_IDS_SEQUENCE, sequenceName = DBLayer.TABLE_DEP_COMMIT_IDS_SEQUENCE, allocationSize = 1)
public class DBItemDepCommitIds extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DEP_COMMIT_IDS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
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