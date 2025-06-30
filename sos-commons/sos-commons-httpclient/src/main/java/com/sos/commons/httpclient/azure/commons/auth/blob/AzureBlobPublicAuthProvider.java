package com.sos.commons.httpclient.azure.commons.auth.blob;

import java.util.Map;

import com.sos.commons.httpclient.azure.commons.auth.AAzureStorageAuthProvider;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class AzureBlobPublicAuthProvider extends AAzureStorageAuthProvider {

    public AzureBlobPublicAuthProvider(ISOSLogger logger, String accountName, String base64AccountKey, String apiVersion) throws Exception {
        super(logger, AzureBlobStorageClientAuthMethod.PUBLIC, accountName, base64AccountKey, apiVersion);
    }

    @Override
    public Map<String, String> createAuthHeaders(String method, String url, String canonicalizedResource, Map<String, String> existingHeaders,
            long contentLength) throws Exception {
        return existingHeaders;
    }

    @Override
    public String appendToUrl(String rawUrl) {
        return rawUrl;
    }
}
