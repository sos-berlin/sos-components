package com.sos.commons.vfs.http;

import java.net.URI;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments.Impl;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPUtils;

public abstract class HTTPProvider extends AProvider<HTTPProviderArguments> {

    private URI baseURI;

    public static HTTPProvider createInstance(ISOSLogger logger, HTTPProviderArguments args) throws ProviderInitializationException {
        if(Impl.JAVA.equals(args.getImpl().getValue())) {
            return new com.sos.commons.vfs.http.java.ProviderImpl(logger, args);
        }
        return new com.sos.commons.vfs.http.apache.ProviderImpl(logger, args);
    }

    protected HTTPProvider(ISOSLogger logger, HTTPProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        try {
            // if baseURI not found, can be redefined when connecting
            setBaseURI(HTTPUtils.getBaseURI(getArguments().getHost(), getArguments().getPort()));
            setAccessInfo(HTTPUtils.getAccessInfo(getBaseURI(), getArguments().getUser().getValue()));
        } catch (Exception e) {
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

    public HTTPAuthConfig getAuthConfig() {
        // BASIC
        return new HTTPAuthConfig(getArguments().getUser().getValue(), getArguments().getPassword().getValue());
    }

    public SSLArguments getSSLArguments() {
        return Protocol.HTTPS.equals(getArguments().getProtocol().getValue()) ? ((HTTPSProviderArguments) getArguments()).getSSL() : null;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(URI val) {
        baseURI = val;
    }
}
