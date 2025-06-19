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

            // execute a String method - response body is not null
            ExecuteResult<String> resultWithBody = client.executeGET(uri);
            int code = resultWithBody.response().statusCode();
            logger.info("[body=not null]" + resultWithBody.response().body());
            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.getResponseStatus(resultWithBody));
            }
            if (HttpUtils.isNotFound(code)) {
                BaseHttpClient.getResponseStatus(resultWithBody);
            }
            // execute a Void method - response body is null
            ExecuteResult<Void> result = client.executeHEADOrGETNoResponseBody(uri);
            code = result.response().statusCode();
            logger.info("[body=null]" + result.response().body());

            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.getResponseStatus(result));
            }
            if (HttpUtils.isNotFound(code)) {
                BaseHttpClient.getResponseStatus(result);
            }
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
