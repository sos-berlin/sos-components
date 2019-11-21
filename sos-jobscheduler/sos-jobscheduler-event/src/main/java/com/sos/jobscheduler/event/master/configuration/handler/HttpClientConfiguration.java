package com.sos.jobscheduler.event.master.configuration.handler;

import java.util.Properties;

public class HttpClientConfiguration {

    // seconds
    private int connectTimeout = 30;
    private int connectionRequestTimeout = 30;
    private int socketTimeout = 75;

    public void load(final Properties conf) throws Exception {
        if (conf.getProperty("http_client_connect_timeout") != null) {
            connectTimeout = Integer.parseInt(conf.getProperty("http_client_connect_timeout").trim());
        }
        if (conf.getProperty("http_client_connection_request_timeout") != null) {
            connectionRequestTimeout = Integer.parseInt(conf.getProperty("http_client_connection_request_timeout").trim());
        }
        if (conf.getProperty("http_client_socket_timeout") != null) {
            socketTimeout = Integer.parseInt(conf.getProperty("http_client_socket_timeout").trim());
        }
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }
}
