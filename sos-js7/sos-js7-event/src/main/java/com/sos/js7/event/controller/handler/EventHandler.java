package com.sos.js7.event.controller.handler;

import java.net.URI;
import java.net.URISyntaxException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sos.commons.util.SOSString;
import com.sos.js7.event.controller.EventMeta;
import com.sos.js7.event.controller.EventMeta.EventPath;
import com.sos.js7.event.controller.bean.Event;
import com.sos.js7.event.controller.bean.IEntry;
import com.sos.js7.event.controller.configuration.Configuration;
import com.sos.js7.event.http.HttpClient;

public class EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private final Configuration config;
    private final HttpClient httpClient;

    private String identifier;
    private URI baseUri;
    private URI eventUri;

    private final EventPath eventPath;
    private final Class<? extends IEntry> eventEntryClazz;
    private final ObjectMapper objectMapper;

    private boolean useLogin;
    private String user;

    public EventHandler(Configuration configuration) {
        this(configuration, null, null);
    }

    public EventHandler(Configuration configuration, EventPath path, Class<? extends IEntry> clazz) {
        config = configuration;
        eventPath = path;
        eventEntryClazz = clazz;

        httpClient = new HttpClient();
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        SimpleModule sm = new SimpleModule();
        if (eventEntryClazz != null) {
            sm.addAbstractTypeMapping(IEntry.class, eventEntryClazz);
        }
        objectMapper.registerModule(sm);
    }

    public void setUri(String masterUri) throws Exception {
        if (SOSString.isEmpty(masterUri)) {
            throw new Exception("masterUri is empty");
        }
        StringBuilder uri = new StringBuilder(masterUri);
        uri.append(EventMeta.MASTER_API_PATH);

        baseUri = new URI(uri.toString());
        if (eventPath != null) {
            eventUri = getEventUri(eventPath);
        }
    }

    public Event getAfterEvent(Long eventId, String token) throws Exception {
        if (isTraceEnabled) {
            String method = getMethodName("getAfterEvent");
            LOGGER.trace(String.format("%seventId=%s", method, eventId));
        }
        URIBuilder ub = new URIBuilder(eventUri);
        ub.addParameter("after", eventId.toString());
        ub.addParameter("timeout", String.valueOf(config.getWebservice().getTimeout()));
        if (config.getWebservice().getDelay() > 0) {
            ub.addParameter("delay", String.valueOf(config.getWebservice().getDelay()));
        }
        if (config.getWebservice().getLimit() > 0) {
            ub.addParameter("limit", String.valueOf(config.getWebservice().getLimit()));
        }
        return objectMapper.readValue(httpClient.executeGet(ub.build(), token), Event.class);
    }

    public <T> T getEvent(Class<T> clazz, EventPath path, String token) throws Exception {
        if (isTraceEnabled) {
            LOGGER.trace(getMethodName("getEvent"));
        }
        JavaType type = objectMapper.getTypeFactory().constructType(clazz);
        return objectMapper.readValue(httpClient.executeGet(new URIBuilder(getEventUri(path)).build(), token), type);
    }

    public Event getEvent(String token) throws Exception {
        // return getEvent(token, Event.class);
        if (isTraceEnabled) {
            LOGGER.trace(getMethodName("getEvent"));
        }
        return objectMapper.readValue(httpClient.executeGet(new URIBuilder(eventUri).build(), token), Event.class);
    }

    public String releaseEvents(Long eventId, String token) throws Exception {
        String method = getMethodName("releaseEvents");
        try {
            URIBuilder ub = new URIBuilder(baseUri.toString() + EventMeta.Path.command.name());
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("TYPE", "ReleaseEvents");
            ob.add("untilEventId", eventId);
            URI uri = ub.build();
            JsonObject jo = httpClient.response2json(uri, httpClient.executePost(uri, ob, token, true));
            if (jo == null) {
                throw new Exception("JsonObject is null");
            }
            return jo.getString("TYPE");
        } catch (Exception e) {
            throw new Exception(String.format("%s%s", method, e.toString()), e);
        }
    }

    public String login(String userName, String password) throws Exception {
        String method = getMethodName("login");
        try {
            user = userName;
            URIBuilder ub = new URIBuilder(baseUri.toString() + EventMeta.Path.session.name());
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("TYPE", "Login");
            if (useLogin) {
                JsonObjectBuilder obc = Json.createObjectBuilder();
                obc.add("userId", user);
                obc.add("password", password);
                ob.add("userAndPassword", obc);
            }
            URI uri = ub.build();
            JsonObject jo = httpClient.response2json(uri, httpClient.executePost(uri, ob, null, false));
            if (jo == null) {
                throw new Exception("JsonObject is null");
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s]logged in", method, user));
            }
            return jo.getString("sessionToken");
        } catch (Exception e) {
            throw new Exception(String.format("%s[%s]login failed: %s", method, user == null ? "public" : user, e.toString()), e);
        }
    }

    // TODO to remove
    public void logout() {
        String method = getMethodName("logout");
        if (!useLogin) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][skip]useLogin=false", method, user));
            }
            return;
        }

        try {
            URIBuilder ub = new URIBuilder(baseUri.toString() + EventMeta.Path.session.name());
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("TYPE", "Logout");
            httpClient.executePost(ub.build(), ob, null, true);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s]logged out", method, user));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("%s[%s]logout failed: %s", method, user == null ? "public" : user, e.toString()), e);
        }
    }

    public String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    private URI getEventUri(EventPath path) throws URISyntaxException {
        return path == null ? null : new URI(baseUri.toString() + path.name());
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

    public void useLogin(boolean val) {
        useLogin = val;
    }

    public boolean useLogin() {
        return useLogin;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Configuration getConfig() {
        return config;
    }
}