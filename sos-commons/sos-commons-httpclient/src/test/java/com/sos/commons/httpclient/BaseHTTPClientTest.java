package com.sos.commons.httpclient;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sos.commons.httpclient.BaseHttpClient.Builder;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
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
            builder = builder.withAuth(getAuthConfig(user, password));
            builder = builder.withProxyConfig(getProxyConfig());
            builder = builder.withSSL(getSsl());
            builder = builder.withHeaders(List.of("header-name value"));
            BaseHttpClient client = builder.build();

            // Executes a GET request and returns response as String
            HttpExecutionResult<String> result = client.executeGET(uri);
            int code = result.response().statusCode();
            logger.info("[result]" + result.response().body());
            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
            if (HttpUtils.isNotFound(code)) {
                BaseHttpClient.formatExecutionResult(result);
            }
            logger.info("[result]" + BaseHttpClient.formatExecutionResult(result));

            // Executes a GET request and discards the response body
            HttpExecutionResult<Void> resultNoBody = client.executeGETNoResponseBody(uri);
            code = resultNoBody.response().statusCode();
            logger.info("[resultNoBody]" + resultNoBody.response().body());
            logger.info("[resultNoBody]" + BaseHttpClient.formatExecutionResult(resultNoBody));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testJS7Login() {
        SLF4JLogger logger = new SLF4JLogger();
        try {
            String user = "root";
            String password = "root";
            URI uri = new URI("http://localhost:4447/joc/api/authentication/login");

            Builder builder = BaseHttpClient.withBuilder();
            builder = builder.withLogger(logger);
            builder = builder.withConnectTimeout(Duration.ofSeconds(30));
            builder = builder.withAuth(getAuthConfig(user, password));
            builder = builder.withProxyConfig(getProxyConfig());
            // builder = builder.withHeaders(List.of("Accept application/json", "Content-Type application/json"));
            // builder = builder.withSSL(getSsl());
            BaseHttpClient client = builder.build();

            // Executes a GET request and returns response as String
            HttpExecutionResult<String> result = client.executePOST(uri);
            int code = result.response().statusCode();
            logger.info("[result]" + result.response().body());
            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
            if (HttpUtils.isNotFound(code)) {
                BaseHttpClient.formatExecutionResult(result);
            }
            logger.info("[result]" + BaseHttpClient.formatExecutionResult(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testJS7LoginJson() {
        SLF4JLogger logger = new SLF4JLogger();
        try {
            String user = "root";
            String password = "root";
            URI uri = new URI("http://localhost:4447/joc/api/authentication/login");

            Builder builder = BaseHttpClient.withBuilder();
            builder = builder.withLogger(logger);
            builder = builder.withConnectTimeout(Duration.ofSeconds(30));
            builder = builder.withAuth(getAuthConfig(user, password));
            builder = builder.withProxyConfig(getProxyConfig());
            // builder = builder.withHeaders(List.of("Accept application/json", "Content-Type application/json"));
            // builder = builder.withSSL(getSsl());
            BaseHttpClient client = builder.build();

            // Executes a GET request and returns response as String

            HttpExecutionResult<Map<String, Object>> result = client.executePOSTJson(uri, new TypeReference<>() {
            });
            int code = result.response().statusCode();
            logger.info("[result]" + result.response().body());
            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
            if (HttpUtils.isNotFound(code)) {
                BaseHttpClient.formatExecutionResult(result);
            }
            logger.info("[result]" + BaseHttpClient.formatExecutionResult(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testBasicAuth() {
        SLF4JLogger logger = new SLF4JLogger();
        try {
            String user = "yade";
            String password = "yade";
            URI uri = new URI("http://localhost:8080/yade/");

            Builder builder = BaseHttpClient.withBuilder();
            builder = builder.withLogger(logger);
            builder = builder.withConnectTimeout(Duration.ofSeconds(30));
            builder = builder.withAuth(getAuthConfig(user, password));
            builder = builder.withProxyConfig(getProxyConfig());
            // builder = builder.withSSL(getSsl());
            BaseHttpClient client = builder.build();

            // Executes a GET request and returns response as String
            HttpExecutionResult<String> result = client.executeGET(uri);
            int code = result.response().statusCode();
            logger.info("[result]" + result.response().body());
            if (HttpUtils.isServerError(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
            if (HttpUtils.isNotFound(code)) {
                throw new Exception(BaseHttpClient.formatExecutionResult(result));
            }
            logger.info("[result]" + BaseHttpClient.formatExecutionResult(result));
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

    public static void resetSystemProperties() {
        System.setProperty("javax.net.ssl.keyStore", "");
        System.setProperty("javax.net.ssl.keyStorePassword", "");

        System.setProperty("javax.net.ssl.trustStore", "");
        System.setProperty("javax.net.ssl.trustStorePassword", "");
    }
}
