package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOWS)
public class DBItemInventoryWorkflow extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CONFIG_ID]", nullable = false)
    private Long configId;

    @Column(name = "[CONFIGURATION]", nullable = false)
    private String configuration;

    @Column(name = "[CONFIGURATION_JOC]", nullable = false)
    private String configurationJoc;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long val) {
        configId = val;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String val) {
        configuration = val;
    }

    public String getConfigurationJoc() {
        return configurationJoc;
    }

    public void setConfigurationJoc(String val) {
        configurationJoc = val;
    }
}
