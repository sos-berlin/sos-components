package com.sos.jobscheduler.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_CONFIG_TO_SCHEDULER_MAPPING)
public class DBItemJSConfigToSchedulerMapping extends DBItem {

	private static final long serialVersionUID = 1L;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    @Id
    private String schedulerId;

    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    @Id
    private Long configurationId;

    public String getSchedulerId() {
        return schedulerId;
    }
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

	public Long getConfigurationId() {
		return configurationId;
	}
	public void setConfigurationId(Long val) {
		this.configurationId = val;
	}

}
