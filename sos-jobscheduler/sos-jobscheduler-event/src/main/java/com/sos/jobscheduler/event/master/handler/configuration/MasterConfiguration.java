package com.sos.jobscheduler.event.master.handler.configuration;

import java.util.Properties;

import com.sos.commons.util.SOSString;

public class MasterConfiguration implements IMasterConfiguration {

    private Master primary;
    private Master backup;
    private Master current;

    private int webserviceTimeout = 60;// seconds
    private int webserviceDelay = 0; // seconds
    private int webserviceLimit = 1000;

    // milliseconds
    private int httpClientConnectTimeout = 30_000;
    private int httpClientConnectionRequestTimeout = 30_000;
    private int httpClientSocketTimeout = 75_000;

    // milliseconds
    private int waitIntervalOnConnectionRefused = 30_000;
    private int waitIntervalOnMasterSwitch = 30_000;
    private int waitIntervalOnError = 2_000;
    private int waitIntervalOnTooManyRequests = 2_000;
    private int waitIntervalOnEmptyEvent = 1_000;
    private int waitIntervalOnNonEmptyEvent = 0;
    private int waitIntervalOnTornEvent = 2_000;
    private int maxWaitIntervalOnEnd = 30_000;

    // send notification if a torn event has been occurred and was not recovered during the notification interval
    private int notifyIntervalOnTornEvent = 15;
    private int notifyIntervalOnConnectionRefused = 15;

    // TODO
    public MasterConfiguration(Master primaryMaster, Master backupMaster) throws Exception {
        initMasterSettings(primaryMaster, backupMaster);
    }

    // TODO
    public MasterConfiguration(final Properties conf) throws Exception {
        Master primaryMaster = new Master(conf.getProperty("master_id"), conf.getProperty("primary_master_uri"), conf.getProperty(
                "primary_master_user"), conf.getProperty("primary_master_user_password"));

        Master backupMaster = null;
        if (!SOSString.isEmpty(conf.getProperty("backup_master_hostname"))) {
            backupMaster = new Master(primaryMaster.getId(), conf.getProperty("backup_master_uri"), conf.getProperty("backup_master_user"), conf
                    .getProperty("backup_master_user_password"));
        }
        initMasterSettings(primaryMaster, backupMaster);

        // webservice
        if (conf.getProperty("webservice_timeout") != null) {
            webserviceTimeout = Integer.parseInt(conf.getProperty("webservice_timeout").trim());
        }
        if (conf.getProperty("webservice_limit") != null) {
            webserviceLimit = Integer.parseInt(conf.getProperty("webservice_limit").trim());
        }
        if (conf.getProperty("webservice_delay") != null) {
            webserviceDelay = Integer.parseInt(conf.getProperty("webservice_delay").trim());
        }

        // http client
        if (conf.getProperty("http_client_connect_timeout") != null) {
            httpClientConnectTimeout = Integer.parseInt(conf.getProperty("http_client_connect_timeout").trim());
        }
        if (conf.getProperty("http_client_connection_request_timeout") != null) {
            httpClientConnectionRequestTimeout = Integer.parseInt(conf.getProperty("http_client_connection_request_timeout").trim());
        }
        if (conf.getProperty("http_client_socket_timeout") != null) {
            httpClientSocketTimeout = Integer.parseInt(conf.getProperty("http_client_socket_timeout").trim());
        }

        // event handler - wait intervals
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

    private void initMasterSettings(Master primaryMaster, Master backupMaster) throws Exception {
        if (primaryMaster == null) {
            throw new Exception("primaryMaster is null");
        }

        primary = primaryMaster;
        primary.setPrimary(true);

        current = primary;

        backup = backupMaster;
        if (backup != null) {
            backup.setId(primaryMaster.getId());
            backup.setPrimary(false);
        }
    }

    @Override
    public Master getPrimary() {
        return primary;
    }

    @Override
    public Master getBackup() {
        return backup;
    }

    @Override
    public Master getCurrent() {
        return current;
    }

    @Override
    public void setCurrent(Master val) {
        current = val;
    }

    public int getWebserviceTimeout() {
        return webserviceTimeout;
    }

    public int getWebserviceDelay() {
        return webserviceDelay;
    }

    public int getWebserviceLimit() {
        return webserviceLimit;
    }

    public int getHttpClientConnectTimeout() {
        return httpClientConnectTimeout;
    }

    public int getHttpClientConnectionRequestTimeout() {
        return httpClientConnectionRequestTimeout;
    }

    public int getHttpClientSocketTimeout() {
        return httpClientSocketTimeout;
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

    public int getMaxWaitIntervalOnEnd() {
        return maxWaitIntervalOnEnd;
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

    public int getNotifyIntervalOnTornEvent() {
        return notifyIntervalOnTornEvent;
    }

    public int getNotifyIntervalOnConnectionRefused() {
        return notifyIntervalOnConnectionRefused;
    }
}
