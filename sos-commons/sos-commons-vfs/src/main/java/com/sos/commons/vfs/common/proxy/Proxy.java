package com.sos.commons.vfs.common.proxy;

import java.net.InetSocketAddress;

public class Proxy {

    private final java.net.Proxy proxy;
    private final String host;
    private final String user;
    private final String password;
    private final int connectTimeout;// milliseconds
    private int port;

    public Proxy(java.net.Proxy.Type type, String host, int port, String user, String password, int connectTimeout) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.connectTimeout = connectTimeout;
        setPort(type, port);
        proxy = new java.net.Proxy(type, new InetSocketAddress(this.host, this.port));
    }

    public java.net.Proxy getProxy() {
        return proxy;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    private void setPort(java.net.Proxy.Type type, int port) {
        if (port <= 0) {
            switch (type) {
            case HTTP:
                port = 80;
            case SOCKS:
                port = 1080;
            default:
                break;
            }
        }
        this.port = port;
    }

}
