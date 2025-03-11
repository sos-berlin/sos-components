package com.sos.commons.vfs.http.commons;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
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

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSJavaKeyStoreReader;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;

public class HTTPClient implements AutoCloseable {

    static final TrustStrategy TRUST_ALL_STRATEGY = new TrustStrategy() {

        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    };

    // https://datatracker.ietf.org/doc/html/rfc7232#section-2.2
    // - not really normalized ... Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
    // Locale.US - because of the weekdays in English (e.g. Tue)
    private static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(
            ZoneOffset.UTC);

    private final CloseableHttpClient client;
    private final HttpClientContext context;

    private HTTPClient(CloseableHttpClient client, HttpClientContext context) {
        this.client = client;
        this.context = context;
    }

    public static HTTPClient createAuthenticatedClient(ISOSLogger logger, HTTPProviderArguments args, String host, int port, String scheme)
            throws Exception {
        ProxyProvider proxyProvider = ProxyProvider.createInstance(args.getProxy());

        HttpClientBuilder clientBuilder = HttpClients.custom();
        RequestConfig.Builder requestBuilder = RequestConfig.custom();

        HttpClientContext context = HttpClientContext.create();
        AuthCache authCache = new BasicAuthCache();
        BasicCredentialsProvider credentialsProvider = null;
        HttpHost serverHost = null;
        if (proxyProvider == null) {
            if (!args.getUser().isEmpty() && args.getPassword().isEmpty()) {
                serverHost = new HttpHost(host, port, scheme);

                credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(serverHost), new UsernamePasswordCredentials(args.getUser().getValue(), args
                        .getPassword().getValue()));
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
        setSSLContext(logger, args, scheme, clientBuilder);
        clientBuilder
                // Connection manager
                .setConnectionManager(createConnectionManager())
                // Request configuration
                .setDefaultRequestConfig(requestBuilder
                        // Read operations timeout
                        .setSocketTimeout(5000)
                        // Connect timeout
                        .setConnectTimeout(5000)
                        // Cookie
                        .setCookieSpec(CookieSpecs.STANDARD)
                        // build
                        .build())
                // Default headers
                .setDefaultHeaders(getDefaultHeaders(logger, args));
        return new HTTPClient(clientBuilder.build(), context);
    }

    @Override
    public void close() throws Exception {
        SOSClassUtil.closeQuietly(client);
    }

    public CloseableHttpResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException {
        return client.execute(request, context);
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

    public static String getResponseStatus(String uri, StatusLine statusLine) {
        return getResponseStatus(uri, statusLine, null);
    }

    public static String getResponseStatus(URI uri, StatusLine statusLine) {
        String u = uri == null ? "" : uri.toString();
        return getResponseStatus(u, statusLine, null);
    }

    public static String getResponseStatus(URI from, URI to, StatusLine statusLine) {
        String u = from == null ? "" : from.toString();
        if (to != null) {
            u = u + "-" + to.toString();
        }
        return getResponseStatus(u, statusLine, null);
    }

    public static String getResponseStatus(String uri, StatusLine statusLine, Exception ex) {
        int code = statusLine.getStatusCode();
        String text = statusLine.getReasonPhrase();
        if (ex == null) {
            return String.format("[%s][%s]%s", uri, code, text);
        } else {
            return String.format("[%s][%s][%s]%s", uri, code, text, ex);
        }
    }

    public static long getLastModifiedInMillis(HttpResponse response) {
        if (response == null) {
            return -1L;
        }
        Header header = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (header == null || SOSString.isEmpty(header.getValue())) {
            return -1L;
        }
        try {
            return ZonedDateTime.parse(header.getValue(), LAST_MODIFIED_FORMATTER).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            return -1L;
        }
    }

    private static void setSSLContext(ISOSLogger logger, HTTPProviderArguments args, String baseURLScheme, HttpClientBuilder clientBuilder)
            throws Exception {
        if (baseURLScheme.equalsIgnoreCase("https")) {
            if (!(args instanceof HTTPSProviderArguments)) {
                new Exception(("[HTTPClient][setSSLContext]missing HTTPSProviderArguments"));
            }

            HTTPSProviderArguments httpsArgs = (HTTPSProviderArguments) args;
            if (httpsArgs.getVerifyCertificateHostname().isTrue()) {
                clientBuilder.setSSLHostnameVerifier(null);
            } else {
                clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

                logger.info("*********************** Security warning *********************************************************************");
                logger.info("YADE option \"%s\" is currently \"false\". ", httpsArgs.getVerifyCertificateHostname().getName());
                logger.info("The certificate verification process will not verify the DNS name of the certificate presented by the server,");
                logger.info("with the hostname of the server in the URL used by the YADE client.");
                logger.info("**************************************************************************************************************");
            }

            SSLContextBuilder builder = SSLContexts.custom();
            if (httpsArgs.getAcceptUntrustedCertificate().isTrue()) {
                builder.loadTrustMaterial(TRUST_ALL_STRATEGY);
                if (logger.isDebugEnabled()) {
                    logger.debug("[HTTPClient][setSSLContext][%s=true]TRUST_ALL_STRATEGY", httpsArgs.getAcceptUntrustedCertificate().getName());
                }
            } else {
                if (httpsArgs.getJavaKeyStore() == null) {
                    new Exception(("[HTTPClient][setSSLContext]missing Java KeyStore arguments"));
                } else {
                    SOSJavaKeyStoreReader r = new SOSJavaKeyStoreReader(SOSJavaKeyStoreReader.Type.KEY_AND_TRUSTSTORE, httpsArgs.getJavaKeyStore());
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

    private static PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
        m.setMaxTotal(100);
        m.setDefaultMaxPerRoute(20);
        return m;
    }

    private static List<Header> getDefaultHeaders(ISOSLogger logger, HTTPProviderArguments args) {
        if (SOSCollection.isEmpty(args.getHTTPHeaders().getValue())) {
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
        return args.getHTTPHeaders().getValue().stream()
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

}
