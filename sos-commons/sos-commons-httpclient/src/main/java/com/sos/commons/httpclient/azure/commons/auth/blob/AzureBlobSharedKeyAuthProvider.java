package com.sos.commons.httpclient.azure.commons.auth.blob;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sos.commons.httpclient.azure.AzureBlobStorageClient;
import com.sos.commons.httpclient.azure.commons.auth.AAzureStorageAuthProvider;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;

/** https://learn.microsoft.com/en-us/rest/api/storageservices/versioning-for-the-azure-storage-services<br/>
 * https://learn.microsoft.com/en-us/rest/api/storageservices/blob-service-rest-api */
public class AzureBlobSharedKeyAuthProvider extends AAzureStorageAuthProvider {

    private static final String HEADER_X_MS_DATE = "x-ms-date";
    private static final String HEADER_X_MS_VERSION = "x-ms-version";

    // DateTimeFormatter.RFC_1123_DATE_TIME not used because returns 1width date e.g '1 Jul' instead of '01 Jul'
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(
            ZoneOffset.UTC);

    public AzureBlobSharedKeyAuthProvider(ISOSLogger logger, String accountName, String base64AccountKey, String apiVersion) {
        super(logger, AzureBlobStorageClientAuthMethod.SHARED_KEY, accountName, base64AccountKey, apiVersion);
    }

    /** Create authorization and related headers for the request.
     *
     * @param method HTTP method (GET, PUT, DELETE...)
     * @param url full URL of the request
     * @param canonicalizedResource canonicalized resource string for signing
     * @param existingHeaders any existing headers that must be considered (e.g. Content-Type)
     * @return map of headers to add/set on request (e.g. Authorization, x-ms-date)
     * @throws Exception on signing errors */
    @Override
    public Map<String, String> createAuthHeaders(String method, String url, String canonicalizedEncodedResource, Map<String, String> existingHeaders,
            long contentLength) throws Exception {
        Map<String, String> headers = HttpUtils.normalizeHeaderKeys(existingHeaders);

        // Add required x-ms headers
        if (!headers.containsKey(HEADER_X_MS_DATE)) {
            headers.put(HEADER_X_MS_DATE, FORMATTER.format(ZonedDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(5)));
        }
        if (!headers.containsKey(HEADER_X_MS_VERSION)) {
            headers.put(HEADER_X_MS_VERSION, getApiVersion());
        }

        String canonicalizedHeaders = buildCanonicalizedHeaders(headers);
        String stringToSign = buildStringToSign(method, headers, canonicalizedHeaders, canonicalizedEncodedResource, contentLength);
        if (getLogger().isDebugEnabled()) {
            // getLogger().debug("[createAuthHeaders][stringToSign]" + stringToSign);
        }

        String signature = signString(stringToSign);
        headers.put(HttpUtils.HEADER_AUTHORIZATION, "SharedKey " + getAccountName() + ":" + signature);
        return headers;
    }

    @Override
    public String appendToUrl(String rawUrl) {
        return rawUrl;
    }

    private String buildCanonicalizedHeaders(Map<String, String> headers) {
        // According to Azure spec, headers starting with x-ms- must be included in lower case sorted order
        SortedMap<String, String> sorted = new TreeMap<>();
        headers.forEach((k, v) -> { // key already lower case
            if (k.startsWith("x-ms-")) {
                sorted.put(k, v.trim());
            }
        });

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue()).append(NEW_LINE);
        }
        return sb.toString();
    }

    private String buildStringToSign(String method, Map<String, String> headers, String canonicalizedHeaders, String canonicalizedEncodedResource,
            long contentLength) {
        // According to https://learn.microsoft.com/en-us/rest/api/storageservices/authorize-with-shared-key
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_CONTENT_ENCODING, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_CONTENT_LANGUAGE, "")).append(NEW_LINE);
        if (contentLength > 0) {
            sb.append(String.valueOf(contentLength)).append(NEW_LINE);
        } else {
            sb.append(AzureBlobStorageClient.getContentLengthHeaderValue(headers.getOrDefault(HttpUtils.HEADER_CONTENT_LENGTH, ""))).append(NEW_LINE);
        }
        sb.append(headers.getOrDefault(HttpUtils.HEADER_CONTENT_MD5, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_CONTENT_TYPE, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_DATE, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_IF_MODIFIED_SINCE, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_IF_MATCH, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_IF_NONE_MATCH, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_IF_UNMODIFIED_SINCE, "")).append(NEW_LINE);
        sb.append(headers.getOrDefault(HttpUtils.HEADER_RANGE, "")).append(NEW_LINE);
        sb.append(canonicalizedHeaders).append(canonicalizedEncodedResource);

        return sb.toString();
    }

}
