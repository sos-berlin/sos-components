package com.sos.commons.vfs.webdav.commons;

import java.net.URI;
import java.net.URISyntaxException;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPUtils;

public abstract class AWebDAVProvider extends AProvider<WebDAVProviderArguments> {

    private URI baseURI;

    /** Layer for instantiating a Real Provider: JACKRABBIT or ... */
    public AWebDAVProvider() throws ProviderInitializationException {
        super(null, null);
    }

    /** Real Provider */
    public AWebDAVProvider(ISOSLogger logger, WebDAVProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        try {
            // if baseURI not found, can be redefined when connecting
            baseURI = HTTPUtils.getBaseURI(getArguments().getHost(), getArguments().getPort());
            setAccessInfo(HTTPUtils.getAccessInfo(baseURI, getArguments().getUser().getValue()));
        } catch (URISyntaxException e) {
            throw new ProviderInitializationException(e);
        }
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtils.PATH_SEPARATOR_UNIX;
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtils.isAbsoluteURIPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        return HTTPUtils.normalizePath(baseURI, path);
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(URI val) {
        baseURI = val;
    }

    public SSLArguments getSSLArguments() {
        return Protocol.WEBDAVS.equals(getArguments().getProtocol().getValue()) ? ((WebDAVSProviderArguments) getArguments()).getSSL() : null;
    }

    public HTTPAuthConfig getAuthConfig() {
        if (WebDAVAuthMethod.NTLM.equals(getArguments().getAuthMethod().getValue())) {
            return new HTTPAuthConfig(getLogger(), getArguments().getUser().getValue(), getArguments().getPassword().getValue(), getArguments()
                    .getWorkstation().getValue(), getArguments().getDomain().getValue());
        }
        // BASIC
        return new HTTPAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
    }

}
