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
    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[MAX_PROCESSES]", nullable = false)
    private Integer maxProcesses;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public Integer getMaxProcesses() {
        return maxProcesses;
    }

    public void setMaxProcesses(Integer val) {
        if (val == null) {
            val = 0;
        }
        maxProcesses = val;
    }
}
