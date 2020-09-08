package com.sos.joc.db.deployment;

import java.nio.file.Paths;
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

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DEP_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_DEP_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_DEP_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemDeploymentHistory extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DEP_HISTORY_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

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

    /* DEPLOYED, NOT_DEPLOYED */
    @Column(name = "[STATE]", nullable = false)
    private Integer state;

    @Column(name = "[ERROR_MESSAGE]", nullable = true)
    private String errorMessage;

    @Column(name = "[DEPLOYMENT_DATE]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deploymentDate;

    @Column(name = "[DELETED_DATE]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
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

    public Integer getState() {
        return state;
    }
    public void setState(Integer state) {
        this.state = state;
    }

    public Date getDeploymentDate() {
        return deploymentDate;
    }
    public void setDeploymentDate(Date deploymentDate) {
        this.deploymentDate = deploymentDate;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        if (errorMessage != null && errorMessage.length() > 255) {
            errorMessage = errorMessage.substring(0, 254);
        }
        this.errorMessage = errorMessage;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }
    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

}
