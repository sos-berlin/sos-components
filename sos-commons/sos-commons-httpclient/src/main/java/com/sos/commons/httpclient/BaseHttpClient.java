package com.sos.commons.httpclient;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.httpclient.commons.auth.HttpClientAuthConfig;
import com.sos.commons.httpclient.commons.auth.HttpClientBasicAuthStrategy;
import com.sos.commons.httpclient.commons.auth.IHttpClientAuthStrategy;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.commons.util.ssl.SslContextFactory;

/** Base HTTP client wrapper for Java's HttpClient.<br/>
 * Provides convenient methods for executing HTTP requests with or without parsing the response body.<br/>
 * Supports GET, PUT, DELETE, and conditional HEAD fallback.<br/>
 */
public class BaseHttpClient implements AutoCloseable {

    @SuppressWarnings("unused")
    private final ISOSLogger logger;
    /** Underlying Java HTTP client instance */
    private final HttpClient client;
    /** Optional headers to be applied to all requests */
    private Map<String, String> headers;
    /** Cached result of whether HEAD is allowed on the target server */
    private Boolean isHEADMethodAllowed;
    // TODO how to implement not chunked transfer?
    // Set Content-Lenght throws the java.lang.IllegalArgumentException: restricted header name: "Content-Length" Exception...
    // System.setProperty("jdk.httpclient.allowRestrictedHeaders", "false");
    /** Whether chunked transfer encoding should be used for uploads */
    private boolean chunkedTransfer = true;

    private BaseHttpClient(ISOSLogger logger, HttpClient client) {
        this.logger = logger;
        this.client = client;
    }

    public static Builder withBuilder() {
        return new Builder();
    }

    public static Builder withBuilder(HttpClient.Builder builder) {
        return new Builder(builder);
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
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
        setRequestHeaders(builder);
        return builder;
    }

    /** Executes a generic request with a specified body handler */
    public <T> ExecuteResult<T> execute(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws Exception {
        return new ExecuteResult<>(request, client.send(request, bodyHandler));
    }

    /** Executes a request and returns the response body as a String */
    public ExecuteResult<String> executeWithResponseBody(HttpRequest request) throws Exception {
        return execute(request, HttpResponse.BodyHandlers.ofString());
    }

    /** Executes a request and discards the response body<br/>
     * Useful for HEAD or requests where only status is needed */
    public ExecuteResult<Void> executeNoResponseBody(HttpRequest request) throws Exception {
        return execute(request, HttpResponse.BodyHandlers.discarding());
    }

    /** Executes a HEAD request if supported, otherwise falls back to GET<br/>
     * Only returns status and headers, no response body<br/>
     */
    public ExecuteResult<Void> executeHEADOrGETNoResponseBody(URI uri) throws Exception {
        HttpRequest request = null;
        boolean isHEAD = false;
        if (isHEADMethodAllowed == null || isHEADMethodAllowed) {
            request = createHEADRequest(uri);
            isHEAD = true;
        } else {
            request = createGETRequest(uri);
        }
        ExecuteResult<Void> result = executeNoResponseBody(request);
        if (isHEAD) {
            if (HttpUtils.isMethodNotAllowed(result.response.statusCode())) {
                isHEADMethodAllowed = false;
                result = executeNoResponseBody(createGETRequest(uri));
            } else {
                isHEADMethodAllowed = true;
            }
        }
        return result;
    }

    /** Executes a GET request and handles the response via provided handler */
    public <T> ExecuteResult<T> executeGET(URI uri, HttpResponse.BodyHandler<T> handler) throws Exception {
        return execute(createGETRequest(uri), handler);
    }

    /** Executes a GET request and returns response as String */
    public ExecuteResult<String> executeGET(URI uri) throws Exception {
        return executeWithResponseBody(createGETRequest(uri));
    }

    /** Executes a GET request and discards the response body */
    public ExecuteResult<Void> executeGETNoResponseBody(URI uri) throws Exception {
        return executeNoResponseBody(createGETRequest(uri));
    }

    /** Executes a DELETE request and returns the response body with a given handler */
    public <T> ExecuteResult<T> executeDELETE(URI uri, HttpResponse.BodyHandler<T> handler) throws Exception {
        return execute(createDELETERequest(uri), handler);
    }

    /** Executes a DELETE request and returns the response as String */
    public ExecuteResult<String> executeDELETE(URI uri) throws Exception {
        return executeWithResponseBody(createDELETERequest(uri));
    }

    /** Executes a DELETE request and discards the response body */
    public ExecuteResult<Void> executeDELETENoResponseBody(URI uri) throws Exception {
        return executeNoResponseBody(createDELETERequest(uri));
    }

    /** Executes a PUT request with text content and a response body handler */
    public <T> ExecuteResult<T> executePUT(URI uri, HttpResponse.BodyHandler<T> handler, String content) throws Exception {
        return execute(createPUTRequest(uri, content, false), handler);
    }

    /** Executes a PUT request with text content and returns the response as String */
    public ExecuteResult<String> executePUT(URI uri, String content) throws Exception {
        return executeWithResponseBody(createPUTRequest(uri, content, false));
    }

    /** Executes a PUT request with text content and discards the response body */
    public ExecuteResult<Void> executePUTNoResponseBody(URI uri, String content) throws Exception {
        return executeNoResponseBody(createPUTRequest(uri, content, false));
    }

    /** Executes a PUT request with WebDAV overwrite header and discards response body */
    public ExecuteResult<Void> executePUTNoResponseBody(URI uri, String content, boolean isWebDAV) throws Exception {
        return executeNoResponseBody(createPUTRequest(uri, content, isWebDAV));
    }

    /** Executes a PUT request with InputStream content and response handler */
    public <T> ExecuteResult<T> executePUT(URI uri, HttpResponse.BodyHandler<T> handler, InputStream is, long size) throws Exception {
        return execute(createPUTInputStreamRequest(uri, is, size, false), handler);
    }

    /** Executes a PUT request with InputStream content and returns response as String */
    public ExecuteResult<String> executePUT(URI uri, InputStream is, long size) throws Exception {
        return executeWithResponseBody(createPUTInputStreamRequest(uri, is, size, false));
    }

    /** Executes a PUT request with InputStream and discards the response body */
    public ExecuteResult<Void> executePUTNoResponseBody(URI uri, InputStream is, long size) throws Exception {
        return executeNoResponseBody(createPUTInputStreamRequest(uri, is, size, false));
    }

    /** Executes a PUT request with InputStream and WebDAV overwrite header, discarding response */
    public ExecuteResult<Void> executePUTNoResponseBody(URI uri, InputStream is, long size, boolean isWebDAV) throws Exception {
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
            ExecuteResult<?> result = new ExecuteResult<>(request, response);
            if (HttpUtils.isNotFound(code)) {
                throw new SOSNoSuchFileException(uri.toString(), new Exception(BaseHttpClient.getResponseStatus(result)));
            }
            throw new Exception(BaseHttpClient.getResponseStatus(result));
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

    /** Constructs a readable string representation of the response status */
    public static String getResponseStatus(ExecuteResult<?> result) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(result.request().method()).append("]");
        sb.append("[").append(result.request().uri()).append("]");
        sb.append("[").append(result.response().statusCode()).append("]");
        sb.append(HttpUtils.getReasonPhrase(result.response().statusCode()));
        return sb.toString();
    }

    /** Returns whether chunked transfer is enabled */
    public boolean isChunkedTransfer() {
        return chunkedTransfer;
    }

    private long getFileSizeIfChunkedTransferEncoding(URI uri) throws Exception {
        try (InputStream is = getHTTPInputStream(uri)) {
            return SOSClassUtil.countBytes(is);
        }
    }

    private HttpRequest createHEADRequest(URI uri) {
        return createRequestBuilder(uri).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
    }

    private HttpRequest createGETRequest(URI uri) {
        return createRequestBuilder(uri).GET().build();
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

    private void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    private void setRequestHeaders(HttpRequest.Builder builder) {
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

    /** Holds result of an executed request and its response */
    public class ExecuteResult<T> {

        private final HttpRequest request;
        private final HttpResponse<T> response;

        private ExecuteResult(HttpRequest request, HttpResponse<T> response) {
            this.request = request;
            this.response = response;
        }

        public HttpRequest request() {
            return request;
        }

        public HttpResponse<T> response() {
            return response;
        }
    }

    public static class Builder {

        private final HttpClient.Builder httpClientBuilder;

        private ISOSLogger logger = new SLF4JLogger();
        private ProxyConfig proxyConfig;
        private SslArguments ssl;
        private IHttpClientAuthStrategy auth = null;
        private Map<String, String> headers;
        private Duration connectTimeout;

        /** Set followRedirects(HttpClient.Redirect.ALWAYS) to automatically follow 3xx redirects.<br/>
         * -- Note: java.net.http.HttpClient default=NEVER, Apache HttpClient - ALWAYS<br/>
         * - No need to manually check 3xx status codes or handle redirects. The client takes care of following redirects and processes the final resource.<br/>
         * - Simplifies success checks (e.g., code >= 200 && code < 300) or existence checks (e.g., 404).<br/>
         * -- Otherwise, a exists check should contain, for example, 302 (Found), 304 (Not Modified)...<br/>
         * - No manual redirect handling required for DELETE, GET, or other operations.<br/>
         */
        public Builder() {
            this(HttpClient.newBuilder().followRedirects(Redirect.ALWAYS));
        }

        public Builder(HttpClient.Builder builder) {
            this.httpClientBuilder = builder;
        }

        public Builder withLogger(ISOSLogger logger) {
            this.logger = logger;
            return this;
        }

        public Builder withProxyConfig(ProxyConfig proxyConfig) {
            this.proxyConfig = proxyConfig;
            return this;
        }

        public Builder withSSL(SslArguments ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder withAuth(HttpClientAuthConfig authConfig) {
            this.auth = authConfig == null ? null : authConfig.getStrategy();
            return this;
        }

        public Builder withAuth(String username, String password) {
            this.auth = new HttpClientBasicAuthStrategy(username, password);
            return this;
        }

        public Builder withHeaders(List<String> headers) {
            if (headers == null) {
                this.headers = null;
            } else {
                setHeaders(headers);
            }
            return this;
        }

        public Builder withHeaders(Map<String, String> headers) {
            if (headers == null) {
                this.headers = headers;
            } else {
                if (this.headers == null) {
                    this.headers = headers;
                } else {
                    this.headers.putAll(headers);
                }
            }
            return this;
        }

        public Builder withHeader(String name, String value) {
            if (SOSString.isEmpty(name)) {
                return this;
            }
            return withHeaders(Map.of(name, value == null ? "" : value));
        }

        public Builder withConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public BaseHttpClient build() throws Exception {
            if (connectTimeout != null) {
                httpClientBuilder.connectTimeout(connectTimeout);
            }

            if (auth != null) {
                if (auth.hasAuthenticator()) { // BASIC
                    httpClientBuilder.authenticator(auth.toAuthenticator());
                }
                Map<String, String> authHeaders = auth.getAuthHeaders();
                if (authHeaders != null && authHeaders.size() > 0) {
                    if (headers == null || headers.size() == 0) {
                        headers = authHeaders;
                    } else {
                        // adds to headers if not exists
                        authHeaders.entrySet().stream().forEach(e -> headers.putIfAbsent(e.getKey(), e.getValue()));
                    }
                }
            }

            if (proxyConfig != null) {
                httpClientBuilder.proxy(java.net.ProxySelector.of(new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort())));
                if (proxyConfig.hasUserAndPassword()) {
                    httpClientBuilder.authenticator(new Authenticator() {

                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyConfig.getUser(), proxyConfig.getPassword().toCharArray());
                        }
                    });
                }
            }
            if (ssl != null) {
                SSLContext sslContext = SslContextFactory.create(logger, ssl);
                // SSLParameters sslParameters = sslContext.getDefaultSSLParameters();
                // sslParameters.setEndpointIdentificationAlgorithm(""); // disable hostname verification
                httpClientBuilder.sslContext(sslContext);
                // builder.sslParameters(sslParameters);
            }

            BaseHttpClient client = new BaseHttpClient(logger, httpClientBuilder.build());
            client.setHeaders(headers);
            return client;
        }

        private void setHeaders(List<String> defaultHeaders) {
            final boolean isDebugEnabled = logger.isDebugEnabled();
            if (headers == null) {
                headers = new LinkedHashMap<>();
            }
            defaultHeaders.stream()
                    // https://www.rfc-editor.org/rfc/rfc7230#section-3.2.4
                    // No whitespace is allowed between the header field-name and colon.
                    .map(String::trim)
                    // only name or name value
                    .forEach(header -> {
                        int p = header.indexOf(" ");
                        if (p == -1) {
                            if (isDebugEnabled) {
                                logger.debug("[BaseHttpClient][setHeaders]" + header);
                            }
                            headers.put(header, "");
                        } else {
                            String name = header.substring(0, p).trim();
                            String value = header.substring(p).trim();
                            if (isDebugEnabled) {
                                logger.debug("[BaseHttpClient][setHeaders]name=" + name + ", value=" + value);
                            }
                            headers.put(name, value);
                        }
                    });
        }

    }

}
