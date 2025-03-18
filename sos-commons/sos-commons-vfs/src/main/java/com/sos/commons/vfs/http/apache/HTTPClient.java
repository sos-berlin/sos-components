package com.sos.commons.vfs.http.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSJavaKeyStoreReader;
import com.sos.commons.util.SOSJavaKeyStoreReader.SOSJavaKeyStoreResult;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;
import com.sos.commons.vfs.http.commons.HTTPUtils;

public class HTTPClient implements AutoCloseable {

    static final TrustStrategy TRUST_ALL_STRATEGY = new TrustStrategy() {

        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    };

    private final CloseableHttpClient client;
    private final HttpClientContext context;

    private Boolean isHEADMethodAllowed;

    private HTTPClient(CloseableHttpClient client, HttpClientContext context) {
        this.client = client;
        this.context = context;
    }

    public static HTTPClient createAuthenticatedClient(ISOSLogger logger, URI baseURI, HTTPAuthConfig authConfig, ProxyProvider proxyProvider,
            SSLArguments sslArgs, List<String> defaultHeaders) throws Exception {

        HttpClientBuilder clientBuilder = HttpClients.custom();
        RequestConfig.Builder requestBuilder = RequestConfig.custom();

        HttpClientContext context = HttpClientContext.create();
        AuthCache authCache = new BasicAuthCache();
        BasicCredentialsProvider credentialsProvider = null;
        HttpHost serverHost = null;
        if (proxyProvider == null) {
            if (authConfig.getNTLM() == null) {
                if (!SOSString.isEmpty(authConfig.getUsername())) {
                    serverHost = new HttpHost(baseURI.getHost(), baseURI.getPort(), baseURI.getScheme());

                    credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(new AuthScope(serverHost), new UsernamePasswordCredentials(authConfig.getUsername(), authConfig
                            .getPassword()));
                }
            } else {
                credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new NTCredentials(authConfig.getNTLM().getUsername(), authConfig.getPassword(),
                        authConfig.getNTLM().getWorkstation(), authConfig.getNTLM().getDomain()));
            }
        } else {
            serverHost = new HttpHost(proxyProvider.getHost(), proxyProvider.getPort());
            ProxySelector proxySelector = new ProxySelector() {

                @Override
                public java.util.List<Proxy> select(URI uri) {
                    return Collections.singletonList(proxyProvider.getProxy());
                }

                @Override
                public void connectFailed(URI uri, java.net.SocketAddress sa, java.io.IOException ioe) {
                    // error handling
                }
            };
            requestBuilder.setProxy(serverHost);
            clientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(proxySelector));

            if (proxyProvider.hasUserAndPassword()) {
                credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(serverHost), new UsernamePasswordCredentials(proxyProvider.getUser(), proxyProvider
                        .getPassword()));
            }

        }
        if (credentialsProvider != null) {
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            context.setCredentialsProvider(credentialsProvider);
            authCache.put(serverHost, new BasicScheme());
        }
        context.setAuthCache(authCache);

        // Client builder
        setSSLContext(logger, sslArgs, baseURI.getScheme(), clientBuilder);
        clientBuilder
                // Connection manager
                // .setConnectionManager(createConnectionManager())
                // Request configuration
                .setDefaultRequestConfig(requestBuilder
                        // 30_000 (30 seconds), 300_000 (5 minutes)
                        // Read operations timeout
                        .setSocketTimeout(30_000)
                        // Connect timeout
                        .setConnectTimeout(30_000)
                        // ConnectionRequestTimeout
                        .setConnectionRequestTimeout(300_000)
                        // Redirect - without Redirect - 404 for directories
                        .setRedirectsEnabled(true)
                        // Cookie
                        .setCookieSpec(CookieSpecs.STANDARD)
                        // build
                        .build())
                // Default headers
                .setDefaultHeaders(toHeaders(logger, defaultHeaders));
        return new HTTPClient(clientBuilder.build(), context);
    }

    @Override
    public void close() throws Exception {
        SOSClassUtil.closeQuietly(client);
    }

    public ExecuteResult execute(HttpRequestBase request) throws ClientProtocolException, IOException {
        return new ExecuteResult(request, client.execute(request, context));
    }

    public ExecuteResult executeHEADOrGET(URI uri) throws ClientProtocolException, IOException {
        HttpRequestBase request = null;
        boolean isHEAD = false;
        if (isHEADMethodAllowed == null || isHEADMethodAllowed) {
            request = new HttpHead(uri);
            isHEAD = true;
        } else {
            request = new HttpGet(uri);
        }
        ExecuteResult result = execute(request);
        if (isHEAD) {
            if (HTTPUtils.isMethodNotAllowed(result.response.getStatusLine().getStatusCode())) {
                isHEADMethodAllowed = false;
                result.response.close();

                result = execute(new HttpGet(uri));
            } else {
                isHEADMethodAllowed = true;
            }
        }
        return result;
    }

    public InputStream getHTTPInputStream(URI uri) throws Exception {
        CloseableHttpResponse response = null;
        try {
            HttpGet request = new HttpGet(uri);
            ExecuteResult result = execute(request);
            response = result.getResponse();
            int code = response.getStatusLine().getStatusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    throw new SOSNoSuchFileException(uri.toString(), new Exception(getResponseStatus(result)));
                }
                throw new Exception(getResponseStatus(result));
            }
            return new HTTPInputStream(response);
        } catch (Throwable e) {
            SOSClassUtil.closeQuietly(response);
            throw e;
        }
    }

    public static long getFileSizeIfChunkedTransferEncoding(HttpEntity entity) throws Exception {
        long size = -1L;
        if (entity == null) {
            return size;
        }
        try (InputStream is = entity.getContent()) {
            size = is.transferTo(OutputStream.nullOutputStream());
        }
        return size;
    }

    public static long getLastModifiedInMillis(HttpResponse response) {
        if (response == null) {
            return HTTPUtils.DEFAULT_LAST_MODIFIED;
        }
        Header header = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (header == null) {
            return HTTPUtils.DEFAULT_LAST_MODIFIED;
        }
        return getLastModifiedInMillis(header.getValue());
    }

    public static long getLastModifiedInMillis(String httpDate) {
        if (SOSString.isEmpty(httpDate)) {
            return -1l;
        }
        Date date = DateUtils.parseDate(httpDate);
        return date == null ? HTTPUtils.DEFAULT_LAST_MODIFIED : date.getTime();
    }

    private static void setSSLContext(ISOSLogger logger, SSLArguments args, String baseURLScheme, HttpClientBuilder clientBuilder) throws Exception {
        if (baseURLScheme.equalsIgnoreCase("https")) {
            if (args == null) {
                throw new Exception(("[HTTPClient][setSSLContext]missing SSLArguments"));
            }

            if (args.getVerifyCertificateHostname().isTrue()) {
                clientBuilder.setSSLHostnameVerifier(null);
            } else {
                clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

                logger.info("*********************** Security warning *********************************************************************");
                logger.info("YADE option \"%s\" is currently \"false\". ", args.getVerifyCertificateHostname().getName());
                logger.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                logger.info("with the hostname of the server in the URL used by the YADE client.");
                logger.info("**************************************************************************************************************");
            }

            SSLContextBuilder builder = SSLContexts.custom();
            if (args.getAcceptUntrustedCertificate().isTrue()) {
                builder.loadTrustMaterial(TRUST_ALL_STRATEGY);
                if (logger.isDebugEnabled()) {
                    logger.debug("[HTTPClient][setSSLContext][%s=true]TRUST_ALL_STRATEGY", args.getAcceptUntrustedCertificate().getName());
                }
            } else {
                if (args.getJavaKeyStore() == null) {
                    new Exception(("[HTTPClient][setSSLContext]missing Java KeyStore arguments"));
                } else {
                    SOSJavaKeyStoreResult result = SOSJavaKeyStoreReader.read(args.getJavaKeyStore());
                    if (result == null) {
                        new Exception(("[HTTPClient][setSSLContext]KeyMaterial not found"));
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[HTTPClient][setSSLContext][loadKeyMaterial]%s", result.toString());
                        }

                        builder.loadKeyMaterial(result.getKeyStore(), result.getKeyStorePassword());
                        builder.loadTrustMaterial(result.getTrustStore(), null);
                    }
                }
            }
            clientBuilder.setSSLContext(builder.build());
        }
    }

    @SuppressWarnings("unused")
    private static PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
        m.setMaxTotal(100);
        m.setDefaultMaxPerRoute(20);
        return m;
    }

    private static List<Header> toHeaders(ISOSLogger logger, List<String> defaultHeaders) {
        if (SOSCollection.isEmpty(defaultHeaders)) {
            return List.of();
        }

        // name<blank>value
        // -> Keep-Alive timeout=5, max=1000
        // -> Content-Type application/json; charset=utf-8
        // -> Accept text/html
        // -> ...
        // only name
        // -> Connection
        // -> Cache-Control
        // -> Accept-Encoding
        // -> ...
        final boolean isDebugEnabled = logger.isDebugEnabled();
        return defaultHeaders.stream()
                // https://www.rfc-editor.org/rfc/rfc7230#section-3.2.4
                // No whitespace is allowed between the header field-name and colon.
                .map(String::trim)
                // only name or name value
                .map(header -> {
                    int p = header.indexOf(" ");
                    if (p == -1) {
                        if (isDebugEnabled) {
                            logger.debug("[HTTPClient][getDefaultHeaders]" + header);
                        }
                        return new BasicHeader(header, "");
                    } else {
                        String name = header.substring(0, p).trim();
                        String value = header.substring(p).trim();
                        if (isDebugEnabled) {
                            logger.debug("[HTTPClient][getDefaultHeaders]" + name + ":" + value);
                        }
                        return new BasicHeader(name, value);
                    }
                }).collect(Collectors.toList());
    }

    public static String getResponseStatus(ExecuteResult result) {
        StatusLine sl = result.getResponse().getStatusLine();
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(result.getRequest().getMethod()).append("]");
        sb.append("[").append(result.getRequest().getURI()).append("]");
        sb.append("[").append(sl.getStatusCode()).append("]");
        sb.append(SOSString.isEmpty(sl.getReasonPhrase()) ? HTTPUtils.getReasonPhrase(sl.getStatusCode()) : sl.getReasonPhrase());
        return sb.toString();
    }

    public class ExecuteResult implements AutoCloseable {

        private final HttpRequestBase request;
        private final CloseableHttpResponse response;

        private ExecuteResult(HttpRequestBase request, CloseableHttpResponse response) {
            this.request = request;
            this.response = response;
        }

        public HttpRequestBase getRequest() {
            return request;
        }

        public CloseableHttpResponse getResponse() {
            return response;
        }

        @Override
        public void close() throws IOException {

        }
    }

}
