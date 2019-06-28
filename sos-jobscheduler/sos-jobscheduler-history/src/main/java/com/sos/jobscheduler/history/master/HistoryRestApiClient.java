package com.sos.jobscheduler.history.master;

import java.net.URI;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSForbiddenException;
import com.sos.commons.httpclient.exception.SOSTooManyRequestsException;
import com.sos.commons.httpclient.exception.SOSUnauthorizedException;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.handler.RestServiceDuration;

import javassist.NotFoundException;

public class HistoryRestApiClient {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_VALUE_GZIP = "gzip";
    public static final String HEADER_VALUE_CACHE_CONTROL = "no-cache, no-store";
    public static final String HEADER_VALUE_TEXT_PLAIN = "text/plain";

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryRestApiClient.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private String identifier;
    private SOSRestApiClient client;

    /* intervals in milliseconds */
    private int httpClientConnectTimeout = 30_000;
    private int httpClientConnectionRequestTimeout = 30_000;
    private int httpClientSocketTimeout = 75_000;

    /* intervals in seconds */
    private int webserviceTimeout = 60;
    private int webserviceDelay = 0;

    private int webserviceLimit = 1_000;
    private RestServiceDuration lastRestServiceDuration;

    public HistoryRestApiClient(String id) {
        identifier = id;
    }

    public String doGet(URI uri) throws Exception {
        String response = null;
        try {
            createRestApiClient();
            response = executeGet(uri);
        } catch (Throwable t) {
            throw t;
        } finally {
            closeRestApiClient();
        }
        return response;
    }

    public String doPost(URI uri, String body) throws Exception {
        String response = null;
        try {
            createRestApiClient();
            response = executePost(uri, body, true);
        } catch (Throwable t) {
            throw t;
        } finally {
            closeRestApiClient();
        }
        return response;
    }

    private void createRestApiClient() {
        String method = getMethodName("createRestApiClient");

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%sconnectTimeout=%sms, socketTimeout=%sms, connectionRequestTimeout=%sms", method, httpClientConnectTimeout,
                    httpClientSocketTimeout, httpClientConnectionRequestTimeout));
        }
        client = new SOSRestApiClient();
        client.setAutoCloseHttpClient(false);
        client.setConnectionTimeout(httpClientConnectTimeout);
        client.setConnectionRequestTimeout(httpClientConnectionRequestTimeout);
        client.setSocketTimeout(httpClientSocketTimeout);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        client.createHttpClient();
    }

    private void closeRestApiClient() {
        String method = getMethodName("closeRestApiClient");

        if (client != null) {
            LOGGER.debug(method);
            client.closeHttpClient();
        } else {
            LOGGER.debug(String.format("%sskip", method));
        }
        client = null;
    }

    private String executeGet(URI uri) throws Exception {
        lastRestServiceDuration = new RestServiceDuration();
        String method = "";
        if (isDebugEnabled) {
            method = getMethodName("executeJsonGet");
            LOGGER.debug(String.format("%s%s", method, uri));
        }
        client.clearHeaders();
        // client.addHeader(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
        // client.addHeader(HEADER_ACCEPT, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_CACHE_CONTROL, HEADER_VALUE_CACHE_CONTROL);
        client.addHeader(HEADER_ACCEPT_ENCODING, HEADER_VALUE_GZIP);

        lastRestServiceDuration.start();
        String response = client.getRestService(uri);
        lastRestServiceDuration.end();
        client.clearHeaders();
        checkResponse(uri, response);
        return response;
    }

    private String executePost(URI uri, String body, boolean logBodyParams) throws Exception {
        lastRestServiceDuration = new RestServiceDuration();
        String method = "";
        if (isDebugEnabled) {
            method = getMethodName("executeJsonPost");
            LOGGER.debug(String.format("%s%s, body=%s", method, uri, logBodyParams ? body : "***"));
        }
        client.clearHeaders();
        client.addHeader(HEADER_CONTENT_TYPE, HEADER_VALUE_TEXT_PLAIN);
        client.addHeader(HEADER_ACCEPT, HEADER_VALUE_TEXT_PLAIN);
        client.addHeader(HEADER_ACCEPT_ENCODING, HEADER_VALUE_GZIP);

        lastRestServiceDuration.start();
        String response = client.postRestService(uri, body);
        lastRestServiceDuration.end();
        client.clearHeaders();
        checkResponse(uri, response);
        return response;
    }

    private void checkResponse(URI uri, String response) throws Exception {
        String method = getMethodName("checkResponse");

        int statusCode = client.statusCode();
        String contentType = client.getResponseHeader(HEADER_CONTENT_TYPE);
        if (isTraceEnabled) {
            LOGGER.trace(String.format("%sstatusCode=%s, contentType=%s", method, statusCode, contentType));
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

    private String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    public String getIdentifier() {
        return identifier;
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

    public RestServiceDuration getLastRestServiceDuration() {
        return lastRestServiceDuration;
    }
}
