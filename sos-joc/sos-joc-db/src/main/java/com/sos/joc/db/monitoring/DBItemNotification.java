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

import org.hibernate.annotations.Type;

import com.sos.history.JobWarning;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.MonitoringConstants;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

@Entity
@Table(name = DBLayer.TABLE_MON_NOTIFICATIONS)
@SequenceGenerator(name = DBLayer.TABLE_MON_NOTIFICATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_MON_NOTIFICATIONS_SEQUENCE, allocationSize = 1)
public class DBItemNotification extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_MON_NOTIFICATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[RANGE]", nullable = false)
    private Integer range;

    @Column(name = "[NOTIFICATION_ID]", nullable = false)
    private String notificationId;

    @Column(name = "[RECOVERED_ID]", nullable = false)
    private Long recoveredId;// reference ID

    @Column(name = "[HAS_MONITORS]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean hasMonitors;

    @Column(name = "[WARN]", nullable = false)
    private Integer warn;

    @Column(name = "[WARN_TEXT]", nullable = true)
    private String warnText;

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

    public void setType(Integer val) {
        if (val == null) {
            val = NotificationType.ERROR.intValue();
        }
        type = val;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer val) {
        if (val == null) {
            val = NotificationRange.WORKFLOW.intValue();
        }
        range = val;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String val) {
        notificationId = val;
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

    public void setHasMonitors(boolean val) {
        hasMonitors = val;
    }

    public boolean getHasMonitors() {
        return hasMonitors;
    }

    public void setWarn(Integer val) {
        if (val == null) {
            val = JobWarning.NONE.intValue();
        }
        warn = val;
    }

    public Integer getWarn() {
        return warn;
    }

    public void setWarnText(String val) {
        warnText = normalizeWarnText(val);
    }

    public String getWarnText() {
        return warnText;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    @Transient
    public NotificationType getTypeAsEnum() {
        try {
            return NotificationType.fromValue(type);
        } catch (Throwable e) {
            return NotificationType.ERROR;
        }
    }

    @Transient
    public void setType(NotificationType val) {
        setType(val == null ? null : val.intValue());
    }

    @Transient
    public NotificationRange getRangeAsEnum() {
        try {
            return NotificationRange.fromValue(range);
        } catch (Throwable e) {
            return NotificationRange.WORKFLOW;
        }
    }

    @Transient
    public void setRange(NotificationRange val) {
        setRange(val == null ? null : val.intValue());
    }

    @Transient
    public static String normalizeWarnText(String val) {
        return normalizeValue(val, MonitoringConstants.MAX_LEN_WARN_TEXT);
    }

    @Transient
    public void setWarn(JobWarning val) {
        setWarn(val == null ? null : val.intValue());
    }

    @Transient
    public JobWarning getWarnAsEnum() {
        try {
            return JobWarning.fromValue(warn);
        } catch (IllegalArgumentException e) {
            return JobWarning.NONE;
        }
    }
}
