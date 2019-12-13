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
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[OBJECT_ID]", "[CONFIGURATION_ID]", "[OPERATION]" }) })
public class DBItemJSOperationHistory extends DBItem {

	private static final long serialVersionUID = 1L;

    @Column(name = "[OBJECT_ID]", nullable = false)
    @Id
    private Long objectId;

    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    @Id
    private Long configurationId;

    @Column(name = "[OPERATION]", nullable = false)
    @Id
    private String operation;

	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long val) {
		this.objectId = val;
	}
	
	public Long getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(Long val) {
		this.configurationId = val;
	}

	public String getOperation() {
		return operation;
	}
	public void setOperation(String val) {
		this.operation = val;
	}
}
