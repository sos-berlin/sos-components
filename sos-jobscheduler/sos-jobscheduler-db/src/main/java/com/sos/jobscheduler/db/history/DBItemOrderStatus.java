package com.sos.jobscheduler.db.history;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.HISTORY_TABLE_ORDER_STATUS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_ORDER_STATUS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_ORDER_STATUS_SEQUENCE, allocationSize = 1)
public class DBItemOrderStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_ORDER_STATUS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[MASTER_ID]", nullable = false)
    private String masterId;

    @Column(name = "[ORDER_KEY]", nullable = false)
    private String orderKey;// event TODO redundant?

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;// event

    @Column(name = "[WORKFLOW_VERSION_ID]", nullable = false)
    private String workflowVersionId; // event

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition; // event

    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID */
    @Column(name = "[MAIN_ORDER_ID]", nullable = false)
    private Long mainOrderId;// db

    @Column(name = "[ORDER_ID]", nullable = false)
    private Long orderId;// db

    @Column(name = "[ORDER_STEP_ID]", nullable = false)
    private Long orderStepId;// db

    /** Others */
    @Column(name = "[STATUS]", nullable = false)
    private String status;/* started, cancelled, stopped, suspended, finished... */

    @Column(name = "[STATUS_TIME]", nullable = false)
    private Date statusTime;

    @Column(name = "[CONSTRAINT_HASH]", nullable = false)
    private String constraintHash;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemOrderStatus() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String val) {
        masterId = val;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String val) {
        orderKey = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getWorkflowVersionId() {
        return workflowVersionId;
    }

    public void setWorkflowVersionId(String val) {
        workflowVersionId = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }

    public Long getMainOrderId() {
        return mainOrderId;
    }

    public void setMainOrderId(Long val) {
        mainOrderId = val;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long val) {
        orderId = val;
    }

    public Long getOrderStepId() {
        return orderStepId;
    }

    public void setOrderStepId(Long val) {
        orderStepId = val;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String val) {
        status = val;
    }

    public void setStatusTime(Date val) {
        statusTime = val;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public String getConstraintHash() {
        return constraintHash;
    }

    public void setConstraintHash(String val) {
        constraintHash = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemOrderStatus)) {
            return false;
        }
        DBItemOrderStatus item = (DBItemOrderStatus) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
