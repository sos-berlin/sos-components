package com.sos.commons.vfs.commons.proxy;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.vfs.commons.AProviderArguments;

public class ProxyProvider {

    private static final String SYSTEM_PROPERTY_NAME_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String SYSTEM_PROPERTY_NAME_HTTP_PROXY_PORT = "http.proxyPort";

    private static final String SYSTEM_PROPERTY_NAME_HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String SYSTEM_PROPERTY_NAME_HTTPS_PROXY_PORT = "https.proxyPort";

    private static final String SYSTEM_PROPERTY_NAME_SOCKS_PROXY_HOST = "socksProxyHost";
    private static final String SYSTEM_PROPERTY_NAME_SOCKS_PROXY_PORT = "socksProxyPort";

    // private static final String SYSTEM_PROPERTY_NAME_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";

    private final java.net.Proxy proxy;
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final int connectTimeout;// milliseconds

    private Charset charset = Charset.defaultCharset();

    public static ProxyProvider createInstance(ProxyArguments args) {
        if (args == null || args.getType().isEmpty()) {
            return null;
        }
        String host = getHost(args.getType(), args.getHost());
        if (SOSString.isEmpty(host)) {
            return null;
        }
        return new ProxyProvider(host, args);
    }

    private ProxyProvider(String host, ProxyArguments args) {
        this.host = host;
        this.port = getPort(args.getType(), args.getPort());
        this.user = args.getUser().getValue();
        this.password = args.getPassword().getValue();
        this.connectTimeout = AProviderArguments.asMs(args.getConnectTimeout());

        this.proxy = new java.net.Proxy(args.getType().getValue(), new InetSocketAddress(this.host, this.port));
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

    /** <proxy_type>://[user@]host:port<br/>
     * Examples:<br/>
     * - http://user123@proxy.example.com:80<br/>
     * - http://proxy.example.com:80<br/>
     * - socks://user123@proxy.example.com:1080<br/>
     * - socks://proxy.example.com:1080<br/>
     * 
     * @return */
    public String getAccessInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(proxy.type().name().toLowerCase()).append("://");
        if (!SOSString.isEmpty(user)) {
            sb.append(user).append("@");
        }
        sb.append(host).append(":").append(port);
        return sb.toString();
    }

    private static String getHost(SOSArgument<java.net.Proxy.Type> typeArg, SOSArgument<String> hostArg) {
        if (typeArg == null || hostArg == null) {
            return null;
        }
        if (!hostArg.isEmpty()) {
            return hostArg.getValue();
        }
        switch (typeArg.getValue()) {
        case SOCKS:
            return System.getProperty(SYSTEM_PROPERTY_NAME_SOCKS_PROXY_HOST);
        case HTTP:
        default:
            String host = System.getProperty(SYSTEM_PROPERTY_NAME_HTTP_PROXY_HOST);
            if (!SOSString.isEmpty(host)) {
                return host;
            }
            return System.getProperty(SYSTEM_PROPERTY_NAME_HTTPS_PROXY_HOST);
        }
    }

    private int getPort(SOSArgument<java.net.Proxy.Type> typeArg, SOSArgument<Integer> portArg) {
        // or portArg.isDirty
        if (!portArg.isEmpty() && portArg.getValue().intValue() > 0) {
            return portArg.getValue().intValue();
        }

        String port = null;
        switch (typeArg.getValue()) {
        case SOCKS:
            port = System.getProperty(SYSTEM_PROPERTY_NAME_SOCKS_PROXY_PORT);
            if (!SOSString.isEmpty(port)) {
                return Integer.parseInt(port);
            }
            return 1080;
        case HTTP:
        default:
            port = System.getProperty(SYSTEM_PROPERTY_NAME_HTTP_PROXY_PORT);
            if (!SOSString.isEmpty(port)) {
                return Integer.parseInt(port);
            }
            port = System.getProperty(SYSTEM_PROPERTY_NAME_HTTPS_PROXY_PORT);
            if (!SOSString.isEmpty(port)) {
                return Integer.parseInt(port);
            }

            return 80;
        }
    }

}
