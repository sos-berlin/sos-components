package com.sos.commons.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.httpclient.exception.SOSConnectionResetException;
import com.sos.commons.httpclient.exception.SOSNoResponseException;
import com.sos.commons.httpclient.exception.SOSSSLException;

public class SOSRestApiClient {

    private String accept = "application/json";
    private String basicAuthorization = null;
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> responseHeaders = new HashMap<String, String>();
    private List<String> origResponseHeaders = new ArrayList<String>();
    private List<String> cookies = new ArrayList<String>();
    private RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    private CredentialsProvider credentialsProvider = null;
    private HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
    private HttpResponse httpResponse;
    private HttpRequestRetryHandler httpRequestRetryHandler;
    private CloseableHttpClient httpClient = null;
    private boolean forcedClosingHttpClient = false;
    private boolean autoCloseHttpClient = true;
    private String keystorePath = null;
    private String keystorePass = null;
    private String keystoreType = null; // e.g. "JKS" or "PKCS12"
    private String keyPass = null;
    private KeyStore clientCertificate = null;
    private char[] clientCertificatePass = null;
    private KeyStore truststore = null;
    private SSLContext sslContext = null;
    
    public enum HttpMethod {
        POST, GET, PUT, DELETE
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public void setBasicAuthorization(String basicAuthorization) {
        this.basicAuthorization = basicAuthorization;
    }

    public String getBasicAuthorization() {
        return basicAuthorization;
    }

    public int statusCode() {
        return httpResponse.getStatusLine().getStatusCode();
    }
    
    public String printStatusLine() {
        StatusLine s = httpResponse.getStatusLine();
        return String.format("%s %d %s", s.getProtocolVersion(), s.getStatusCode(), s.getReasonPhrase());
    }

    public void clearHeaders() {
        headers.clear();
    }

    public String getResponseHeader(String key) {
        if (responseHeaders != null && key != null) {
            return responseHeaders.get(key.toLowerCase());
        }
        return null;
    }

    /*
     * the time (in milliseconds) to establish the connection with the remote host
     */
    public void setConnectionTimeout(int connectionTimeout) {
        requestConfigBuilder.setConnectTimeout(connectionTimeout);
    }

    /*
     * the timeout in milliseconds used when requesting a connection from the connection manager.
     */
    public void setConnectionRequestTimeout(int connectionTimeout) {
        requestConfigBuilder.setConnectionRequestTimeout(connectionTimeout);
    }

    /*
     * the time (in milliseconds) waiting for data after the connection was established; maximum time of inactivity between two data packets
     */
    public void setSocketTimeout(int socketTimeout) {
        requestConfigBuilder.setSocketTimeout(socketTimeout);
    }

    public void setAllowAllHostnameVerifier(boolean flag) {
        if (flag) {
            this.hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        } else {
            // null = SSLConnectionSocketFactory.getDefaultHostnameVerifier()
            this.hostnameVerifier = null;
        }
    }

    public void setHttpRequestRetryHandler(HttpRequestRetryHandler handler) {
        httpRequestRetryHandler = handler;
    }

    public void setProxy(String proxyHost, Integer proxyPort) {
        setProxy(proxyHost, proxyPort, null, null);
    }

    public void setProxy(String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) {
        requestConfigBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
        if (proxyUser != null && !proxyUser.isEmpty()) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));
        }
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    private Path getKeystorePath() throws SOSMissingDataException {
        String kStorePath = keystorePath;
        if (kStorePath == null) {
            kStorePath = System.getProperty("javax.net.ssl.keyStore");
        }
        if (kStorePath == null) {
            throw new SOSMissingDataException("The keystore path is missing.");
        }
        return Paths.get(kStorePath);
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    private char[] getKeystorePass() {
        String kStorePass = keystorePass;
        if (kStorePass == null) {
            kStorePass = System.getProperty("javax.net.ssl.keyStorePassword");
        }
        if (kStorePass != null) {
            return kStorePass.toCharArray();
        }
        return null;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    private String getKeystoreType() {
        String kStoreType = keystoreType;
        if (kStoreType == null) {
            kStoreType = System.getProperty("javax.net.ssl.keyStoreType");
        }
        if (kStoreType == null) {
            kStoreType = "JKS"; // TODO Security.getProperty()
        }
        return kStoreType;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    private char[] getKeyPass() {
        String kPass = keyPass;
        if (kPass == null) {
            kPass = System.getProperty("javax.net.ssl.keyPassword");
        }
        if (kPass != null) {
            return kPass.toCharArray();
        }
        return null;
    }

    public void createHttpClient() {
        createHttpClient(getDefaultHttpClientBuilder());
    }

    public void createHttpClient(HttpClientBuilder builder) {
        if (httpClient == null) {
            if (builder == null) {
                builder = getDefaultHttpClientBuilder();
            }
            httpClient = builder.setDefaultRequestConfig(requestConfigBuilder.build()).build();
        }
    }

    public HttpClientBuilder getDefaultHttpClientBuilder() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (httpRequestRetryHandler != null) {
            builder.setRetryHandler(httpRequestRetryHandler);
        } else {
            builder.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        }
        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        if (sslContext != null) {
            builder.setSSLContext(sslContext);
        }
        if (hostnameVerifier != null) {
            builder.setSSLHostnameVerifier(hostnameVerifier);
        }
        return builder;
    }
    
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void closeHttpClient() {
        try {
            if (httpClient != null) {
                // forcedClosingHttpClient = false;
                httpClient.close();
            }
        } catch (Exception e) {
        } finally {
            httpClient = null;
        }
    }

    public void forcedClosingHttpClient() {
        try {
            if (httpClient != null) {
                forcedClosingHttpClient = true;
                httpClient.close();
            }
        } catch (Exception e) {
        } finally {
            httpClient = null;
        }
    }

    public boolean isForcedClosingHttpClient() {
        return forcedClosingHttpClient;
    }

    public boolean isAutoCloseHttpClient() {
        return autoCloseHttpClient;
    }

    public void setAutoCloseHttpClient(boolean autoCloseHttpClient) {
        this.autoCloseHttpClient = autoCloseHttpClient;
    }
    
    public void setSSLContext() throws SOSSSLException {
        if (clientCertificate != null || truststore != null) {
            try {
                SSLContextBuilder sslContextBuilder = SSLContexts.custom();
                if (clientCertificate != null) {
                    sslContextBuilder.loadKeyMaterial(clientCertificate, clientCertificatePass);
                }
                if (truststore != null) {
                    sslContextBuilder.loadTrustMaterial(truststore, null);
                }
                sslContext = sslContextBuilder.build();
            } catch (GeneralSecurityException e) {
                throw new SOSSSLException(e);
            }
        }
    }
    
    public void setSSLContext(KeyStore clientCertificate, char[] clientCertificatePass, KeyStore truststore) throws SOSSSLException {
        setClientCertificate(clientCertificate, clientCertificatePass);
        setTruststore(truststore);
        setSSLContext();
    }
    
    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public String executeRestServiceCommand(String restCommand, String urlParam) throws SOSException, SocketException {
        String s = urlParam.replaceFirst("^([^:]*)://.*$", "$1");
        if (s.equals(urlParam)) {
            urlParam = "http://" + urlParam;
        }
        URI url;
        try {
            url = URI.create(urlParam);
        } catch (Exception e) {
            throw new SOSException(e);
        }
        return executeRestServiceCommand(restCommand, url);
    }

    public String executeRestServiceCommand(String restCommand, URI uri) throws SOSException, SocketException {
        return executeRestServiceCommand(restCommand, uri, null);
    }
    
    public String executeRestService(String urlParam) throws SOSException, SocketException {
        return executeRestServiceCommand("get", urlParam);
    }

    public String executeRestServiceCommand(String restCommand, URI uri, String body) throws SOSException, SocketException {

        String result = "";
        if (body == null) {
            body = SOSRestClient.getParameter(restCommand);
        }
        if (restCommand.toLowerCase().startsWith("post")) {
            result = postRestService(uri, body);
        } else if ("get".equalsIgnoreCase(restCommand)) {
            result = getRestService(uri);
        } else if ("delete".equalsIgnoreCase(restCommand)) {
            result = deleteRestService(uri);
        } else if (restCommand.toLowerCase().startsWith("put")) {
            result = putRestService(uri, body);
        } else {
            throw new SOSException(String.format("Unknown rest command method: %s (usage: get|post(body)|delete|put(body))", restCommand));
        }
        return result;
    }
    
    /**
     * 
     * @param method
     * @param uri
     * @param body (could be String, byte[] or InputStream)
     * @return T (could be String, byte[] or InputStream)
     * @throws SocketException
     * @throws SOSException
     */
    public <B> String executeRestService(HttpMethod method, URI uri, B body) throws SocketException, SOSException {
        return executeRestService(method, uri, body, String.class);
    }
    
    /**
     * 
     * @param method
     * @param uri
     * @param body (could be String, byte[] or InputStream)
     * @param clazz (could be String, byte[] or InputStream)
     * @return T (could be String, byte[] or InputStream)
     * @throws SocketException
     * @throws SOSException
     */
    public <T, B> T executeRestService(HttpMethod method, URI uri, B body, Class<T> clazz) throws SocketException, SOSException {
        switch(method) {
        case GET:
            return getRestService(uri, clazz);
        case POST:
            return postRestService(uri, body, clazz);
        case PUT:
            return putRestService(uri, body, clazz);
        case DELETE:
            return deleteRestService(uri, clazz);
        }
        return null;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }
    
    public void addCookieHeader() {
        addCookieHeader(cookies);
    }
    
    /*
     * List entry of the form : key=val
     */
    public void addCookieHeader(List<String> _cookies) {
        if (_cookies != null & !_cookies.isEmpty() ) {
            headers.put("Cookie", String.join("; ", _cookies));
        }
    }

    public String deleteRestService(HttpHost target, String path) throws SOSException, SocketException {
        return getResponse(target, new HttpDelete(path), String.class);
    }

    public String deleteRestService(URI uri) throws SOSException, SocketException {
        return getResponse(new HttpDelete(uri), String.class);
    }

    public <T> T deleteRestService(URI uri, Class<T> clazz) throws SOSException, SocketException {
        return getResponse(new HttpDelete(uri), clazz);
    }

    public String getRestService(HttpHost target, String path) throws SOSException, SocketException {
        return getResponse(target, new HttpGet(path), String.class);
    }
    
    public String getRestService(URI uri) throws SOSException, SocketException {
        return getResponse(new HttpGet(uri), String.class);
    }

    public <T> T getRestService(URI uri, Class<T> clazz) throws SOSException, SocketException {
        return getResponse(new HttpGet(uri), clazz);
    }

    public Path getFilePathByRestService(URI uri, String prefix, boolean withGzipEncoding) throws SOSException, SocketException {
        return getFilePathResponse(new HttpGet(uri), prefix, withGzipEncoding);
    }

    public StreamingOutput getStreamingOutputByRestService(URI uri, boolean withGzipEncoding) throws SOSException, SocketException {
        return getStreamingOutputResponse(new HttpGet(uri), withGzipEncoding);
    }

    public <B> String postRestService(HttpHost target, String path, B body) throws SOSException {
        HttpPost requestPost = new HttpPost(path);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPost.setEntity(entity);
        }
        return getResponse(target, requestPost, String.class);
    }
    
    public <T, B> T postRestService(HttpHost target, String path, B body, Class<T> clazz) throws SOSException {
        HttpPost requestPost = new HttpPost(path);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPost.setEntity(entity);
        }
        return getResponse(target, requestPost, clazz);
    }
    
    public <B> String postRestService(URI uri, B body) throws SOSException {
        HttpPost requestPost = new HttpPost(uri);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPost.setEntity(entity);
        }
        return getResponse(requestPost, String.class);
    }

    public <T, B> T postRestService(URI uri, B body, Class<T> clazz) throws SOSException {
        HttpPost requestPost = new HttpPost(uri);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPost.setEntity(entity);
        }
        return getResponse(requestPost, clazz);
    }

    public <B> String putRestService(HttpHost target, String path, B body) throws SOSException, SocketException {
        HttpPut requestPut = new HttpPut(path);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPut.setEntity(entity);
        }
        return getResponse(target, requestPut, String.class);
    }

    public <T, B> T putRestService(HttpHost target, String path, B body, Class<T> clazz) throws SOSException, SocketException {
        HttpPut requestPut = new HttpPut(path);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPut.setEntity(entity);
        }
        return getResponse(target, requestPut, clazz);
    }

    public <B> String putRestService(URI uri, B body) throws SOSException, SocketException {
        HttpPut requestPut = new HttpPut(uri);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPut.setEntity(entity);
        }
        return getResponse(requestPut, String.class);
    }
    
    public <T, B> T putRestService(URI uri, B body, Class<T> clazz) throws SOSException, SocketException {
        HttpPut requestPut = new HttpPut(uri);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPut.setEntity(entity);
        }
        return getResponse(requestPut, clazz);
    }
    
    public String printHttpRequestHeaders() {
        return printHttpRequestHeaders(Collections.emptySet(), true);
    }
    
    public String printHttpRequestHeaders(Set<String> maskedHeaders, boolean pretty) {
        Map<String, String> h = new HashMap<>();
        h.put("Accept", accept);
        h.putAll(headers);
        if (h.containsKey("Authorization")) {
            h.put("Authorization", "********");
        } else if (basicAuthorization != null && !basicAuthorization.isEmpty()) {
            h.put("Authorization", "********");
        }
        for (String maskedHeader : maskedHeaders) {
           if (h.containsKey(maskedHeader)) {
               h.put(maskedHeader, "********");
           }
        }
        Stream<String> s = h.entrySet().stream().map(e -> e.getKey() +": "+ e.getValue());
        if (pretty) {
            return s.collect(Collectors.joining(" \n\t> ", "Request headers \n\t> ", ""));
        } else {
            return s.collect(Collectors.joining("; ", "Request headers: ", ""));
        }
    }
    
    public String printHttpResponseHeaders() {
        return printHttpResponseHeaders(true);
    }
    
    public String printHttpResponseHeaders(boolean pretty) {
        Stream<String> s = origResponseHeaders.stream();
        if (pretty) {
            return s.collect(Collectors.joining(" \n\t< ", "Response headers \n\t< ", ""));
        } else {
            return s.collect(Collectors.joining("; ", "Response headers: ", ""));
        }
    }
    
    private <B> HttpEntity getEntity(B body) throws SOSBadRequestException {
        HttpEntity entity = null;
        if (body != null) {
            try {
                if (body instanceof String) {
                    String b = (String) body;
                    if (!b.isEmpty()) {
                        entity = new StringEntity(b, StandardCharsets.UTF_8);
                    }
                } else if (body instanceof byte[]) {
                    entity = new ByteArrayEntity((byte[]) body);
                } else if (body instanceof InputStream) {
                    entity = new InputStreamEntity((InputStream) body);
                }
            } catch (Exception e) {
                throw new SOSBadRequestException(e);
            }
        }
        return entity;
    }

    private <T> T getResponse(HttpHost target, HttpRequest request, Class<T> clazz) throws SOSException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(target, request);
            return getResponse(clazz);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(request, e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(request, e);
            }
            throw new SOSConnectionRefusedException(request, e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        }
    }

    private <T> T getResponse(HttpUriRequest request, Class<T> clazz) throws SOSException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(request);
            return getResponse(clazz);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(request, e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(request, e);
            }
            throw new SOSConnectionRefusedException(request, e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        }
    }

    private Path getFilePathResponse(HttpUriRequest request, String prefix, boolean withGzipEncoding) throws SOSException, SocketException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(request);
            return getFilePathResponse(prefix, withGzipEncoding);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(request, e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(request, e);
            }
            throw new SOSConnectionRefusedException(request, e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        }
    }

    private StreamingOutput getStreamingOutputResponse(HttpUriRequest request, boolean withGzipEncoding) throws SOSException, SocketException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(request);
            return getStreamingOutputResponse(withGzipEncoding);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(request, e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(request, e);
            }
            throw new SOSConnectionRefusedException(request, e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(request, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getResponse(Class<T> clazz) throws SOSNoResponseException {
        try {
            T s = null;
            setHttpResponseHeaders();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                if (clazz.equals(String.class)) {
                    s = (T) EntityUtils.toString(entity, StandardCharsets.UTF_8);
                } else if (clazz.equals(byte[].class)) {
                    s = (T) EntityUtils.toByteArray(entity);
                } else if (clazz.equals(InputStream.class)) {
                    s = (T) entity.getContent();
                }
            }
            if (isAutoCloseHttpClient()) {
                closeHttpClient();
            }
            return s;
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        }
    }

    private Path getFilePathResponse(String prefix, boolean withGzipEncoding) throws SOSNoResponseException {
        Path path = null;
        try {
            setHttpResponseHeaders();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                OutputStream out = null;
                if (instream != null) {
                    try {
                        if (prefix == null) {
                            prefix = "sos-download-";
                        }
                        path = Files.createTempFile(prefix, null);
                        if (withGzipEncoding) {
                            out = new GZIPOutputStream(Files.newOutputStream(path));
                        } else {
                            out = Files.newOutputStream(path);
                        }
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = instream.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        out.flush();
                    } finally {
                        try {
                            instream.close();
                            instream = null;
                        } catch (Exception e) {
                        }
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
            if (isAutoCloseHttpClient()) {
                closeHttpClient();
            }
            return path;
        } catch (Exception e) {
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e1) {
                }
            }
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } catch (Throwable e) {
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e1) {
                }
            }
            closeHttpClient();
            throw e;
        }
    }

    private StreamingOutput getStreamingOutputResponse(boolean withGzipEncoding) throws SOSNoResponseException {
        StreamingOutput fileStream = null;
        try {
            setHttpResponseHeaders();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                final InputStream instream = entity.getContent();
                fileStream = new StreamingOutput() {

                    @Override
                    public void write(OutputStream output) throws IOException {
                        if (withGzipEncoding) {
                            output = new GZIPOutputStream(output);
                        }
                        try {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = instream.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }
                            output.flush();
                        } finally {
                            try {
                                output.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                };
            }
            if (isAutoCloseHttpClient()) {
                closeHttpClient();
            }
            return fileStream;
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } catch (Throwable e) {
            closeHttpClient();
            throw e;
        }
    }

    private void setHttpRequestHeaders(HttpRequest request) {
        request.setHeader("Accept", accept);
        if (basicAuthorization != null && !basicAuthorization.isEmpty()) {
            request.setHeader("Authorization", "Basic " + basicAuthorization);
        }
        for (Entry<String, String> entry : headers.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
    }

    private void setHttpResponseHeaders() {
        if (httpResponse != null) {
            Header[] headers = httpResponse.getAllHeaders();
            responseHeaders.clear();
            origResponseHeaders.clear();
            for (Header header : headers) {
                if ("set-cookie".equals(header.getName().toLowerCase())) {
                    String[] cookieParts = header.getValue().split(";", 2);
                    if (cookieParts.length >= 1) {
                       cookies.add(cookieParts[0]);
                    }
                } else {
                    responseHeaders.put(header.getName().toLowerCase(), header.getValue());
                }
                origResponseHeaders.add(String.format("%s: %s", header.getName(), header.getValue()));
            }
            
        }
    }

    public void addAuthorizationHeader(String user, String password) {
        addAuthorizationHeader(user + ":" + password);
    }

    public void addAuthorizationHeader(String account) {
        byte[] authEncBytes = Base64.encodeBase64(account.getBytes());
        String authStringEnc = new String(authEncBytes);
        addHeader("Authorization", "Basic " + authStringEnc);
    }
    
    public void setClientCertificate() throws SOSMissingDataException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        clientCertificate = readKeyStore();
        clientCertificatePass = getKeyPass();
    }
    
    public void setClientCertificate(KeyStore clientCertificate, char[] clientCertificatePass) {
        this.clientCertificate = clientCertificate;
        this.clientCertificatePass = clientCertificatePass;
    }

    public void setClientCertificate(String keystorePath, String keyPass, String keystoreType, String keystorePass) throws SOSMissingDataException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        setKeystorePath(keystorePath);
        setKeyPass(keyPass);
        setKeystoreType(keystoreType);
        setKeystorePass(keystorePass);
        clientCertificate = readKeyStore();
        clientCertificatePass = getKeyPass();
    }

    private KeyStore readKeyStore() throws SOSMissingDataException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        InputStream keyStoreStream = null;
        try {
            keyStoreStream = Files.newInputStream(getKeystorePath());
            KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
            keyStore.load(keyStoreStream, getKeystorePass());
            return keyStore;
        } finally {
            if (keyStoreStream != null) {
                try {
                    keyStoreStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    public void setTruststore(KeyStore truststore) {
        this.truststore = truststore;
    }
}