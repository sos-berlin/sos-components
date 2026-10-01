package com.sos.commons.util.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class DefaultSocketFactory extends SocketFactory {

    private final Proxy proxy;

    /** The default constructor. */
    protected DefaultSocketFactory() {
        this(null);
    }

    public DefaultSocketFactory(final Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public Socket createSocket() throws IOException {
        if (proxy != null) {
            return new Socket(proxy);
        }
        return new Socket();
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port, localAddr, localPort);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws UnknownHostException, IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddr, final int localPort) throws UnknownHostException,
            IOException {
        if (proxy != null) {
            final Socket s = new Socket(proxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port, localAddr, localPort);
    }
}
