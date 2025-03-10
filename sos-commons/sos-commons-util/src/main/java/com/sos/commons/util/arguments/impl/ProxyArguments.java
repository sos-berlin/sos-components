package com.sos.commons.util.arguments.impl;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class ProxyArguments extends ASOSArguments {

    public static final String CLASS_KEY = "PROXY";

    private SOSArgument<java.net.Proxy.Type> type = new SOSArgument<>("proxy_type", false);
    private SOSArgument<String> host = new SOSArgument<>("proxy_host", false);
    private SOSArgument<Integer> port = new SOSArgument<>("proxy_port", false, -1);
    private SOSArgument<String> user = new SOSArgument<>("proxy_user", false);
    private SOSArgument<String> password = new SOSArgument<>("proxy_password", false, DisplayMode.MASKED);
    /** Socket connect timeout in seconds based on socket.connect */
    private SOSArgument<Integer> connectTimeout = new SOSArgument<>("proxy_connect_timeout", false, 30);

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
    public SOSArgument<Integer> getConnectTimeout() {
        return connectTimeout;
    }
}
