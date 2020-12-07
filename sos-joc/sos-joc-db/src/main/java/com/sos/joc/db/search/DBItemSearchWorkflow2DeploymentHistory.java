package com.sos.joc.db.search;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY)
public class DBItemSearchWorkflow2DeploymentHistory extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[SEARCH_WID]", nullable = false) /* SEARCH_WORKFLOWS.ID */
    private Long searchWorkflowId;

    @Id
    @Column(name = "[INV_CID]", nullable = false) /* INV_CONFIGURATIONS.ID */
    private Long inventoryConfigurationId;

    @Id
    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[DEP_HID]", nullable = false) /* DEP_HISTORY.ID */
    private Long deploymentHistoryId;

    public Long getSearchWorkflowId() {
        return searchWorkflowId;
    }

    public void setSearchWorkflowId(Long val) {
        searchWorkflowId = val;
    }

    public Long getInventoryConfigurationId() {
        return inventoryConfigurationId;
    }

    public void setInventoryConfigurationId(Long val) {
        inventoryConfigurationId = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public Long getDeploymentHistoryId() {
        return deploymentHistoryId;
    }

    public void setDeploymentHistoryId(Long val) {
        deploymentHistoryId = val;
    }
}
