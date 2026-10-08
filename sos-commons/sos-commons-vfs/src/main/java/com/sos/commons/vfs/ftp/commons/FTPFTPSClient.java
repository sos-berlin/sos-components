package com.sos.commons.vfs.ftp.commons;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.proxy.ProxySocketFactory;
import com.sos.commons.util.socket.HostnamePreservingSocketFactory;
import com.sos.commons.util.socket.SessionReuseSocket;
import com.sos.commons.util.ssl.SslContextFactory;
import com.sos.commons.vfs.ftp.FTPProvider;

public class FTPFTPSClient extends FTPSClient {

    private final ISOSLogger logger;
    private final SSLContext context;
    private final String serverHostname;
    private final int serverPort;

    private boolean resolveHostname;
    private String loggerPrefix;
    private SSLSocket dataSslSocket;

    private FTPFTPSClient(final ISOSLogger logger, final boolean isImplicit, final SSLContext context, final String serverHostname, int serverPort) {
        super(isImplicit, context);
        this.logger = logger;
        this.context = context;
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;

        this.resolveHostname = true;
    }

    // System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
    // System.setProperty("jdk.tls.allowLegacyResumption", "true");
    // System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
    // System.setProperty("jdk.tls.client.enableSessionTicketExtension", "false");
    public static FTPFTPSClient create(FTPProvider provider) throws Exception {
        FTPSProviderArguments args = (FTPSProviderArguments) provider.getArguments();
        boolean customStoresEnabled = args.getSsl().getTrustedSsl().isCustomStoresEnabled();
        SSLContext context = customStoresEnabled ? SslContextFactory.create(provider.getLogger(), args.getSsl()) : SslContextFactory
                .createValidatingServer(provider.getLogger());

        FTPFTPSClient client = new FTPFTPSClient(provider.getLogger(), args.isSecurityModeImplicit(), context, args.getHost().getValue(), args
                .getPort().getValue());
        client.loggerPrefix = provider.getLogPrefix();

        if (!customStoresEnabled) {
            if (!args.getSsl().getUntrustedSslVerifyCertificateHostname().isTrue()) {
                client.setHostnameVerifier(null);
                provider.logIfHostnameVerificationDisabled(args.getSsl());
            }
        }

        String[] protocols = SslContextFactory.getFilteredEnabledProtocols(args.getSsl());
        if (protocols != null && protocols.length > 0) {
            client.setEnabledProtocols(protocols);
        }

        if (provider.getProxyConfig() != null) {
            if (provider.getProxyConfig().shouldResolveSocksHostname()) {
                client.setSocketFactory(new ProxySocketFactory(provider.getProxyConfig()));
            } else {
                client.resolveHostname = false;
                // Remote verification must be disabled because getRemoteAddress() returns a synthetic InetAddress that does not represent the actual remote
                // address.
                client.setRemoteVerificationEnabled(false);
                client.setSocketFactory(new HostnamePreservingSocketFactory(client.logger, client.serverHostname, provider.getProxyConfig()
                        .getProxy()));
            }
        }
        if (client.logger.isDebugEnabled()) {
            client.logger.debug(client.loggerPrefix + "[FTPFTPSClient][create]serverHostname=" + client.serverHostname + ", resolveHostname="
                    + client.resolveHostname);
        }
        return client;
    }

    @Override
    public void execPROT(String prot) throws SSLException, IOException {
        // Do not call super.execPROT(prot) here.
        // For PROT P, Commons Net replaces the configured socket factories with its default FTPS socket factories.
        // This would discard the custom socket factory configuration required for the SOCKS5 connection.
        prot = prot == null ? "C" : prot;
        if (!Set.of("C", "E", "S", "P").contains(prot)) {
            throw new IllegalArgumentException("Invalid PROT value: " + prot);
        }

        if (FTPReply.COMMAND_OK != sendCommand("PROT", prot)) {
            throw new SSLException(getReplyString());
        }
    }

    /** Opens and initializes the FTPS data connection for the specified FTP command. This method establishes the TCP connection to the server's passive data
     * port, wraps it in an SSL/TLS socket, and reuses the TLS session of the control connection for the data connection.
     *
     * <p>
     * This method overrides the data connection implementation provided by {@link FTPClient#_openDataConnection_(String, String)} to establish the data
     * connection using FTPS/TLS.
     * </p>
     * -Djavax.net.debug=ssl,handshake,session - javax.net.ssl|DEBUG|10|main|2026-10-08 12:26:24.044 CEST|PreSharedKeyExtension.java:910|Resuming session: ...
     *
     * @param command the FTP command for which the data connection is required
     * @param arg the argument associated with the FTP command
     * @return the established SSL/TLS data connection, or {@code null} if the connection could not be established
     * @throws IOException if an I/O error occurs while establishing the connection */
    @Override
    protected Socket _openDataConnection_(final String command, final String arg) throws IOException {
        // Part 1) See FTPSClient._openDataConnection_ -> openDataSecureConnection

        // Note: Active mode is not handled - YADE uses passive mode exclusively for FTPS transfers
        // - See FTPProvider.postLoginOperationsIfFTPS - client.enterLocalPassiveMode();
        if (getDataConnectionMode() != PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
            return null;
        }

        final boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;
        final boolean attemptEPSV = isUseEPSVwithIPv4() || isInet6Address;
        if (attemptEPSV && epsv() == FTPReply.ENTERING_EPSV_MODE) {
            _parseExtendedPassiveModeReply(_replyLines.get(0));
        } else {
            // If EPSV failed on IPV4, revert to PASV
            if (isInet6Address || pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
                return null;
            }
            _parsePassiveModeReply(_replyLines.get(0));
        }

        final int soTimeoutMillis = DurationUtils.toMillisInt(getDataTimeout());
        final int passivePort = getPassivePort();

        final Socket socket = _socketFactory_.createSocket();
        try {
            if (getReceiveDataSocketBufferSize() > 0) {
                socket.setReceiveBufferSize(getReceiveDataSocketBufferSize());
            }
            if (getSendDataSocketBufferSize() > 0) {
                socket.setSendBufferSize(getSendDataSocketBufferSize());
            }
            if (getPassiveLocalIPAddress() != null) {
                socket.bind(new InetSocketAddress(getPassiveLocalIPAddress(), 0));
            }
            if (soTimeoutMillis >= 0) {
                socket.setSoTimeout(soTimeoutMillis);
            }

            // Create the socket address for the passive data connection.
            // Resolve the hostname to an IP address if resolveHostname is enabled.
            // Otherwise, create an unresolved address using the original hostname.
            InetSocketAddress socketAdress = resolveHostname ? new InetSocketAddress(getPassiveHost(), getPassivePort()) : InetSocketAddress
                    .createUnresolved(serverHostname, passivePort);
            // Establish the TCP connection to the FTP server's passive data port.
            // The connection is not encrypted at this point.
            // TLS encryption is added later by wrapping the socket in an SSLSocket.
            socket.connect(socketAdress, getConnectTimeout());

            // Wrap the existing socket in a logical socket that supports TLS session reuse.
            // This allows the data connection to reuse the TLS session of the control connection.
            // Required by FTP servers that enforce TLS session resumption for data connections,
            // e.g. to avoid FileZilla error 425: "TLS session of data connection not resumed".
            Socket logical = new SessionReuseSocket(socket, serverPort);
            // Create an SSL socket layered over the existing TCP connection.
            // The SSL socket provides TLS encryption for the data connection.
            final SSLSocket sslSocket = (SSLSocket) context.getSocketFactory().createSocket(logical, serverHostname, passivePort, true);
            // final SSLSocket sslSocket = (SSLSocket) context.getSocketFactory().createSocket(logical, serverHostname, serverPort, true);

            if (getRestartOffset() > 0 && !restart(getRestartOffset()) || !FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
                closeSockets(socket, sslSocket);
                return null;
            }

            if (isRemoteVerificationEnabled() && !verifyRemote(socket)) {
                // Grab the host before we close the socket to avoid NET-663
                final InetAddress socketHost = socket.getInetAddress();
                closeSockets(socket, sslSocket);
                throw new IOException("Host attempting data connection " + socketHost.getHostAddress() + " is not same as server "
                        + getRemoteAddress().getHostAddress());
            }

            // Part 2) See FTPSClient._openDataConnection_ after openDataSecureConnection
            _prepareDataSocket_(sslSocket);
            sslSocket.setUseClientMode(isClientMode());
            sslSocket.setEnableSessionCreation(isCreation());

            // server mode
            if (!isClientMode()) {
                sslSocket.setNeedClientAuth(isNeedClientAuth());
                sslSocket.setWantClientAuth(isWantClientAuth());
            }

            String[] suites = getSuites();
            if (suites != null) {
                sslSocket.setEnabledCipherSuites(suites);
            }
            String[] protocols = getProtocols();
            if (protocols != null) {
                sslSocket.setEnabledProtocols(protocols);
            }

            // Perform the TLS handshake and establish the secure data connection.
            sslSocket.startHandshake();

            if (logger.isDebugEnabled()) {
                logger.debug("%s[Data TLS connection established]Remote=%s, Protocol=%s, Cipher=%s", loggerPrefix, sslSocket.getRemoteSocketAddress(),
                        sslSocket.getSession().getProtocol(), sslSocket.getSession().getCipherSuite());
                // logger.debug("%s[Data TLS connection established]local=%s remote=%s protocol=%s cipher=%s sessionId=%s", loggerPrefix, sslSocket
                // .getLocalSocketAddress(), sslSocket.getRemoteSocketAddress(), sslSocket.getSession().getProtocol(), sslSocket.getSession()
                // .getCipherSuite(), HexFormat.of().formatHex(sslSocket.getSession().getId()));
            }

            dataSslSocket = sslSocket;
            return sslSocket;
        } catch (IOException | RuntimeException e) {
            SOSClassUtil.closeQuietly(socket);
            throw e;
        }
    }

    @Override
    public InetAddress getRemoteAddress() {
        return resolveHostname ? super.getRemoteAddress() : FTPFqdnFTPClient.getRemoteAddress(serverHostname);
    }

    public void dataSslSocketShutdownOutput() throws IOException {
        if (dataSslSocket == null || dataSslSocket.isClosed()) {
            return;
        }
        dataSslSocket.shutdownOutput();
    }

    private void closeSockets(final Socket socket, final Socket sslSocket) throws IOException {
        IOUtils.close(socket, sslSocket);
    }

}
