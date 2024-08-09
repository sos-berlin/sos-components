package com.sos.joc.db.monitoring;

import java.util.Date;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.HistoryConstants;
import com.sos.joc.db.common.MonitoringConstants;
import com.sos.monitoring.MonitorType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_MON_NOT_MONITORS)
@Proxy(lazy = false)
public class DBItemNotificationMonitor extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_MON_NOT_MONITORS_SEQUENCE)
    private Long id;

    @Column(name = "[NOT_ID]", nullable = false)
    private Long notificationId;

    /** 0 - MON_NOTIFICATIONS, 1 - MON_SYSNOTIFICATIONS */
    @Column(name = "[APPLICATION]", nullable = false)
    private Integer application;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[CONFIGURATION]", nullable = false)
    private String configuration;

    @Column(name = "[MESSAGE]", nullable = false)
    private String message;

    @Column(name = "[ERROR]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean error;

    @Column(name = "[ERROR_TEXT]", nullable = true)
    private String errorText;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemNotificationMonitor() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long val) {
        notificationId = val;
    }

    public Integer getApplication() {
        return application;
    }

    public void setApplication(Integer val) {
        if (val == null) {
            val = MonitoringConstants.NOTIFICATION_DEFAULT_APPLICATION.intValue();
        }
        application = val;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer val) {
        if (val == null) {
            val = MonitorType.COMMAND.intValue();
        }
        type = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String val) {
        configuration = normalizeValue(val, MonitoringConstants.MAX_LEN_CONFIGURATION);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        if (val == null) {
            message = DBLayer.DEFAULT_KEY;
        } else {
            val = SOSString.remove4ByteCharacters(val).trim();
            message = normalizeValue(val, MonitoringConstants.MAX_LEN_MESSAGE);
        }
    }

    public void setError(boolean val) {
        error = val;
    }

    public boolean getError() {
        return error;
    }

    public void setErrorText(String val) {
        if (val == null) {
            errorText = null;
        } else {
            val = SOSString.remove4ByteCharacters(val).trim();
            errorText = normalizeErrorText(val);
        }
    }

    public String getErrorText() {
        return errorText;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    @Transient
    public static String normalizeErrorText(String val) {
        return normalizeValue(val, HistoryConstants.MAX_LEN_ERROR_TEXT);
    }

    @Transient
    public MonitorType getTypeAsEnum() {
        try {
            return MonitorType.fromValue(type);
        } catch (Throwable e) {
            return MonitorType.COMMAND;
        }
    }

    @Transient
    public void setType(MonitorType val) {
        setType(val == null ? null : val.intValue());
    }

}
