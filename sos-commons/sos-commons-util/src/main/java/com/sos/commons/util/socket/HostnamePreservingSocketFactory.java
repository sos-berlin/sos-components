package com.sos.commons.util.socket;

import java.io.IOException;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sos.commons.util.loggers.base.ISOSLogger;

/** A socket factory that creates sockets which preserve the target hostname when establishing a connection.
 *
 * <p>
 * If a connection endpoint contains a resolved IP address, the resolved address is ignored and the target hostname is used instead.<br />
 * This allows the hostname to remain available to the underlying proxy or connection mechanism for DNS resolution. */
public class HostnamePreservingSocketFactory extends DefaultSocketFactory {

    private final Proxy proxy;
    private final String targetHostname;

    public HostnamePreservingSocketFactory(final ISOSLogger logger, final String targetHostname) {
        this(logger, targetHostname, null);
    }

    public HostnamePreservingSocketFactory(final ISOSLogger logger, final String targetHostname, Proxy proxy) {
        this.targetHostname = targetHostname;
        this.proxy = proxy;

        if (logger.isDebugEnabled()) {
            logger.debug("[HostnamePreservingSocketFactory]targetHostname=" + targetHostname + ", proxy=" + proxy);
        }
    }

    @Override
    public Socket createSocket() throws UnknownHostException, IOException {
        return new HostnamePreservingSocket(targetHostname, proxy);
    }

}
