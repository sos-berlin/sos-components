package com.sos.jobscheduler.event.master;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventPath;

import javassist.NotFoundException;

public class JobSchedulerEventHandler {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_APPLICATION_JSON = "application/json";

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    /* all intervals in seconds */
    private int httpClientConnectTimeout = 30;
    private int httpClientConnectionRequestTimeout = 30;
    private int httpClientSocketTimeout = 75;

    private int webserviceTimeout = 60;
    private int webserviceDelay = 0;
    private int webserviceLimit = 1000;

    private String identifier;
    private String baseUrl;
    private SOSRestApiClient client;
    private int methodExecutionTimeout = 90;

    public void createRestApiClient() {
        String method = getMethodName("createRestApiClient");

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s: connectTimeout=%ss, socketTimeout=%ss, connectionRequestTimeout=%ss", method, httpClientConnectTimeout,
                    httpClientSocketTimeout, httpClientConnectionRequestTimeout));
        }
        client = new SOSRestApiClient();
        client.setAutoCloseHttpClient(false);
        client.setConnectionTimeout(httpClientConnectTimeout * 1000);
        client.setConnectionRequestTimeout(httpClientConnectionRequestTimeout * 1000);
        client.setSocketTimeout(httpClientSocketTimeout * 1000);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        client.createHttpClient();
    }

    public void closeRestApiClient() {
        String method = getMethodName("closeRestApiClient");

        if (client != null) {
            LOGGER.debug(method);
            client.closeHttpClient();
        } else {
            LOGGER.debug(String.format("%s skip", method));
        }
        client = null;
    }

    public void setBaseUrl(String host, String port) {
        baseUrl = String.format("http://%s:%s", host, port);
    }

    public JobSchedulerEvent getEvents(EventPath eventPath, Long eventId) throws Exception {
        if (isDebugEnabled) {
            String method = getMethodName("getEvents");
            LOGGER.debug(String.format("%s eventPath=%s, eventId=%s", method, eventPath, eventId));
        }
        URIBuilder ub = new URIBuilder(getUri(eventPath));
        ub.addParameter("after", eventId.toString());
        ub.addParameter("timeout", String.valueOf(webserviceTimeout));
        if (webserviceDelay > 0) {
            ub.addParameter("delay", String.valueOf(webserviceDelay));
        }
        if (webserviceLimit > 0) {
            ub.addParameter("limit", String.valueOf(webserviceLimit));
        }
        return new JobSchedulerEvent(eventId, executeJsonGet(ub.build()));
    }

    public JsonObject executeJsonGetTimeLimited(URI uri) throws Exception {

        final SimpleTimeLimiter timeLimiter = new SimpleTimeLimiter(Executors.newSingleThreadExecutor());
        @SuppressWarnings("unchecked")
        final Callable<JsonObject> timeLimitedCall = timeLimiter.newProxy(new Callable<JsonObject>() {

            @Override
            public JsonObject call() throws Exception {
                return executeJsonGet(uri);
            }
        }, Callable.class, methodExecutionTimeout * 1000, TimeUnit.MILLISECONDS);
        return timeLimitedCall.call();
    }

    public JsonObject executeJsonGet(URI uri) throws Exception {
        String method = "";
        if (isDebugEnabled) {
            method = getMethodName("executeJsonGet");
            LOGGER.debug(String.format("%s call uri=%s", method, uri));
        }
        client.addHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
        String response = client.getRestService(uri);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s response=%s", method, response));
        }
        return readResponse(uri, response);
    }

    public JsonObject executeJsonPostTimeLimited(URI uri) throws Exception {
        return executeJsonPostTimeLimited(uri, null);
    }

    public JsonObject executeJsonPostTimeLimited(URI uri, String bodyParamPath) throws Exception {

        final SimpleTimeLimiter timeLimiter = new SimpleTimeLimiter(Executors.newSingleThreadExecutor());
        @SuppressWarnings("unchecked")
        final Callable<JsonObject> timeLimitedCall = timeLimiter.newProxy(new Callable<JsonObject>() {

            @Override
            public JsonObject call() throws Exception {
                return executeJsonPost(uri, bodyParamPath);
            }
        }, Callable.class, methodExecutionTimeout * 1000, TimeUnit.MILLISECONDS);
        return timeLimitedCall.call();
    }

    public JsonObject executeJsonPost(URI uri) throws Exception {
        return executeJsonPost(uri, null);
    }

    public JsonObject executeJsonPost(URI uri, String bodyParamPath) throws Exception {
        String method = "";
        if (isDebugEnabled) {
            method = getMethodName("executeJsonPost");
            LOGGER.debug(String.format("%s call uri=%s, bodyParamPath=%s", method, uri, bodyParamPath));
        }
        client.addHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
        String body = null;
        if (!SOSString.isEmpty(bodyParamPath)) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("path", bodyParamPath);
            body = builder.build().toString();
        }
        String response = client.postRestService(uri, body);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s response=%s", method, response));
        }
        return readResponse(uri, response);
    }

    private JsonObject readResponse(URI uri, String response) throws Exception {
        String method = getMethodName("readResponse");

        int statusCode = client.statusCode();
        String contentType = client.getResponseHeader(HEADER_CONTENT_TYPE);
        JsonObject json = null;
        if (contentType.contains(HEADER_APPLICATION_JSON)) {
            StringReader sr = new StringReader(response);
            JsonReader jr = Json.createReader(sr);
            try {
                json = jr.readObject();
            } catch (Exception e) {
                LOGGER.error(String.format("%s read exception %s", method, e.toString()), e);
                throw e;
            } finally {
                jr.close();
                sr.close();
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s statusCode=%s", method, statusCode));
        }
        switch (statusCode) {
        case 200:
            if (json != null) {
                return json;
            } else {
                throw new Exception(String.format("%s unexpected content type '%s'. response: %s", method, contentType, response));
            }
        case 400:
            // TO DO check Content-Type
            // for now the exception is plain/text instead of JSON
            // throw message item value
            if (json != null) {
                throw new Exception(json.getString("message"));
            } else {
                throw new Exception(String.format("%s unexpected content type '%s'. response: %s", method, contentType, response));
            }
        case 404:
            throw new NotFoundException(String.format("%s %s %s, uri=%s", method, statusCode, client.getHttpResponse().getStatusLine()
                    .getReasonPhrase(), uri.toString()));
        default:
            throw new Exception(String.format("%s %s %s", method, statusCode, client.getHttpResponse().getStatusLine().getReasonPhrase()));
        }
    }

    public URI getUri(EventPath path) throws URISyntaxException {
        if (baseUrl == null) {
            throw new URISyntaxException("null", "baseUrl is NULL");
        }
        if (path == null) {
            throw new URISyntaxException("null", "path is NULL");
        }
        StringBuilder uri = new StringBuilder();
        uri.append(baseUrl);
        uri.append(JobSchedulerEvent.MASTER_API_PATH);
        uri.append(path.name());
        return new URI(uri.toString());
    }

    public String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public SOSRestApiClient getRestApiClient() {
        return client;
    }

    public int getHttpClientConnectTimeout() {
        return httpClientConnectTimeout;
    }

    public void setHttpClientConnectTimeout(int val) {
        httpClientConnectTimeout = val;
    }

    public int getHttpClientConnectionRequestTimeout() {
        return httpClientConnectionRequestTimeout;
    }

    public void setHttpClientConnectionRequestTimeout(int val) {
        httpClientConnectionRequestTimeout = val;
    }

    public int getHttpClientSocketTimeout() {
        return httpClientSocketTimeout;
    }

    public void setHttpClientSocketTimeout(int val) {
        httpClientSocketTimeout = val;
    }

    public int getWebserviceTimeout() {
        return webserviceTimeout;
    }

    public void setWebserviceTimeout(int val) {
        webserviceTimeout = val;
    }

    public int getWebserviceDelay() {
        return webserviceDelay;
    }

    public void setWebserviceDelay(int val) {
        webserviceDelay = val;
    }

    public int getWebserviceLimit() {
        return webserviceLimit;
    }

    public void setWebserviceLimit(int val) {
        webserviceLimit = val;
    }

    public int getMethodExecutionTimeout() {
        return methodExecutionTimeout;
    }

    public void setMethodExecutionTimeout(int val) {
        methodExecutionTimeout = val;
    }

}