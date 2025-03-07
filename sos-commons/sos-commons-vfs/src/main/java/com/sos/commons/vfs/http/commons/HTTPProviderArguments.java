package com.sos.commons.vfs.http.commons;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.vfs.commons.AProviderExtendedArguments;

public class HTTPProviderArguments extends AProviderExtendedArguments {

    private static final int DEFAULT_PORT = 80;

    // do not set the default value because SOSArgument.getValue() returns the default value
    // - so HTTPSProviderArguments cannot check whether the port has been set or not
    private SOSArgument<Integer> port = new SOSArgument<>("port", false);

    private SOSArgument<List<String>> httpHeaders = new SOSArgument<>("http_headers", false);

    public HTTPProviderArguments() {
        getProtocol().setValue(Protocol.HTTP);
    }

    public SOSArgument<Integer> getPort() {
        if (port.isEmpty()) {
            port.setValue(Integer.valueOf(DEFAULT_PORT));
        }
        return port;
    }

    public SOSArgument<List<String>> getHTTPHeaders() {
        return httpHeaders;
    }

}
