package com.sos.commons.httpclient.commons;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.net.ssl.SSLSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;

/** Base HTTP client wrapper for Java's HttpClient.<br/>
 * Provides convenient methods for executing HTTP requests with or without parsing the response body.<br/>
 * Supports GET, PUT, DELETE, and conditional HEAD fallback.<br/>
 */
public abstract class ABaseHttpClient implements AutoCloseable {

    private static final Set<String> DEFAULT_SENSITIVE_HEADERS = Set.of("Authorization", "Proxy-Authorization", "Cookie", "Set-Cookie", "X-Api-Key",
            "X-Auth-Token", "Authentication-Token", "Session-Id");

    private static final String MASKED_STRING = "********";
    private final ISOSLogger logger;
    /** Underlying Java HTTP client instance */
    private final HttpClient client;
    /** Optional headers to be applied to all requests */
    private Map<String, String> headers;
    // Header order does not matter in HTTP, but LinkedHashSet preserves insertion order
    // for consistent debug output instead of random order
    private Set<String> sensitiveHeaders = new LinkedHashSet<>(DEFAULT_SENSITIVE_HEADERS);
    /** Cached result of whether HEAD is allowed on the target server */
    private Boolean isHEADMethodAllowed;
    // TODO how to implement not chunked transfer?
    // Set Content-Lenght throws the java.lang.IllegalArgumentException: restricted header name: "Content-Length" Exception...
    // System.setProperty("jdk.httpclient.allowRestrictedHeaders", "false");
    /** Whether chunked transfer encoding should be used for uploads */
    private boolean chunkedTransfer = true;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected ABaseHttpClient(ISOSLogger logger, HttpClient client) {
        this(logger, client, true);
    }

    protected ABaseHttpClient(ISOSLogger logger, HttpClient client, boolean chunkedTransfer) {
        this.logger = logger;
        this.client = client;
        this.chunkedTransfer = chunkedTransfer;
    }

    @Override
    public void close() throws Exception {
        // No resources to close currently
    }

    /** Creates a new HttpRequest builder with base headers
     * 
     * @param uri target URI
     * @return initialized request builder with URI and headers */
    public HttpRequest.Builder createRequestBuilder(URI uri) {
        return createRequestBuilder(uri, headers);
    }

    /** Creates a new HttpRequest builder with given headers
     * 
     * @param uri target URI
     * @return initialized request builder with URI and headers */
    public HttpRequest.Builder createRequestBuilder(URI uri, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
        setRequestHeaders(builder, headers);
        return builder;
    }

    /** Executes a generic request with a specified body handler */
    public <T> HttpExecutionResult<T> execute(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws Exception {
        return new HttpExecutionResult<>(request, client.send(request, bodyHandler));
    }

    /** Executes a request and returns the response body as a String */
    public HttpExecutionResult<String> executeWithResponseBody(HttpRequest request) throws Exception {
        return execute(request, HttpResponse.BodyHandlers.ofString());
    }

    /** Executes a request and returns the response body as a JsonNode */
    public HttpExecutionResult<JsonNode> executeWithJsonNodeResponseBody(HttpRequest request) throws Exception {
        HttpExecutionResult<String> r = executeWithResponseBody(request);
        verifyJsonResponse(r.response());
        return wrapHttpExecutionResultJsonNode(r);
    }

    /** Executes a request and parses the JSON response body into the specified type */
    public <T> HttpExecutionResult<T> executeWithJsonResponseBody(HttpRequest request, Class<T> type) throws Exception {
        HttpExecutionResult<String> r = executeWithResponseBody(request);
        verifyJsonResponse(r.response());
        return wrapHttpExecutionResultJson(r, type);
    }

    /** Executes a request and parses the JSON response body into the specified generic type (Map, List etc) */
    public <T> HttpExecutionResult<T> executeWithJsonResponseBody(HttpRequest request, TypeReference<T> typeRef) throws Exception {
        HttpExecutionResult<String> r = executeWithResponseBody(request);
        verifyJsonResponse(r.response());
        return wrapHttpExecutionResultJson(r, typeRef);
    }

    /** Executes a request and parses the JSON response body into the Map String,Object */
    public HttpExecutionResult<Map<String, Object>> executeWithJsonAsMapResponseBody(HttpRequest request) throws Exception {
        HttpExecutionResult<String> r = executeWithResponseBody(request);
        verifyJsonResponse(r.response());
        return wrapHttpExecutionResultJson(r, new TypeReference<Map<String, Object>>() {
        });
    }

    /** Executes a request and discards the response body<br/>
     * Useful for HEAD or requests where only status is needed */
    public HttpExecutionResult<Void> executeNoResponseBody(HttpRequest request) throws Exception {
        return execute(request, HttpResponse.BodyHandlers.discarding());
    }

    /** Executes a HEAD request if supported, otherwise falls back to GET<br/>
     * Only returns status and headers, no response body<br/>
     */
    public HttpExecutionResult<Void> executeHEADOrGETNoResponseBody(URI uri) throws Exception {
        HttpRequest request = null;
        boolean isHEAD = false;
        if (isHEADMethodAllowed == null || isHEADMethodAllowed) {
            request = createHEADRequest(uri);
            isHEAD = true;
        } else {
            request = createGETRequest(uri);
        }
        HttpExecutionResult<Void> result = executeNoResponseBody(request);
        if (isHEAD) {
            if (HttpUtils.isMethodNotAllowed(result.response().statusCode())) {
                isHEADMethodAllowed = false;
                result = executeNoResponseBody(createGETRequest(uri));
            } else {
                isHEADMethodAllowed = true;
            }
        }
        return result;
    }

    /** Executes a GET request and handles the response via provided handler */
    public <T> HttpExecutionResult<T> executeGET(URI uri, HttpResponse.BodyHandler<T> handler) throws Exception {
        return execute(createGETRequest(uri), handler);
    }

    /** Executes a GET request and returns response as String */
    public HttpExecutionResult<String> executeGET(URI uri) throws Exception {
        return executeWithResponseBody(createGETRequest(uri));
    }

    /** Executes a GET request and returns response as JsonNode */
    public HttpExecutionResult<JsonNode> executeGETJson(URI uri) throws Exception {
        return executeWithJsonNodeResponseBody(createGETRequest(uri));
    }

    /** Executes a GET request and parses the JSON response body into the specified type */
    public <T> HttpExecutionResult<T> executeGETJson(URI uri, Class<T> type) throws Exception {
        return executeWithJsonResponseBody(createGETRequest(uri), type);
    }

    /** Executes a GET request and parses the JSON response body into the specified generic type (Map, List etc) */
    public <T> HttpExecutionResult<T> executeGETJson(URI uri, TypeReference<T> typeRef) throws Exception {
        return executeWithJsonResponseBody(createGETRequest(uri), typeRef);
    }

    /** Executes a GET request and discards the response body */
    public HttpExecutionResult<Void> executeGETNoResponseBody(URI uri) throws Exception {
        return executeNoResponseBody(createGETRequest(uri));
    }

    /** Executes a POST request without request body and handles the response via provided handler */
    public <T> HttpExecutionResult<T> executePOST(URI uri, HttpResponse.BodyHandler<T> handler) throws Exception {
        return execute(createPOSTRequest(uri, HttpRequest.BodyPublishers.noBody()), handler);
    }

    /** Executes a POST request with request body publisher and handles the response via provided handler */
    public <T> HttpExecutionResult<T> executePOST(URI uri, HttpRequest.BodyPublisher bodyPublisher, HttpResponse.BodyHandler<T> handler)
            throws Exception {
        return execute(createPOSTRequest(uri, bodyPublisher), handler);
    }

    /** Executes a POST request with request body and handles the response via provided handler */
    public <T> HttpExecutionResult<T> executePOST(URI uri, String requestBody, HttpResponse.BodyHandler<T> handler) throws Exception {
        return execute(createPOSTRequest(uri, HttpRequest.BodyPublishers.ofString(requestBody)), handler);
    }

    /** Executes a POST request without request body and returns response as String */
    public HttpExecutionResult<String> executePOST(URI uri) throws Exception {
        return executeWithResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.noBody()));
    }

    /** Executes a POST request with request body publisher and returns response as String */
    public HttpExecutionResult<String> executePOST(URI uri, HttpRequest.BodyPublisher bodyPublisher) throws Exception {
        return executeWithResponseBody(createPOSTRequest(uri, bodyPublisher));
    }

    /** Executes a POST request with request body and returns response as String */
    public HttpExecutionResult<String> executePOST(URI uri, String requestBody) throws Exception {
        return executeWithResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.ofString(requestBody)));
    }

    /** Executes a POST request with request body publisher and returns response as JsonNode */
    public HttpExecutionResult<JsonNode> executePOSTJson(URI uri, HttpRequest.BodyPublisher bodyPublisher) throws Exception {
        return executeWithJsonNodeResponseBody(createPOSTRequest(uri, bodyPublisher));
    }

    /** Executes a POST request with request body and returns response as JsonNode */
    public HttpExecutionResult<JsonNode> executePOSTJson(URI uri, String requestBody) throws Exception {
        return executeWithJsonNodeResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.ofString(requestBody)));
    }

    /** Executes a POST request without request body and returns response as JsonNode */
    public HttpExecutionResult<JsonNode> executePOSTJson(URI uri) throws Exception {
        return executeWithJsonNodeResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.noBody()));
    }

    /** Executes a POST request with request body publisher and parses the JSON response body into the specified type */
    public <T> HttpExecutionResult<T> executePOSTJson(URI uri, HttpRequest.BodyPublisher bodyPublisher, Class<T> type) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, bodyPublisher), type);
    }

    /** Executes a POST request with request body and parses the JSON response body into the specified type */
    public <T> HttpExecutionResult<T> executePOSTJson(URI uri, String requestBody, Class<T> type) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.ofString(requestBody)), type);
    }

    /** Executes a POST request without request body and parses the JSON response body into the specified type */
    public <T> HttpExecutionResult<T> executePOSTJson(URI uri, Class<T> type) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.noBody()), type);
    }

    /** Executes a POST request with request body publisher and parses the JSON response body into the specified generic type (Map, List etc) */
    public <T> HttpExecutionResult<T> executePOSTJson(URI uri, HttpRequest.BodyPublisher bodyPublisher, TypeReference<T> typeRef) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, bodyPublisher), typeRef);
    }

    /** Executes a POST request with request body and parses the JSON response body into the specified generic type (Map, List etc) */
    public <T> HttpExecutionResult<T> executePOSTJson(URI uri, String requestBody, TypeReference<T> typeRef) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.ofString(requestBody)), typeRef);
    }

    /** Executes a POST request without request body and parses the JSON response body into the specified generic type (Map, List etc) */
    public <T> HttpExecutionResult<T> executePOSTJson(URI uri, TypeReference<T> typeRef) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.noBody()), typeRef);
    }

    /** Executes a POST request with request body and parses the JSON response body into the Map String,Object */
    public HttpExecutionResult<Map<String, Object>> executePOSTJsonAsMap(URI uri, String requestBody) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.ofString(requestBody)),
                new TypeReference<Map<String, Object>>() {
                });
    }

    /** Executes a POST request without request body and parses the JSON response body into the Map String,Object */
    public HttpExecutionResult<Map<String, Object>> executePOSTJsonAsMap(URI uri) throws Exception {
        return executeWithJsonResponseBody(createPOSTRequest(uri, HttpRequest.BodyPublishers.noBody()), new TypeReference<Map<String, Object>>() {
        });
    }

    /** Executes a DELETE request and returns the response body with a given handler */
    public <T> HttpExecutionResult<T> executeDELETE(URI uri, HttpResponse.BodyHandler<T> handler) throws Exception {
        return execute(createDELETERequest(uri), handler);
    }

    /** Executes a DELETE request and returns the response as String */
    public HttpExecutionResult<String> executeDELETE(URI uri) throws Exception {
        return executeWithResponseBody(createDELETERequest(uri));
    }

    /** Executes a DELETE request and discards the response body */
    public HttpExecutionResult<Void> executeDELETENoResponseBody(URI uri) throws Exception {
        return executeNoResponseBody(createDELETERequest(uri));
    }

    /** Executes a PUT request with text content and a response body handler */
    public <T> HttpExecutionResult<T> executePUT(URI uri, HttpResponse.BodyHandler<T> handler, String content) throws Exception {
        return execute(createPUTRequest(uri, content, false), handler);
    }

    /** Executes a PUT request with text content and returns the response as String */
    public HttpExecutionResult<String> executePUT(URI uri, String content) throws Exception {
        return executeWithResponseBody(createPUTRequest(uri, content, false));
    }

    /** Executes a PUT request with text content and discards the response body */
    public HttpExecutionResult<Void> executePUTNoResponseBody(URI uri, String content) throws Exception {
        return executeNoResponseBody(createPUTRequest(uri, content, false));
    }

    /** Executes a PUT request with WebDAV overwrite header and discards response body */
    public HttpExecutionResult<Void> executePUTNoResponseBody(URI uri, String content, boolean isWebDAV) throws Exception {
        return executeNoResponseBody(createPUTRequest(uri, content, isWebDAV));
    }

    /** Executes a PUT request with InputStream content and response handler */
    public <T> HttpExecutionResult<T> executePUT(URI uri, HttpResponse.BodyHandler<T> handler, InputStream is, long size) throws Exception {
        return execute(createPUTInputStreamRequest(uri, is, size, false), handler);
    }

    /** Executes a PUT request with InputStream content and returns response as String */
    public HttpExecutionResult<String> executePUT(URI uri, InputStream is, long size) throws Exception {
        return executeWithResponseBody(createPUTInputStreamRequest(uri, is, size, false));
    }

    /** Executes a PUT request with InputStream and discards the response body */
    public HttpExecutionResult<Void> executePUTNoResponseBody(URI uri, InputStream is, long size) throws Exception {
        return executeNoResponseBody(createPUTInputStreamRequest(uri, is, size, false));
    }

    /** Executes a PUT request with InputStream and WebDAV overwrite header, discarding response */
    public HttpExecutionResult<Void> executePUTNoResponseBody(URI uri, InputStream is, long size, boolean isWebDAV) throws Exception {
        return executeNoResponseBody(createPUTInputStreamRequest(uri, is, size, isWebDAV));
    }

    /** Adds WebDAV Overwrite header if enabled */
    public static void withWebDAVOverwrite(HttpRequest.Builder builder, boolean withWebDAVOverwrite) {
        if (withWebDAVOverwrite) {
            builder.header(HttpUtils.HEADER_WEBDAV_OVERWRITE, HttpUtils.HEADER_WEBDAV_OVERWRITE_VALUE);
        }
    }

    /** Executes GET request and returns the response InputStream */
    public InputStream getHTTPInputStream(URI uri) throws Exception {
        HttpRequest request = createGETRequest(uri);
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int code = response.statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            HttpExecutionResult<?> result = new HttpExecutionResult<>(request, response);
            if (HttpUtils.isNotFound(code)) {
                throw new SOSNoSuchFileException(uri.toString(), new Exception(formatExecutionResult(result)));
            }
            throw new Exception(formatExecutionResult(result));
        }
        return response.body();
    }

    /** Extracts file size from the response headers or content stream if chunked */
    public long getFileSize(HttpResponse<?> response) throws Exception {
        long size = response.headers().firstValueAsLong(HttpUtils.HEADER_CONTENT_LENGTH).orElse(-1);
        if (size < 0) {// e.g. Transfer-Encoding: chunked
            size = getFileSizeIfChunkedTransferEncoding(response.uri());
        }
        return size;
    }

    /** Returns the last modified timestamp from the response header */
    public static long getLastModifiedInMillis(HttpResponse<?> response) {
        if (response == null) {
            return HttpUtils.DEFAULT_LAST_MODIFIED;
        }
        Optional<String> header = response.headers().firstValue(HttpUtils.HEADER_LAST_MODIFIED);
        if (!header.isPresent()) {
            return HttpUtils.DEFAULT_LAST_MODIFIED;
        }
        return HttpUtils.httpDateToMillis(header.get());
    }

    public static String maskSensitiveUri(URI uri) {
        if (uri == null) {
            return "";
        }
        if (SOSString.isEmpty(uri.getQuery())) {
            return uri.toString();
        }
        String base = uri.getScheme() + "://" + uri.getHost();
        if (uri.getPath() != null) {
            base += uri.getPath();
        }
        base = base.endsWith("/") ? base : base + "/";
        return base + MASKED_STRING;
    }

    /** Constructs a readable string representation of the response status */
    public static String formatExecutionResult(HttpExecutionResult<?> result) {
        return buildExecutionResultSummary(result);
    }

    private static String buildExecutionResultSummary(HttpExecutionResult<?> result) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(result.request().method()).append("]");
        if (result.formatWithMaskRequestURIQueryParams()) {
            sb.append("[").append(maskSensitiveUri(result.request().uri())).append("]");
        } else {
            sb.append("[").append(result.request().uri()).append("]");
        }
        sb.append("[").append(result.response().statusCode()).append("]");
        if (result.formatWithResponseBody()) {
            Object body = result.response().body();
            if (body == null) {
                sb.append(HttpUtils.getReasonPhrase(result.response().statusCode()));
            } else {
                sb.append(body);
            }
        } else {
            sb.append(HttpUtils.getReasonPhrase(result.response().statusCode()));
        }
        return sb.toString();
    }

    /** Returns whether chunked transfer is enabled */
    public boolean isChunkedTransfer() {
        return chunkedTransfer;
    }

    public void setSensitiveHeaders(Set<String> val) {
        this.sensitiveHeaders = new LinkedHashSet<>(val);
    }

    public void addSensitiveHeader(String val) {
        this.sensitiveHeaders.add(val);
    }

    public boolean isSensitiveHeader(String headerName) {
        return sensitiveHeaders.stream().anyMatch(h -> h.equalsIgnoreCase(headerName));
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpRequest createHEADRequest(URI uri) {
        return createHEADRequest(uri, headers);
    }

    public HttpRequest createHEADRequest(URI uri, Map<String, String> headers) {
        return createRequestBuilder(uri, headers).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
    }

    public ISOSLogger getLogger() {
        return logger;
    }

    private HttpRequest createGETRequest(URI uri) {
        return createRequestBuilder(uri).GET().build();
    }

    private HttpRequest createPOSTRequest(URI uri, HttpRequest.BodyPublisher bodyPublisher) {
        return createRequestBuilder(uri).POST(bodyPublisher == null ? HttpRequest.BodyPublishers.noBody() : bodyPublisher).build();
    }

    private HttpRequest createDELETERequest(URI uri) {
        return createRequestBuilder(uri).DELETE().build();
    }

    private HttpRequest createPUTRequest(URI uri, String content, boolean isWebDAV) {
        HttpRequest.Builder builder = createRequestBuilder(uri);
        builder.header(HttpUtils.HEADER_CONTENT_TYPE, HttpUtils.HEADER_CONTENT_TYPE_BINARY);
        withWebDAVOverwrite(builder, isWebDAV);

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (!chunkedTransfer) {
            // set the HEADER_CONTENT_LENGTH to avoid chunked transfer
            builder.header(HttpUtils.HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
        }
        return builder.PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)).build();
    }

    private HttpRequest createPUTInputStreamRequest(URI uri, InputStream is, long size, boolean isWebDAV) {
        HttpRequest.Builder builder = createRequestBuilder(uri);
        builder.header(HttpUtils.HEADER_CONTENT_TYPE, HttpUtils.HEADER_CONTENT_TYPE_BINARY);
        withWebDAVOverwrite(builder, isWebDAV);
        if (!chunkedTransfer) {
            // set the HEADER_CONTENT_LENGTH to avoid chunked transfer
            builder.header(HttpUtils.HEADER_CONTENT_LENGTH, String.valueOf(size));
        }
        return builder.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> is)).build();
    }

    protected void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    private long getFileSizeIfChunkedTransferEncoding(URI uri) throws Exception {
        try (InputStream is = getHTTPInputStream(uri)) {
            return SOSClassUtil.countBytes(is);
        }
    }

    private void setRequestHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        headers.forEach((name, value) -> {
            if (value.isEmpty()) {
                builder.header(name, "");
            } else {
                builder.header(name, value);
            }
        });
    }

    private void verifyJsonResponse(HttpResponse<String> response) throws Exception {
        String contentType = response.headers().firstValue(HttpUtils.HEADER_CONTENT_TYPE).orElse("");
        if (!contentType.contains(HttpUtils.HEADER_CONTENT_TYPE_JSON)) {
            throw new Exception("Expected " + HttpUtils.HEADER_CONTENT_TYPE_JSON + " but got: " + contentType);
        }
    }

    private <T> HttpResponse<T> wrapResponse(HttpResponse<String> original, T parsedBody) {
        return new HttpResponse<T>() {

            @Override
            public int statusCode() {
                return original.statusCode();
            }

            @Override
            public HttpRequest request() {
                return original.request();
            }

            @Override
            public Optional<HttpResponse<T>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return original.headers();
            }

            @Override
            public T body() {
                return parsedBody;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return original.sslSession();
            }

            @Override
            public URI uri() {
                return original.uri();
            }

            @Override
            public HttpClient.Version version() {
                return original.version();
            }
        };
    }

    private HttpExecutionResult<JsonNode> wrapHttpExecutionResultJsonNode(HttpExecutionResult<String> original) throws Exception {
        return new HttpExecutionResult<>(original.request(), wrapResponse(original.response(), OBJECT_MAPPER.readTree(original.response().body())));
    }

    private <T> HttpExecutionResult<T> wrapHttpExecutionResultJson(HttpExecutionResult<String> original, Class<T> type) throws Exception {
        return new HttpExecutionResult<>(original.request(), wrapResponse(original.response(), OBJECT_MAPPER.readValue(original.response().body(),
                type)));
    }

    private <T> HttpExecutionResult<T> wrapHttpExecutionResultJson(HttpExecutionResult<String> original, TypeReference<T> typeRef) throws Exception {
        return new HttpExecutionResult<>(original.request(), wrapResponse(original.response(), OBJECT_MAPPER.readValue(original.response().body(),
                typeRef)));
    }

}
