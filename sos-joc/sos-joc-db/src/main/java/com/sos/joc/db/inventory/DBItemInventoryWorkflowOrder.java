package com.sos.joc.db.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_WORKFLOW_ORDERS)
public class DBItemInventoryWorkflowOrder extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[CID_WORKFLOW]", nullable = false)
    private Long cidWorkflow;

    @Column(name = "[CID_CALENDAR]", nullable = false)
    private Long cidCalendar;

    @Column(name = "[CID_NW_CALENDAR]", nullable = false)
    private Long cidNwCalendar;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
    }

    public Long getCidWorkflow() {
        return cidWorkflow;
    }

    public void setCidWorkflow(Long val) {
        cidWorkflow = val;
    }

    public Long getCidCalendar() {
        return cidCalendar;
    }

    public void setCidCalendar(Long val) {
        cidCalendar = val;
    }

    public Long getCidNwCalendar() {
        return cidNwCalendar;
    }

    public void setCidNwCalendar(Long val) {
        cidNwCalendar = val;
    }

}
