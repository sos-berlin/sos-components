package com.sos.commons.httpclient;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.httpclient.BaseHttpClient.Builder;
import com.sos.commons.httpclient.BaseHttpClient.ExecuteResult;
import com.sos.commons.httpclient.commons.auth.HttpClientAuthConfig;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.util.ssl.SslArguments;

public class BaseHTTPClientTest {

    @Ignore
    @Test
    public void test() {
        SLF4JLogger logger = new SLF4JLogger();
        try {
            String user = null;
            String password = null;
            URI uri = new URI("https://www.google.de");

            Builder builder = BaseHttpClient.withBuilder();
            builder = builder.withLogger(logger);
            builder = builder.withConnectTimeout(Duration.ofSeconds(30));
            builder = builder.withHeaders(List.of("header-name value"));
            builder = builder.withAuth(getAuthConfig(user, password));
            builder = builder.withProxyConfig(getProxyConfig());
            builder = builder.withSSL(getSsl());
            BaseHttpClient client = builder.build();

            // Executes a GET request and returns response as String
            ExecuteResult<String> result = client.executeGET(uri);
            int code = result.response().statusCode();
            logger.info("[result]" + result.response().body());
            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.getResponseStatus(result));
            }
            if (HttpUtils.isNotFound(code)) {
                BaseHttpClient.getResponseStatus(result);
            }
            logger.info("[result]" + BaseHttpClient.getResponseStatus(result));

            // Executes a GET request and discards the response body
            ExecuteResult<Void> resultNoBody = client.executeGETNoResponseBody(uri);
            code = resultNoBody.response().statusCode();
            logger.info("[resultNoBody]" + resultNoBody.response().body());
            logger.info("[resultNoBody]" + BaseHttpClient.getResponseStatus(resultNoBody));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpClientAuthConfig getAuthConfig(String user, String password) {
        if (SOSString.isEmpty(user)) {
            return null;
        }
        return new HttpClientAuthConfig(user, password);
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
        args.getUntrustedSsl().setValue(true);
        args.getUntrustedSslVerifyCertificateHostname().setValue(false);
        return args;
    }
}
