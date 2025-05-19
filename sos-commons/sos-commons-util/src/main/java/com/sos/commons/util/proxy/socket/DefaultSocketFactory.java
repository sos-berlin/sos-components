package com.sos.commons.util.proxy.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class DefaultSocketFactory extends SocketFactory {

    private final Proxy connProxy;

    /** The default constructor. */
    public DefaultSocketFactory() {
        this(null);
    }

    public DefaultSocketFactory(final Proxy proxy) {
        connProxy = proxy;
    }

    @Override
    public Socket createSocket() throws IOException {
        if (connProxy != null) {
            return new Socket(connProxy);
        }
        return new Socket();
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port) throws IOException {
        if (connProxy != null) {
            final Socket s = new Socket(connProxy);
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        if (connProxy != null) {
            final Socket s = new Socket(connProxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(address, port));
            return s;
        }
        return new Socket(address, port, localAddr, localPort);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws UnknownHostException, IOException {
        if (connProxy != null) {
            final Socket s = new Socket(connProxy);
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddr, final int localPort) throws UnknownHostException,
            IOException {
        if (connProxy != null) {
            final Socket s = new Socket(connProxy);
            s.bind(new InetSocketAddress(localAddr, localPort));
            s.connect(new InetSocketAddress(host, port));
            return s;
        }
        return new Socket(host, port, localAddr, localPort);
    }
}
