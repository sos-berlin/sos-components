package com.sos.commons.vfs.http.commons;

import com.sos.commons.vfs.commons.AProviderArguments;

public class HTTPSProviderArguments extends HTTPProviderArguments {

    public static final int DEFAULT_PORT = 443;

    public HTTPSProviderArguments() {
        getProtocol().setValue(Protocol.HTTPS);
        getPort().setDefaultValue(HTTPSProviderArguments.DEFAULT_PORT);
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return getSsl().getTrustedSslInfo();
    }
}
