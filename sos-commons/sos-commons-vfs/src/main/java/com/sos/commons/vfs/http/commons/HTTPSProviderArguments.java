package com.sos.commons.vfs.http.commons;

import com.sos.commons.util.arguments.impl.SSLArguments;

public class HTTPSProviderArguments extends HTTPProviderArguments {

    public static final int DEFAULT_PORT = 443;

    private SSLArguments ssl;

    public HTTPSProviderArguments() {
        getProtocol().setValue(Protocol.HTTPS);
        getPort().setDefaultValue(HTTPSProviderArguments.DEFAULT_PORT);
    }

    public SSLArguments getSSL() {
        if (ssl == null) {
            ssl = new SSLArguments();
            ssl.applyDefaultIfNullQuietly();
        }
        return ssl;
    }

    public void setSSL(SSLArguments val) {
        ssl = val;
    }
}
