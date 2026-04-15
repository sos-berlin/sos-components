package com.sos.joc.db.monitoring;

import java.util.Date;

import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.history.JobWarning;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.MonitoringConstants;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.OrderNotificationRange;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = DBLayer.TABLE_MON_NOTIFICATIONS)
public class DBItemNotification extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_MON_NOTIFICATIONS_SEQUENCE)
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
    @Convert(converter = NumericBooleanConverter.class)
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
            val = MonitoringConstants.NOTIFICATION_DEFAULT_TYPE.intValue();
        }
        type = val;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer val) {
        if (val == null) {
            val = MonitoringConstants.NOTIFICATION_DEFAULT_RANGE.intValue();
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
            val = MonitoringConstants.NOTIFICATION_DEFAULT_JOB_WARNING.intValue();
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
            return MonitoringConstants.NOTIFICATION_DEFAULT_TYPE;
        }
    }

    @Transient
    public void setType(NotificationType val) {
        setType(val == null ? null : val.intValue());
    }

    @Transient
    public OrderNotificationRange getRangeAsEnum() {
        try {
            return OrderNotificationRange.fromValue(range);
        } catch (Throwable e) {
            return MonitoringConstants.NOTIFICATION_DEFAULT_RANGE;
        }
    }

    @Transient
    public void setRange(OrderNotificationRange val) {
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
            return MonitoringConstants.NOTIFICATION_DEFAULT_JOB_WARNING;
        }
    }
}
