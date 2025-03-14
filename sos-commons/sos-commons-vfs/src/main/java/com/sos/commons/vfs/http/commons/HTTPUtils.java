package com.sos.commons.vfs.http.commons;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;

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

    public static URI getParentURI(URI uri) {
        if (uri == null) {
            return null;
        }
        // uri.getPath().substring(0, uri.getPath().lastIndexOf('/'));
        // new URI(uri.getScheme(), uri.getHost(), newPath, null, null)
        return uri.resolve("..").normalize();
    }

    public static String encodeURL(String input) {
        // URLEncoder.encode converts blank to +
        return URLEncoder.encode(input, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public static long getFileSizeIfChunkedTransferEncoding(HttpEntity entity) throws Exception {
        long size = -1L;
        if (entity == null) {
            return size;
        }

        try (InputStream is = entity.getContent()) {
            size = 0L;

            byte[] buffer = new byte[4_096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                size += bytesRead;
            }
        }
        return size;
    }

}
