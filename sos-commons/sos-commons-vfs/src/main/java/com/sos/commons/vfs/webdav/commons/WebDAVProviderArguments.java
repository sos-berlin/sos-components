package com.sos.commons.vfs.webdav.commons;

import com.sos.commons.vfs.http.commons.HTTPProviderArguments;

public class WebDAVProviderArguments extends HTTPProviderArguments {

    public WebDAVProviderArguments() {
        getProtocol().setValue(Protocol.WEBDAV);
        getPort().setDefaultValue(HTTPProviderArguments.DEFAULT_PORT);
    }
}
