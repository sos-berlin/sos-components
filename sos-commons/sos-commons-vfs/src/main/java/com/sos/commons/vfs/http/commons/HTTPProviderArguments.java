package com.sos.commons.vfs.http.commons;

import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;

public class HTTPProviderArguments extends AProviderArguments {

    private static final int DEFAULT_PORT = 80;

    private SOSArgument<List<String>> httpHeaders = new SOSArgument<>("http_headers", false);

    public HTTPProviderArguments() {
        getProtocol().setValue(Protocol.HTTP);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    public SOSArgument<List<String>> getHTTPHeaders() {
        return httpHeaders;
    }

}
