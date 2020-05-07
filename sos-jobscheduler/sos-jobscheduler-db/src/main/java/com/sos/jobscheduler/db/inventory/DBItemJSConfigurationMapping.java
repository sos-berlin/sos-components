package com.sos.jobscheduler.db.inventory;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Embeddable
@Table( name = DBLayer.TABLE_JS_CONFIGURATION_MAPPING, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONFIGURATION_ID]", "[OBJECT_ID]" }) })
public class DBItemJSConfigurationMapping extends DBItem {

	private static final long serialVersionUID = 1L;

    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    private Long configurationId;

    @Column(name = "[OBJECT_ID]", nullable = false)
    private Long objectId;

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

}
