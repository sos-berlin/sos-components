package com.sos.commons.httpclient.azure;

import java.time.Duration;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.httpclient.BaseHTTPClientTest;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.azure.AzureBlobStorageClient.Builder;
import com.sos.commons.httpclient.azure.commons.auth.blob.AzureBlobSharedKeyAuthProvider;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.util.ssl.SslArguments;

public class AzureBlobStorageClientTest {

    @Ignore
    @Test
    public void testSharedKey() {
        BaseHTTPClientTest.resetSystemProperties();

        SLF4JLogger logger = new SLF4JLogger();
        try {
            String accountName = "account";
            String accountKey = "xyz";
            String container = "yade";
            String apiVersion = "2020-10-02";
            String blob = "testdir/test.txt";

            String directory = "testdir";
            boolean recursive = false;

            Builder builder = AzureBlobStorageClient.withBuilder();
            builder = builder.withLogger(logger);
            builder = builder.withConnectTimeout(Duration.ofSeconds(30));
            builder = builder.withProxyConfig(getProxyConfig());
            builder = builder.withSSL(getSsl());
            builder = builder.withHeader("Content-Type", "application/json");

            builder = builder.withServiceEndpoint("https://" + accountName + ".blob.core.windows.net");
            builder = builder.withAuthProvider(new AzureBlobSharedKeyAuthProvider(logger, accountName, accountKey, apiVersion));
            AzureBlobStorageClient client = builder.build();

            // Executes a LIST(GET) request and returns response as String
            HttpExecutionResult<String> result = client.executeGETBlobList(container, directory + "/", recursive);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            logger.info("[result]" + BaseHttpClient.formatExecutionResult(result));
            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
            logger.info("[result][blobExists][" + blob + "]" + client.executeHEADBlob(container, blob));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ProxyConfig getProxyConfig() {
        ProxyConfigArguments args = new ProxyConfigArguments();
        args.getType().setValue(java.net.Proxy.Type.HTTP);

        args.getHost().setValue(null);
        args.getPort().setValue(null);

        args.getUser().setValue(null);
        args.getPassword().setValue(null);

        args.getConnectTimeout().setValue("30s");

        ProxyConfig.createInstance(args);
        return ProxyConfig.createInstance(args);
    }

    private SslArguments getSsl() {
        SslArguments args = new SslArguments();
        // args.getTrustedSsl().set
        // args.getUntrustedSsl().setValue(true);
        // args.getUntrustedSslVerifyCertificateHostname().setValue(false);
        return args;
    }
}
