package com.sos.commons.vfs.commons.proxy.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.net.DefaultSocketFactory;

import com.sos.commons.vfs.commons.proxy.ProxyProvider;

public class HttpProxySocketFactory extends DefaultSocketFactory {

    private final ProxyProvider provider;

    public HttpProxySocketFactory(final ProxyProvider provider) {
        this.provider = provider;
    }

    @Override
    public Socket createSocket() throws UnknownHostException, IOException {
        return new HttpProxySocket(provider);
    }

}
