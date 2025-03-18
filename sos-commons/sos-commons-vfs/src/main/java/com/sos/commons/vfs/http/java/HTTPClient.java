package com.sos.commons.vfs.http.java;

import java.io.InputStream;
import java.io.OutputStream;
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

import org.apache.http.client.utils.DateUtils;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSSSLContextFactory;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPUtils;

public class HTTPClient implements AutoCloseable {

    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_EXPECT = "Expect";
    public static final String HEADER_EXPECT_VALUE = "100-continue";

    private final HttpClient client;

    private Map<String, String> headers;
    private Boolean isHEADMethodAllowed;

    private HTTPClient(HttpClient client) {
        this.client = client;
    }

    public static HTTPClient createAuthenticatedClient(ISOSLogger logger, URI baseURI, HTTPAuthConfig authConfig, ProxyProvider proxyProvider,
            SSLArguments sslArgs, List<String> defaultHeaders) throws Exception {

        HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30));
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
                // credentialsProvider = new BasicCredentialsProvider();
                // credentialsProvider.setCredentials(AuthScope.ANY, new NTCredentials(authConfig.getNTLM().getUsername(), authConfig.getPassword(),
                // authConfig.getNTLM().getWorkstation(), authConfig.getNTLM().getDomain()));
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

        HTTPClient client = new HTTPClient(builder.build());
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
        return builder;
    }

    public ExecuteResult execute(HttpRequest request) throws Exception {
        return new ExecuteResult(request, client.send(request, HttpResponse.BodyHandlers.discarding()));
    }

    public ExecuteResult executeHEADOrGET(URI uri) throws Exception {
        HttpRequest request = null;
        boolean isHEAD = false;
        if (isHEADMethodAllowed == null || isHEADMethodAllowed) {
            request = createHEADRequest(uri);
            isHEAD = true;
        } else {
            request = createGETRequest(uri);
        }
        ExecuteResult result = execute(request);
        if (isHEAD) {
            if (HTTPUtils.isMethodNotAllowed(result.response.statusCode())) {
                isHEADMethodAllowed = false;
                result = execute(createGETRequest(uri));
            } else {
                isHEADMethodAllowed = true;
            }
        }
        return result;
    }

    public ExecuteResult executeGET(URI uri) throws Exception {
        return execute(createGETRequest(uri));
    }

    public ExecuteResult executeDELETE(URI uri) throws Exception {
        return execute(createRequestBuilder(uri).DELETE().build());
    }

    public ExecuteResult executePUT(URI uri, String content) throws Exception {
        return execute(createRequestBuilder(uri).PUT(HttpRequest.BodyPublishers.ofByteArray(content.getBytes(StandardCharsets.UTF_8))).build());
    }

    public ExecuteResult executePUT(URI uri, InputStream is) throws Exception {
        return execute(createRequestBuilder(uri).PUT(HttpRequest.BodyPublishers.ofInputStream(() -> is)).build());
    }

    public InputStream getHTTPInputStream(URI uri) throws Exception {
        HttpRequest request = createGETRequest(uri);
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int code = response.statusCode();
        if (!HTTPUtils.isSuccessful(code)) {
            ExecuteResult result = new ExecuteResult(request, response);
            if (HTTPUtils.isNotFound(code)) {
                throw new SOSNoSuchFileException(uri.toString(), new Exception(HTTPClient.getResponseStatus(result)));
            }
            throw new Exception(HTTPClient.getResponseStatus(result));
        }
        return response.body();
    }

    public long getFileSizeIfChunkedTransferEncoding(URI uri) throws Exception {
        try (InputStream is = getHTTPInputStream(uri)) {
            return is.transferTo(OutputStream.nullOutputStream());
        }
    }

    public static long getLastModifiedInMillis(HttpResponse<?> response) {
        if (response == null) {
            return HTTPUtils.DEFAULT_LAST_MODIFIED;
        }
        Optional<String> header = response.headers().firstValue(HEADER_LAST_MODIFIED);
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
        Date date = DateUtils.parseDate(httpDate);
        return date == null ? HTTPUtils.DEFAULT_LAST_MODIFIED : date.getTime();
    }

    public static String getResponseStatus(ExecuteResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(result.request().method()).append("]");
        sb.append("[").append(result.request().uri()).append("]");
        sb.append("[").append(result.response().statusCode()).append("]");
        sb.append(HTTPUtils.getReasonPhrase(result.response().statusCode()));
        return sb.toString();
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

    public class ExecuteResult {

        private final HttpRequest request;
        private final HttpResponse<?> response;

        private ExecuteResult(HttpRequest request, HttpResponse<?> response) {
            this.request = request;
            this.response = response;
        }

        public HttpRequest request() {
            return request;
        }

        public HttpResponse<?> response() {
            return response;
        }
    }

}
