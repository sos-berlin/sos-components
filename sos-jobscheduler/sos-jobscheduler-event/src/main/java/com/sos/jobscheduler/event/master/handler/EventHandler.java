package com.sos.jobscheduler.event.master.handler;

import java.io.StringReader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSUnauthorizedException;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;

import javassist.NotFoundException;

public class EventHandler {

    public static final String HEADER_SCHEDULER_SESSION = "X-JobScheduler-Session";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    public static final String HEADER_VALUE_GZIP = "gzip";
    public static final String HEADER_VALUE_CACHE_CONTROL = "no-cache, no-store";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    /* all intervals in milliseconds */
    private int httpClientConnectTimeout = 30_000;
    private int httpClientConnectionRequestTimeout = 30_000;
    private int httpClientSocketTimeout = 75_000;

    private int webserviceTimeout = 60;// seconds
    private int webserviceDelay = 0;// seconds
    private int webserviceLimit = 1000;

    private String identifier;
    private URI baseUri;
    private URI eventUri;
    private SOSRestApiClient client;

    private final EventPath eventPath;
    private final Class<? extends IEntry> eventEntryClazz;
    private final ObjectMapper objectMapper;

    private boolean useLogin;
    private String user;

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
            LOGGER.debug(String.format("%s: connectTimeout=%sms, socketTimeout=%sms, connectionRequestTimeout=%sms", method, httpClientConnectTimeout,
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

        baseUri = new URI(uri.toString());
        eventUri = new URI(baseUri.toString() + eventPath.name());
    }

    public Event getEvent(Long eventId, String token) throws Exception {
        if (isDebugEnabled) {
            String method = getMethodName("getEvent");
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
        URIBuilder ub = new URIBuilder(eventUri);
        ub.addParameter("after", eventId.toString());
        ub.addParameter("timeout", String.valueOf(webserviceTimeout));
        if (webserviceDelay > 0) {
            ub.addParameter("delay", String.valueOf(webserviceDelay));
        }
        if (webserviceLimit > 0) {
            ub.addParameter("limit", String.valueOf(webserviceLimit));
        }
        return objectMapper.readValue(executeJsonGet(ub.build(), token), Event.class);
    }

    public String login(String userName, String password) throws Exception {
        String method = getMethodName("login");
        if (!useLogin) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]useLogin=false", method));
            }
            return null;
        }
        try {
            user = userName;
            if (client == null) {
                throw new Exception(String.format("%s client is null", method));
            }
            URIBuilder ub = new URIBuilder(baseUri.toString() + EventMeta.Path.session.name());
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("TYPE", "Login");
            JsonObjectBuilder obc = Json.createObjectBuilder();
            obc.add("userId", user);
            obc.add("password", password);
            ob.add("userAndPassword", obc);
            JsonObject jo = readResponse(executeJsonPost(ub.build(), ob, null));
            if (jo == null) {
                throw new Exception("JsonObject is null");
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s]logged in", method, user));
            }
            return jo.getString("sessionToken");
        } catch (Exception e) {
            throw new Exception(String.format("%s[%s]login failed: %s", method, user, e.toString()));
        }
    }

    public void logout() {
        String method = getMethodName("logout");
        if (!useLogin) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][skip]useLogin=false", method, user));
            }
            return;
        }

        try {
            if (client == null) {
                throw new Exception("client is null");
            }
            URIBuilder ub = new URIBuilder(baseUri.toString() + EventMeta.Path.session.name());
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("TYPE", "Logout");
            executeJsonPost(ub.build(), ob, null);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s]logged out", method, user));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("%s[%s]logout failed: %s", method, user, e.toString()));
        }
    }

    public JsonObject readResponse(String response) {
        String method = getMethodName("readResponse");
        JsonObject json = null;
        StringReader sr = new StringReader(response);
        JsonReader jr = Json.createReader(sr);
        try {
            json = jr.readObject();
        } catch (Exception e) {
            LOGGER.error(String.format("%s: read exception %s", method, e.toString()), e);
            throw e;
        } finally {
            jr.close();
            sr.close();
        }
        return json;
    }

    public String executeJsonGet(URI uri, String token) throws Exception {
        String method = "";
        if (isDebugEnabled) {
            method = getMethodName("executeJsonGet");
            LOGGER.debug(String.format("%s call uri=%s", method, uri));
        }
        // client.addHeader(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_CACHE_CONTROL, HEADER_VALUE_CACHE_CONTROL);
        client.addHeader(HEADER_ACCEPT_ENCODING, HEADER_VALUE_GZIP);
        if (!SOSString.isEmpty(token)) {
            client.addHeader(HEADER_SCHEDULER_SESSION, token);
        }
        String response = client.getRestService(uri);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s response=%s", method, response));
        }
        checkResponse(uri, response);
        return response;
    }

    public String executeJsonPost(URI uri, JsonObjectBuilder bodyParams, String token) throws Exception {
        String method = "";
        String body = bodyParams == null ? null : bodyParams.build().toString();
        if (isDebugEnabled) {
            method = getMethodName("executeJsonPost");
            LOGGER.debug(String.format("%s call uri=%s, body=%s", method, uri, body));
        }
        client.addHeader(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT, HEADER_VALUE_APPLICATION_JSON);
        client.addHeader(HEADER_ACCEPT_ENCODING, HEADER_VALUE_GZIP);
        // client.addHeader(HEADER_CONTENT_ENCODING, HEADER_VALUE_GZIP);

        if (!SOSString.isEmpty(token)) {
            client.addHeader(HEADER_SCHEDULER_SESSION, token);
        }
        String response = client.postRestService(uri, body);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s response=%s", method, response));
        }
        checkResponse(uri, response);
        return response;
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
                throw new Exception(String.format("%s[%s][%s][%s]response is empty", method, uri.toString(), statusCode, contentType));
            }
            break;
        case 400:
            // TO DO check Content-Type
            // for now the exception is plain/text instead of JSON
            // throw message item value
            throw new Exception(String.format("%s[%s][%s][%s][%s]%s", method, uri.toString(), statusCode, contentType, response,
                    getResponseReason()));
        case 401:
            throw new SOSUnauthorizedException(String.format("%s[%s][%s][%s][%s]%s", method, uri.toString(), statusCode, contentType, response,
                    getResponseReason()));
        case 404:
            throw new NotFoundException(String.format("%s[%s][%s][%s][%s]%s", method, uri.toString(), statusCode, contentType, response,
                    getResponseReason()));
        default:
            throw new Exception(String.format("%s[%s][%s][%s][%s]%s", method, uri.toString(), statusCode, contentType, response,
                    getResponseReason()));
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

    public URI getEventUri() {
        return eventUri;
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

    public void useLogin(boolean val) {
        useLogin = val;
    }
}