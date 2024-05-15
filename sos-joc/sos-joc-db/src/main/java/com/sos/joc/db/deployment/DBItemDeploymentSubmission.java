package com.sos.joc.db.deployment;

import java.nio.file.Paths;
import java.util.Date;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = DBLayer.TABLE_DEP_SUBMISSIONS)
public class DBItemDeploymentSubmission extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_DEP_SUBMISSIONS_SEQUENCE)
    private Long id;

    @Column(name = "[DEP_HID]", nullable = false)
    private Long depHistoryId;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[INV_CID]", nullable = false)
    private Long inventoryConfigurationId;
    
    @Column(name = "[INV_IID]", nullable = false)
    private Long controllerInstanceId;
    
    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;
    
    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[SIGNATURE]", nullable = false)
    private String signedContent;

    @Column(name = "[COMMIT_ID]", nullable = false)
    private String commitId;

    @Column(name = "[VERSION]", nullable = false)
    private String version;
    
    /* ADD, UPDATE, DELETE */
    @Column(name = "[OPERATION]", nullable = false)
    private Integer operation;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "[DELETED_DATE]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepHistoryId() {
        return depHistoryId;
    }
    public void setDepHistoryId(Long depHistoryId) {
        this.depHistoryId = depHistoryId;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getFolder() {
        if (folder == null || folder.isEmpty()) {
            folder = Paths.get(path).getParent().toString().replaceAll("\\\\", "/");
        }
        return folder;
    }
    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
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
    
    public Long getControllerInstanceId() {
        return controllerInstanceId;
    }
    public void setControllerInstanceId(Long controllerInstanceId) {
        this.controllerInstanceId = controllerInstanceId;
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
    
    public String getSignedContent() {
        return signedContent;
    }
    public void setSignedContent(String signedContent) {
        this.signedContent = signedContent;
    }
    
    public String getCommitId() {
        return commitId;
    }
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
    
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Integer getOperation() {
        return operation;
    }
    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }
    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

}
