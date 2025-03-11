package com.sos.commons.vfs.commons.proxy;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.vfs.commons.AProviderArguments;

public class ProxyProvider {

    private final java.net.Proxy proxy;
    private final String host;
    private final String user;
    private final String password;
    private final int connectTimeout;// milliseconds
    private int port;
    private Charset charset = Charset.defaultCharset();

    public static ProxyProvider createInstance(ProxyArguments args) {
        if (args == null || args.getType().isEmpty() || args.getHost().isEmpty()) {
            return null;
        }
        return new ProxyProvider(args);
    }

    private ProxyProvider(ProxyArguments args) {
        this.host = args.getHost().getValue();
        this.user = args.getUser().getValue();
        this.password = args.getPassword().getValue();
        this.connectTimeout = AProviderArguments.asMs(args.getConnectTimeout());
        setPort(args.getType().getValue(), port);
        proxy = new java.net.Proxy(args.getType().getValue(), new InetSocketAddress(this.host, this.port));
    }

    public java.net.Proxy getProxy() {
        return proxy;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean hasUserAndPassword() {
        return !SOSString.isEmpty(user) && !SOSString.isEmpty(password);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setCharset(Charset val) {
        charset = val;
    }

    public Charset getCharset() {
        return charset;
    }

    private void setPort(java.net.Proxy.Type type, int port) {
        if (port <= 0) {
            switch (type) {
            case HTTP:
                port = 80;
            case SOCKS:
                port = 1080;
            default:
                break;
            }
        }
        this.port = port;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("[");
        if (proxy != null) {
            sb.append("type=").append(proxy.type());
        }
        sb.append(",host=").append(host);
        sb.append(",port=").append(port);
        sb.append(",user=").append(user);
        if (!SOSString.isEmpty(password)) {
            sb.append(",password=").append(DisplayMode.MASKED.getValue());
        }
        sb.append(",charset=").append(charset);
        sb.append(",connectTimeout=").append(connectTimeout).append("ms");
        sb.append("]");
        return sb.toString();
    }

}
