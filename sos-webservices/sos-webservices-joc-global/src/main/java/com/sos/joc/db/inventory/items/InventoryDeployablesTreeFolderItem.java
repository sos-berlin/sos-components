package com.sos.joc.db.inventory.items;

import java.util.Date;

public class InventoryDeployablesTreeFolderItem {

    private Long configId;
    private String configPath;
    private String configFolder;
    private String configName;
    private Integer configType;
    private Date configModified;
    private Long deploymentId;
    private String deploymentVersion;
    private Date deploymentDate;

    public InventoryDeployablesTreeFolderItem(Long configId, String configPath, String configFolder, String configName, Integer configType,
            Date configModified, Long deploymentId, String deploymentVersion, Date deploymentDate) {
        this.configId = configId;
        this.configPath = configPath;
        this.configFolder = configFolder;
        this.configName = configName;
        this.configModified = configModified;

        this.deploymentId = deploymentId;
        this.deploymentVersion = deploymentVersion;
        this.deploymentDate = deploymentDate;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long val) {
        configId = val;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String val) {
        configPath = val;
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public void setConfigFolder(String val) {
        configFolder = val;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String val) {
        configName = val;
    }

    public Integer getConfigType() {
        return configType;
    }

    public void setConfigType(Integer val) {
        configType = val;
    }

    public Date getConfigModified() {
        return configModified;
    }

    public void setConfigModified(Date val) {
        configModified = val;
    }

    public Long getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(Long val) {
        deploymentId = val;
    }

    public String getDeploymentVersion() {
        return deploymentVersion;
    }

    public void setDeploymentVersion(String val) {
        deploymentVersion = val;
    }

    public Date getDeploymentDate() {
        return deploymentDate;
    }

    public void setDeploymentDate(Date val) {
        deploymentDate = val;
    }

}
