package com.sos.commons.vfs.commons.proxy.http;

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

import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;

public class HttpProxySocket extends Socket {

    private final ProxyProvider provider;

    public HttpProxySocket(final ProxyProvider provider) throws UnknownHostException, IOException {
        super();
        this.provider = provider;
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        super.connect(provider.getProxy().address(), provider.getConnectTimeoutAsMillis());

        String basicAuth = null;
        if (!SOSString.isEmpty(provider.getUser())) {
            basicAuth = new String(Base64.getEncoder().encode(new String(provider.getUser() + ":" + provider.getPassword()).getBytes()));
        }

        InetSocketAddress address = (InetSocketAddress) endpoint;
        OutputStream out = this.getOutputStream();
        IOUtils.write(String.format("CONNECT %s:%s HTTP/1.0\r\n", address.getHostName(), address.getPort()), out, provider.getCharset());
        if (basicAuth != null) {
            IOUtils.write("Proxy-Authorization: Basic ", out, provider.getCharset());
            IOUtils.write(basicAuth, out, provider.getCharset());
        }
        IOUtils.write("\r\n", out, provider.getCharset());
        IOUtils.write("\r\n", out, provider.getCharset());
        out.flush();

        InputStream in = this.getInputStream();
        String response = new LineNumberReader(new InputStreamReader(in)).readLine();
        if (response == null) {
            throw new SocketException(String.format("[%s]missing response", ((InetSocketAddress) provider.getProxy().address()).getHostName()));
        }
        if (response.contains("200")) {
            in.skip(in.available());
        } else {
            throw new SocketException(String.format("[%s][invalid response]%s", ((InetSocketAddress) provider.getProxy().address()).getHostName(),
                    response));
        }
    }
}
