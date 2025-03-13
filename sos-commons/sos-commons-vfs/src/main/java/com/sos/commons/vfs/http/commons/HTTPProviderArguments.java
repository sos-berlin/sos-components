package com.sos.commons.vfs.http.commons;

import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.vfs.commons.AProviderArguments;

public class HTTPProviderArguments extends AProviderArguments {

    public static final int DEFAULT_PORT = 80;

    private ProxyArguments proxy;

    // JS7 new - not in the XML schema - currently only BASIC supported
    private SOSArgument<HTTPAuthMethod> authMethod = new SOSArgument<>("auth_method", false, HTTPAuthMethod.BASIC);
    private SOSArgument<List<String>> httpHeaders = new SOSArgument<>("http_headers", false);

    public HTTPProviderArguments() {
        getProtocol().setValue(Protocol.HTTP);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    public ProxyArguments getProxy() {
        return proxy;
    }

    public void setProxy(ProxyArguments val) {
        proxy = val;
    }

    public SOSArgument<HTTPAuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<List<String>> getHTTPHeaders() {
        return httpHeaders;
    }

}
