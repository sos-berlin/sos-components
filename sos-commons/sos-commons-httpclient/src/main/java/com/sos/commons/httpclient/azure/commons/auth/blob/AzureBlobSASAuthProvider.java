package com.sos.commons.httpclient.azure.commons.auth.blob;

import java.util.Map;

import com.sos.commons.httpclient.azure.commons.auth.AAzureStorageAuthProvider;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class AzureBlobSASAuthProvider extends AAzureStorageAuthProvider {

    private final String sasToken;

    public AzureBlobSASAuthProvider(ISOSLogger logger, String accountName, String base64AccountKey, String apiVersion, String sasToken)
            throws Exception {
        super(logger, AzureBlobStorageClientAuthMethod.SAS_TOKEN, accountName, base64AccountKey, apiVersion);
        this.sasToken = format(sasToken);
    }

    @Override
    public Map<String, String> createAuthHeaders(String method, String url, String canonicalizedResource, Map<String, String> existingHeaders,
            long contentLength) throws Exception {
        return existingHeaders;
    }

    @Override
    public String appendToUrl(String rawUrl) {
        return rawUrl.contains("?") ? rawUrl + "&" + sasToken.substring(1) : rawUrl + sasToken;
    }

    private String format(String sasToken) throws Exception {
        String resolved = AzureBlobSASAuthTokenResolver.resolveToken(this, sasToken);
        return resolved.startsWith("?") ? resolved : "?" + resolved;
    }

}
