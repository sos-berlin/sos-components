package com.sos.commons.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;

public class HttpUtils {

    /** HTTP header names are case-insensitive by spec (RFC 7230) */
    public static final String HEADER_AUTHORIZATION = "authorization";
    public static final String HEADER_PROXY_AUTHORIZATION = "proxy-authorization";

    public static final String HEADER_COOKIE = "cookie";
    public static final String HEADER_SET_COOKIE = "set-cookie";

    public static final String HEADER_SERVER = "server";
    public static final String HEADER_RANGE = "range";
    public static final String HEADER_DATE = "date";
    public static final String HEADER_IF_MODIFIED_SINCE = "if-modified-since";
    public static final String HEADER_IF_UNMODIFIED_SINCE = "if-unmodified-since";
    public static final String HEADER_IF_MATCH = "if-match";
    public static final String HEADER_IF_NONE_MATCH = "if-none-match";

    public static final String HEADER_CONTENT_ENCODING = "content-encoding";
    public static final String HEADER_CONTENT_LANGUAGE = "content-language";
    public static final String HEADER_CONTENT_DISPOSITION = "content-disposition";
    public static final String HEADER_CONTENT_LENGTH = "content-length";
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String HEADER_CONTENT_MD5 = "content-md5";
    public static final String HEADER_LAST_MODIFIED = "last-modified";

    public static final String HEADER_CONTENT_TYPE_BINARY = "application/octet-stream";
    public static final String HEADER_CONTENT_TYPE_JSON = "application/json";

    public static final String HEADER_WEBDAV_OVERWRITE = "overwrite";
    public static final String HEADER_WEBDAV_OVERWRITE_VALUE = "T";

    public static final long DEFAULT_LAST_MODIFIED = -1L;

    // HTTP-date formats (RFC 1123, RFC 1036, asctime)
    // https://datatracker.ietf.org/doc/html/rfc7232#section-2.2
    // - not really normalized ... Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
    // Locale.US - because of the weekdays in English (e.g. Tue)
    private static final List<DateTimeFormatter> HTTP_DATE_FORMATTERS = List.of(
            // RFC 1123
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH).withZone(ZoneOffset.UTC),
            // RFC 1036
            DateTimeFormatter.ofPattern("EEE, dd-MMM-yy HH:mm:ss zzz", Locale.ENGLISH).withZone(ZoneOffset.UTC),
            // asctime
            DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH).withZone(ZoneOffset.UTC));

    public static String getAccessInfo(URI baseURI, String username) {
        if (baseURI == null) {
            return null;
        }
        String uri = SOSString.trimEnd(baseURI.toString(), "/");
        if (SOSString.isEmpty(username)) {
            return uri;
        } else {
            // HTTP Format: https://user:pass@example.com:8443/path/to/resource
            // SOSHTTPUtils Format: [user]https://example.com:8443/path/to/resource
            return "[" + username + "]" + uri;
        }
    }

    /** Normalizes the given path by resolving it against the base URI - without encoding/fallback with encoding
     * 
     * @param baseURI
     * @param path
     * @return */
    public static String normalizePath(URI baseURI, String path) {
        try {
            if (baseURI == null) {
                if (SOSPathUtils.isAbsoluteURIPath(path)) {
                    // new URI() instead of URI.create (due to possible double encoding)
                    return new URI(path).normalize().toString();
                }
                return SOSPathUtils.toUnixStyle(path);
            }
            // uses toString() and not toASCIIString() (see normalizePathEncoded) because
            // this method assumes the input is already a well-formed URI or path.
            // Using toASCIIString() here could incorrectly double-encode percent-encoded characters
            // (e.g., "%20" would become "%2520"), which breaks correct URIs.
            // Encoding is only performed in normalizePathEncoded when needed.
            return baseURI.resolve(path).normalize().toString();
        } catch (IllegalArgumentException | NullPointerException | URISyntaxException e) {
            return normalizePathEncoded(baseURI, path);
        }
    }

    public static URI getParentURI(final URI uri) {
        if (uri == null) {
            return null;
        }
        // root or no path information available
        if (SOSString.isEmpty(uri.getPath()) || uri.getPath().equals("/")) {
            return uri;
        }
        // uri.getPath().substring(0, uri.getPath().lastIndexOf('/'));
        // new URI(uri.getScheme(), uri.getHost(), newPath, null, null)

        // must end with / to enable resolution, otherwise incorrect result
        URI directoryUri = ensureDirectoryURI(uri);
        return directoryUri.resolve("..").normalize();
    }

    public static URI ensureDirectoryURI(URI uri) {
        if (uri != null && uri.getPath() != null && !uri.getPath().endsWith("/")) {
            return URI.create(uri.toString() + "/");
        }
        return uri;
    }

    /** Encodes a string for safe use as a URL query parameter.
     * <p>
     * This method uses {@link URLEncoder} with UTF-8 encoding to escape characters that are unsafe in query parameters.<br/>
     * Note that {@code URLEncoder} encodes spaces as {@code +}, which is valid and expected in application/x-www-form-urlencoded data.
     * </p>
     *
     * @param input the raw query parameter value to encode
     * @return the encoded query string, or {@code null} if the input is null */
    public static String encodeQueryParam(String input) {
        if (input == null) {
            return null;
        }
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    /** Decodes a URL-encoded query parameter string using UTF-8.
     * <p>
     * This method decodes percent-encoded characters (e.g. {@code %20} → space) and converts {@code +} to space,<br/>
     * as defined in the application/x-www-form-urlencoded format.
     * </p>
     *
     * @param input the encoded query parameter string to decode
     * @return the decoded string, or {@code null} if the input is null */
    public static String decodeQueryParam(String input) {
        if (input == null) {
            return null;
        }
        return URLDecoder.decode(input, StandardCharsets.UTF_8);
    }

    /** Ensures a safe, normalized, and percent-encoded relative URI path.<br/>
     * This avoids {@link URLEncoder} and preserves {@code +} characters.<br/>
     * 
     * <p>
     * The input can be a simple filename ("myfile.txt") or a relative path ("subdir/myfile.txt").
     * </p>
     * <p>
     * If the path contains no slashes and has characters invalid in a URI (e.g. ':', '<', '>'), it attempts to prepend a slash to allow URI construction, then
     * removes it to return a relative path.
     * </p>
     * 
     * <p>
     * Falls back to the original input if encoding fails.
     * </p>
     *
     * @param relativePath relative path or file name
     * @return encoded relative path (without leading slash), or the original input if encoding fails */
    public static String normalizeAndEncodeRelativePath(String relativePath) {
        if (relativePath == null) {
            return null;
        }
        try {
            String uri = new URI(null, relativePath, null).normalize().toASCIIString();
            // remove leading slash(s) to return a relative path
            return SOSString.trimStart(uri, "/");
        } catch (URISyntaxException e) {
            // if relativePath is a single name and contains some invalid characters that should be encoded (e.g.: "myfile:<>.txt"),
            // adding leading slashes helps to fix the URI
            if (!relativePath.contains("/")) {
                return normalizeAndEncodeRelativePath("/" + relativePath);
            }
            return relativePath;
        }
    }

    /** Safely decodes a URI-encoded string, while preserving the '+' character as is.
     * <p>
     * This method decodes all percent-encoded sequences while ensuring that the '+' character is preserved and not converted into a space. The method decodes
     * characters like '%20' (space) but leaves '+' as it is.
     * </p>
     * 
     * @param encoded the URI-encoded string
     * @return the decoded string, or the original string if decoding fails */
    public static String decodeUriPath(String encoded) {
        if (encoded == null) {
            return null;
        }

        try {
            // Temporarily replace '+' with a placeholder to avoid URLDecoder treating it as a space
            String replacedPlus = encoded.replace("+", "%2B");

            // Decode the URI-encoded string, now '%20' becomes a space but '%2B' stays as '+'
            String decoded = URLDecoder.decode(replacedPlus, "UTF-8");

            // Return the decoded string with the placeholder '%2B' converted back to '+'
            return decoded.replace("%2B", "+");
        } catch (UnsupportedEncodingException e) {
            return encoded;
        }
    }

    /** Converts a potentially URI-encoded string into a valid filesystem name.
     * <p>
     * This method safely decodes percent-encoded sequences (e.g., {@code %20} -> space) and replaces illegal filesystem characters with underscores.<br/>
     * It also handles malformed percent-encodings (e.g., {@code %&} → {@code %25&}) to avoid decoding exceptions.
     * </p>
     * 
     * @param input the original string, possibly URI-encoded
     * @param isWindows true if the resulting name should be valid on Windows, false for Unix
     * @return a sanitized string safe to use as a file or directory name */
    public static String toValidFileSystemName(String input, boolean isWindows) {
        if (input == null) {
            return null;
        }
        String illegalChars = isWindows ? SOSPathUtils.FILENAME_ILLEGAL_CHARS_REGEX_WINDOWS : SOSPathUtils.FILENAME_ILLEGAL_CHARS_REGEX_UNIX;
        try {
            // Replace invalid percent sequences (e.g., "%&") with "%25&" to avoid URI decoding failures
            if (input.contains("%")) {
                input = input.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                input = decodeUriPath(input);
            }
            return input.replaceAll(illegalChars, "_");
        } catch (IllegalArgumentException e) {
            return input.replaceAll(illegalChars, "_");
        }
    }

    public static long httpDateToMillis(String httpDate) {
        if (SOSString.isEmpty(httpDate)) {
            return DEFAULT_LAST_MODIFIED;
        }
        for (DateTimeFormatter formatter : HTTP_DATE_FORMATTERS) {
            try {
                return ZonedDateTime.parse(httpDate, formatter).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
            }
        }
        return DEFAULT_LAST_MODIFIED;
    }

    /** Converts the input string to lowercase using Locale.ROOT to ensure consistent, locale-independent behavior.<br/>
     * This is especially important when processing technical identifiers like HTTP header names.
     *
     * Without specifying Locale.ROOT, the result of toLowerCase() can vary depending on the system's default locale.<br/>
     * For example, in the Turkish locale,<br/>
     * "Content-ID".toLowerCase() would produce "content-ıd" (with a dotless 'ı') instead of the expected "content-id".<br/>
     *
     * This can lead to subtle and hard-to-find bugs when comparing or looking up headers in a case-insensitive context, such as when normalizing HTTP headers.
     *
     * @param val the input string
     * @return the lowercase version of the input, or null if the input is null */
    public static String normalizeHeaderName(String val) {
        if (val == null) {
            return null;
        }
        return val.toLowerCase(Locale.ROOT);
    }

    /** Returns a new LinkedHashMap with all header names normalized to lowercase, using Locale.ROOT to ensure locale-independent behavior.
     *
     * Useful for case-insensitive HTTP header lookups.
     *
     * If multiple keys in the original map differ only by case, the last one wins.
     *
     * @param headers the original headers map
     * @return a new map with all keys in lowercase */
    public static Map<String, String> normalizeHeaderKeys(Map<String, String> headers) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (headers == null) {
            return normalized;
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = normalizeHeaderName(entry.getKey());
            normalized.put(key, entry.getValue());
        }
        return normalized;
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

    public static boolean isUnauthorized(int code) {
        return code == 401;
    }

    public static boolean isForbidden(int code) {
        return code == 403;
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

    /** Normalizes the given path by resolving it against the base URI and ensuring proper encoding.
     * <p>
     * This method uses {@code new URI(null, path, null)} instead of {@code URLEncoder.encode()},<br/>
     * because {@code URLEncoder} is designed for query parameters, not path components.<br/>
     * It encodes slashes ('/') and replaces spaces with plus signs ('+'), which breaks URI path semantics. <br/>
     * <br/>
     * {@code new URI(null, path, null)} correctly encodes only illegal characters in the path segment while preserving slashes and path structure.<br/>
     * It produces a valid percent-encoded URI path without over-encoding already-safe characters.
     * </p>
     * <p>
     * The result is returned using {@code toASCIIString()} to ensure it's safe for transmission in HTTP or WebDAV headers,<br/>
     * where non-ASCII characters must be percent-encoded.<br/>
     * - {@code toASCIIString()}: RFC-3986-conform ASCII-Version, with UTF-8 percent-encoding (e.g for: ß ...)
     * </p>
     *
     * @param baseURI the base URI to resolve against, or {@code null} if the path is absolute
     * @param path the path to normalize and encode
     * @return a properly normalized and ASCII-encoded URI path */
    private static String normalizePathEncoded(URI baseURI, String path) {
        try {
            // new URI(null, path, null) not throw an exception if the path contains invalid characters
            if (baseURI == null) {
                return new URI(null, path, null).normalize().toASCIIString();
            }
            return baseURI.resolve(new URI(null, path, null)).normalize().toASCIIString();
        } catch (URISyntaxException e) {
            if (baseURI == null) {
                return SOSPathUtils.toUnixStyle(path);
            }
            return SOSPathUtils.appendPath(baseURI.toString(), path, "/");
        }
    }

}
