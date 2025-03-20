package com.sos.commons.vfs.http.commons;

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
import com.sos.commons.vfs.http.commons.HTTPAuthConfig.NTLM;

public class HTTPUtils {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_TYPE_BINARY = "application/octet-stream";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    public static final String HEADER_WEBDAV_OVERWRITE = "Overwrite";
    public static final String HEADER_WEBDAV_OVERWRITE_VALUE = "T";

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

    public static String toValidFileSystemName(String input) {
        // e.g. converts %20 to blank etc
        return URLDecoder.decode(input, StandardCharsets.UTF_8).replaceAll("[<>:\"/\\|?*]", "_");
    }

    // TODO use smbj NTLMAuthenticator ...
    public static String getNTLMAuthToken(NTLM config) throws Exception {

        return null;
    }

    /** This check is sufficient if the client follows redirects.<br />
     * For explanation see
     * {@link HTTPClient#createAuthenticatedClient(com.sos.commons.util.loggers.base.ISOSLogger, URI, HTTPAuthConfig, com.sos.commons.vfs.commons.proxy.ProxyProvider, com.sos.commons.util.arguments.impl.SSLArguments, java.util.List)
     * 
     * @param code
     * @return */
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
        String reason = getReasonPhraseFromOfficialCodes(code);
        if (reason == null) {
            reason = getReasonPhraseFromUnofficialCodes(code);
            if (reason == null) {
                reason = "Unknown Status Code: " + code + " - No description available.";
            }
        }
        return reason;
    }

    private static String getReasonPhraseFromOfficialCodes(int code) {
        return switch (code) {
        // - 1xx ----------------------
        case 100 -> "Continue - The server has received the request headers and the client should proceed to send the request body.";
        case 101 -> "Switching Protocols - The server is switching protocols as requested by the client.";
        // 102 - WebDAV, deprecated
        case 102 -> "Processing - This code was used in WebDAV contexts to indicate that a request has been received by the server, but no status was available at the time of the response..";
        case 103 -> "Early Hints - Used to return some response headers before final HTTP message.";

        // - 2xx ----------------------
        case 200 -> "OK - The request was successful.";
        case 201 -> "Created - The request was successful and a resource was created.";
        case 202 -> "Accepted - The request has been accepted for processing, but the processing is not complete.";
        case 203 -> "Non-Authoritative Information - TThe server is a transforming proxy (e.g. a Web accelerator) that received a 200 OK from its origin, but is returning a modified version of the origin's response.";
        case 204 -> "No Content - The server successfully processed the request but is not returning any content.";
        case 205 -> "Reset Content - The server successfully processed the request, asks that the requester reset its document view, and is not returning any content.";
        case 206 -> "Partial Content - The server is delivering only part of the resource (byte serving) due to a range header sent by the client. The range header is used by HTTP clients to enable resuming of interrupted downloads, or split a download into multiple simultaneous streams.";
        // 207 - WebDAV
        case 207 -> "Multi-Status - The message body that follows is by default an XML message and can contain a number of separate response codes, depending on how many sub-requests were made.";
        // 208 - WebDAV
        case 208 -> "Already Reported - The members of a DAV binding have already been enumerated in a preceding part of the (multistatus) response, and are not being included again.";
        case 226 -> "IM Used (HTTP Delta encoding) - The server has fulfilled a request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance.";

        // - 3xx ----------------------
        case 300 -> "Multiple Choices - Indicates multiple options for the resource from which the client may choose (via agent-driven content negotiation).";
        case 301 -> "Moved Permanently - The resource has been permanently moved to a new location.";
        case 302 -> "Found - The resource has temporarily moved to a different location.";
        case 303 -> "See Other - The server sent this response to direct the client to get the requested resource at another URI with a GET request.";
        case 304 -> "Not Modified - The resource has not been modified since the last request.";
        // 305 - deprecated
        case 305 -> "Use Proxy -  Defined in a previous version of the HTTP specification to indicate that a requested response must be accessed by a proxy.";
        // 306 - no longer used
        case 306 -> "Switch Proxy - Originally meant 'Subsequent requests should use the specified proxy'.";
        case 307 -> "Temporary Redirect - The server sends this response to direct the client to get the requested resource at another URI with the same method that was used in the prior request.";
        case 308 -> "Permanent Redirect - This and all future requests should be directed to the given URI.";

        // - 4xx ----------------------
        case 400 -> "Bad Request - The server could not understand the request due to invalid syntax.";
        case 401 -> "Unauthorized - Authentication is required and has failed or has not yet been provided.";
        // 402 - Reserved for future use. The initial purpose of this code was for digital payment systems,
        // however this status code is rarely used and no standard convention exists.
        case 402 -> "Payment Required";
        case 403 -> "Forbidden - The client does not have access rights to the content.";
        case 404 -> "Not Found - The server can not find the requested resource.";
        case 405 -> "Method Not Allowed - The request method is not supported for the requested resource.";
        case 406 -> "Not Acceptable - The requested resource is capable of generating only content not acceptable according to the Accept headers sent in the request.";
        case 407 -> "Proxy Authentication Required - The client must first authenticate itself with the proxy.";
        case 408 -> "Request Timeout - The server timed out waiting for the request.";
        case 409 -> "Conflict - The request conflicts with the current state of the resource.";
        case 410 -> "Gone - The resource requested is no longer available and will not be available again.";
        case 411 -> "Length Required - Server rejected the request because the Content-Length header field is not defined and the server requires it.";
        case 412 -> "Precondition Failed - The server does not meet one of the preconditions that the requester put on the request header fields.";
        case 413 -> "Payload Too Large - The request entity is larger than the server is willing to process.";
        case 414 -> "URI Too Long - The URI requested by the client is longer than the server is willing to interpret.";
        case 415 -> "Unsupported Media Type - The media format of the requested data is not supported by the server.";
        case 416 -> "Range Not Satisfiable - The client has asked for a portion of the file (byte serving, Range header), but the server cannot supply that portion.";
        case 417 -> "Expectation Failed - The server cannot meet the requirements of the Expect request-header field.";
        case 418 -> "I'm a teapot - An April Fools' joke response code (RFC 2324).";
        case 421 -> "Misdirected Request - The request was directed at a server that is not able to produce a response (for example because of connection reuse).";
        // 422 - WebDAV
        case 422 -> "Unprocessable Content - The request was well-formed (i.e., syntactically correct) but could not be processed.";
        // 423 - WebDAV
        case 423 -> "Locked - The resource that is being accessed is locked.";
        // 424 - WebDAV
        case 424 -> "Failed Dependency - The request failed because it depended on another request and that request failed (e.g., a PROPPATCH).";
        case 425 -> "Too Early - Indicates that the server is unwilling to risk processing a request that might be replayed.";
        case 426 -> "Upgrade Required - The client should switch to a different protocol such as TLS/1.3, given in the Upgrade header field.";
        case 428 -> "Precondition Required - The origin server requires the request to be conditional.";
        case 429 -> "Too Many Requests - The user has sent too many requests in a given amount of time.";
        case 431 -> "Request Header Fields Too Large - The server is unwilling to process the request because either an individual header field, or all the header fields collectively, are too large.";
        case 451 -> "Unavailable For Legal Reasons - The user agent requested a resource that cannot legally be provided, such as a web page censored by a government.";

        // - 5xx ----------------------
        case 500 -> "Internal Server Error - The server encountered an internal error and could not complete the request.";
        case 501 -> "Not Implemented - The server does not support the functionality required to fulfill the request.";
        case 502 -> "Bad Gateway - The server, while acting as a gateway, received an invalid response.";
        case 503 -> "Service Unavailable - The server is not ready to handle the request.";
        case 504 -> "Gateway Timeout - The server, while acting as a gateway, did not receive a timely response.";
        case 505 -> "HTTP Version Not Supported - The server does not support the HTTP protocol version used in the request.";
        case 506 -> "Variant Also Negotiates - Transparent content negotiation for the request results in a circular reference.";
        // 507 - WebDAV
        case 507 -> "Insufficient Storage - The server is unable to store the representation needed to complete the request.";
        // 508 - WebDAV
        case 508 -> "Loop Detected - The server detected an infinite loop while processing the request (sent instead of 208 Already Reported).";
        case 510 -> "Not Extended - Further extensions to the request are required for the server to fulfil it.";
        case 511 -> "Network Authentication Required - The client needs to authenticate to gain network access.";
        default -> null;
        };
    }

    private static String getReasonPhraseFromUnofficialCodes(int code) {
        return switch (code) {
        // 218 - Apache HTTP Server - A catch-all error condition allowing the passage of message bodies through the server when the
        // - ProxyErrorOverride setting is enabled. It is displayed in this situation instead of a 4xx or 5xx error message.
        case 218 -> "[?]This is fine";
        // 419 - Used by the Laravel Framework when a CSRF Token is missing or expired.
        case 419 -> "[?]Page Expired";
        // 420 - Spring, Twitter: Method Failure (Spring Framework), Enhance Your Calm (Twitter)
        case 420 -> "[?]Method Failure/Enhance Your Calm";
        // 430 - Shopify - A deprecated response used by Shopify, instead of the 429 Too Many Requests response code, when too many URLs are
        // - requested within a certain time frame.
        case 430 -> "[?]Request Header Fields Too Large/Shopify Security Rejection";
        // 440 - Microsoft, IIS
        case 440 -> "[?]Login Time-out";
        // 444 - nginx web server
        case 444 -> "[?]No Response";
        // 449 - Microsoft, IIS
        case 449 -> "[?]Retry With";
        // 450 - Microsoft
        case 450 -> "[?]Blocked by Windows Parental Controls";
        // 460 - AWS Elastic Load Balancing
        case 460 -> "[?]Client closed the connection with the load balancer before the idle timeout period elapsed";
        // 463 - AWS Elastic Load Balancing
        case 463 -> "[?]The load balancer received an X-Forwarded-For request header with more than 30 IP addresses";
        // 464 - AWS Elastic Load Balancing
        case 464 -> "[?]Incompatible protocol versions between Client and Origin server";
        // 494 - nginx web server
        case 494 -> "[?]Request header too large";
        // 495 - nginx web server
        case 495 -> "[?]SSL Certificate Error";
        // 496 - nginx web server
        case 496 -> "[?]SSL Certificate Required";
        // 497 - nginx web server
        case 497 -> "[?]HTTP Request Sent to HTTPS Port";
        // 498 - Esri, Returned by ArcGIS for Server. Code 498 indicates an expired or otherwise invalid token.
        case 498 -> "[?]Invalid Token";
        // 499 - nginx web server/Esri
        case 499 -> "[?]Client Closed Request/Token Required";
        // 509 - Apache Web Server/cPanel
        case 509 -> "[?]Bandwidth Limit Exceeded";
        // 520 - Cloudflare
        case 520 -> "[?]Web Server Returned an Unknown Error";
        // 521 - Cloudflare
        case 521 -> "[?]Web Server Is Down";
        // 522 - Cloudflare
        case 522 -> "[?]Connection Timed Out";
        // 523 - Cloudflare
        case 523 -> "[?]Origin Is Unreachable";
        // 524 - Cloudflare
        case 524 -> "[?]A Timeout Occurred";
        // 525 - Cloudflare
        case 525 -> "[?]SSL Handshake Failed";
        // 526 - Cloudflare
        case 526 -> "[?]Invalid SSL Certificate";
        // 527 - Cloudflare
        case 527 -> "[?]Railgun Error (obsolete)";
        // 529 - Used by Qualys in the SSLLabs server testing API to signal that the site can not process the request.
        case 529 -> "[?]Site is overloaded";
        // 530 - Pantheon/Shopify
        case 530 -> "[?]Site is frozen/Origin DNS Error";
        // 540 - Shopify - Used by Shopify to indicate that the requested endpoint has been temporarily disabled.
        case 540 -> "[?]Temporarily Disabled";
        // 561 - AWS Elastic Load Balancing
        case 561 -> "[?]Unauthorized";
        // 598 - Used by some HTTP proxies to signal a network read timeout behind the proxy to a client in front of the proxy.
        case 598 -> "[?][HTTP proxy]Network read timeout error";
        // 599 - An error used by some HTTP proxies to signal a network connect timeout behind the proxy to a client in front of the proxy.
        case 599 -> "[?][HTTP proxy]Network Connect Timeout Error";
        // 783 - Used by Shopify to indicate that the request includes a JSON syntax error.
        case 783 -> "[?]Unexpected Token";
        // 999 - Error 999 is used by LinkedIn and is related to being blocked/walled or unable to access their webpages without first signing in.
        case 999 -> "[?]Non-standard";
        default -> null;
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

}
