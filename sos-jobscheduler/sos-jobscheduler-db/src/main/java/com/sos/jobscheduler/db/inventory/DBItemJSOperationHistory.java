package com.sos.jobscheduler.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_OPERATION_HISTORY, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[WORKFLOW_ID]", "[CONFIGURATION_ID]", "[OPERATION]" }) })
public class DBItemJSOperationHistory extends DBItem {

	private static final long serialVersionUID = 1L;

    @Column(name = "[WORKFLOW_ID]", nullable = false)
    @Id
    private Long workflowId;

    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    @Id
    private Long configurationId;

    @Column(name = "[OPERATION]", nullable = false)
    @Id
    private String operation;

	public Long getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(Long workflowId) {
		this.workflowId = workflowId;
	}
	
	public Long getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(Long configurationId) {
		this.configurationId = configurationId;
	}

	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}

}
