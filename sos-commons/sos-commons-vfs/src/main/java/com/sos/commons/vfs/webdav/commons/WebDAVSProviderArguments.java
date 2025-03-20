package com.sos.commons.vfs.webdav.commons;

import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;

public class WebDAVSProviderArguments extends WebDAVProviderArguments {

    public WebDAVSProviderArguments() {
        getProtocol().setValue(Protocol.WEBDAVS);
        getPort().setDefaultValue(HTTPSProviderArguments.DEFAULT_PORT);
    }
}
