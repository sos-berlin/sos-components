package com.sos.joc.db.monitoring;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

@Entity
@Table(name = DBLayer.TABLE_NOTIFICATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[TYPE]", "[RANGE]", "[MON_ORDER_ID]",
        "[MON_ORDER_STEP_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_NOTIFICATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_NOTIFICATIONS_SEQUENCE, allocationSize = 1)
public class DBItemNotification extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_NOTIFICATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[RANGE]", nullable = false)
    private Integer range;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[MON_ORDER_ID]", nullable = false)
    private Long orderId;

    @Column(name = "[MON_ORDER_STEP_ID]", nullable = false)
    private Long stepId;

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition;

    @Column(name = "[RECOVERED_ID]", nullable = false)
    private Long recoveredId;// reference ID

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemNotification() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Integer getType() {
        return type;
    }

    @Transient
    public NotificationType getTypeAsEnum() {
        try {
            return NotificationType.fromValue(type);
        } catch (Throwable e) {
            return NotificationType.ERROR;
        }
    }

    public void setType(Integer val) {
        if (val == null) {
            val = NotificationType.ERROR.intValue();
        }
        type = val;
    }

    @Transient
    public void setType(NotificationType val) {
        setType(val == null ? null : val.intValue());
    }

    public Integer getRange() {
        return range;
    }

    @Transient
    public NotificationRange getRangeAsEnum() {
        try {
            return NotificationRange.fromValue(range);
        } catch (Throwable e) {
            return NotificationRange.WORKFLOW;
        }
    }

    public void setRange(Integer val) {
        if (val == null) {
            val = NotificationRange.WORKFLOW.intValue();
        }
        range = val;
    }

    @Transient
    public void setRange(NotificationRange val) {
        setRange(val == null ? null : val.intValue());
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
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

    public Long getRecoveredId() {
        return recoveredId;
    }

    public void setRecoveredId(Long val) {
        if (val == null) {
            val = 0L;
        }
        recoveredId = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

}
