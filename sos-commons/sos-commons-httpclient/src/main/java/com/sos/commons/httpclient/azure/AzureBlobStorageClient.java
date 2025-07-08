package com.sos.commons.httpclient.azure;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.httpclient.azure.commons.AzureBlobStorageUploader;
import com.sos.commons.httpclient.azure.commons.auth.AAzureStorageAuthProvider;
import com.sos.commons.httpclient.commons.ABaseHttpClient;
import com.sos.commons.httpclient.commons.ABaseHttpClientBuilder;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class AzureBlobStorageClient extends ABaseHttpClient {

    private final String serviceEndpoint;
    private final AAzureStorageAuthProvider authProvider;
    private final boolean maskQueryParams;

    protected AzureBlobStorageClient(ISOSLogger logger, HttpClient client, String serviceEndpoint, AAzureStorageAuthProvider authProvider) {
        super(logger, client, false);

        this.serviceEndpoint = serviceEndpoint;
        this.authProvider = authProvider;
        this.maskQueryParams = authProvider != null && authProvider.isSASToken();
    }

    public static Builder withBuilder() {
        return new Builder();
    }

    public static Builder withBuilder(HttpClient.Builder builder) {
        return new Builder(builder);
    }

    public String formatExecutionResultForException(HttpExecutionResult<?> result) {
        result.formatWithResponseBody(true);
        result.formatWithMaskRequestURIQueryParams(maskQueryParams);
        return AzureBlobStorageClient.formatExecutionResult(result);
    }

    public HttpExecutionResult<String> executeGETStorage() throws Exception {
        LinkedHashMap<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("comp", "list");

        String canonicalizedQuery = toCanonicalizedQuery(queryParams);
        String canonicalizedResource = canonicalize(null, null, canonicalizedQuery);

        String path = "?comp=list";
        String rawUrl = buildUrl(path);
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> authHeaders = authProvider.createAuthHeaders("GET", url, canonicalizedResource, getDefaultHeaders(), 0);
        return executeWithResponseBody(createRequestBuilder(URI.create(url), authHeaders).GET().build());
    }

    public HttpExecutionResult<Void> executeHEADBlob(String containerName, String blobPath) throws Exception {
        String encodedBlobPath = HttpUtils.normalizeAndEncodeRelativePath(blobPath);
        String path = containerName + "/" + encodedBlobPath;
        String rawUrl = buildUrl(path);
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> authHeaders = authProvider.createAuthHeaders("HEAD", url, canonicalize(containerName, encodedBlobPath, ""),
                getDefaultHeaders(), 0);
        return executeNoResponseBody(createHEADRequest(URI.create(url), authHeaders));
    }

    /** Executes GET request and returns the response InputStream */
    public HttpExecutionResult<InputStream> executeGETBlobInputStream(String containerName, String blobPath) throws Exception {
        String encodedBlobPath = HttpUtils.normalizeAndEncodeRelativePath(blobPath);
        String path = containerName + "/" + encodedBlobPath;
        String rawUrl = buildUrl(path);
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> authHeaders = authProvider.createAuthHeaders("GET", url, canonicalize(containerName, encodedBlobPath, ""),
                getDefaultHeaders(), 0);
        return execute(createRequestBuilder(URI.create(url), authHeaders).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
    }

    public HttpExecutionResult<String> executeGETBlobContent(String containerName, String blobPath) throws Exception {
        String encodedBlobPath = HttpUtils.normalizeAndEncodeRelativePath(blobPath);
        String path = containerName + "/" + encodedBlobPath;
        String rawUrl = buildUrl(path);
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> authHeaders = authProvider.createAuthHeaders("GET", url, canonicalize(containerName, encodedBlobPath, ""),
                getDefaultHeaders(), 0);
        return executeWithResponseBody(createRequestBuilder(URI.create(url), authHeaders).GET().build());
    }

    public HttpExecutionResult<String> executeDELETEBlob(String containerName, String blobPath) throws Exception {
        String encodedBlobPath = HttpUtils.normalizeAndEncodeRelativePath(blobPath);
        String path = containerName + "/" + encodedBlobPath;
        String rawUrl = buildUrl(path);
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> authHeaders = authProvider.createAuthHeaders("DELETE", url, canonicalize(containerName, encodedBlobPath, ""),
                getDefaultHeaders(), 0);
        HttpRequest.Builder builder = createRequestBuilder(URI.create(url), authHeaders).DELETE();
        return executeWithResponseBody(builder.build());
    }

    /** XML answer: https://learn.microsoft.com/en-us/rest/api/storageservices/list-blobs?tabs=microsoft-entra-id
     * 
     * @param containerName
     * @param directory
     * @param recursive
     * @return
     * @throws Exception */
    public HttpExecutionResult<String> executeGETBlobList(String containerName, String blobPath, boolean recursive) throws Exception {
        LinkedHashMap<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("restype", "container");
        queryParams.put("comp", "list");

        if (!SOSString.isEmpty(blobPath) && !"/".equals(blobPath)) {
            // TODO if DirectoryInfo - add /
            // String prefix = path.endsWith("/") ? path : path + "/";
            queryParams.put("prefix", blobPath);
        }

        if (!recursive) {
            queryParams.put("delimiter", "/");
        }
        // showonly={deleted,files,directories} Version 2020-12-06 and later.
        // causes <Error><Code>UnsupportedQueryParameter</Code><Message>One of the query parameters specified in the request URI is not supported.
        // queryParams.put("showonly", "files");

        // Canonicalized query string (sorted, non-encoded)
        String canonicalizedQuery = toCanonicalizedQuery(queryParams);
        String canonicalizedResource = canonicalize(containerName, null, canonicalizedQuery);

        // Build final URL (encoded)
        StringBuilder urlBuilder = new StringBuilder(buildUrl(containerName)).append("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            urlBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        String rawUrl = urlBuilder.toString();
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> authHeaders = authProvider.createAuthHeaders("GET", url, canonicalizedResource, getDefaultHeaders(), 0);
        HttpRequest.Builder builder = createRequestBuilder(URI.create(url), authHeaders).GET();
        return executeWithResponseBody(builder.build());
    }

    public HttpExecutionResult<String> executePUTBlobInputStream(String containerName, String blobName, InputStream is, String contentType)
            throws Exception {
        AzureBlobStorageUploader uploader = new AzureBlobStorageUploader(this);
        return uploader.upload(containerName, blobName, is, contentType);
    }

    public HttpExecutionResult<String> executePUTBlob(String containerName, String blobPath, InputStream is, String contentType) throws Exception {
        String encodedBlobPath = HttpUtils.normalizeAndEncodeRelativePath(blobPath);
        String path = containerName + "/" + encodedBlobPath;
        String rawUrl = buildUrl(path);
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> existingHeaders = new HashMap<>(getDefaultHeaders());
        existingHeaders.put("x-ms-blob-type", "BlockBlob");

        // sdk client throws java.lang.IllegalArgumentException: restricted header name: "Content-Length"
        // existingHeaders.put(HttpUtils.HEADER_CONTENT_LENGTH, getContentLengthHeaderValue(SOSClassUtil.countBytes(is)));
        if (contentType != null) {
            existingHeaders.put(HttpUtils.HEADER_CONTENT_TYPE, contentType);
        }
        byte[] data = SOSClassUtil.toByteArray(is);
        Map<String, String> authHeaders = authProvider.createAuthHeaders("PUT", url, canonicalize(containerName, encodedBlobPath, ""),
                existingHeaders, data.length);
        // HttpRequest.Builder builder = createRequestBuilder(URI.create(url), authHeaders).PUT(HttpRequest.BodyPublishers.ofInputStream(() -> is));
        // authHeaders.remove(HttpUtils.HEADER_CONTENT_LENGTH);
        HttpRequest.Builder builder = createRequestBuilder(URI.create(url), authHeaders).PUT(HttpRequest.BodyPublishers.ofByteArray(data));

        return executeWithResponseBody(builder.build());
    }

    public HttpExecutionResult<String> executePUTBlob(String containerName, String blobPath, byte[] data, String contentType) throws Exception {
        String encodedBlobPath = HttpUtils.normalizeAndEncodeRelativePath(blobPath);
        String path = containerName + "/" + encodedBlobPath;
        String rawUrl = buildUrl(path);
        String url = authProvider.appendToUrl(rawUrl);

        Map<String, String> existingHeaders = new HashMap<>(getDefaultHeaders());
        existingHeaders.put("x-ms-blob-type", "BlockBlob");

        // sdk client throws java.lang.IllegalArgumentException: restricted header name: "Content-Length"
        // existingHeaders.put(HttpUtils.HEADER_CONTENT_LENGTH, getContentLengthHeaderValue(data.length));
        if (contentType != null) {
            existingHeaders.put(HttpUtils.HEADER_CONTENT_TYPE, contentType);
        }
        Map<String, String> authHeaders = authProvider.createAuthHeaders("PUT", url, canonicalize(containerName, encodedBlobPath, ""),
                existingHeaders, data.length);
        HttpRequest.Builder builder = createRequestBuilder(URI.create(url), authHeaders).PUT(HttpRequest.BodyPublishers.ofByteArray(data));
        return executeWithResponseBody(builder.build());
    }

    public static String getContentLengthHeaderValue(String contentLength) {
        // Per MS docs, if Content-Length is zero, it should be empty string
        if ("0".equals(contentLength)) {
            return "";
        }
        return contentLength;
    }

    public static String getContentLengthHeaderValue(int contentLength) {
        return getContentLengthHeaderValue(String.valueOf(contentLength));
    }

    public static String getContentLengthHeaderValue(long contentLength) {
        return getContentLengthHeaderValue(String.valueOf(contentLength));
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public AAzureStorageAuthProvider getAuthProvider() {
        return authProvider;
    }

    public String buildUrl(String path) {
        return serviceEndpoint + "/" + path;
    }

    public String canonicalize(String containerName, String blobPath, String canonicalizedHeaders) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(authProvider.getAccountName()).append("/");
        if (!SOSString.isEmpty(containerName)) {
            sb.append(containerName);
        }
        if (!SOSString.isEmpty(blobPath)) {
            sb.append("/" + blobPath);
        }
        if (!canonicalizedHeaders.isEmpty()) {
            sb.append("\n").append(canonicalizedHeaders);
        }
        return sb.toString();
    }

    // Canonicalized query string (sorted, non-encoded)
    public String toCanonicalizedQuery(LinkedHashMap<String, String> queryParams) {
        return queryParams.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> e.getKey().toLowerCase() + ":" + e.getValue()).collect(
                Collectors.joining("\n"));
    }

    public static class Builder extends ABaseHttpClientBuilder<AzureBlobStorageClient, Builder> {

        private String serviceEndpoint;
        private AAzureStorageAuthProvider authProvider;

        public Builder() {
            super();
        }

        public Builder(HttpClient.Builder builder) {
            super(builder);
        }

        public Builder withServiceEndpoint(String serviceEndpoint) {
            this.serviceEndpoint = serviceEndpoint;
            return this;
        }

        public Builder withAuthProvider(AAzureStorageAuthProvider authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        @Override
        protected AzureBlobStorageClient createInstance(ISOSLogger logger, HttpClient client) throws Exception {
            if (SOSString.isEmpty(serviceEndpoint)) {
                throw new SOSRequiredArgumentMissingException("ServiceEndpoint");
            }
            if (authProvider == null) {
                throw new SOSRequiredArgumentMissingException("AuthProvider");
            }
            return new AzureBlobStorageClient(logger, client, serviceEndpoint, authProvider);
        }
    }

}
