package com.sos.commons.httpclient.azure.commons;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sos.commons.httpclient.azure.AzureBlobStorageClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;

/** TODO - in development ... */
public class AzureBlobStorageUploader {

    private static final int BLOCK_SIZE = 4 * 1024 * 1024; // 4MB
    private final AzureBlobStorageClient client;

    public AzureBlobStorageUploader(AzureBlobStorageClient client) {
        this.client = client;
    }

    public HttpExecutionResult<String> upload(String containerName, String blobName, InputStream is, String contentType) throws Exception {
        byte[] buffer = new byte[BLOCK_SIZE];
        List<String> blockIds = new ArrayList<>();
        int blockIndex = 0;
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            byte[] block = (bytesRead == BLOCK_SIZE) ? buffer : Arrays.copyOf(buffer, bytesRead);
            String blockId = Base64.getEncoder().encodeToString(("block-" + blockIndex).getBytes(StandardCharsets.UTF_8));
            blockIds.add(blockId);
            uploadBlock(containerName, blobName, blockId, block);
            blockIndex++;
        }

        return commitBlocks(containerName, blobName, blockIds, contentType);
    }

    private HttpExecutionResult<String> uploadBlock(String containerName, String blobName, String blockId, byte[] blockData) throws Exception {
        LinkedHashMap<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("blockid", blockId);
        queryParams.put("comp", "block");

        // Canonicalized query string (sorted, non-encoded)
        String canonicalizedQuery = client.toCanonicalizedQuery(queryParams);
        String canonicalizedResource = client.canonicalize(containerName, blobName, canonicalizedQuery);

        // Build final URL (encoded)
        StringBuilder urlBuilder = new StringBuilder(client.buildUrl(containerName)).append("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            urlBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        String rawUrl = urlBuilder.toString();
        String url = client.getAuthProvider().appendToUrl(rawUrl);

        Map<String, String> existingHeaders = new HashMap<>(client.getDefaultHeaders());
        existingHeaders.put("x-ms-blob-type", "BlockBlob");

        Map<String, String> authHeaders = client.getAuthProvider().createAuthHeaders("PUT", url, canonicalizedResource, existingHeaders,
                blockData.length);
        HttpRequest.Builder request = client.createRequestBuilder(URI.create(url), authHeaders).PUT(HttpRequest.BodyPublishers.ofByteArray(
                blockData));
        return client.executeWithResponseBody(request.build());
    }

    private HttpExecutionResult<String> commitBlocks(String containerName, String blobName, List<String> blockIds, String contentType)
            throws Exception {
        LinkedHashMap<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("comp", "blocklist");

        String path = containerName + "/" + blobName + "?" + toQueryString(queryParams);
        String rawUrl = client.buildUrl(path);
        String url = client.getAuthProvider().appendToUrl(rawUrl);

        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?><BlockList>");
        for (String id : blockIds) {
            xml.append("<Latest>").append(id).append("</Latest>");
        }
        xml.append("</BlockList>");
        byte[] xmlBytes = xml.toString().getBytes(StandardCharsets.UTF_8);

        Map<String, String> existingHeaders = new HashMap<>(client.getDefaultHeaders());
        existingHeaders.put("x-ms-blob-content-type", contentType);
        existingHeaders.put("Content-Type", "application/xml");

        // Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // headers.put("x-ms-blob-content-type", contentType);
        // headers.put("Content-Type", "application/xml");
        // headers.put("Content-Length", String.valueOf(xmlBytes.length));
        // headers.put("x-ms-version", client.getApiVersion());

        String canonicalizedQuery = client.toCanonicalizedQuery(queryParams);
        String canonicalizedResource = client.canonicalize(containerName, blobName, canonicalizedQuery);
        Map<String, String> authHeaders = client.getAuthProvider().createAuthHeaders("PUT", url, canonicalizedResource, existingHeaders,
                xmlBytes.length);

        HttpRequest.Builder request = client.createRequestBuilder(URI.create(url), authHeaders).PUT(HttpRequest.BodyPublishers.ofByteArray(xmlBytes));

        return client.executeWithResponseBody(request.build());
    }

    private String toQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first)
                sb.append("&");
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }
}
