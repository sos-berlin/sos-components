package com.sos.commons.vfs.ftp.commons;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.net.ftp.FTPClient;

import com.sos.commons.util.socket.HostnamePreservingSocketFactory;

/** An {@link FTPClient} extension that preserves the server hostname without triggering DNS resolution when the remote address is requested.
 *
 * <p>
 * This is part of the mechanism used to keep the server hostname available for downstream connection handling.<br />
 * 
 * @see HostnamePreservingSocketFactory */
public class FTPFqdnFTPClient extends FTPClient {

    private final String serverHostname;

    public FTPFqdnFTPClient(String serverHostname) {
        super();
        this.serverHostname = serverHostname;

        // Remote verification must be disabled because getRemoteAddress() returns a synthetic InetAddress that does not represent the actual remote address.
        setRemoteVerificationEnabled(false);
    }

    /** @see #getRemoteAddress(String) */
    @Override
    public InetAddress getRemoteAddress() {
        return getRemoteAddress(serverHostname);
    }

    /** FTPClient.getRemoteAddress() requires an {@link InetAddress}.<br />
     * The {@link HostnamePreservingSocketFactory} uses an unresolved {@link java.net.InetSocketAddress}, so the address itself is not available.<br />
     * Resolving the server hostname with {@link InetAddress#getByName(String)} is not an option here, as this would trigger DNS resolution.<br />
     * Instead, a synthetic {@link InetAddress} is created that carries the server hostname without resolving it. */
    public static InetAddress getRemoteAddress(String serverHostname) {
        try {
            return InetAddress.getByAddress(serverHostname, new byte[] { 0, 0, 0, 0 });
        } catch (UnknownHostException e) {
            throw new IllegalStateException("[getRemoteAddress][" + serverHostname + "]Cannot create InetAddress", e);
        }
    }

}
