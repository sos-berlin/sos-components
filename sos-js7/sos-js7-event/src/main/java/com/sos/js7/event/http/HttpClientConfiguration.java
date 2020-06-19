package com.sos.js7.event.http;

import java.util.Properties;

public class HttpClientConfiguration {

    private String host;
    // seconds
    private int connectTimeout = 30;
    private int connectionRequestTimeout = 30;
    private int socketTimeout = 75;

    public HttpClientConfiguration() {
        this(null);
    }

    public HttpClientConfiguration(String hostname) {
        host = hostname;
    }

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

    public void setHost(String val) {
        host = val;
    }

    public String getHost() {
        return host;
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
