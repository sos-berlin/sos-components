package com.sos.jobscheduler.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JOIN_DEP_CFG_DEP_CFG_HISTORY, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONFIGURATION_ID]", "[OBJECT_ID]", "[OPERATION]" }) })
public class DBItemJoinDepCfgDepCfgHistory extends DBItem {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    private Long configurationId;

    @Column(name = "[OBJECT_ID]", nullable = false)
    private Long objectId;

    @Column(name = "[OPERATION]", nullable = false)
    private String operation;

    public Long getConfigurationId() {
        return configurationId;
    }
    public void setConfigurationId(Long val) {
        this.configurationId = val;
    }

    public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long val) {
		this.objectId = val;
	}

    public String getOperation() {
        return operation;
    }
    public void setOperation(String val) {
        this.operation = val;
    }

}
