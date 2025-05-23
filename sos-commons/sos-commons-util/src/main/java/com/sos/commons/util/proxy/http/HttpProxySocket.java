package com.sos.commons.util.proxy.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Base64;

import org.apache.commons.io.IOUtils;

import com.sos.commons.util.proxy.ProxyConfig;

public class HttpProxySocket extends Socket {

    private final ProxyConfig config;

    public HttpProxySocket(final ProxyConfig config) throws UnknownHostException, IOException {
        super();
        this.config = config;
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        super.connect(config.getProxy().address(), config.getConnectTimeoutAsMillis());

        String basicAuth = null;
        if (config.hasUserAndPassword()) {
            basicAuth = new String(Base64.getEncoder().encode(new String(config.getUser() + ":" + config.getPassword()).getBytes()));
        }

        InetSocketAddress address = (InetSocketAddress) endpoint;
        OutputStream out = this.getOutputStream();
        IOUtils.write(String.format("CONNECT %s:%s HTTP/1.0\r\n", address.getHostName(), address.getPort()), out, config.getCharset());
        if (basicAuth != null) {
            IOUtils.write("Proxy-Authorization: Basic ", out, config.getCharset());
            IOUtils.write(basicAuth, out, config.getCharset());
        }
        IOUtils.write("\r\n", out, config.getCharset());
        IOUtils.write("\r\n", out, config.getCharset());
        out.flush();

        InputStream in = this.getInputStream();
        String response = new LineNumberReader(new InputStreamReader(in)).readLine();
        if (response == null) {
            throw new SocketException(String.format("[%s]missing response", ((InetSocketAddress) config.getProxy().address()).getHostName()));
        }
        if (response.contains("200")) {
            in.skip(in.available());
        } else {
            throw new SocketException(String.format("[%s][invalid response]%s", ((InetSocketAddress) config.getProxy().address()).getHostName(),
                    response));
        }
    }
}
