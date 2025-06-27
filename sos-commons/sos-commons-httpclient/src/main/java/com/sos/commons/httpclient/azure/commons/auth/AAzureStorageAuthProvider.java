package com.sos.commons.httpclient.azure.commons.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobStorageClientAuthMethod;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;

public abstract class AAzureStorageAuthProvider {

    public static final String NEW_LINE = "\n";

    // 2020-10-02 - GA (General Availability) - stable, supports BlockBlob, List, SAS, SharedKey, Recursive List etc
    private static final String DEFAULT_API_VERSION = "2020-10-02";

    private final ISOSLogger logger;
    private final AzureBlobStorageClientAuthMethod method;
    private final String accountName;
    private final byte[] accountKeyBytes;

    private String apiVersion;

    public AAzureStorageAuthProvider(ISOSLogger logger, AzureBlobStorageClientAuthMethod method, String accountName, String base64AccountKey,
            String apiVersion) {
        this.logger = logger;
        this.method = method;
        this.accountName = accountName;
        this.accountKeyBytes = decodeAccountKey(base64AccountKey);
        this.apiVersion = SOSString.isEmpty(apiVersion) ? DEFAULT_API_VERSION : apiVersion;
    }

    public abstract Map<String, String> createAuthHeaders(String method, String url, String canonicalizedEncodedResource,
            Map<String, String> existingHeaders, long contentLength) throws Exception;;

    public abstract String appendToUrl(String rawUrl);

    public byte[] decodeAccountKey(String base64AccountKey) {
        return base64AccountKey == null ? null : Base64.getDecoder().decode(base64AccountKey);
    }

    public String signString(String stringToSign) throws Exception {
        if (stringToSign == null || accountKeyBytes == null) {
            return null;
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(accountKeyBytes, "HmacSHA256"));
        byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    public String getAccountName() {
        // Not strictly needed for SAS, but could be parsed from endpoint if required
        return accountName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public ISOSLogger getLogger() {
        return logger;
    }

    public AzureBlobStorageClientAuthMethod getMethod() {
        return method;
    }

    public boolean hasAccountKey() {
        return accountKeyBytes != null;
    }

    public boolean isSharedKey() {
        return AzureBlobStorageClientAuthMethod.SHARED_KEY.equals(method);
    }

    public boolean isSASToken() {
        return AzureBlobStorageClientAuthMethod.SAS_TOKEN.equals(method);
    }
}
