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
    @Column(name = "[DEP_HID]", nullable = false) /* DEP_HISTORY.ID */
    private Long deploymentHistoryId;

    public Long getSearchWorkflowId() {
        return searchWorkflowId;
    }

    public void setSearchWorkflowId(Long val) {
        searchWorkflowId = val;
    }

    public Long getDeploymentHistoryId() {
        return deploymentHistoryId;
    }

    public void setDeploymentHistoryId(Long val) {
        deploymentHistoryId = val;
    }
}
