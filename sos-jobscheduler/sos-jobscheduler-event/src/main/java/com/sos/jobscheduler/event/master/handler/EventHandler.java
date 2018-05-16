package com.sos.jobscheduler.event.master.handler;

import java.net.URI;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;

import javassist.NotFoundException;

public class EventHandler {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_APPLICATION_JSON = "application/json";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    /* all intervals in seconds */
    private int httpClientConnectTimeout = 30;
    private int httpClientConnectionRequestTimeout = 30;
    private int httpClientSocketTimeout = 75;

    private int webserviceTimeout = 60;
    private int webserviceDelay = 0;
    private int webserviceLimit = 1000;

    private String identifier;
    private URI baseUri;
    private SOSRestApiClient client;
    private int methodExecutionTimeout = 90;

    private final EventPath eventPath;
    private final Class<? extends IEntry> eventEntryClazz;
    private final ObjectMapper objectMapper;

    public EventHandler(EventPath path, Class<? extends IEntry> clazz) {
        eventPath = path;
        eventEntryClazz = clazz;

        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        SimpleModule sm = new SimpleModule();
        sm.addAbstractTypeMapping(IEntry.class, eventEntryClazz);
        objectMapper.registerModule(sm);
    }

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

    public void setBaseUri(String host, String port) throws Exception {
        if (SOSString.isEmpty(host)) {
            throw new Exception("host is empty");
        }
        if (SOSString.isEmpty(port)) {
            throw new Exception("port is empty");
        }
        StringBuilder uri = new StringBuilder();
        uri.append("http://");
        uri.append(host);
        uri.append(":");
        uri.append(port);
        uri.append(EventMeta.MASTER_API_PATH);
        uri.append(eventPath.name());
        baseUri = new URI(uri.toString());
    }

    public Event getEvent(Long eventId) throws Exception {
        if (isDebugEnabled) {
            String method = getMethodName("getEvent");
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
        URIBuilder ub = new URIBuilder(baseUri);
        ub.addParameter("after", eventId.toString());
        ub.addParameter("timeout", String.valueOf(webserviceTimeout));
        if (webserviceDelay > 0) {
            ub.addParameter("delay", String.valueOf(webserviceDelay));
        }
        if (webserviceLimit > 0) {
            ub.addParameter("limit", String.valueOf(webserviceLimit));
        }
        return executeJsonGet(ub.build());
    }

    public Event executeJsonGet(URI uri) throws Exception {
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
        checkResponse(uri, response);
        return objectMapper.readValue(response, Event.class);
    }

    private void checkResponse(URI uri, String response) throws Exception {
        String method = getMethodName("checkResponse");

        int statusCode = client.statusCode();
        String contentType = client.getResponseHeader(HEADER_CONTENT_TYPE);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s statusCode=%s, contentType=%s", method, statusCode, contentType));
        }
        switch (statusCode) {
        case 200:
            if (SOSString.isEmpty(response)) {
                throw new Exception(String.format("%s response is empty. statusCode=%s, contentType=%s", method, statusCode, contentType));
            }
            break;
        case 400:
            // TO DO check Content-Type
            // for now the exception is plain/text instead of JSON
            // throw message item value
            throw new Exception(String.format("%s statusCode=%s, contentType=%s %s", method, statusCode, contentType, getResponseReason()));
        case 404:
            throw new NotFoundException(String.format("%s %s %s, uri=%s", method, statusCode, getResponseReason(), uri.toString()));
        default:
            throw new Exception(String.format("%s %s %s, uri=%s", method, statusCode, getResponseReason(), uri.toString()));
        }
    }

    private String getResponseReason() {
        try {
            return client.getHttpResponse().getStatusLine().getReasonPhrase();
        } catch (Throwable t) {
        }
        return "";
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

    public URI getBaseUri() {
        return baseUri;
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