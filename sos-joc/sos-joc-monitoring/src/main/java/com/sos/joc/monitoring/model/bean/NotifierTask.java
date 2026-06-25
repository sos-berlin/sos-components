package com.sos.joc.monitoring.model.bean;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.sos.history.JobWarning;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.model.OrderNotifyAnalyzer;
import com.sos.joc.monitoring.notification.notifier.NotifyResult;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.OrderNotificationRange;

public class NotifierTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private final OrderNotificationRange range;
    private final OrderNotifyAnalyzer analyzer;
    private final Notification notification;
    private final NotificationType type;
    private final AMonitor monitor;
    private final String identifier;
    private final DBItemNotification dbNotification;
    private final DBItemMonitoringOrderStep dbOrderStep;
    private final JobWarning warnReason;

    // Result
    private NotifyResult notifyResult;
    private boolean saveNotificationMonitor;
    private boolean executed;
    private Exception exception;

    private Instant submitted;

    public NotifierTask(OrderNotificationRange range, OrderNotifyAnalyzer analyzer, Notification notification, NotificationType type,
            final AMonitor monitor, String identifier, DBItemNotification dbNotification, DBItemMonitoringOrderStep dbOrderStep,
            JobWarning warnReason) {
        this.range = range;
        this.analyzer = analyzer;
        this.notification = notification;
        this.type = type;
        this.monitor = monitor;
        this.identifier = identifier;
        this.dbNotification = dbNotification;
        this.dbOrderStep = dbOrderStep;
        this.warnReason = warnReason;
    }

    public NotifierTask copyWithoutResult() {
        return new NotifierTask(range, analyzer, notification, type, monitor, identifier, dbNotification, dbOrderStep, warnReason);
    }

    public OrderNotificationRange getRange() {
        return range;
    }

    public OrderNotifyAnalyzer getAnalyzer() {
        return analyzer;
    }

    public Notification getNotification() {
        return notification;
    }

    public NotificationType getType() {
        return type;
    }

    public AMonitor getMonitor() {
        return monitor;
    }

    public String getIdentifier() {
        return identifier;
    }

    public DBItemNotification getDbNotification() {
        return dbNotification;
    }

    public DBItemMonitoringOrderStep getDbOrderStep() {
        return dbOrderStep;
    }

    public JobWarning getWarnReason() {
        return warnReason;
    }

    public void setSubmitted(Instant val) {
        submitted = val;
    }

    public Instant getSubmitted() {
        return submitted;
    }

    public boolean isExceeded(Instant val) {
        if (submitted == null) {
            submitted = val;
            return false;
        }
        return submitted.plus(2, ChronoUnit.DAYS).isBefore(val);
    }

    public void setNotifyResult(NotifyResult val) {
        notifyResult = val;
    }

    public NotifyResult getNotifyResult() {
        return notifyResult;
    }

    public void setException(Exception e) {
        exception = e;
    }

    public Exception getException() {
        return exception;
    }

    public void saveNotificationMonitor() {
        saveNotificationMonitor = true;
    }

    public boolean isSaveNotificationMonitor() {
        return saveNotificationMonitor;
    }

    public void setExecuted() {
        executed = true;
    }

    public boolean isExecuted() {
        return executed;
    }

}
