package com.sos.commons.vfs.webdav.commons;

import java.net.URI;
import java.net.URISyntaxException;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPUtils;

public abstract class AWebDAVProvider extends AProvider<WebDAVProviderArguments> {

    private URI baseURI;

    /** Layer for instantiating a Real Provider: JACKRABBIT or ... */
    public AWebDAVProvider() throws SOSProviderInitializationException {
        super(null, null);
    }

    /** Real Provider */
    public AWebDAVProvider(ISOSLogger logger, WebDAVProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args);
        try {
            // can be redefined on connect if not found
            baseURI = info();
        } catch (URISyntaxException e) {
            throw new SOSProviderInitializationException(e);
        }
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtil.PATH_SEPARATOR_UNIX;
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteURIPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)}<br/>
     * Normalizes the given path by resolving it against the base URI and ensuring proper encoding.
     * <p>
     * This method ensures that both relative and absolute paths are handled correctly.<br/>
     * It avoids using {@code new URI(String)} directly, as it would throw an exception<br/>
     * if the input contains invalid characters (e.g., spaces, special symbols).<br/>
     * Similarly, {@code new URL(String)} is not used for relative paths since it requires an absolute URL.
     * </p>
     *
     * @param path The input path, which can be relative or absolute.
     * @return A properly normalized and encoded URL string. */
    @Override
    public String normalizePath(String path) {
        // return baseURI.resolve(path).normalize().toString();
        try {
            // new URI(null, path, null) not throw an exception if the path contains invalid characters
            return baseURI.resolve(new URI(null, path, null)).toString();
        } catch (URISyntaxException e) {
            return baseURI.resolve(HTTPUtils.encodeURL(path)).normalize().toString();
        }
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

    private URI info() throws URISyntaxException {
        URI baseURI;
        String hostOrUrl = SOSPathUtil.getUnixStyleDirectoryWithoutTrailingSeparator(getArguments().getHost().getValue());
        if (isAbsolutePath(hostOrUrl)) {
            baseURI = HTTPUtils.toBaseURI(hostOrUrl);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("http://").append(hostOrUrl);
            if (getArguments().getPort().isDirty()) {
                sb.append(":").append(getArguments().getPort().getValue());
            }
            baseURI = HTTPUtils.toBaseURI(sb.toString());
        }
        setAccessInfo(HTTPUtils.getAccessInfo(baseURI, getArguments().getUser().getValue()));
        return baseURI;
    }

}
