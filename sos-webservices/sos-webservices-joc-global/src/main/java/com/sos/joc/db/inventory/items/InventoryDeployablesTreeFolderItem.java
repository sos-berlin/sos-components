package com.sos.joc.db.inventory.items;

import java.util.Date;

public class InventoryDeployablesTreeFolderItem {

    private Long id;
    private String path;
    private String folder;
    private String name;
    private Integer type;
    private boolean valide;
    private boolean deleted;
    private boolean deployed;
    private Date modified;

    private InventoryDeploymentItem deployment;

    public InventoryDeployablesTreeFolderItem(Long configId, String configPath, String configFolder, String configName, Integer configType,
            boolean configValide, boolean configDeleted, boolean configDeployed, Date configModified, Long deploymentId, String deploymentVersion,
            Integer deploymentOperation, Date deploymentDate, String deploymentPath, String controllerId) {
        id = configId;
        path = configPath;
        folder = configFolder;
        name = configName;
        type = configType;
        valide = configValide;
        deleted = configDeleted;
        deployed = configDeployed;
        modified = configModified;

        if (controllerId != null) {
            deployment = new InventoryDeploymentItem(deploymentId, deploymentVersion, deploymentOperation, deploymentDate, null, deploymentPath,
                    controllerId);
        }
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

    public boolean getValide() {
        return valide;
    }

    public void setValide(boolean val) {
        valide = val;
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
