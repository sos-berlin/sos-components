package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOIN_INV_JS_DEP_CFG_HISTORY, uniqueConstraints = { @UniqueConstraint(columnNames = { "[JOBSCHEDULER_ID]" }) })
public class DBItemJoinJSDepCfgHistory extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[JOBSCHEDULER_ID]", nullable = false)
    private String jobschedulerId;

    @Column(name = "[CONFIGURATION_ID]", nullable = false)
    private Long configurationId;

    public String getJobschedulerId() {
        return jobschedulerId;
    }

    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Long val) {
        this.configurationId = val;
    }

}
