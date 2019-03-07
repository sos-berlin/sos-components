package com.sos.jobscheduler.event.master.handler;

import java.util.Properties;

public class EventHandlerMasterSettings {

    private String id;
    private String hostname;
    private String port;
    private String user;
    private String password;
    private boolean useLogin;

    private int webserviceTimeout = 60;// seconds
    private int webserviceDelay = 0; // seconds
    private int webserviceLimit = 1000;

    // milliseconds
    private int httpClientConnectTimeout = 30_000;
    private int httpClientConnectionRequestTimeout = 30_000;
    private int httpClientSocketTimeout = 75_000;

    // milliseconds
    private int waitIntervalOnConnectionRefused = 30_000;
    private int waitIntervalOnError = 2_000;
    private int waitIntervalOnTooManyRequests = 2_000;
    private int waitIntervalOnEmptyEvent = 1_000;
    private int waitIntervalOnNonEmptyEvent = 0;
    private int waitIntervalOnTornEvent = 2_000;
    private int maxWaitIntervalOnEnd = 30_000;
    private int minExecutionTimeOnNonEmptyEvent = 10; // to avoid master 429 TooManyRequestsException
    // minutes,
    // send KeepEvents command
    private int keepEventsInterval = 15;
    // send notification if a torn event has been occurred and was not recovered during the notification interval
    private int notifyIntervalOnTornEvent = 15;
    private int notifyIntervalOnConnectionRefused = 15;

    private int maxTransactions = 100;
    private boolean saveOrderStatus = false;
    private String logDir;
    private long startDiagnosticIfNotEmptyEventLonger = 0; // milliseconds
    private long startDiagnosticIfHistoryLonger = 0; // milliseconds
    private String diagnosticScript;
    private String uriHistoryExecutor;

    public EventHandlerMasterSettings(String masterId, String masterHost, String masterPort) throws Exception {
        this(masterId, masterHost, masterPort, null, null);
    }

    // TODO
    public EventHandlerMasterSettings(String masterId, String masterHost, String masterPort, String masterUser, String masterUserPassword)
            throws Exception {
        if (masterId == null) {
            throw new Exception("masterId is NULL");
        }
        if (masterHost == null) {
            throw new Exception("masterHost is NULL");
        }
        if (masterPort == null) {
            throw new Exception("masterPort is NULL");
        }

        id = masterId.trim();
        hostname = masterHost.trim();
        port = masterPort.trim();

        if (masterUser != null) {
            useLogin = true;
            user = masterUser.trim();
            if (masterUserPassword != null) {
                password = masterUserPassword.trim();
            }
        }
    }

    // TODO
    public EventHandlerMasterSettings(final Properties conf) throws Exception {
        this(conf.getProperty("master_id"), conf.getProperty("master_hostname"), conf.getProperty("master_port"), conf.getProperty("master_user"),
                conf.getProperty("master_user_password"));

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
        if (conf.getProperty("webservice_keep_events_interval") != null) {
            keepEventsInterval = Integer.parseInt(conf.getProperty("webservice_keep_events_interval").trim());
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
        if (conf.getProperty("min_execution_time_on_non_empty_event") != null) {
            minExecutionTimeOnNonEmptyEvent = Integer.parseInt(conf.getProperty("min_execution_time_on_non_empty_event").trim());
        }

        if (conf.getProperty("notify_interval_on_torn_event") != null) {
            notifyIntervalOnTornEvent = Integer.parseInt(conf.getProperty("notify_interval_on_torn_event").trim());
        }
        if (conf.getProperty("notify_interval_on_connection_refused") != null) {
            notifyIntervalOnConnectionRefused = Integer.parseInt(conf.getProperty("notify_interval_on_connection_refused").trim());
        }

        // event handler
        if (conf.getProperty("max_transactions") != null) {
            maxTransactions = Integer.parseInt(conf.getProperty("max_transactions").trim());
        }
        if (conf.getProperty("save_order_status") != null) {
            saveOrderStatus = Boolean.parseBoolean(conf.getProperty("save_order_status").trim());
        }
        if (conf.getProperty("log_dir") != null) {
            logDir = conf.getProperty("log_dir").trim();
        }
        if (conf.getProperty("diagnostic_script") != null) {
            diagnosticScript = conf.getProperty("diagnostic_script").trim();
        }

        if (conf.getProperty("start_diagnostic_if_not_empty_event_longer") != null) {
            startDiagnosticIfNotEmptyEventLonger = Long.parseLong(conf.getProperty("start_diagnostic_if_not_empty_event_longer").trim());
        }
        if (conf.getProperty("start_diagnostic_if_history_longer") != null) {
            startDiagnosticIfHistoryLonger = Long.parseLong(conf.getProperty("start_diagnostic_if_history_longer").trim());
        }

        if (conf.getProperty("uri_history_executor") != null) {
            uriHistoryExecutor = conf.getProperty("uri_history_executor").trim();
        }
    }

    public String getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean useLogin() {
        return useLogin;
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

    public int getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public int getWaitIntervalOnTooManyRequests() {
        return waitIntervalOnTooManyRequests;
    }

    public int getMaxWaitIntervalOnEnd() {
        return maxWaitIntervalOnEnd;
    }

    public int getMinExecutionTimeOnNonEmptyEvent() {
        return minExecutionTimeOnNonEmptyEvent;
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

    public int getKeepEventsInterval() {
        return keepEventsInterval;
    }

    public int getNotifyIntervalOnTornEvent() {
        return notifyIntervalOnTornEvent;
    }

    public int getNotifyIntervalOnConnectionRefused() {
        return notifyIntervalOnConnectionRefused;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public boolean getSaveOrderStatus() {
        return saveOrderStatus;
    }

    public String getLogDir() {
        return logDir;
    }

    public long getStartDiagnosticIfNotEmptyEventLonger() {
        return startDiagnosticIfNotEmptyEventLonger;
    }

    public long getStartDiagnosticIfHistoryLonger() {
        return startDiagnosticIfHistoryLonger;
    }

    public String getDiagnosticScript() {
        return diagnosticScript;
    }

    public String getUriHistoryExecutor() {
        return uriHistoryExecutor;
    }
}
