package com.sos.commons.vfs.http.commons;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;

public class HTTPUtils {

    public static final long DEFAULT_LAST_MODIFIED = -1L;

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

    public static String asValidFileSystemName(String input) {
        // e.g. converts %20 to blank etc
        return URLDecoder.decode(input, StandardCharsets.UTF_8).replaceAll("[<>:\"/\\|?*]", "_");
    }

    public static boolean isSuccessful(int code) {
        if (code >= 200 && code < 300) {
            return true;
        }
        return false;
    }

    public static boolean isServerError(int code) {
        return code >= 500;
    }

    public static boolean isNotFound(int code) {
        return code == 404;
    }

    public static boolean isMethodNotAllowed(int code) {
        return code == 405;
    }

    public static String getReasonPhrase(int code) {
        return switch (code) {
        case 100 -> "Continue - The server has received the request headers and the client should proceed to send the request body.";
        case 101 -> "Switching Protocols - The server is switching protocols as requested by the client.";
        case 200 -> "OK - The request was successful.";
        case 201 -> "Created - The request was successful and a resource was created.";
        case 202 -> "Accepted - The request has been accepted for processing, but the processing is not complete.";
        case 204 -> "No Content - The server successfully processed the request but is not returning any content.";
        case 301 -> "Moved Permanently - The resource has been permanently moved to a new location.";
        case 302 -> "Found - The resource has temporarily moved to a different location.";
        case 304 -> "Not Modified - The resource has not been modified since the last request.";
        case 400 -> "Bad Request - The server could not understand the request due to invalid syntax.";
        case 401 -> "Unauthorized - Authentication is required and has failed or has not yet been provided.";
        case 403 -> "Forbidden - The client does not have access rights to the content.";
        case 404 -> "Not Found - The server can not find the requested resource.";
        case 405 -> "Method Not Allowed - The request method is not supported for the requested resource.";
        case 408 -> "Request Timeout - The server timed out waiting for the request.";
        case 409 -> "Conflict - The request conflicts with the current state of the resource.";
        case 410 -> "Gone - The resource requested is no longer available and will not be available again.";
        case 413 -> "Payload Too Large - The request entity is larger than the server is willing to process.";
        case 414 -> "URI Too Long - The URI requested by the client is longer than the server is willing to interpret.";
        case 415 -> "Unsupported Media Type - The media format of the requested data is not supported by the server.";
        case 418 -> "I'm a teapot - An April Fools' joke response code (RFC 2324).";
        case 429 -> "Too Many Requests - The user has sent too many requests in a given amount of time.";
        case 500 -> "Internal Server Error - The server encountered an internal error and could not complete the request.";
        case 501 -> "Not Implemented - The server does not support the functionality required to fulfill the request.";
        case 502 -> "Bad Gateway - The server, while acting as a gateway, received an invalid response.";
        case 503 -> "Service Unavailable - The server is not ready to handle the request.";
        case 504 -> "Gateway Timeout - The server, while acting as a gateway, did not receive a timely response.";
        case 505 -> "HTTP Version Not Supported - The server does not support the HTTP protocol version used in the request.";
        default -> "Unknown Status Code: " + code + " - No description available.";
        };
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

    @SuppressWarnings("unused")
    // is.transferTo(OutputStream.nullOutputStream()); used
    private static long countBytes(InputStream is) throws IOException {
        long count = 0L;

        byte[] buffer = new byte[4_096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            count += bytesRead;
        }
        return count;
    }

}
