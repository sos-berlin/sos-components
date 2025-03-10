package com.sos.commons.vfs.commons.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.net.DefaultSocketFactory;

import com.sos.commons.vfs.commons.proxy.http.HttpProxySocketFactory;

public class ProxySocketFactory extends DefaultSocketFactory {

    private final ProxyProvider provider;

    public ProxySocketFactory(ProxyProvider provider) {
        this.provider = provider;
    }

    @Override
    public Socket createSocket() throws IOException {
        switch (provider.getProxy().type()) {
        case HTTP:
            return new HttpProxySocketFactory(provider).createSocket();
        case SOCKS:
            return new DefaultSocketFactory(provider.getProxy()).createSocket();
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
