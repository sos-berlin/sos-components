package com.sos.commons.util.proxy.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sos.commons.util.proxy.SOSProxyProvider;
import com.sos.commons.util.proxy.socket.DefaultSocketFactory;

public class HttpProxySocketFactory extends DefaultSocketFactory {

    private final SOSProxyProvider provider;

    public HttpProxySocketFactory(final SOSProxyProvider provider) {
        this.provider = provider;
    }

    @Override
    public Socket createSocket() throws UnknownHostException, IOException {
        return new HttpProxySocket(provider);
    }

}
