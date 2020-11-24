package com.sos.joc.db.inventory.items;

import java.util.Date;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class InventoryDeployablesTreeFolderItem {

    private Long id;
    private String path;
    private String folder;
    private String name;
    private Integer type;
    private boolean valid;
    private boolean deleted;
    private boolean deployed;
    private Date modified;

    private InventoryDeploymentItem deployment;
    private DBItemInventoryConfiguration configuration;
    
    public InventoryDeployablesTreeFolderItem(DBItemInventoryConfiguration conf, Long deploymentId, String commitId, String deploymentVersion,
            Integer deploymentOperation, Date deploymentDate, String deploymentPath, String controllerId) {
        if (conf != null) {
            conf.setContent(null);
        }
        configuration = conf;
        if (deploymentId != null) {
            deployment = new InventoryDeploymentItem(deploymentId, commitId, deploymentVersion, deploymentOperation, deploymentDate, null,
                    deploymentPath, controllerId);
        }
    }

    public InventoryDeployablesTreeFolderItem(Long configId, String configPath, String configFolder, String configName, Integer configType,
            boolean configValid, boolean configDeleted, boolean configDeployed, Date configModified, Long deploymentId, String commitId,
            String deploymentVersion, Integer deploymentOperation, Date deploymentDate, String deploymentPath, String controllerId) {
        id = configId;
        path = configPath;
        folder = configFolder;
        name = configName;
        type = configType;
        valid = configValid;
        deleted = configDeleted;
        deployed = configDeployed;
        modified = configModified;

        if (deploymentId != null) {
            deployment = new InventoryDeploymentItem(deploymentId, commitId, deploymentVersion, deploymentOperation, deploymentDate, null,
                    deploymentPath, controllerId);
        }
    }
    
    public DBItemInventoryConfiguration getConfiguration() {
        return configuration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer val) {
        type = val;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean val) {
        valid = val;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean val) {
        deleted = val;
    }

    public boolean getDeployed() {
        return deployed;
    }

    public void setDeployed(boolean val) {
        deployed = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public InventoryDeploymentItem getDeployment() {
        return deployment;
    }

    public void setDeployment(InventoryDeploymentItem val) {
        deployment = val;
    }

}
