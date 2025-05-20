package com.sos.commons.util.proxy.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.http.HttpProxySocketFactory;

public class ProxySocketFactory extends DefaultSocketFactory {

    private final ProxyConfig config;

    public ProxySocketFactory(ProxyConfig config) {
        this.config = config;
    }

    @Override
    public Socket createSocket() throws IOException {
        switch (config.getProxy().type()) {
        case HTTP:
            return new HttpProxySocketFactory(config).createSocket();
        case SOCKS:
            return new DefaultSocketFactory(config.getProxy()).createSocket();
        default:
            break;
        }
        return null;
    }

    @Override
    public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
        return createSocket();
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        return createSocket();
    }

    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
        return createSocket();
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        return createSocket();
    }
}
