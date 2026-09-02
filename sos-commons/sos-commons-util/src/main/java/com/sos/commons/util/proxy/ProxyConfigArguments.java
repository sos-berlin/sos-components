package com.sos.commons.util.proxy;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class ProxyConfigArguments extends ASOSArguments {

    public static final String CLASS_KEY = "PROXY";

    public static final String ENV_VAR_PROXY_SOCKS_RESOLVE_HOSTNAME = "PROXY_SOCKS_RESOLVE_HOSTNAME";
    public static final String ARG_PROXY_SOCKS_RESOLVE_HOSTNAME = ENV_VAR_PROXY_SOCKS_RESOLVE_HOSTNAME.toLowerCase();

    private SOSArgument<java.net.Proxy.Type> type = new SOSArgument<>("proxy_type", false);
    private SOSArgument<String> host = new SOSArgument<>("proxy_host", false);
    private SOSArgument<Integer> port = new SOSArgument<>("proxy_port", false, -1);
    private SOSArgument<String> user = new SOSArgument<>("proxy_user", false);
    private SOSArgument<String> password = new SOSArgument<>("proxy_password", false, DisplayMode.MASKED);
    /** Socket connect timeout in seconds based on socket.connect<br/>
     * see {@link ASOSArguments#asSeconds(SOSArgument, long) */
    private SOSArgument<String> connectTimeout = new SOSArgument<>("proxy_connect_timeout", false, "30s");

    /** Controls whether the target hostname is resolved locally for SOCKS proxies.<br />
     * If disabled, the hostname is passed to the SOCKS proxy for remote resolution. */
    private SOSArgument<Boolean> socksResolveHostname = new SOSArgument<>(ARG_PROXY_SOCKS_RESOLVE_HOSTNAME, false, Boolean.FALSE);

    public SOSArgument<java.net.Proxy.Type> getType() {
        return type;
    }

    public SOSArgument<String> getHost() {
        return host;
    }

    public SOSArgument<Integer> getPort() {
        return port;
    }

    public SOSArgument<String> getUser() {
        return user;
    }

    public SOSArgument<String> getPassword() {
        return password;
    }

    /** Socket connect timeout in seconds based on socket.connect */
    public SOSArgument<String> getConnectTimeout() {
        return connectTimeout;
    }

    public SOSArgument<Boolean> getSocksResolveHostname() {
        return socksResolveHostname;
    }

    public boolean isSOCKS() {
        return java.net.Proxy.Type.SOCKS.equals(type.getValue());
    }

    public boolean isHTTP() {
        return java.net.Proxy.Type.HTTP.equals(type.getValue());
    }

    public boolean isDIRECT() {
        return java.net.Proxy.Type.DIRECT.equals(type.getValue());
    }
}
