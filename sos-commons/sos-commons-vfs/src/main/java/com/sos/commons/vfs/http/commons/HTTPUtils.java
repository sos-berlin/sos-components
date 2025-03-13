package com.sos.commons.vfs.http.commons;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.sos.commons.util.SOSString;

public class HTTPUtils {

    public static String getAccessInfo(URI baseURI, String username) {
        if (SOSString.isEmpty(username)) {
            return baseURI.toString();
        } else {
            // HTTP Format: https://user:pass@example.com:8443/path/to/resource
            // HTTPProvider: [user]https://example.com:8443/path/to/resource
            return "[" + username + "]" + baseURI.toString();
        }
    }

    /** @param spec absolute path
     * @return URI representation
     * @throws URISyntaxException */
    public static URI toBaseURI(String spec) throws URISyntaxException {
        try {
            // ensuring proper encoding
            // variant 1) new URL
            // or
            // variant 2) new URI(null,spec,null);
            return new URL(spec).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return new URI(encodeURL(spec));
        }
    }

    public static String encodeURL(String input) {
        // URLEncoder.encode converts blank to +
        return URLEncoder.encode(input, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
