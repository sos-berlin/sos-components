package com.sos.commons.vfs.common;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.vfs.common.proxy.Proxy;

public abstract class AProviderExtendedArguments extends AProviderArguments {

    // Proxy
    private Proxy proxy;
    private SOSArgument<java.net.Proxy.Type> proxyType = new SOSArgument<java.net.Proxy.Type>("proxy_type", false);
    private SOSArgument<String> proxyHost = new SOSArgument<String>("proxy_host", false);
    private SOSArgument<Integer> proxyPort = new SOSArgument<Integer>("proxy_port", false, -1);
    private SOSArgument<String> proxyUser = new SOSArgument<String>("proxy_user", false);
    private SOSArgument<String> proxyPassword = new SOSArgument<String>("proxy_password", false, DisplayMode.MASKED);
    // Socket connect timeout in seconds based on socket.connect
    private SOSArgument<Integer> proxyConnectTimeout = new SOSArgument<Integer>("proxy_connect_timeout", false, 30);

    public Proxy getProxy() {
        if (proxy != null) {
            return proxy;
        }
        tryCreateProxy();
        return proxy;
    }

    protected Proxy recreateProxy() {
        if (proxy == null) {
            return proxy;
        }
        tryCreateProxy();
        return proxy;
    }

    private void tryCreateProxy() {
        if (proxyType.getValue() != null && proxyHost.getValue() != null) {
            proxy = new Proxy(proxyType.getValue(), proxyHost.getValue(), proxyPort.getValue(), proxyUser.getValue(), proxyPassword.getValue(), asMs(
                    proxyConnectTimeout));
        }
    }

    public void setProxy(Proxy val) {
        proxy = val;
    }

    protected SOSArgument<java.net.Proxy.Type> getProxyType() {
        return proxyType;
    }

    protected SOSArgument<String> getProxyHost() {
        return proxyHost;
    }

    protected SOSArgument<Integer> getProxyPort() {
        return proxyPort;
    }

    protected SOSArgument<String> getProxyUser() {
        return proxyUser;
    }

    protected SOSArgument<String> getProxyPassword() {
        return proxyPassword;
    }

}
