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
    private int waitIntervalOnEmptyEvent = 1_000;
    private int waitIntervalOnTornEvent = 2_000;
    private int maxWaitIntervalOnEnd = 30_000;

    // minutes, send Kommando KeepEvents
    private int keepEventsInterval = 15;

    private int maxTransactions = 100;
    private boolean saveOrderStatus = false;

    public EventHandlerMasterSettings(String masterId, String masterHost, String masterPort) {
        this(masterId, masterHost, masterPort, null, null);
    }

    public EventHandlerMasterSettings(String masterId, String masterHost, String masterPort, String masterUser, String masterUserPassword) {
        id = masterId;
        hostname = masterHost;
        port = masterPort;
        user = masterUser;
        password = masterUserPassword;
        if (user != null) {
            useLogin = true;
        }
    }

    public EventHandlerMasterSettings(final Properties conf) {
        // TODO
        id = conf.getProperty("master_id").trim();
        hostname = conf.getProperty("master_hostname").trim();
        port = conf.getProperty("master_port").trim();
        useLogin = Boolean.parseBoolean(conf.getProperty("master_use_login").trim());
        user = conf.getProperty("master_user").trim();
        password = conf.getProperty("master_user_password").trim();

        webserviceTimeout = Integer.parseInt(conf.getProperty("webservice_timeout").trim());
        webserviceLimit = Integer.parseInt(conf.getProperty("webservice_limit").trim());
        webserviceDelay = Integer.parseInt(conf.getProperty("webservice_delay").trim());

        httpClientConnectTimeout = Integer.parseInt(conf.getProperty("http_client_connect_timeout").trim());
        httpClientConnectionRequestTimeout = Integer.parseInt(conf.getProperty("http_client_connection_request_timeout").trim());
        httpClientSocketTimeout = Integer.parseInt(conf.getProperty("http_client_socket_timeout").trim());

        waitIntervalOnConnectionRefused = Integer.parseInt(conf.getProperty("wait_interval_on_connection_refused").trim());
        waitIntervalOnError = Integer.parseInt(conf.getProperty("wait_interval_on_error").trim());
        waitIntervalOnEmptyEvent = Integer.parseInt(conf.getProperty("wait_interval_on_empty_event").trim());
        waitIntervalOnTornEvent = Integer.parseInt(conf.getProperty("wait_interval_on_torn_event").trim());
        maxWaitIntervalOnEnd = Integer.parseInt(conf.getProperty("max_wait_interval_on_end").trim());

        keepEventsInterval = Integer.parseInt(conf.getProperty("webservice_keep_events_interval").trim());
        maxTransactions = Integer.parseInt(conf.getProperty("max_transactions").trim());
        saveOrderStatus = Boolean.parseBoolean(conf.getProperty("save_order_status").trim());
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

    public int getMaxWaitIntervalOnEnd() {
        return maxWaitIntervalOnEnd;
    }

    public int getWaitIntervalOnEmptyEvent() {
        return waitIntervalOnEmptyEvent;
    }

    public int getWaitIntervalOnTornEvent() {
        return waitIntervalOnTornEvent;
    }

    public int getKeepEventsInterval() {
        return keepEventsInterval;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public boolean getSaveOrderStatus() {
        return saveOrderStatus;
    }

}
