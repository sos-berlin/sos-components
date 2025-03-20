package com.sos.commons.vfs.http.commons;

public class HTTPSProviderArguments extends HTTPProviderArguments {

    public static final int DEFAULT_PORT = 443;

    public HTTPSProviderArguments() {
        getProtocol().setValue(Protocol.HTTPS);
        getPort().setDefaultValue(HTTPSProviderArguments.DEFAULT_PORT);
    }
}
