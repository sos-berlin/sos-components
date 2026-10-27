package com.sos.commons.util.proxy.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.sos.commons.util.proxy.ProxyConfig;

public class HttpProxySocket extends Socket {

    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private static final String NEW_LINE = "\r\n";

    private final ProxyConfig config;

    public HttpProxySocket(final ProxyConfig config) throws IOException {
        super();
        this.config = config;
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        if (!(endpoint instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Endpoint must be an InetSocketAddress");
        }

        InetSocketAddress proxy = (InetSocketAddress) config.getProxy().address();
        // Establish a TCP connection to the HTTP proxy.
        super.connect(proxy, config.getConnectTimeoutAsMillis());

        // Retrieve the destination host and port from the endpoint.
        InetSocketAddress target = (InetSocketAddress) endpoint;
        String host = target.getHostString(); // Unlike getHostName(), avoids reverse DNS lookups.
        int port = target.getPort();

        StringBuilder request = new StringBuilder();
        request.append("CONNECT ").append(host).append(":").append(port).append(" HTTP/1.1").append(NEW_LINE);
        request.append("Host: ").append(host).append(":").append(port).append(NEW_LINE);

        // Add proxy authentication if credentials are configured.
        if (config.hasUserAndPassword()) {
            String credentials = config.getUser() + ":" + config.getPassword();
            String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(CHARSET));
            request.append("Proxy-Authorization: Basic ").append(basicAuth).append(NEW_LINE);
        }
        request.append(NEW_LINE);

        // Send the HTTP CONNECT request to the proxy.
        OutputStream out = getOutputStream();
        out.write(request.toString().getBytes(CHARSET));
        out.flush();

        // Read the HTTP response from the proxy.
        InputStream in = getInputStream();
        String statusLine = readHttpLine(proxy, in);
        if (statusLine == null) {
            close();
            throw new SocketException("[HTTP proxy " + proxy.getHostString() + "] HTTP proxy returned no response");
        }

        // Verify that the proxy accepted the CONNECT request.
        // e.g.: HTTP/1.1 200 Connection established
        String[] statusParts = statusLine.split(" ", 3);
        if (statusParts.length < 2 || !statusParts[0].startsWith("HTTP/") || !"200".equals(statusParts[1])) {
            close();
            throw new SocketException("[HTTP proxy " + proxy.getHostString() + "] HTTP CONNECT failed: " + statusLine);
        }

        // Read all HTTP headers until the terminating blank line.
        // Do not consume any additional bytes, as they may belong to SSH.
        String line;
        while ((line = readHttpLine(proxy, in)) != null && !line.isEmpty()) {
            // No further processing of HTTP headers is required.
        }
        if (line == null) {
            close();
            throw new SocketException("[HTTP proxy " + proxy.getHostString() + "] Incomplete HTTP CONNECT response headers");
        }
    }

    /** Reads a single HTTP line and removes its trailing CRLF.
     *
     * @param proxy the HTTP proxy address, used for error messages
     * @param in the input stream to read from
     * @return the line without its line terminator, or null if the stream ends before any bytes are read
     * @throws IOException if the stream ends in the middle of a line */
    private static String readHttpLine(final InetSocketAddress proxy, final InputStream in) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        boolean carriageReturn = false;
        while (true) {
            int value = in.read();
            if (value == -1) {
                if (buffer.size() == 0 && !carriageReturn) {
                    return null;
                }
                // ... Partial line: [HTTP/1.1 200 OK\r]
                throw new IOException("[HTTP proxy " + proxy.getHostString()
                        + "] Unexpected end of stream while reading HTTP response. Partial line: [" + buffer.toString(CHARSET) + (carriageReturn
                                ? "\\r" : "") + "]");
            }

            if (carriageReturn) {
                if (value == '\n') {
                    return buffer.toString(CHARSET);
                }
                // A CR not followed by LF is part of the line, so add it back.
                buffer.write('\r');
                carriageReturn = false;
            }

            if (value == '\r') {
                carriageReturn = true;
            } else if (value == '\n') {
                // Tolerate LF-only line endings from non-conforming proxies.
                return buffer.toString(CHARSET);
            } else {
                buffer.write(value);
            }

            // if (buffer.size() > 8192) {
            // throw new IOException("[HTTP proxy " + proxy.getHostString() + "] HTTP response line too long");
            // }
        }
    }

}
