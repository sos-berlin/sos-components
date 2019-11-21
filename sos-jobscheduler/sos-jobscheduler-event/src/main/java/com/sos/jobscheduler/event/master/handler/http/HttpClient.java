package com.sos.jobscheduler.event.master.handler.http;

import java.io.StringReader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.commons.httpclient.exception.SOSForbiddenException;
import com.sos.commons.httpclient.exception.SOSTooManyRequestsException;
import com.sos.commons.httpclient.exception.SOSUnauthorizedException;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.configuration.handler.HttpClientConfiguration;

import javassist.NotFoundException;

public class HttpClient {

    public static final String HEADER_SCHEDULER_SESSION = "X-JobScheduler-Session";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    public static final String HEADER_VALUE_GZIP = "gzip";
    public static final String HEADER_VALUE_CACHE_CONTROL = "no-cache, no-store";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private SOSRestApiClient client;
    private String identifier;
    private RestServiceDuration lastRestServiceDuration;

    public void create(HttpClientConfiguration config) {
        String method = getMethodName("create");

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[connectTimeout=%ss][socketTimeout=%ss][connectionRequestTimeout=%ss]", method, config.getConnectTimeout(),
                    config.getSocketTimeout(), config.getConnectionRequestTimeout()));
        }
        client = new SOSRestApiClient();
        client.setAutoCloseHttpClient(false);
        client.setConnectionTimeout(config.getConnectTimeout() * 1_000);
        client.setSocketTimeout(config.getSocketTimeout() * 1_000);
        client.setConnectionRequestTimeout(config.getConnectionRequestTimeout() * 1_000);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        client.createHttpClient();
    }

    public void tryCreate(HttpClientConfiguration config) {
        if (client == null) {
            create(config);
        }
    }

    public void close() {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s%s", getMethodName("close"), client == null ? "[skip]client is NULL" : ""));
        }

        if (client != null) {
            client.closeHttpClient();
            client = null;
        }
    }

    public boolean isClosed() {
        return client == null;
    }

    public String executeGet(URI uri, String token) throws Exception {
        lastRestServiceDuration = new RestServiceDuration();
        String method = "";
        if (isDebugEnabled) {
            method = getMethodName("executeJsonGet");
            LOGGER.debug(String.format("%s%s", method, uri));
        }
        client.clearHeaders();
        // client.addHeader(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_CACHE_CONTROL, HEADER_VALUE_CACHE_CONTROL);
        client.addHeader(HEADER_ACCEPT_ENCODING, HEADER_VALUE_GZIP);
        if (!SOSString.isEmpty(token)) {
            client.addHeader(HEADER_SCHEDULER_SESSION, token);
        }
        lastRestServiceDuration.start();
        String response = client.getRestService(uri);
        lastRestServiceDuration.end();
        client.clearHeaders();
        checkResponse(uri, response);
        // return readResponse(uri, response);
        return response;
    }

    public String executePost(URI uri, JsonObjectBuilder bodyParams, String token, boolean logBodyParams) throws Exception {
        String body = bodyParams == null ? null : bodyParams.build().toString();
        return executePost(uri, body, token, logBodyParams);
    }

    public String executePost(URI uri, String bodyParams, String token, boolean logBodyParams) throws Exception {
        lastRestServiceDuration = new RestServiceDuration();
        String method = "";
        String body = bodyParams;
        if (isDebugEnabled) {
            method = getMethodName("executeJsonPost");
            LOGGER.debug(String.format("%s%s, body=%s", method, uri, logBodyParams ? body : "***"));
        }
        client.clearHeaders();
        client.addHeader(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT_ENCODING, HEADER_VALUE_GZIP);
        // client.addHeader(HEADER_CONTENT_ENCODING, HEADER_VALUE_GZIP);

        if (!SOSString.isEmpty(token)) {
            client.addHeader(HEADER_SCHEDULER_SESSION, token);
        }
        lastRestServiceDuration.start();
        String response = client.postRestService(uri, body);
        lastRestServiceDuration.end();
        client.clearHeaders();
        checkResponse(uri, response);
        return response;
    }

    public static Exception findConnectionRefusedException(Throwable cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof SOSConnectionRefusedException) {
                return (SOSConnectionRefusedException) e;
            }
            e = e.getCause();
        }
        return null;
    }

    private void checkResponse(URI uri, String response) throws Exception {
        String method = getMethodName("checkResponse");
        int statusCode = client.statusCode();
        String contentType = client.getResponseHeader(HEADER_CONTENT_TYPE);
        if (isTraceEnabled) {
            LOGGER.trace(String.format("%s[%s][%s]", method, statusCode, contentType));
        }
        switch (statusCode) {
        case 200:
            if (SOSString.isEmpty(response)) {
                throw new Exception(String.format("%s[%s][%s][%s][%s]response is empty", method, lastRestServiceDuration, uri.toString(), statusCode,
                        contentType));
            }
            break;
        case 400:
            // TO DO check Content-Type
            // for now the exception is plain/text instead of JSON
            // throw message item value
            throw new Exception(String.format("%s[%s][%s][%s][%s][%s]%s", method, lastRestServiceDuration, uri.toString(), statusCode, contentType,
                    response, getResponseReason()));
        case 401:
            throw new SOSUnauthorizedException(String.format("%s[%s][%s][%s][%s][%s]%s", method, lastRestServiceDuration, uri.toString(), statusCode,
                    contentType, response, getResponseReason()));
        case 403:
            throw new SOSForbiddenException(String.format("%s[%s][%s][%s][%s][%s]%s", method, lastRestServiceDuration, uri.toString(), statusCode,
                    contentType, response, getResponseReason()));
        case 404:
            throw new NotFoundException(String.format("%s[%s][%s][%s][%s][%s]%s", method, lastRestServiceDuration, uri.toString(), statusCode,
                    contentType, response, getResponseReason()));
        case 429:
            throw new SOSTooManyRequestsException(String.format("%s[%s][%s][%s][%s][%s]%s", method, lastRestServiceDuration, uri.toString(),
                    statusCode, contentType, response, getResponseReason()));
        default:
            throw new Exception(String.format("%s[%s][%s][%s][%s][%s]%s", method, lastRestServiceDuration, uri.toString(), statusCode, contentType,
                    response, getResponseReason()));
        }
    }

    private String getResponseReason() {
        try {
            return client.getHttpResponse().getStatusLine().getReasonPhrase();
        } catch (Throwable t) {
        }
        return "";
    }

    public JsonObject response2json(URI uri, String response) throws Exception {
        JsonObject json = null;
        StringReader sr = null;
        JsonReader jr = null;
        try {
            sr = new StringReader(response);
            jr = Json.createReader(sr);

            json = jr.readObject();
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[%s][see exception details below]%s", getMethodName("readResponse"), uri.toString(), response));
            LOGGER.error(String.format("%s[%s]%s", getMethodName("readResponse"), uri.toString(), e.toString()), e);
            throw e;
        } finally {
            if (jr != null) {
                jr.close();
            }
            if (sr != null) {
                sr.close();
            }
        }

        return json;
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

    public RestServiceDuration getLastRestServiceDuration() {
        return lastRestServiceDuration;
    }
}
