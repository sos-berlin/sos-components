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
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.commons.util.ssl.SslContextFactory;

public class BaseHttpClient implements AutoCloseable {

    @SuppressWarnings("unused")
    private final ISOSLogger logger;
    private final HttpClient client;

    private Map<String, String> headers;
    private Boolean isHEADMethodAllowed;
    // TODO how to implement not chunked transfer?
    // Set Content-Lenght throws the java.lang.IllegalArgumentException: restricted header name: "Content-Length" Exception...
    // System.setProperty("jdk.httpclient.allowRestrictedHeaders", "false");
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
        //
    }

    public HttpRequest.Builder createRequestBuilder(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
        setRequestHeaders(builder);
        return builder;
    }

    public ExecuteResult<Void> executeWithoutResponseBody(HttpRequest request) throws Exception {
        return new ExecuteResult<Void>(request, client.send(request, HttpResponse.BodyHandlers.discarding()));
    }

    public ExecuteResult<String> executeWithResponseBody(HttpRequest request) throws Exception {
        return new ExecuteResult<String>(request, client.send(request, HttpResponse.BodyHandlers.ofString()));
    }

    public ExecuteResult<Void> executeHEADOrGET(URI uri) throws Exception {
        HttpRequest request = null;
        boolean isHEAD = false;
        if (isHEADMethodAllowed == null || isHEADMethodAllowed) {
            request = createHEADRequest(uri);
            isHEAD = true;
        } else {
            request = createGETRequest(uri);
        }
        ExecuteResult<Void> result = executeWithoutResponseBody(request);
        if (isHEAD) {
            if (HttpUtils.isMethodNotAllowed(result.response.statusCode())) {
                isHEADMethodAllowed = false;
                result = executeWithoutResponseBody(createGETRequest(uri));
            } else {
                isHEADMethodAllowed = true;
            }
        }
        return result;
    }

    public ExecuteResult<Void> executeGET(URI uri) throws Exception {
        return executeWithoutResponseBody(createGETRequest(uri));
    }

    public ExecuteResult<Void> executeDELETE(URI uri) throws Exception {
        return executeWithoutResponseBody(createRequestBuilder(uri).DELETE().build());
    }

    public ExecuteResult<Void> executePUT(URI uri, String content) throws Exception {
        return executePUT(uri, content, false);
    }

    public ExecuteResult<Void> executePUT(URI uri, String content, boolean isWebDAV) throws Exception {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        HttpRequest.Builder builder = createRequestBuilder(uri);
        builder.header(HttpUtils.HEADER_CONTENT_TYPE, HttpUtils.HEADER_CONTENT_TYPE_BINARY);
        withWebDAVOverwrite(builder, isWebDAV);
        if (!chunkedTransfer) {
            // set the HEADER_CONTENT_LENGTH to avoid chunked transfer
            builder.header(HttpUtils.HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
        }
        return executeWithoutResponseBody(builder.PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)).build());
    }

    public ExecuteResult<Void> executePUT(URI uri, InputStream is, long size) throws Exception {
        return executePUT(uri, is, size, false);
    }

    public ExecuteResult<Void> executePUT(URI uri, InputStream is, long size, boolean isWebDAV) throws Exception {
        HttpRequest.Builder builder = createRequestBuilder(uri);
        builder.header(HttpUtils.HEADER_CONTENT_TYPE, HttpUtils.HEADER_CONTENT_TYPE_BINARY);
        withWebDAVOverwrite(builder, isWebDAV);
        if (!chunkedTransfer) {
            // set the HEADER_CONTENT_LENGTH to avoid chunked transfer
            builder.header(HttpUtils.HEADER_CONTENT_LENGTH, String.valueOf(size));
        }
        return executeWithoutResponseBody(builder.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> is)).build());
    }

    public static void withWebDAVOverwrite(HttpRequest.Builder builder, boolean withWebDAVOverwrite) {
        if (withWebDAVOverwrite) {
            builder.header(HttpUtils.HEADER_WEBDAV_OVERWRITE, HttpUtils.HEADER_WEBDAV_OVERWRITE_VALUE);
        }
    }

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

    public long getFileSize(HttpResponse<?> response) throws Exception {
        long size = response.headers().firstValueAsLong(HttpUtils.HEADER_CONTENT_LENGTH).orElse(-1);
        if (size < 0) {// e.g. Transfer-Encoding: chunked
            size = getFileSizeIfChunkedTransferEncoding(response.uri());
        }
        return size;
    }

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

    public static String getResponseStatus(ExecuteResult<?> result) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(result.request().method()).append("]");
        sb.append("[").append(result.request().uri()).append("]");
        sb.append("[").append(result.response().statusCode()).append("]");
        sb.append(HttpUtils.getReasonPhrase(result.response().statusCode()));
        return sb.toString();
    }

    public boolean isChunkedTransfer() {
        return chunkedTransfer;
    }

    private long getFileSizeIfChunkedTransferEncoding(URI uri) throws Exception {
        try (InputStream is = getHTTPInputStream(uri)) {
            return SOSClassUtil.countBytes(is);
        }
    }

    private HttpRequest createGETRequest(URI uri) {
        return createRequestBuilder(uri).GET().build();
    }

    private HttpRequest createHEADRequest(URI uri) {
        return createRequestBuilder(uri).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
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
            setHeaders(headers);
            return this;
        }

        public Builder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
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
            if (SOSCollection.isEmpty(defaultHeaders)) {
                return;
            }
            final boolean isDebugEnabled = logger.isDebugEnabled();
            headers = new LinkedHashMap<>();
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
                                logger.debug("[BaseHttpClient][setHeaders]" + name + ":" + value);
                            }
                            headers.put(header, value);
                        }
                    });
        }

    }

}
