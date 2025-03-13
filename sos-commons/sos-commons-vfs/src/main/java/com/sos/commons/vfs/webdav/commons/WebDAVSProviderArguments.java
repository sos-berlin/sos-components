package com.sos.commons.vfs.webdav.commons;

import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;

public class WebDAVSProviderArguments extends WebDAVProviderArguments {

    private SSLArguments ssl;

    public WebDAVSProviderArguments() {
        getProtocol().setValue(Protocol.WEBDAVS);
        getPort().setDefaultValue(HTTPSProviderArguments.DEFAULT_PORT);
    }

    public SSLArguments getSSL() {
        return ssl;
    }

    public void setSSL(SSLArguments val) {
        ssl = val;
    }
}
