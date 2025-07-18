package com.sos.commons.httpclient.deprecated;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.httpclient.exception.SOSConnectionResetException;
import com.sos.commons.httpclient.exception.SOSNoResponseException;
import com.sos.commons.httpclient.exception.SOSSSLException;

import jakarta.ws.rs.core.StreamingOutput;

public class SOSRestApiClient {

    private String accept = "application/json";
    private String basicAuthorization = null;

    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> responseHeaders = new HashMap<String, String>();
    private List<String> origResponseHeaders = new ArrayList<String>();
    private List<String> cookies = new ArrayList<String>();
    private RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

    private CredentialsProvider credentialsProvider = null;

    private HttpResponse httpResponse;
    private HttpRequestRetryHandler httpRequestRetryHandler;
    private CloseableHttpClient httpClient = null;
    private boolean forcedClosingHttpClient = false;
    private boolean autoCloseHttpClient = true;

    private HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
    private KeyStore clientCertificate = null;
    private char[] clientCertificatePass = null;
    private String clientCertificateAlias = null;
    private KeyStore truststore = null;
    private SSLContext sslContext = null;

    public enum HttpMethod {
        POST, GET, PUT, DELETE
    }

    public HttpResponse getHttpResponse() {

        return httpResponse;
    }

    public void setBasicAuthorization(String basicAuthorization) {
        this.basicAuthorization = basicAuthorization;
    }

    public int statusCode() {
        if (httpResponse.getStatusLine() != null) {
            return httpResponse.getStatusLine().getStatusCode();
        } else {
            return -1;
        }
    }

    public String printStatusLine() {
        StatusLine s = httpResponse.getStatusLine();
        if (s != null) {
            return String.format("%s %d %s", s.getProtocolVersion(), s.getStatusCode(), s.getReasonPhrase());
        } else {
            return "statusline n/a";
        }
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

    public boolean isForcedClosingHttpClient() {
        return forcedClosingHttpClient;
    }

    public void setAutoCloseHttpClient(boolean autoCloseHttpClient) {
        this.autoCloseHttpClient = autoCloseHttpClient;
    }

    public void setSSLContext(KeyStore clientCertificate, char[] clientCertificatePass, KeyStore truststore) throws SOSSSLException {
        setSSLContext(clientCertificate, clientCertificatePass, null, truststore);
    }

    public void setSSLContext(KeyStore clientCertificate, char[] clientCertificatePass, String clientCertificateAlias, KeyStore truststore)
            throws SOSSSLException {
        if (clientCertificate != null) {
            setClientCertificate(clientCertificate, clientCertificatePass, clientCertificateAlias);
        }
        setTruststore(truststore);
        setSSLContext();
    }

    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    /** @param method
     * @param uri
     * @param body (could be String, byte[] or InputStream)
     * @param clazz (could be String, byte[] or InputStream)
     * @return T (could be String, byte[] or InputStream)
     * @throws SocketException
     * @throws SOSException */
    public <T, B> T executeRestService(HttpMethod method, URI uri, B body, Class<T> clazz) throws SocketException, SOSException {
        switch (method) {
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

//    public <B> String postRestService(URI uri, B body) throws SOSException {
//        HttpPost requestPost = new HttpPost(uri);
//        HttpEntity entity = getEntity(body);
//        if (entity != null) {
//            requestPost.setEntity(entity);
//        }
//        return getResponse(requestPost, String.class);
//    }

    public <B> String postRestService(URI uri, B body) throws SOSException {
        HttpPost requestPost = new HttpPost(uri);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPost.setEntity(entity);
        }
        createHttpClient();
        setHttpRequestHeaders(requestPost);
        try {
            httpResponse = getResponse(requestPost);
            return getResponse(httpResponse);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        }
    }

    public String printHttpRequestHeaders() {
        return printHttpRequestHeaders(Collections.emptySet(), true);
    }

    public String printHttpResponseHeaders() {
        return printHttpResponseHeaders(true);
    }

    private <T, B> T postRestService(URI uri, B body, Class<T> clazz) throws SOSException {
        HttpPost requestPost = new HttpPost(uri);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPost.setEntity(entity);
        }
        return getResponse(requestPost, clazz);
    }

    private <T, B> T putRestService(URI uri, B body, Class<T> clazz) throws SOSException, SocketException {
        HttpPut requestPut = new HttpPut(uri);
        HttpEntity entity = getEntity(body);
        if (entity != null) {
            requestPut.setEntity(entity);
        }
        return getResponse(requestPut, clazz);
    }

    private String printHttpRequestHeaders(Set<String> maskedHeaders, boolean pretty) {
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
        Stream<String> s = h.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue());
        if (pretty) {
            return s.collect(Collectors.joining(" \n\t> ", "Request headers \n\t> ", ""));
        } else {
            return s.collect(Collectors.joining("; ", "Request headers: ", ""));
        }
    }

    private <T> T deleteRestService(URI uri, Class<T> clazz) throws SOSException, SocketException {
        return getResponse(new HttpDelete(uri), clazz);
    }

    /*
     * List entry of the form : key=val
     */
    private void addCookieHeader(List<String> _cookies) {
        if (_cookies != null & !_cookies.isEmpty()) {
            headers.put("Cookie", String.join("; ", _cookies));
        }
    }

    private void setSSLContext() throws SOSSSLException {
        if (clientCertificate != null || truststore != null) {
            try {
                SSLContextBuilder sslContextBuilder = SSLContexts.custom();
                if (clientCertificate != null) {
                    if (clientCertificateAlias != null && !clientCertificateAlias.isEmpty()) {
                        sslContextBuilder.loadKeyMaterial(clientCertificate, clientCertificatePass, (aliases, socket) -> clientCertificateAlias);
                    } else {
                        sslContextBuilder.loadKeyMaterial(clientCertificate, clientCertificatePass);
                    }
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

    private void createHttpClient() {
        createHttpClient(getDefaultHttpClientBuilder());
    }

    private void createHttpClient(HttpClientBuilder builder) {
        if (httpClient == null) {
            if (builder == null) {
                builder = getDefaultHttpClientBuilder();
            }
            httpClient = builder.setDefaultRequestConfig(requestConfigBuilder.build()).build();
        }
    }

    private HttpClientBuilder getDefaultHttpClientBuilder() {
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

    private String printHttpResponseHeaders(boolean pretty) {
        Stream<String> s = origResponseHeaders.stream();
        if (pretty) {
            return s.collect(Collectors.joining(" \n\t< ", "Response headers \n\t< ", ""));
        } else {
            return s.collect(Collectors.joining("; ", "Response headers: ", ""));
        }
    }

    private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                if (first) {
                    first = false;
                } else {
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        return result.toString();
    }

    private <B> HttpEntity getEntity(B body) throws SOSBadRequestException {
        HttpEntity entity = null;
        if (body != null) {
            try {
                if (body instanceof HashMap) {
                    @SuppressWarnings("unchecked")
                    String b = getDataString((HashMap<String, String>) body);
                    if (!b.isEmpty()) {
                        entity = new StringEntity(b, StandardCharsets.UTF_8);
                    }
                } else if (body instanceof String) {
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
    
    private HttpResponse getResponse(HttpUriRequest request) throws SOSException {
        try {
            return httpClient.execute(request);
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
            httpResponse = getResponse(request);
            return getResponse(clazz);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
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
            httpResponse = getResponse(request);
            return getStreamingOutputResponse(withGzipEncoding);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        }
    }

    private String getResponse(HttpResponse response) throws SOSNoResponseException {
        try {
            setHttpResponseHeaders();
            String contentType = getResponseHeader("Content-Type");
            String contentEncoding = getResponseHeader("Content-Encoding");
            String contentDisposition = getResponseHeader("Content-Disposition");
            HttpEntity entity = httpResponse.getEntity();
            if(contentType != null && !contentType.isEmpty()) {
                if((contentDisposition != null && contentDisposition.contains("filename"))
                        || (contentEncoding != null && contentEncoding.contains("gzip"))) {
                    return processInputStreamFromResponse(entity);
                }
            }
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } finally {
            if (autoCloseHttpClient) {
                closeHttpClient();
            }
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
            if (autoCloseHttpClient) {
                closeHttpClient();
            }
            return s;
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        }
    }
    
    private String processInputStreamFromResponse (HttpEntity entity) throws SOSException {
        Path filePath = null;
        try {
            String targetPath = headers.get("X-Export-Directory"); 
            if(targetPath == null || targetPath.isEmpty()) {
                targetPath = headers.get("X-Export-Directory".toLowerCase());
            }
            Path target = Paths.get(System.getProperty("user.dir"));
            if (targetPath != null && !targetPath.isEmpty()) {
                target = target.resolve(targetPath);
            }
            if (entity != null) {
                InputStream instream = entity.getContent();
                OutputStream out = null;
                if (instream != null) {
                    try {
                        String contentDisposition = getResponseHeader("Content-Disposition");
                        Path path = null;
                        if(contentDisposition != null) {
                            Files.createDirectories(target);
                            String filename = decodeDisposition(contentDisposition);
                            filePath = target.resolve(filename);
                            if(Files.exists(filePath)) {
                                Files.delete(filePath);
                            }
                            path = Files.createFile(filePath);
                            out = Files.newOutputStream(path);
                            if(getResponseHeader("Content-Encoding") != null) {
                                instream = new GZIPInputStream(instream);
                            }
                        } else {
                            if(getResponseHeader("Content-Encoding") != null) {
                                instream = new GZIPInputStream(instream);
                            }
                            out = new ByteArrayOutputStream();
                        }
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = instream.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        out.flush();
                        if(out instanceof ByteArrayOutputStream) {
                            return ((ByteArrayOutputStream)out).toString(StandardCharsets.UTF_8);
                        }
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
        } catch (UnsupportedOperationException|IOException e) {
            throw new SOSException(e);
        }
        if(filePath != null) {
            return "outfile:" + filePath.toString();
        } else {
            return null;
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
            if (autoCloseHttpClient) {
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
            if (autoCloseHttpClient) {
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

    private void setClientCertificate(KeyStore clientCertificate, char[] clientCertificatePass, String clientCertificateAlias) {
        this.clientCertificate = clientCertificate;
        this.clientCertificatePass = clientCertificatePass;
        this.clientCertificateAlias = clientCertificateAlias;
    }

    private void setTruststore(KeyStore truststore) {
        this.truststore = truststore;
    }

    public static String decodeDisposition(String disposition) throws UnsupportedEncodingException {
        String dispositionFilenameValue = disposition.replaceFirst("(?i)^.*filename(?:=\"?([^\"]+)\"?|\\*=([^;,]+)).*$", "$1$2");
        return decodeFromUriFormat(dispositionFilenameValue);
    }
    
    private static String decodeFromUriFormat(String parameter) throws UnsupportedEncodingException {
        final Pattern filenamePattern = Pattern.compile("(?<charset>[^']+)'(?<lang>[a-z]{2,8}(-[a-z0-9-]+)?)?'(?<filename>.+)",
                Pattern.CASE_INSENSITIVE);
        final Matcher matcher = filenamePattern.matcher(parameter);
        if (matcher.matches()) {
            final String filename = matcher.group("filename");
            final String charset = matcher.group("charset");
            return URLDecoder.decode(filename.replaceAll("%25", "%"), charset);
        } else {
            return parameter;
        }
    }
    
//    private static SOSArchiveFormat getFormatFromBody(String body) {
//        //"format" : "ZIP"
//        final Pattern formatPattern = Pattern.compile("\"format\"\\s*:\\s*\"([^\"]*)\"");
//        final Matcher matcher = formatPattern.matcher(body);
//        if(matcher.find()) {
//            final String format = matcher.group(1);
//            if(format != null) {
//                if(format.equals("ZIP")) {
//                    return SOSArchiveFormat.ZIP;
//                } else {
//                    return SOSArchiveFormat.GZIP;
//                }
//            }
//        }
//        return null;
//    }
    
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

}