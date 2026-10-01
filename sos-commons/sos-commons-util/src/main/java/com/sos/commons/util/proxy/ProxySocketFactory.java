package com.sos.commons.util.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import com.sos.commons.util.proxy.http.HttpProxySocketFactory;
import com.sos.commons.util.socket.DefaultSocketFactory;

/** A socket factory that delegates socket creation to a configured socket factory selected according to the proxy configuration.
 *
 * <p>
 * All {@code createSocket(...)} overloads delegate to the parameterless {@link #createSocket()} method.<br />
 * The parameters are intentionally ignored to ensure consistent socket creation through the configured factory. */
public class ProxySocketFactory extends DefaultSocketFactory {

    private final SocketFactory delegate;

    public ProxySocketFactory(ProxyConfig config) {
        this(createFactory(config));
    }

    public ProxySocketFactory(SocketFactory socketFactory) {
        this.delegate = socketFactory;
    }

    private static SocketFactory createFactory(ProxyConfig config) {
        return switch (config.getProxy().type()) {
        case HTTP -> new HttpProxySocketFactory(config);
        case SOCKS, DIRECT -> new DefaultSocketFactory(config.getProxy());
        };
    }

    @Override
    public Socket createSocket() throws IOException {
        return delegate.createSocket();
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
