package com.sos.joc.db.inventory.items;

import java.util.Date;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

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
    private Date dhDeploymentDate;
    private String dhPath;
    private String dhControllerId;

    private InventoryDeploymentItem deployment;
    private DBItemInventoryConfiguration configuration;

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
        if (dhId != null) {
            deployment = new InventoryDeploymentItem(dhId, dhCommitId, dhVersion, dhOperation, dhDeploymentDate, null, dhPath, dhControllerId);
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
