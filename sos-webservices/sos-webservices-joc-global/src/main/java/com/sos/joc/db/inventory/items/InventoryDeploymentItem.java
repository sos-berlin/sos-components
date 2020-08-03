package com.sos.joc.db.inventory.items;

import java.util.Date;

public class InventoryDeploymentItem {

    private Long id;
    private String version;
    private Date deploymentDate;
    private String content;
    private String controllerId;

    public InventoryDeploymentItem(Long deploymentId, String deploymentVersion, Date deploymentDate, String content, String controllerId) {
        this.id = deploymentId;
        this.version = deploymentVersion;
        this.deploymentDate = deploymentDate;
        this.content = content;
        this.controllerId = controllerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
        version = val;
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

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

}
