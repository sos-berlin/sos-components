package com.sos.commons.util.proxy.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.socket.DefaultSocketFactory;

public class HttpProxySocketFactory extends DefaultSocketFactory {

    private final ProxyConfig config;

    public HttpProxySocketFactory(final ProxyConfig config) {
        this.config = config;
    }

    @Override
    public Socket createSocket() throws UnknownHostException, IOException {
        return new HttpProxySocket(config);
    }

}
