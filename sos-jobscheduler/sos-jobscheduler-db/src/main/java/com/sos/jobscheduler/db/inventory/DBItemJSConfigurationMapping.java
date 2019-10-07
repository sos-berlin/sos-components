package com.sos.jobscheduler.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_CONFIGURATION_MAPPING, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[WORKFLOW_ID]", "[CONFIGURATION_ID]", "[VERSION]" }) })
public class DBItemJSConfigurationMapping extends DBItem {

	private static final long serialVersionUID = 1L;

    @Column(name = "[WORKFLOW_ID]", nullable = false)
    private Long workflowId;

    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    private Long configurationId;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;

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

	public String getSchedulerId() {
		return schedulerId;
	}
	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

}
