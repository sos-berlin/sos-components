package com.sos.jobscheduler.event.master.configuration.handler;

import java.util.Properties;

public class HandlerConfiguration {

    // seconds
    private int waitIntervalOnConnectionRefused = 30;
    private int waitIntervalOnMasterSwitch = 5;
    private int waitIntervalOnError = 5;
    private int waitIntervalOnTooManyRequests = 5;
    private int waitIntervalOnEmptyEvent = 1;
    private int waitIntervalOnNonEmptyEvent = 0;
    private int waitIntervalOnTornEvent = 5;
    private int maxWaitIntervalOnEnd = 30;

    // send notification if a torn event has been occurred and was not recovered during the notification interval
    private int notifyIntervalOnTornEvent = 15;
    private int notifyIntervalOnConnectionRefused = 15;

    public void load(Properties conf) {
        if (conf.getProperty("wait_interval_on_connection_refused") != null) {
            waitIntervalOnConnectionRefused = Integer.parseInt(conf.getProperty("wait_interval_on_connection_refused").trim());
        }
        if (conf.getProperty("wait_interval_on_master_switch") != null) {
            waitIntervalOnMasterSwitch = Integer.parseInt(conf.getProperty("wait_interval_on_master_switch").trim());
        }
        if (conf.getProperty("wait_interval_on_error") != null) {
            waitIntervalOnError = Integer.parseInt(conf.getProperty("wait_interval_on_error").trim());
        }
        if (conf.getProperty("wait_interval_on_too_many_requests") != null) {
            waitIntervalOnTooManyRequests = Integer.parseInt(conf.getProperty("wait_interval_on_too_many_requests").trim());
        }
        if (conf.getProperty("wait_interval_on_empty_event") != null) {
            waitIntervalOnEmptyEvent = Integer.parseInt(conf.getProperty("wait_interval_on_empty_event").trim());
        }
        if (conf.getProperty("wait_interval_on_non_empty_event") != null) {
            waitIntervalOnNonEmptyEvent = Integer.parseInt(conf.getProperty("wait_interval_on_non_empty_event").trim());
        }
        if (conf.getProperty("wait_interval_on_torn_event") != null) {
            waitIntervalOnTornEvent = Integer.parseInt(conf.getProperty("wait_interval_on_torn_event").trim());
        }
        if (conf.getProperty("max_wait_interval_on_end") != null) {
            maxWaitIntervalOnEnd = Integer.parseInt(conf.getProperty("max_wait_interval_on_end").trim());
        }

        if (conf.getProperty("notify_interval_on_torn_event") != null) {
            notifyIntervalOnTornEvent = Integer.parseInt(conf.getProperty("notify_interval_on_torn_event").trim());
        }
        if (conf.getProperty("notify_interval_on_connection_refused") != null) {
            notifyIntervalOnConnectionRefused = Integer.parseInt(conf.getProperty("notify_interval_on_connection_refused").trim());
        }
    }

    public int getWaitIntervalOnConnectionRefused() {
        return waitIntervalOnConnectionRefused;
    }

    public int getWaitIntervalOnMasterSwitch() {
        return waitIntervalOnMasterSwitch;
    }

    public int getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public int getWaitIntervalOnTooManyRequests() {
        return waitIntervalOnTooManyRequests;
    }

    public int getWaitIntervalOnEmptyEvent() {
        return waitIntervalOnEmptyEvent;
    }

    public int getWaitIntervalOnNonEmptyEvent() {
        return waitIntervalOnNonEmptyEvent;
    }

    public int getWaitIntervalOnTornEvent() {
        return waitIntervalOnTornEvent;
    }

    public int getMaxWaitIntervalOnEnd() {
        return maxWaitIntervalOnEnd;
    }

    public int getNotifyIntervalOnTornEvent() {
        return notifyIntervalOnTornEvent;
    }

    public int getNotifyIntervalOnConnectionRefused() {
        return notifyIntervalOnConnectionRefused;
    }

}
