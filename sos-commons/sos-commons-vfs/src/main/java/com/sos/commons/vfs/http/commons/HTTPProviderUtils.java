package com.sos.commons.vfs.http.commons;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig.NTLM;

public class HTTPProviderUtils {

    /** Returns a URI with a trailing slash (e.g., https://example.com/, https://example.com/test/).<br>
     * Ensures that the URI ends with '/' to allow correct resolution (see {@link HTTPUtils#normalizePathEncoded(URI, String)}).<br>
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

    // TODO use smbj NTLMAuthenticator ...
    public static String getNTLMAuthToken(NTLM config) throws Exception {

        return null;
    }

    /** Returns a URI with a trailing slash (e.g., https://example.com/, https://example.com/test/).<br>
     * Ensures that the URI ends with '/' to allow correct resolution (see {@link HTTPUtils#normalizePathEncoded(URI, String)}).<br>
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
