package com.sos.commons.vfs.http.commons;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;

public class HTTPUtils {

    public static String getAccessInfo(URI baseURI, String username) {
        String uri = SOSString.trimEnd(baseURI.toString(), "/");
        if (SOSString.isEmpty(username)) {
            return uri;
        } else {
            // HTTP Format: https://user:pass@example.com:8443/path/to/resource
            // HTTPProvider Format: [user]https://example.com:8443/path/to/resource
            return "[" + username + "]" + uri;
        }
    }

    /** Returns a URI with a trailing slash (e.g., https://example.com/, https://example.com/test/).<br>
     * Ensures that the URI ends with '/' to allow correct resolution (see {@link HTTPUtils#normalizePath(URI, String)}).<br>
     * Without a trailing slash, relative resolution may produce incorrect results.
     * 
     * See toBaseURI(String)
     * 
     * @throws MalformedURLException */
    public static URI getBaseURI(SOSArgument<String> hostArg, SOSArgument<Integer> portArg) throws Exception {
        String hostOrUrl = SOSPathUtils.toUnixStyle(hostArg.getValue());
        if (SOSPathUtils.isAbsoluteURIPath(hostOrUrl)) {
            return toBaseURI(hostOrUrl);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("http://").append(hostOrUrl);
            if (portArg.isDirty()) {
                sb.append(":").append(portArg.getValue());
            }
            sb.append("/");
            return toBaseURI(sb.toString());
        }
    }

    /** Normalizes the given path by resolving it against the base URI and ensuring proper encoding.
     * <p>
     * This method ensures that both relative and absolute paths are handled correctly.<br/>
     * It avoids using {@code new URI(String)} directly, as it would throw an exception<br/>
     * if the input contains invalid characters (e.g., spaces, special symbols).<br/>
     * Similarly, {@code new URL(String)} is not used for relative paths since it requires an absolute URL.
     * </p>
     * Note: Without a trailing slash, relative resolution may produce incorrect results.<br/>
     * - see toBaseURI(String)
     *
     * @param path The input path, which can be relative or absolute.
     * @return A properly normalized and encoded URL string. */
    public static String normalizePath(URI baseURI, String path) {
        // return baseURI.resolve(path).normalize().toString();
        try {
            // new URI(null, path, null) not throw an exception if the path contains invalid characters
            return baseURI.resolve(new URI(null, path, null)).normalize().toString();
        } catch (URISyntaxException e) {
            return baseURI.resolve(path).normalize().toString();
        }
    }

    public static URI getParentURI(final URI uri) {
        if (uri == null) {
            return null;
        }
        // root or no path information available
        if (uri.getPath() == null || uri.getPath().equals("/")) {
            return uri;
        }
        // uri.getPath().substring(0, uri.getPath().lastIndexOf('/'));
        // new URI(uri.getScheme(), uri.getHost(), newPath, null, null)
        try {
            // must end with / to enable resolution, otherwise incorrect result
            URI normalizedUri = uri.getPath().endsWith("/") ? uri : new URI(uri.toString() + "/");
            return normalizedUri.resolve("..").normalize();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("[URISyntaxException]" + uri, e);
        }
    }

    public static String encode(String input) {
        // URLEncoder.encode converts blank to +
        return URLEncoder.encode(input, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Returns a URI with a trailing slash (e.g., https://example.com/, https://example.com/test/).<br>
     * Ensures that the URI ends with '/' to allow correct resolution (see {@link HTTPUtils#normalizePath(URI, String)}).<br>
     * Without a trailing slash, relative resolution may produce incorrect results.
     * 
     * @param spec Absolute HTTP path
     * @return URI
     * @throws URISyntaxException
     * @throws MalformedURLException */
    private static URI toBaseURI(final String spec) throws Exception {
        String baseURI = spec;
        if (!baseURI.endsWith("/")) {
            if (baseURI.contains("?")) {// with query parameters
                baseURI = SOSPathUtils.getParentPath(baseURI);
            }
            baseURI = baseURI + "/";
        }
        // ensuring proper encoding
        // variant 1) new URL
        // or
        // variant 2) new URI(null,spec,null);
        return new URL(baseURI).toURI();
    }

}
