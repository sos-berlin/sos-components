package com.sos.commons.vfs.http.commons;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSSSLContextFactory;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;

public class HTTPClient implements AutoCloseable {

    private final HttpClient client;

    private Map<String, String> headers;
    private Boolean isHEADMethodAllowed;
    // TODO how to implement not chunked transfer?
    // Set Content-Lenght throws the java.lang.IllegalArgumentException: restricted header name: "Content-Length" Exception...
    // System.setProperty("jdk.httpclient.allowRestrictedHeaders", "false");
    private boolean chunkedTransfer = true;

    private String ntlmMAuthToken = null;

    private HTTPClient(HttpClient client, String ntlmMAuthToken) {
        this.client = client;
        this.ntlmMAuthToken = ntlmMAuthToken;
    }

    /** Set followRedirects(HttpClient.Redirect.ALWAYS) to automatically follow 3xx redirects.<br/>
     * -- Note: java.net.http.HttpClient default=NEVER, Apache HttpClient - ALWAYS<br/>
     * - No need to manually check 3xx status codes or handle redirects. The client takes care of following redirects and processes the final resource.<br/>
     * - Simplifies success checks (e.g., code >= 200 && code < 300) or existence checks (e.g., 404).<br/>
     * -- Otherwise, a exists check should contain, for example, 302 (Found), 304 (Not Modified)...<br/>
     * - No manual redirect handling required for DELETE, GET, or other operations.<br/>
     */
    public static HTTPClient createAuthenticatedClient(ISOSLogger logger, URI baseURI, HTTPAuthConfig authConfig, ProxyProvider proxyProvider,
            SSLArguments sslArgs, List<String> defaultHeaders) throws Exception {

        HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30));
        builder.followRedirects(HttpClient.Redirect.ALWAYS);

        String ntlmMAuthToken = null;
        if (proxyProvider == null) {
            if (authConfig.getNTLM() == null) {
                if (!SOSString.isEmpty(authConfig.getUsername())) {
                    builder.authenticator(new Authenticator() {

                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(authConfig.getUsername(), authConfig.getPassword().toCharArray());
                        }
                    });
                }
            } else {
                ntlmMAuthToken = HTTPUtils.getNTLMAuthToken(authConfig.getNTLM());
            }
        } else {
            builder.proxy(java.net.ProxySelector.of(new InetSocketAddress(proxyProvider.getHost(), proxyProvider.getPort())));
            if (proxyProvider.hasUserAndPassword()) {
                builder.authenticator(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyProvider.getUser(), proxyProvider.getPassword().toCharArray());
                    }
                });
            }

        }

        // Client builder
        setSSLContext(logger, sslArgs, baseURI.getScheme(), builder);

        HTTPClient client = new HTTPClient(builder.build(), ntlmMAuthToken);
        client.setHeaders(logger, defaultHeaders);
        return client;
    }

    @Override
    public void close() throws Exception {
        //
    }

    public HttpRequest.Builder createRequestBuilder(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
        setHeaders(builder);
        if (ntlmMAuthToken != null) {
            builder.header("Authorization", "NTLM " + ntlmMAuthToken);
        }
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
            if (HTTPUtils.isMethodNotAllowed(result.response.statusCode())) {
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

    public ExecuteResult<Void> executePUT(URI uri, String content, boolean isWebDAV) throws Exception {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        HttpRequest.Builder builder = createRequestBuilder(uri);
        builder.header(HTTPUtils.HEADER_CONTENT_TYPE, HTTPUtils.HEADER_CONTENT_TYPE_BINARY);
        withWebDAVOverwrite(builder, isWebDAV);
        if (!chunkedTransfer) {
            // set the HEADER_CONTENT_LENGTH to avoid chunked transfer
            builder.header(HTTPUtils.HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
        }
        return executeWithoutResponseBody(builder.PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)).build());
    }

    public ExecuteResult<Void> executePUT(URI uri, InputStream is, long size, boolean isWebDAV) throws Exception {
        HttpRequest.Builder builder = createRequestBuilder(uri);
        builder.header(HTTPUtils.HEADER_CONTENT_TYPE, HTTPUtils.HEADER_CONTENT_TYPE_BINARY);
        withWebDAVOverwrite(builder, isWebDAV);
        if (!chunkedTransfer) {
            // set the HEADER_CONTENT_LENGTH to avoid chunked transfer
            builder.header(HTTPUtils.HEADER_CONTENT_LENGTH, String.valueOf(size));
        }
        return executeWithoutResponseBody(builder.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> is)).build());
    }

    public static void withWebDAVOverwrite(HttpRequest.Builder builder, boolean withWebDAVOverwrite) {
        if (withWebDAVOverwrite) {
            builder.header(HTTPUtils.HEADER_WEBDAV_OVERWRITE, HTTPUtils.HEADER_WEBDAV_OVERWRITE_VALUE);
        }
    }

    public InputStream getHTTPInputStream(URI uri) throws Exception {
        HttpRequest request = createGETRequest(uri);
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int code = response.statusCode();
        if (!HTTPUtils.isSuccessful(code)) {
            ExecuteResult<?> result = new ExecuteResult<>(request, response);
            if (HTTPUtils.isNotFound(code)) {
                throw new SOSNoSuchFileException(uri.toString(), new Exception(HTTPClient.getResponseStatus(result)));
            }
            throw new Exception(HTTPClient.getResponseStatus(result));
        }
        return response.body();
    }

    public long getFileSizeIfChunkedTransferEncoding(URI uri) throws Exception {
        try (InputStream is = getHTTPInputStream(uri)) {
            return SOSClassUtil.countBytes(is);
        }
    }

    public static long getLastModifiedInMillis(HttpResponse<?> response) {
        if (response == null) {
            return HTTPUtils.DEFAULT_LAST_MODIFIED;
        }
        Optional<String> header = response.headers().firstValue(HTTPUtils.HEADER_LAST_MODIFIED);
        if (!header.isPresent()) {
            return HTTPUtils.DEFAULT_LAST_MODIFIED;
        }
        return getLastModifiedInMillis(header.get());
    }

    public static long getLastModifiedInMillis(String httpDate) {
        if (SOSString.isEmpty(httpDate)) {
            return -1l;
        }
        // TODO replace org.apache.http.client.utils.DateUtils with own code
        Date date = HTTPDateUtils.parseDate(httpDate);
        return date == null ? HTTPUtils.DEFAULT_LAST_MODIFIED : date.getTime();
    }

    public static String getResponseStatus(ExecuteResult<?> result) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(result.request().method()).append("]");
        sb.append("[").append(result.request().uri()).append("]");
        sb.append("[").append(result.response().statusCode()).append("]");
        sb.append(HTTPUtils.getReasonPhrase(result.response().statusCode()));
        return sb.toString();
    }

    public boolean isChunkedTransfer() {
        return chunkedTransfer;
    }

    private HttpRequest createGETRequest(URI uri) {
        return createRequestBuilder(uri).GET().build();
    }

    private HttpRequest createHEADRequest(URI uri) {
        return createRequestBuilder(uri).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
    }

    private static void setSSLContext(ISOSLogger logger, SSLArguments args, String baseURLScheme, HttpClient.Builder builder) throws Exception {
        if (baseURLScheme.equalsIgnoreCase("https")) {
            if (args == null) {
                throw new Exception(("[HTTPClient][setSSLContext]missing SSLArguments"));
            }

            if (!args.getVerifyCertificateHostname().isTrue()) {
                // clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

                logger.info("*********************** Security warning *********************************************************************");
                logger.info("YADE option \"%s\" is currently \"false\". ", args.getVerifyCertificateHostname().getName());
                logger.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                logger.info("with the hostname of the server in the URL used by the YADE client.");
                logger.info("**************************************************************************************************************");
            }
            builder.sslContext(SOSSSLContextFactory.create(args));
        }
    }

    private void setHeaders(ISOSLogger logger, List<String> defaultHeaders) {
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
                            logger.debug("[HTTPClient][getDefaultHeaders]" + header);
                        }
                        headers.put(header, "");
                    } else {
                        String name = header.substring(0, p).trim();
                        String value = header.substring(p).trim();
                        if (isDebugEnabled) {
                            logger.debug("[HTTPClient][getDefaultHeaders]" + name + ":" + value);
                        }
                        headers.put(header, value);
                    }
                });
    }

    private void setHeaders(HttpRequest.Builder builder) {
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

}
