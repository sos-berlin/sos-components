package com.sos.commons.util.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;

/** A socket that preserves the target hostname when establishing a connection.
 * 
 * 
 * This is useful when the endpoint provided by the caller contains an already resolved IP address, but the original target hostname must be retained.<br />
 * The hostname is preserved while the connection is established using the provided address and port.
 * 
 * 
 * For example, an application may provide a resolved endpoint such as {@code 192.168.1.50:50123} for a connection.<br />
 * The resolved IP address is ignored, and the target hostname is used together with the provided port instead. */
public class HostnamePreservingSocket extends Socket {

    private final String targetHostname;

    public HostnamePreservingSocket(String targetHostname) {
        this(targetHostname, null);
    }

    public HostnamePreservingSocket(String targetHostname, Proxy proxy) {
        super(proxy);
        this.targetHostname = targetHostname;
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        super.connect(prepareEndpoint(endpoint));
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        super.connect(prepareEndpoint(endpoint), timeout);
    }

    private SocketAddress prepareEndpoint(SocketAddress endpoint) {
        if (endpoint instanceof InetSocketAddress address) {
            return InetSocketAddress.createUnresolved(targetHostname, address.getPort());
        }
        return endpoint;
    }

}
