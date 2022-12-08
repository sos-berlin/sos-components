package com.sos.joc.monitoring.bean;

import com.sos.commons.util.SOSString;
import com.sos.joc.event.bean.monitoring.NotificationLogEvent;
import com.sos.monitoring.notification.NotificationType;

public class SystemMonitoringEvent {

    public enum Category {
        JOC, SYSTEM
    }

    public static String SECTION_DATABASE = "Database";
    public static String SECTION_DATABASE_WARNING = SECTION_DATABASE + "_" + NotificationType.WARNING.name();
    public static String SECTION_DATABASE_ERROR = SECTION_DATABASE + "_" + NotificationType.ERROR.name();

    private final NotificationType type;
    private final String section;
    private final long epochMillis;
    private final String loggerName;
    private final String message;
    private final Throwable thrown;

    private Category category;

    public SystemMonitoringEvent(NotificationLogEvent evt) {
        type = getType(evt.getLevel());
        category = getCategory(evt.getCategory());
        section = getSection(evt);
        epochMillis = evt.getEpochMillis();
        loggerName = evt.getLoggerName();
        message = evt.getMessage();
        thrown = evt.getThrown();
    }

    private NotificationType getType(String level) {
        if (level == null) {
            return NotificationType.ERROR;
        }
        switch (level.toLowerCase()) {
        case "warn":
        case "warning":
            return NotificationType.WARNING;
        default:
            return NotificationType.ERROR;
        }
    }

    private Category getCategory(String val) {
        if (SOSString.isEmpty(val)) {
            return Category.SYSTEM;
        }
        return val.toLowerCase().equals("joc") ? Category.JOC : Category.SYSTEM;
    }

    private String getSection(NotificationLogEvent evt) {
        if (evt.getLoggerName().startsWith("org.hibernate") || evt.getLoggerName().startsWith("com.zaxxer")) {
            return SECTION_DATABASE;
        } else {
            int i = evt.getLoggerName().lastIndexOf(".");
            if (i > -1) {
                return evt.getLoggerName().substring(i + 1);
            }
            return evt.getLoggerName();
        }
    }

    public NotificationType getType() {
        return type;
    }

    public void setCategory(Category val) {
        category = val;
    }

    public Category getCategory() {
        return category;
    }

    public String getSection() {
        return section;
    }

    public long getEpochMillis() {
        return epochMillis;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrown() {
        return thrown;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
