package com.sos.joc.monitoring.bean;

import java.util.Map;

import com.sos.commons.util.SOSString;
import com.sos.joc.event.bean.monitoring.NotificationLogEvent;
import com.sos.joc.monitoring.model.HistoryMonitoringModel;
import com.sos.joc.monitoring.model.HistoryNotifierModel;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.SystemNotificationCategory;

import js7.proxy.JournaledProxy;

public class SystemMonitoringEvent {

    private static final String SECTION_DATABASE = "Database";
    private static final String SECTION_CONTROLLER = "Controller";
    private static final String SECTION_MONITORING = "Monitoring";
    private static final String SECTION_HISTORY = "History";
    private static final String SECTION_CLEANUP = "Cleanup";
    private static final String SECTION_DEPLOYMENT = "Deployment";
    private static final String SECTION_DAILYPLAN = "DailyPlan";
    private static final String SECTION_INVENTORY = "Inventory";

    private static final String SECTION_DATABASE_WARNING = SECTION_DATABASE + "_" + NotificationType.WARNING.name();
    private static final String SECTION_DATABASE_ERROR = SECTION_DATABASE + "_" + NotificationType.ERROR.name();
    private static final String SECTION_CONTROLLER_WARNING = SECTION_CONTROLLER + "_" + NotificationType.WARNING.name();
    private static final String SECTION_CONTROLLER_ERROR = SECTION_CONTROLLER + "_" + NotificationType.ERROR.name();

    private final NotificationType type;
    private final String section;
    private final long epochMillis;
    private final String loggerName;
    private final String caller;
    private final String message;
    private final Throwable thrown;

    private SystemNotificationCategory category;

    private String key;
    private boolean forceNotify;

    public SystemMonitoringEvent(NotificationLogEvent evt) {
        type = getType(evt.getLevel());
        category = getCategory(evt.getCategory());
        section = getSection(evt);
        epochMillis = evt.getEpochMillis();
        loggerName = evt.getLoggerName();
        caller = evt.getMarkerName();
        message = evt.getMessage();
        thrown = evt.getThrown();
    }

    public boolean skip(Map<String, SystemMonitoringEvent> events) {
        if (forceNotify) {
            if (category.equals(SystemNotificationCategory.JOC) && loggerName.equals(JournaledProxy.class.getName()) && message.startsWith(
                    "EventSeqTorn")) {
                return true;
            }
        } else {
            if (key.equals(SystemMonitoringEvent.SECTION_DATABASE_WARNING)) {
                if (events.containsKey(SystemMonitoringEvent.SECTION_DATABASE_ERROR)) {
                    return true;
                }
            } else if (key.equals(SystemMonitoringEvent.SECTION_CONTROLLER_WARNING)) {
                if (events.containsKey(SystemMonitoringEvent.SECTION_CONTROLLER_ERROR)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void init() {
        maybeChangeCategory();
        setForceNotify();
        setKey();
    }

    private void setKey() {
        if (category.equals(SystemNotificationCategory.JOC)) {
            if (section.equals(SECTION_CONTROLLER)) {
                key = section + "_" + type;
            } else {
                key = loggerName + "_" + type;
            }
        } else {
            key = section + "_" + type;
        }
    }

    public String getKey() {
        return key;
    }

    private void maybeChangeCategory() {
        if (loggerName.equals(HistoryNotifierModel.class.getName()) || loggerName.equals(HistoryMonitoringModel.class.getName())) {
            category = SystemNotificationCategory.JOC;
        }
        if (section.equals(SECTION_DATABASE)) {
            category = SystemNotificationCategory.SYSTEM;
        }
    }

    private void setForceNotify() {
        if (category.equals(SystemNotificationCategory.JOC)) {
            if (section.equals(SECTION_CONTROLLER)) {
                forceNotify = false;
            } else {
                forceNotify = true;
            }
        } else {
            forceNotify = false;
        }
    }

    public boolean forceNotify() {
        return forceNotify;
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

    private SystemNotificationCategory getCategory(String val) {
        if (SOSString.isEmpty(val)) {
            return SystemNotificationCategory.SYSTEM;
        }
        return val.toLowerCase().equals("joc") ? SystemNotificationCategory.JOC : SystemNotificationCategory.SYSTEM;
    }

    // @TODO RegExp
    private String getSection(NotificationLogEvent evt) {
        if (evt.getLoggerName().startsWith("org.hibernate") || evt.getLoggerName().startsWith("com.zaxxer")) {
            return SECTION_DATABASE;
        }// ??? all js7.
        else if (evt.getLoggerName().startsWith("js7.common") || evt.getLoggerName().startsWith("js7.base")) {
            return SECTION_CONTROLLER;
        } else {
            String section = getSectionByJocClassName(evt.getLoggerName());
            if (section != null) {
                return section;
            }
            if (evt.getThrown() != null) {
                for (StackTraceElement el : evt.getThrown().getStackTrace()) {
                    section = getSectionByJocClassName(el.getClassName());
                    if (section != null) {
                        return section;
                    }
                }
            }
            int i = evt.getLoggerName().lastIndexOf(".");
            if (i > -1) {
                return evt.getLoggerName().substring(i + 1);
            }
            return evt.getLoggerName();
        }
    }

    // @TODO optimize
    private String getSectionByJocClassName(String className) {
        if (className.startsWith("com.sos.joc.publish")) {
            return SECTION_DEPLOYMENT;
        } else if (className.startsWith("com.sos.joc.inventory")) {
            return SECTION_INVENTORY;
        } else if (className.startsWith("com.sos.joc.dailyplan")) {
            return SECTION_DAILYPLAN;
        } else if (className.startsWith("com.sos.joc.history")) {
            return SECTION_HISTORY;
        } else if (className.startsWith("com.sos.joc.monitoring")) {
            return SECTION_MONITORING;
        } else if (className.startsWith("com.sos.joc.cleanup")) {
            return SECTION_CLEANUP;
        }
        return null;
    }

    public NotificationType getType() {
        return type;
    }

    public void setCategory(SystemNotificationCategory val) {
        category = val;
    }

    public SystemNotificationCategory getCategory() {
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

    public String getCaller() {
        return caller;
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
