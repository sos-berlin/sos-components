package com.sos.joc.monitoring.bean;

import java.util.ArrayList;
import java.util.List;

import com.sos.joc.db.monitoring.DBItemNotificationMonitor;
import com.sos.joc.db.monitoring.DBItemSystemNotification;

public class SystemNotifierResult {

    private final DBItemSystemNotification notification;

    private List<DBItemNotificationMonitor> monitors = new ArrayList<>();

    public SystemNotifierResult(DBItemSystemNotification notification) {
        this.notification = notification;
    }

    public void addMonitor(DBItemNotificationMonitor val) {
        monitors.add(val);
    }

    public DBItemSystemNotification getNotification() {
        return notification;
    }

    public List<DBItemNotificationMonitor> getMonitors() {
        return monitors;
    }
}
