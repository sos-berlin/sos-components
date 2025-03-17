package com.sos.commons.vfs.http.apache;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.security.KeyStore;
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
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;
import com.sos.commons.vfs.http.commons.HTTPAuthConfig;

public class HTTPClient implements AutoCloseable {

    static final TrustStrategy TRUST_ALL_STRATEGY = new TrustStrategy() {

        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    };

    private static final long DEFAULT_LAST_MODIFIED = -1L;

    private final CloseableHttpClient client;
    private final HttpClientContext context;

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
                        // Read operations timeout - 300_000 (5 minutes)
                        .setSocketTimeout(30_000)
                        // Connect timeout
                        .setConnectTimeout(30_000).setConnectionRequestTimeout(300_000)
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

    public CloseableHttpResponse execute(HttpRequestBase request) throws ClientProtocolException, IOException {
        return client.execute(request, context);
    }

    public InputStream getHTTPInputStream(URI uri) throws Exception {
        CloseableHttpResponse response = null;
        try {
            HttpGet request = new HttpGet(uri);
            response = execute(request);
            StatusLine sl = response.getStatusLine();
            if (!HTTPClient.isSuccessful(sl)) {
                if (HTTPClient.isNotFound(sl)) {
                    throw new SOSNoSuchFileException(uri.toString(), new Exception(HTTPClient.getResponseStatus(request, response)));
                }
                throw new Exception(HTTPClient.getResponseStatus(request, response));
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
            size = 0L;

            byte[] buffer = new byte[4_096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                size += bytesRead;
            }
        }
        return size;
    }

    public static boolean isSuccessful(StatusLine statusLine) {
        int sc = statusLine.getStatusCode();
        if (sc >= 200 && sc < 300) {
            return true;
        }
        return false;
    }

    public static boolean isServerError(StatusLine statusLine) {
        return statusLine.getStatusCode() >= 500;
    }

    public static boolean isNotFound(StatusLine statusLine) {
        return statusLine.getStatusCode() == 404;
    }

    public static String getResponseStatus(HttpRequestBase request, HttpResponse response) {
        return getResponseStatus(request, response, null);
    }

    public static long getLastModifiedInMillis(HttpResponse response) {
        if (response == null) {
            return DEFAULT_LAST_MODIFIED;
        }
        Header header = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (header == null) {
            return DEFAULT_LAST_MODIFIED;
        }
        return getLastModifiedInMillis(header.getValue());
    }

    public static long getLastModifiedInMillis(String httpDate) {
        if (SOSString.isEmpty(httpDate)) {
            return -1l;
        }
        Date date = DateUtils.parseDate(httpDate);
        return date == null ? DEFAULT_LAST_MODIFIED : date.getTime();
    }

    private static void setSSLContext(ISOSLogger logger, SSLArguments args, String baseURLScheme, HttpClientBuilder clientBuilder) throws Exception {
        if (baseURLScheme.equalsIgnoreCase("https")) {
            if (args == null) {
                new Exception(("[HTTPClient][setSSLContext]missing SSLArguments"));
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
                    SOSJavaKeyStoreReader r = new SOSJavaKeyStoreReader(SOSJavaKeyStoreReader.Type.KEY_AND_TRUSTSTORE, args.getJavaKeyStore());
                    KeyStore ks = r.read();
                    if (ks == null) {
                        new Exception(("[HTTPClient][setSSLContext][" + r.toString() + "]KeyMaterial not found"));
                    } else {
                        builder.loadKeyMaterial(ks, r.getPassword());
                        builder.loadTrustMaterial(ks, null);
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

    private static String getResponseStatus(HttpRequestBase request, HttpResponse response, Exception ex) {
        StatusLine sl = response.getStatusLine();
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(request.getMethod()).append("]");
        sb.append("[").append(request.getURI()).append("]");
        sb.append("[").append(sl.getStatusCode()).append("]");
        if (ex == null) {
            sb.append(getReasonPhraseOrDefault(sl));
        } else {
            sb.append("[").append(getReasonPhraseOrDefault(sl)).append("]");
            sb.append(ex.toString());
        }
        return sb.toString();
    }

    private static String getReasonPhraseOrDefault(StatusLine sl) {
        String reason = sl.getReasonPhrase();
        if (!SOSString.isEmpty(reason)) {
            return reason;
        }

        return switch (sl.getStatusCode()) {
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
        default -> "Unknown Status Code: " + sl.getStatusCode() + " - No description available.";
        };
    }

}
