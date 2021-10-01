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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.common.IDeployObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

@Entity
@Table(name = DBLayer.TABLE_DEP_HISTORY, uniqueConstraints = { @UniqueConstraint(columnNames = { "[NAME]", "[TYPE]", "[CONTROLLER_ID]",
        "[COMMIT_ID]" }) })
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

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

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

    @Column(name = "[INV_CONTENT]", nullable = false)
    private String invContent;

    @Transient
    private IDeployObject updateableContent;

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
    private Date deleteDate;

    @Column(name = "[AUDITLOG_ID]", nullable = false)
    private Long auditlogId;

    @Transient
    private long workflowCount = 0L;
    
    @Transient
    private long lockCount = 0L;
    
    @Transient
    private long fosCount = 0L;
    
    @Transient
    private long jobResourceCount = 0L;
    
    @Transient
    private long boardCount = 0L;

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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String val) {
        title = val;
    }

    public String getFolder() {
        if ((folder == null || folder.isEmpty()) && (path != null && !path.isEmpty())) {
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

    public Date getDeleteDate() {
        return deleteDate;
    }
    public void setDeleteDate(Date deletedDate) {
        this.deleteDate = deletedDate;
    }

    public String getInvContent() {
        return invContent;
    }
    public void setInvContent(String invContent) {
        this.invContent = invContent;
    }

    public Long getAuditlogId() {
        return auditlogId;
    }
    public void setAuditlogId(Long auditlogId) {
        this.auditlogId = auditlogId;
    }

    @Transient
    public IDeployObject readUpdateableContent() {
        return updateableContent;
    }
    @Transient
    public void writeUpdateableContent(IDeployObject updateableContent) {
        this.updateableContent = updateableContent;
    }

    @Transient
    public DeployType getTypeAsEnum() {
        try {
            return DeployType.fromValue(type);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Transient
    public long getWorkflowCount() {
        return workflowCount;
    }
    @Transient
    public void setWorkflowCount(long workflowCount) {
        this.workflowCount = workflowCount;
    }
    
    @Transient
    public long getLockCount() {
        return lockCount;
    }
    @Transient
    public void setLockCount(long lockCount) {
        this.lockCount = lockCount;
    }
    
    @Transient
    public long getFosCount() {
        return fosCount;
    }
    @Transient
    public void setFosCount(long fosCount) {
        this.fosCount = fosCount;
    }
    
    @Transient
    public long getJobResourceCount() {
        return jobResourceCount;
    }
    @Transient
    public void setJobResourceCount(long jobResourceCount) {
        this.jobResourceCount = jobResourceCount;
    }
    
    @Transient
    public long getBoardCount() {
        return boardCount;
    }
    @Transient
    public void setBoardCount(long boardCount) {
        this.boardCount = boardCount;
    }

}
