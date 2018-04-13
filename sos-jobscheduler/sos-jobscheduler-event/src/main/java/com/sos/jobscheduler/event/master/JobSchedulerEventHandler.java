package com.sos.jobscheduler.event.master;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventKey;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventOverview;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventPath;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventType;

import javassist.NotFoundException;
import com.sos.commons.util.SOSString;

public class JobSchedulerEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerEventHandler.class);

    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String HEADER_ACCEPT = "Accept";

    public static final String HEADER_APPLICATION_JSON = "application/json";

    /* all intervals in seconds */
    private int httpClientConnectTimeout = 30;
    private int httpClientConnectionRequestTimeout = 30;
    private int httpClientSocketTimeout = 75;

    private int webserviceTimeout = 60;
    private int methodExecutionTimeout = 90;

    private String identifier;
    private String baseUrl;
    private SOSRestApiClient client;

    public void createRestApiClient() {
        String method = getMethodName("createRestApiClient");

        LOGGER.debug(String.format("%s: connectTimeout=%ss, socketTimeout=%ss, connectionRequestTimeout=%ss", method, httpClientConnectTimeout,
                httpClientSocketTimeout, httpClientConnectionRequestTimeout));
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

    public JsonObject getOverview(EventPath path) throws Exception {
        return getOverview(path, getEventOverviewByEventPath(path), null);
    }

    public JsonObject getOverview(EventPath path, String bodyParamPath) throws Exception {
        return getOverview(path, getEventOverviewByEventPath(path), bodyParamPath);
    }

    public JsonObject getOverview(EventPath path, EventOverview overview) throws Exception {
        return getOverview(path, overview, null);
    }

    public JsonObject getOverview(EventPath path, EventOverview overview, String bodyParamPath) throws Exception {
        String method = getMethodName("getOverview");

        LOGGER.debug(String.format("%s eventPath=%s, eventOverview=%s, bodyParamPath=%s", method, path, overview, bodyParamPath));
        URIBuilder ub = new URIBuilder(getUri(path));
        ub.addParameter("return", overview.name());
        return executeJsonPost(ub.build(), bodyParamPath);
    }

    public JsonObject getEvents(Long eventId, EventType[] eventTypes) throws Exception {
        return getEvents(eventId, joinEventTypes(eventTypes), null);
    }

    public JsonObject getEvents(Long eventId, EventType[] eventTypes, String bodyParamPath) throws Exception {
        return getEvents(eventId, joinEventTypes(eventTypes), bodyParamPath);
    }

    public JsonObject getEvents(Long eventId, String eventTypes) throws Exception {
        return getEvents(eventId, eventTypes, null);
    }

    public JsonObject getEvents(Long eventId, String eventTypes, String bodyParamPath) throws Exception {
        String method = getMethodName("getEvents");

        LOGGER.debug(String.format("%s eventId=%s, eventTypes=%s, bodyParamPath=%s", method, eventId, eventTypes, bodyParamPath));

        URIBuilder ub = new URIBuilder(getUri(EventPath.event));
        if (!SOSString.isEmpty(eventTypes)) {
            ub.addParameter("return", eventTypes);
        }
        ub.addParameter("timeout", String.valueOf(webserviceTimeout));
        ub.addParameter("after", eventId.toString());
        if (SOSString.isEmpty(bodyParamPath)) {
            return executeJsonGet(ub.build());
        }
        return executeJsonPost(ub.build(), bodyParamPath);
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
        String method = getMethodName("executeJsonGet");

        LOGGER.debug(String.format("%s call uri=%s", method, uri));

        client.addHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
        String response = client.getRestService(uri);
        LOGGER.debug(String.format("%s response=%s", method, response));
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
        String method = getMethodName("executeJsonPost");

        LOGGER.debug(String.format("%s call uri=%s, bodyParamPath=%s", method, uri, bodyParamPath));

        client.addHeader(HEADER_CONTENT_TYPE, HEADER_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_APPLICATION_JSON);
        String body = null;
        if (!SOSString.isEmpty(bodyParamPath)) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("path", bodyParamPath);
            body = builder.build().toString();
        }
        String response = client.postRestService(uri, body);

        LOGGER.debug(String.format("%s response=%s", method, response));
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
        LOGGER.debug(String.format("%s statusCode=%s", method, statusCode));

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

    public Long getEventId(JsonObject json) {
        Long eventId = null;
        if (json != null) {
            JsonNumber r = json.getJsonNumber(EventKey.eventId.name());
            if (r != null) {
                eventId = r.longValue();
            }
        }
        return eventId;
    }

    public String getEventType(JsonObject json) {
        return json == null ? null : json.getString(EventKey.TYPE.name());
    }

    public JsonArray getEventSnapshots(JsonObject json) {
        return json == null ? null : json.getJsonArray(EventKey.eventSnapshots.name());
    }

    public String getEventKey(JsonObject json) {
        String eventKey = null;
        JsonValue key = json.get(EventKey.key.name());
        if (key != null) {
            if (key.getValueType().equals(ValueType.STRING)) {
                eventKey = key.toString();
            } else if (key.getValueType().equals(ValueType.OBJECT)) {
                if (((JsonObject) key).containsKey(EventKey.jobPath.name())) {
                    eventKey = ((JsonObject) key).getString(EventKey.jobPath.name());
                }
            }
        }
        return eventKey;
    }

    public String joinEventTypes(EventType[] type) {
        return type == null ? "" : Joiner.on(",").join(type);
    }

    public EventOverview getEventOverviewByEventTypes(EventType[] type) {
        if (type != null && type.length > 0) {
            String first = type[0].name();
            if (first.toLowerCase().startsWith(EventPath.fileBased.name().toLowerCase())) {
                return EventOverview.FileBasedOverview;
            } else if (first.toLowerCase().startsWith(EventPath.order.name().toLowerCase())) {
                return EventOverview.OrderOverview;
            } else if (first.toLowerCase().startsWith(EventPath.task.name().toLowerCase())) {
                return EventOverview.TaskOverview;
            } else if (first.toLowerCase().startsWith(EventPath.jobChain.name().toLowerCase())) {
                return EventOverview.JobChainOverview;
            }
        }
        return null;
    }

    public EventOverview getEventOverviewByEventPath(EventPath path) {
        if (path != null) {
            if (path.equals(EventPath.fileBased)) {
                return EventOverview.FileBasedOverview;
            } else if (path.equals(EventPath.order)) {
                return EventOverview.OrderOverview;
            } else if (path.equals(EventPath.task)) {
                return EventOverview.TaskOverview;
            } else if (path.equals(EventPath.jobChain)) {
                return EventOverview.JobChainOverview;
            }
        }
        return null;
    }

    public EventPath getEventPathByEventOverview(EventOverview overview) {
        if (overview != null) {
            if (overview.name().toLowerCase().startsWith(EventPath.fileBased.name().toLowerCase())) {
                return EventPath.fileBased;
            } else if (overview.name().toLowerCase().startsWith(EventPath.order.name().toLowerCase())) {
                return EventPath.order;
            } else if (overview.name().toLowerCase().startsWith(EventPath.task.name().toLowerCase())) {
                return EventPath.task;
            } else if (overview.name().toLowerCase().startsWith(EventPath.jobChain.name().toLowerCase())) {
                return EventPath.jobChain;
            } else if (overview.name().toLowerCase().startsWith(EventPath.event.name().toLowerCase())) {
                return EventPath.event;
            }
        }
        return null;
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

    public int getMethodExecutionTimeout() {
        return methodExecutionTimeout;
    }

    public void setMethodExecutionTimeout(int val) {
        methodExecutionTimeout = val;
    }

}