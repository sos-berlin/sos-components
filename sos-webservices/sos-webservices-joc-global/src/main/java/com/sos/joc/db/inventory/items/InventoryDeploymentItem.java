package com.sos.joc.db.inventory.items;

import java.util.Date;

public class InventoryDeploymentItem {

    private Long id;
    private String commitId;
    private String version;
    private Integer operation;
    private Date deploymentDate;
    private String content;
    private String controllerId;
    private String path;
    
    public InventoryDeploymentItem(Long deploymentId, String commitId, String deploymentVersion, Integer deploymentOperation, Date deploymentDate, String path,
            String controllerId) {
        this.id = deploymentId;
        this.commitId = commitId;
        this.version = deploymentVersion;
        this.operation = deploymentOperation;
        this.deploymentDate = deploymentDate;
        this.content = null;
        this.path = path;
        this.controllerId = controllerId;
    }

    public InventoryDeploymentItem(Long deploymentId, String commitId, String deploymentVersion, Integer deploymentOperation, Date deploymentDate, String content,
            String path, String controllerId) {
        this.id = deploymentId;
        this.commitId = commitId;
        this.version = deploymentVersion;
        this.operation = deploymentOperation;
        this.deploymentDate = deploymentDate;
        this.content = content;
        this.path = path;
        this.controllerId = controllerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }
    
    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String val) {
        commitId = val;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
        version = val;
    }

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer val) {
        operation = val;
    }

    public Date getDeploymentDate() {
        return deploymentDate;
    }

    public void setDeploymentDate(Date val) {
        deploymentDate = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

}
