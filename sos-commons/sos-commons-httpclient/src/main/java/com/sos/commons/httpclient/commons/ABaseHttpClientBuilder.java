package com.sos.commons.httpclient.commons;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import com.sos.commons.httpclient.commons.auth.HttpClientAuthConfig;
import com.sos.commons.httpclient.commons.auth.HttpClientBasicAuthStrategy;
import com.sos.commons.httpclient.commons.auth.IHttpClientAuthStrategy;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.commons.util.ssl.SslContextFactory;

public abstract class ABaseHttpClientBuilder<T extends ABaseHttpClient, B extends ABaseHttpClientBuilder<T, B>> {

    private final HttpClient.Builder httpClientBuilder;

    private ISOSLogger logger = new SLF4JLogger();
    private ProxyConfig proxyConfig;
    private SslArguments ssl;
    private SSLContext sslContext;
    private IHttpClientAuthStrategy auth = null;
    // Header order does not matter in HTTP, but LinkedHashMap preserves insertion order
    // for consistent debug output instead of random order
    private Map<String, String> headers = new LinkedHashMap<String, String>();
    private Duration connectTimeout;
    private Duration requestTimeout;

    /** Set followRedirects(HttpClient.Redirect.ALWAYS) to automatically follow 3xx redirects.<br/>
     * -- Note: java.net.http.HttpClient default=NEVER, Apache HttpClient - ALWAYS<br/>
     * - No need to manually check 3xx status codes or handle redirects. The client takes care of following redirects and processes the final resource.<br/>
     * - Simplifies success checks (e.g., code >= 200 && code < 300) or existence checks (e.g., 404).<br/>
     * -- Otherwise, a exists check should contain, for example, 302 (Found), 304 (Not Modified)...<br/>
     * - No manual redirect handling required for DELETE, GET, or other operations.<br/>
     */
    public ABaseHttpClientBuilder() {
        this(HttpClient.newBuilder().followRedirects(Redirect.ALWAYS));
    }

    public ABaseHttpClientBuilder(HttpClient.Builder builder) {
        this.httpClientBuilder = builder;
    }

    protected abstract T createInstance(ISOSLogger logger, HttpClient client) throws Exception;

    public B withLogger(ISOSLogger logger) {
        this.logger = logger;
        return self();
    }

    public B withProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
        return self();
    }

    public B withSSL(SslArguments ssl) {
        this.ssl = ssl;
        return self();
    }

    public B withSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return self();
    }

    public B withAuth(HttpClientAuthConfig authConfig) {
        this.auth = authConfig == null ? null : authConfig.getStrategy();
        return self();
    }

    public B withAuth(String username, String password) {
        this.auth = new HttpClientBasicAuthStrategy(username, password);
        return self();
    }

    public B withDefaultHeaders(List<String> headers) {
        if (headers != null) {
            setDefaultHeaders(headers);
        }
        return self();
    }

    public B withDefaultHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers.putAll(headers);
        }
        return self();
    }

    public B withHeader(String name, String value) {
        if (SOSString.isEmpty(name)) {
            return self();
        }
        return withDefaultHeaders(Map.of(name, value == null ? "" : value));
    }

    public B withConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return self();
    }
    
    public B withRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return self();
    }

    public T build() throws Exception {
        if (connectTimeout != null) {
            httpClientBuilder.connectTimeout(connectTimeout);
        }

        if (auth != null) {
            if (auth.hasAuthenticator()) {
                httpClientBuilder.authenticator(auth.toAuthenticator());
            }
            if (!SOSCollection.isEmpty(auth.getAuthHeaders())) {
                auth.getAuthHeaders().entrySet().stream().forEach(e -> headers.put(e.getKey(), e.getValue()));
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
        } else if (sslContext != null) {
            httpClientBuilder.sslContext(sslContext);
        }

        T client = createInstance(logger, httpClientBuilder.build());
        client.setDefaultRequestHeaders(headers);
        
        if (requestTimeout != null) {
            client.setRequestTimeout(requestTimeout);
        }
        return client;
    }

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    private void setDefaultHeaders(List<String> defaultHeaders) {
        defaultHeaders.stream()
                // https://www.rfc-editor.org/rfc/rfc7230#section-3.2.4
                // No whitespace is allowed between the header field-name and colon.
                .map(String::trim)
                // only name or name value
                .forEach(header -> {
                    int p = header.indexOf(" ");
                    if (p == -1) {
                        headers.put(header, "");
                    } else {
                        headers.put(header.substring(0, p).trim(), header.substring(p).trim());
                    }
                });
    }

}
