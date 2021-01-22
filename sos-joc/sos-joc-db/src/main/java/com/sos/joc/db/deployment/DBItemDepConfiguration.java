package com.sos.joc.db.deployment;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_CONFIGURATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[TYPE]", "[PATH]" }) })

public class DBItemDepConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[INV_CID]", nullable = false)
    private Long inventoryConfigurationId;
    
    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;
    
    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[COMMIT_ID]", nullable = false)
    private String commitId;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFolder() {
        return folder;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }

    public Long getInventoryConfigurationId() {
        return inventoryConfigurationId;
    }
    public void setInventoryConfigurationId(Long inventoryConfigurationId) {
        this.inventoryConfigurationId = inventoryConfigurationId;
    }
    
    public String getControllerId() {
        return controllerId;
    }
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
    
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCommitId() {
        return commitId;
    }
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }

}
