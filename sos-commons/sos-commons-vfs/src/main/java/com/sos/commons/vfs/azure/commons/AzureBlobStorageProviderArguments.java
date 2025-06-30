package com.sos.commons.vfs.azure.commons;

import java.net.URI;
import java.util.List;

import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobStorageClientAuthMethod;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderUtils;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;

public class AzureBlobStorageProviderArguments extends AProviderArguments {

    private SslArguments ssl;

    private SOSArgument<String> serviceEndpoint = new SOSArgument<>("service_endpoint", true);
    private SOSArgument<String> containerName = new SOSArgument<>("container_name", false);
    private SOSArgument<String> apiVersion = new SOSArgument<>("api_version", false, "2020-10-02");

    private SOSArgument<AzureBlobStorageClientAuthMethod> authMethod = new SOSArgument<>("auth_method", false,
            AzureBlobStorageClientAuthMethod.PUBLIC);
    private SOSArgument<String> accountKey = new SOSArgument<>("account_key", false, DisplayMode.MASKED);
    private SOSArgument<String> sasToken = new SOSArgument<>("sas_token", false, DisplayMode.MASKED);

    private SOSArgument<List<String>> httpHeaders = new SOSArgument<>("http_headers", false);

    public AzureBlobStorageProviderArguments() {
        getProtocol().setValue(Protocol.AZURE_BLOB_STORAGE);
        getPort().setDefaultValue(HTTPSProviderArguments.DEFAULT_PORT);
        getConnectTimeout().setDefaultValue(HTTPProviderArguments.DEFAULT_CONNECT_TIMEOUT_IN_SECONDS + "s");
    }

    /** Overrides {@link AProviderArguments#getAccessInfo() */
    @Override
    public String getAccessInfo() throws ProviderInitializationException {
        URI baseURI = null;
        try {
            baseURI = HTTPProviderUtils.getBaseURI(getHost(), getPort());
        } catch (Exception e) {
            throw new ProviderInitializationException(e);
        }
        if (getUser().isEmpty()) {
            // https://myaccount.blob.core.windows.net
            String host = baseURI.getHost();
            if (host != null) {
                int indx = host.indexOf(".");
                if (indx > 0) {
                    getUser().setValue(host.substring(0, indx));
                }
            }
        }
        return HttpUtils.getAccessInfo(baseURI, getUser().getValue());
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return (getAuthMethodInfo() + " " + getSsl().getTrustedSslInfo()).trim();
    }

    public SslArguments getSsl() {
        if (ssl == null) {
            ssl = new SslArguments();
            ssl.applyDefaultIfNullQuietly();
        }
        return ssl;
    }

    public void setSsl(SslArguments val) {
        ssl = val;
    }

    public int getConnectTimeoutAsSeconds() {
        return (int) SOSArgumentHelper.asSeconds(getConnectTimeout(), HTTPProviderArguments.DEFAULT_CONNECT_TIMEOUT_IN_SECONDS);
    }

    public SOSArgument<List<String>> getHttpHeaders() {
        return httpHeaders;
    }

    public SOSArgument<String> getServiceEndpoint() {
        return serviceEndpoint;
    }

    public SOSArgument<String> getContainerName() {
        return containerName;
    }

    public SOSArgument<String> getApiVersion() {
        return apiVersion;
    }

    public SOSArgument<AzureBlobStorageClientAuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<String> getAccountKey() {
        return accountKey;
    }

    public SOSArgument<String> getSASToken() {
        return sasToken;
    }

    private String getAuthMethodInfo() {
        if (authMethod.isEmpty()) {
            return "";
        }
        switch (authMethod.getValue()) {
        case SAS_TOKEN:
            return "SAS Token";
        case SHARED_KEY:
            return "Shared Key";
        case PUBLIC:
        default:
            return "Public";
        }
    }
}
