package com.sos.joc.db.monitoring;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.MonitoringConstants;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.SystemNotificationCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = DBLayer.TABLE_MON_SYSNOTIFICATIONS)
@SequenceGenerator(name = DBLayer.TABLE_MON_SYSNOTIFICATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_MON_SYSNOTIFICATIONS_SEQUENCE, allocationSize = 1)
public class DBItemSystemNotification extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_MON_SYSNOTIFICATIONS_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_MON_SYSNOTIFICATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[CATEGORY]", nullable = false)
    private Integer category;

    @Column(name = "[JOC_ID]", nullable = false)
    private String jocId;

    @Column(name = "[SOURCE]", nullable = false)
    private String source;

    @Column(name = "[NOTIFIER]", nullable = false)
    private String notifier;

    @Column(name = "[TIME]", nullable = false)
    private Date time;

    @Column(name = "[MESSAGE]", nullable = true)
    private String message;

    @Column(name = "[EXCEPTION]", nullable = true)
    private String exception;

    @Column(name = "[HAS_MONITORS]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean hasMonitors;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemSystemNotification() {
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

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer val) {
        if (val == null) {
            val = MonitoringConstants.SYSTEM_NOTIFICATION_DEFAULT_CATEGORY.intValue();
        }
        category = val;
    }

    public String getJocId() {
        return jocId;
    }

    public void setJocId(String val) {
        jocId = normalizeJocId(val);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String val) {
        source = normalizeSource(val);
    }

    public void setNotifier(String val) {
        notifier = normalizeNotifier(val);
    }

    public String getNotifier() {
        return notifier;
    }

    public void setTime(Date val) {
        if (val == null) {
            val = new Date();
        }
        time = val;
    }

    public Date getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        message = normalizeMessage(val);
    }

    public String getException() {
        return exception;
    }

    public void setException(String val) {
        exception = normalizeException(val);
    }

    public void setHasMonitors(boolean val) {
        hasMonitors = val;
    }

    public boolean getHasMonitors() {
        return hasMonitors;
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
    public SystemNotificationCategory getCategoryAsEnum() {
        try {
            return SystemNotificationCategory.fromValue(category);
        } catch (Throwable e) {
            return MonitoringConstants.SYSTEM_NOTIFICATION_DEFAULT_CATEGORY;
        }
    }

    @Transient
    public void setCategory(SystemNotificationCategory val) {
        setCategory(val == null ? null : val.intValue());
    }

    @Transient
    private static String normalizeJocId(String val) {
        if (val == null) {
            return MonitoringConstants.SYSTEM_NOTIFICATION_DEFAULT_JOC_ID;
        }
        return normalizeValue(val, MonitoringConstants.MAX_LEN_SYSTEM_NOTIFICATION_JOC_ID);
    }

    @Transient
    private static String normalizeSource(String val) {
        if (val == null) {
            return DBLayer.DEFAULT_KEY;
        }
        return normalizeValue(val, MonitoringConstants.MAX_LEN_SYSTEM_NOTIFICATION_SOURCE);
    }

    @Transient
    private static String normalizeNotifier(String val) {
        if (val == null) {
            return DBLayer.DEFAULT_KEY;
        }
        return normalizeValue(val, MonitoringConstants.MAX_LEN_SYSTEM_NOTIFICATION_NOTIFIER);
    }

    @Transient
    private static String normalizeMessage(String val) {
        if (SOSString.isEmpty(val)) {
            return null;
        }
        return normalizeValue(val, MonitoringConstants.MAX_LEN_SYSTEM_NOTIFICATION_MESSAGE, MonitoringConstants.MAX_LEN_DEFAULT_SUFFIX);
    }

    @Transient
    private static String normalizeException(String val) {
        if (SOSString.isEmpty(val)) {
            return null;
        }
        return normalizeValue(val, MonitoringConstants.MAX_LEN_SYSTEM_NOTIFICATION_EXCEPTION, MonitoringConstants.MAX_LEN_DEFAULT_SUFFIX);
    }
}
