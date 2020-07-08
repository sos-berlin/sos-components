package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_JOB_CLASSES)
public class DBItemInventoryJobClass extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CONFIG_ID]", nullable = false)
    private Long configId;

    @Column(name = "[MAX_PROCESSES]", nullable = false)
    private Long maxProcesses;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long val) {
        configId = val;
    }

    public Long getMaxProcesses() {
        return maxProcesses;
    }

    public void setMaxProcesses(Long val) {
        if (val == null) {
            val = 0L;
        }
        maxProcesses = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
    }
}
