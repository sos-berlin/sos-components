package com.sos.joc.db.inventory.items;

import java.util.Date;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.publish.DeploymentState;

public class InventoryDeployablesTreeFolderItem {

    private Long icId;
    private Integer icType;
    private String icPath;
    private String icName;
    private String icFolder;
    private String icTitle;
    private boolean icValid;
    private boolean icDeleted;
    private boolean icDeployed;
    private boolean icReleased;
    private Long icAuditLogId;
    private Date icCreated;
    private Date icModified;

    private Long dhId;
    private String dhCommitId;
    private String dhVersion;
    private Integer dhOperation;
    private Integer dhState;
    private Date dhDeploymentDate;
    private String dhPath;
    private String dhControllerId;

    private InventoryDeploymentItem deployment;
    private DBItemInventoryConfiguration configuration;
    
    
    public void setIcId(Long icId) {
        this.icId = icId;
    }
    
    public void setIcType(Integer icType) {
        this.icType = icType;
    }

    public void setIcPath(String icPath) {
        this.icPath = icPath;
    }
    
    public void setIcName(String icName) {
        this.icName = icName;
    }
    
    public void setIcFolder(String icFolder) {
        this.icFolder = icFolder;
    }
    
    public void setIcTitle(String icTitle) {
        this.icTitle = icTitle;
    }
    
    public void setIcValid(boolean icValid) {
        this.icValid = icValid;
    }
    
    public void setIcDeleted(boolean icDeleted) {
        this.icDeleted = icDeleted;
    }
    
    public void setIcDeployed(boolean icDeployed) {
        this.icDeployed = icDeployed;
    }

    public void setIcReleased(boolean icReleased) {
        this.icReleased = icReleased;
    }
    
    public void setIcAuditLogId(Long icAuditLogId) {
        this.icAuditLogId = icAuditLogId;
    }
    
    public void setIcCreated(Date icCreated) {
        this.icCreated = icCreated;
    }
    
    public void setIcModified(Date icModified) {
        this.icModified = icModified;
    }
    
    public void setDhId(Long dhId) {
        this.dhId = dhId;
    }

    public void setDhCommitId(String dhCommitId) {
        this.dhCommitId = dhCommitId;
    }
    
    public void setDhVersion(String dhVersion) {
        this.dhVersion = dhVersion;
    }
    
    public void setDhOperation(Integer dhOperation) {
        this.dhOperation = dhOperation;
    }
    
    public void setDhState(Integer dhState) {
        this.dhState = dhState;
    }

    public void setDhDeploymentDate(Date dhDeploymentDate) {
        this.dhDeploymentDate = dhDeploymentDate;
    }

    public void setDhPath(String dhPath) {
        this.dhPath = dhPath;
    }

    public void setDhControllerId(String dhControllerId) {
        this.dhControllerId = dhControllerId;
    }
    
    public InventoryDeployablesTreeFolderItem map() {
        if (icId != null) {
            configuration = new DBItemInventoryConfiguration();
            configuration.setId(icId);
            configuration.setType(icType);
            configuration.setPath(icPath);
            configuration.setName(icName);
            configuration.setFolder(icFolder);
            configuration.setTitle(icTitle);
            configuration.setContent(null);
            configuration.setValid(icValid);
            configuration.setDeleted(icDeleted);
            configuration.setDeployed(icDeployed);
            configuration.setReleased(icReleased);
            configuration.setAuditLogId(icAuditLogId);
            configuration.setCreated(icCreated);
            configuration.setModified(icModified);
        }
        if (dhId != null && DeploymentState.DEPLOYED.value().equals(dhState) ) {
            deployment = new InventoryDeploymentItem(dhId, dhCommitId, dhVersion, dhOperation, dhDeploymentDate, dhPath, dhControllerId);
        }
        return this;
    }

    public InventoryDeploymentItem getDeployment() {
        return deployment;
    }

    public DBItemInventoryConfiguration getConfiguration() {
        return configuration;
    }
}
