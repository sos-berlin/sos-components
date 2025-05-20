package com.sos.commons.vfs.webdav.commons;

import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;

public class WebDAVSProviderArguments extends WebDAVProviderArguments {

    public WebDAVSProviderArguments() {
        getProtocol().setValue(Protocol.WEBDAVS);
        getPort().setDefaultValue(HTTPSProviderArguments.DEFAULT_PORT);
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return getSsl().getTrustStoreInfo();
    }
}
