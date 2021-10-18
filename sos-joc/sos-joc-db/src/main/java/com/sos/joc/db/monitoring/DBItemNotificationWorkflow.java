package com.sos.joc.db.monitoring;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_MON_NOT_WORKFLOWS)
public class DBItemNotificationWorkflow extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[NOT_ID]", nullable = false)
    private Long notificationId;

    @Column(name = "[MON_O_HISTORY_ID]", nullable = false)
    private Long orderHistoryId;

    @Column(name = "[MON_OS_HISTORY_ID]", nullable = false)
    private Long orderStepHistoryId;

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

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    public Long getOrderStepHistoryId() {
        return orderStepHistoryId;
    }

    public void setOrderStepHistoryId(Long val) {
        if (val == null) {
            val = 0L;
        }
        orderStepHistoryId = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }
}
