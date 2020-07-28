package com.sos.joc.db.deployment;

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

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    private Integer objectType;

    @Column(name = "[INV_CID]", nullable = false)
    private Long inventoryConfigurationId;
    
    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private Long controllerId;
    
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

    @Column(name = "[DEPLOYMENT_DATE]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deploymentDate;

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
    
    public Integer getObjectType() {
        return objectType;
    }
    public void setObjectType(Integer objectType) {
        this.objectType = objectType;
    }

    public Long getInventoryConfigurationId() {
        return inventoryConfigurationId;
    }
    public void setInventoryConfigurationId(Long inventoryConfigurationId) {
        this.inventoryConfigurationId = inventoryConfigurationId;
    }
    
    public Long getControllerId() {
        return controllerId;
    }
    public void setControllerId(Long controllerId) {
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

    public Date getDeploymentDate() {
        return deploymentDate;
    }
    public void setDeploymentDate(Date deploymentDate) {
        this.deploymentDate = deploymentDate;
    }

}
