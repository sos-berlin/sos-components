package com.sos.jobscheduler.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_CONFIG_TO_SCHEDULER_MAPPING, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONFIGURATION_ID]", "[SCHEDULER_ID]" }) })
public class DBItemJSConfigToSchedulerMapping extends DBItem {

	private static final long serialVersionUID = 1L;

    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    @Id
    private Long configurationId;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    @Id
    private String schedulerId;

	public Long getConfigurationId() {
		return configurationId;
	}
	public void setConfigurationId(Long val) {
		this.configurationId = val;
	}

    public String getSchedulerId() {
		return schedulerId;
	}
	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

}
