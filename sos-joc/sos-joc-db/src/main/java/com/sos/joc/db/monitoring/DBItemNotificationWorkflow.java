package com.sos.joc.db.monitoring;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_NOTIFICATION_WORKFLOWS)
public class DBItemNotificationWorkflow extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[NOT_ID]", nullable = false)
    private Long notificationId;

    @Column(name = "[MON_ORDER_ID]", nullable = false)
    private Long orderId;

    @Column(name = "[MON_ORDER_STEP_ID]", nullable = false)
    private Long stepId;

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition;

    public DBItemNotificationWorkflow() {
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long val) {
        notificationId = val;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long val) {
        orderId = val;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long val) {
        if (val == null) {
            val = 0L;
        }
        stepId = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }
}
