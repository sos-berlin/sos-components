package com.sos.joc.monitoring.bean;

import java.util.Map;

import com.sos.commons.util.SOSString;
import com.sos.joc.event.bean.monitoring.NotificationLogEvent;
import com.sos.joc.event.bean.monitoring.SystemNotificationLogEvent;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.SystemNotificationCategory;

import js7.proxy.JournaledProxy;

public class SystemMonitoringEvent {

    private static final SystemNotificationCategory DEFAULT_CATEGORY = SystemNotificationCategory.JOC;

    private static final String SOURCE_DATABASE = "Database";
    private static final String SOURCE_MONITOR = "Monitor";
    private static final String SOURCE_HISTORY = "History";
    private static final String SOURCE_CLEANUP = "Cleanup";
    private static final String SOURCE_DEPLOYMENT = "Deployment";
    private static final String SOURCE_RELEASE_NOTIFICATION = "ReleaseNotification";
    private static final String SOURCE_DAILYPLAN = "DailyPlan";
    private static final String SOURCE_INVENTORY = "Inventory";
    private static final String SOURCE_JOC_CLUSTER = "JOCCockpitCluster";
    private static final String SOURCE_LOG_NOTIFICATION = "LogNotification";

    private static final String KEY_DATABASE_WARNING = (SOURCE_DATABASE + "_" + NotificationType.WARNING.name()).toLowerCase();
    private static final String KEY_DATABASE_ERROR = (SOURCE_DATABASE + "_" + NotificationType.ERROR.name()).toLowerCase();
    private static final String KEY_CONTROLLER = SystemNotificationCategory.CONTROLLER.name();
    private static final String KEY_CONTROLLER_WARNING = (KEY_CONTROLLER + "_" + NotificationType.WARNING.name()).toLowerCase();
    private static final String KEY_CONTROLLER_ERROR = (KEY_CONTROLLER + "_" + NotificationType.ERROR.name()).toLowerCase();

    private static final String CLASS_NAME_RELEASE_NOTIFICATION = "com.sos.joc.xmleditor.impl.ReleaseResourceImpl";
    private static final String CLASS_NAME_PROBLEM_HELPER = "com.sos.joc.classes.ProblemHelper";

    private final NotificationType type;
    private final long epochMillis;
    private final String loggerName;
    private final String caller;
    private final String message;
    private final Throwable thrown;
    private final String stacktrace;

    private SystemNotificationCategory category;
    private String source;

    private String key;
    private boolean forceNotify;

    public SystemMonitoringEvent(NotificationLogEvent evt) {
        type = getType(evt.getLevel());
        epochMillis = evt.getEpochMillis();
        loggerName = evt.getLoggerName();
        caller = evt.getMarkerName();
        message = getMessage(evt);
        thrown = evt.getThrown();
        stacktrace = null;
        setCategoryAndSource();
    }
    
    public SystemMonitoringEvent(SystemNotificationLogEvent evt) {
        type = getType(evt.getLevel());
        epochMillis = evt.getInstant().toEpochMilli();
        loggerName = evt.getNotifier();
        message = getMessage(evt);
        stacktrace = evt.getStacktrace();
        thrown = null;
        source = SOURCE_LOG_NOTIFICATION; //evt.getSource();
        category = SystemNotificationCategory.fromValue(evt.getProduct());
        caller = loggerName;
    }
    
    private String getMessage(NotificationLogEvent evt) {
        if (evt.getMessage() != null) {
            return evt.getMessage();
        }
        if (evt.getThrown() != null) {
            return evt.getThrown().toString();
        }
        return null;
    }
    
    private String getMessage(SystemNotificationLogEvent evt) {
        String hostMessagePrefix = evt.getHost() == null ? "" : evt.getHost() + ": ";
        if (evt.getMessage() != null) {
            return hostMessagePrefix+ evt.getMessage();
        }
        if (evt.getStacktrace() != null) {
            return hostMessagePrefix + evt.getStacktrace().split("\r?\n", 2)[0];
        }
        return null;
    }

    private void setCategoryAndSource() {
        if (thrown == null) {
            setByClassName(loggerName, message);
        } else {
            for (StackTraceElement el : thrown.getStackTrace()) {
                setByClassName(el.getClassName(), null);
                if (category != null && source != null) {
                    return;
                }
            }
        }

        if (category == null) {
            category = DEFAULT_CATEGORY;
        }
        if (source == null) {
            // can't be identified by thrown..
            if (loggerName.equals(CLASS_NAME_RELEASE_NOTIFICATION)) {
                category = SystemNotificationCategory.JOC;
                source = SOURCE_RELEASE_NOTIFICATION;
            } else {
                int i = loggerName.lastIndexOf(".");
                source = i > -1 ? loggerName.substring(i + 1) : loggerName;
            }
        }
    }

    public boolean skip(Map<String, SystemMonitoringEvent> events) {
        if (forceNotify) {
            if (loggerName.equals(JournaledProxy.class.getName()) && message.startsWith("EventSeqTorn")) {
                return true;
            }
        } else {
            if (key.equals(SystemMonitoringEvent.KEY_DATABASE_WARNING)) {
                if (events.containsKey(SystemMonitoringEvent.KEY_DATABASE_ERROR)) {
                    return true;
                }
            } else if (key.equals(SystemMonitoringEvent.KEY_CONTROLLER_WARNING)) {
                if (events.containsKey(SystemMonitoringEvent.KEY_CONTROLLER_ERROR)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void init() {
        setForceNotify();
        setKey();
    }

    private void setKey() {
        key = (source + "_" + type).toLowerCase();
    }

    private void setForceNotify() {
        switch (category) {
        case JOC:
            forceNotify = true;
            break;
        default:
            forceNotify = false;
            break;
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

    // @TODO optimize
    private void setByClassName(String className, String message) {
        if (className.startsWith("org.hibernate") || className.startsWith("com.zaxxer")) {
            category = SystemNotificationCategory.SYSTEM;
            source = SOURCE_DATABASE;
        } else if (className.startsWith("com.sos.commons.hibernate")) {
            // applies to all Hibernate errors (not only connection errors)
            category = SystemNotificationCategory.SYSTEM;
            // source(History, Inventory etc) will be evaluated later
        }
        // ??? all js7.
        else if (className.startsWith("js7.common") || className.startsWith("js7.base")) {
            category = SystemNotificationCategory.CONTROLLER;
            // source will be evaluated later

            // TODO CONTROLLER or JOC?
            // className= js7.base.session.SessionApi
            // message=js7.proxy.ControllerApi@702644c4: ControllerApi-0 Connect(localhost:5444): java.net.ConnectException: Connection refused: no further
            // information

        } else if (className.equals(CLASS_NAME_PROBLEM_HELPER)) {
            if (message != null && message.contains("UnknownItemPath")) {
                category = SystemNotificationCategory.CONTROLLER;
                source = SOURCE_DEPLOYMENT;
            }
        } else if (className.startsWith("com.sos.joc.publish")) {
            source = SOURCE_DEPLOYMENT;
        } else if (className.startsWith("com.sos.joc.inventory")) {
            source = SOURCE_INVENTORY;
        } else if (className.startsWith("com.sos.joc.dailyplan")) {
            source = SOURCE_DAILYPLAN;
        } else if (className.startsWith("com.sos.joc.history")) {
            source = SOURCE_HISTORY;
        } else if (className.startsWith("com.sos.joc.monitoring")) {
            source = SOURCE_MONITOR;
        } else if (className.startsWith("com.sos.joc.cleanup")) {
            source = SOURCE_CLEANUP;
        } else if (className.startsWith("com.sos.joc.cluster")) {
            source = SOURCE_JOC_CLUSTER;
        }
        if (source != null) {
            if (category == null) {
                // JOC category and not a default category because the source was set from a sos.joc class
                category = SystemNotificationCategory.JOC;
            }
        }
    }

    public String getKey() {
        return key;
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

    public String getSource() {
        return source;
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
    
    public String getStacktrace() {
        return stacktrace;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
