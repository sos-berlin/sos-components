package com.sos.joc.controller.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.resource.IControllerLogResource;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.log.reader.KeyedLogLine;
import js7.data.node.EngineServerId;
import js7.base.log.LogLevel;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

@Path("controller")
public class ControllerLogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./controller/log";
    private static final String isUrlPattern = "^https?://[^\\s]+$";
    private static final Predicate<String> isUrl = Pattern.compile(isUrlPattern).asPredicate();

    @Override
    public JOCDefaultResponse getDebugLog(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(LOG_API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParamSchema = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(urlParamSchema.getControllerId(), accessToken)
                    .map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParamSchema.getControllerId());
            if (controllerInstances.size() > 1) { // is cluster
                checkRequiredParameter("url", urlParamSchema.getUrl());
                if (!isUrl.test(urlParamSchema.getUrl())) {
                    throw new JocBadRequestException("$.url: does not match the url pattern " + isUrlPattern);
                }
            } else {
                urlParamSchema.setUrl(controllerInstances.get(0).getUri());
            }
            // increase timeout for large log files
            int socketTimeout = Math.max(Globals.httpSocketTimeout, 30000);
            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(urlParamSchema.getUrl(), getAccessToken());
            jocJsonCommand.setUriBuilderForMainLog(true);
            return responseOctetStreamDownloadStatus200(jocJsonCommand.getStreamingOutputFromGet(
                    "text/plain,application/octet-stream", true), "controller.log.gz");
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse getDebugLog(String accessToken, String queryAccessToken, String controllerId, String url) {
        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if(controllerId != null) {
            builder.add("controllerId", controllerId);
        }
        if (url != null) {
            builder.add("url", url);
        }
        return getDebugLog(accessToken, builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public JOCDefaultResponse getLog(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(LOG_API_CALL + "2", filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParamSchema = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(urlParamSchema.getControllerId(), accessToken)
                    .map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParamSchema.getControllerId());
            DBItemInventoryJSInstance dbItem = null;
            if (controllerInstances.size() > 1) { // is cluster
                checkRequiredParameter("url", urlParamSchema.getUrl());
                if (!isUrl.test(urlParamSchema.getUrl())) {
                    throw new JocBadRequestException("$.url: does not match the url pattern " + isUrlPattern);
                }
                dbItem = controllerInstances.stream().filter(ci -> ci.getUri().equals(urlParamSchema.getUrl())).findAny().orElseThrow(
                        () -> new JocBadRequestException("Unknown URL: " + urlParamSchema.getUrl()));
            } else {
                dbItem = controllerInstances.get(0);
            }
            
            JControllerProxy proxy = Proxy.of(dbItem.getControllerId());
            EngineServerId serverId = dbItem.getIsPrimary() ? EngineServerId.primaryController : EngineServerId.backupController;
            ZoneId zoneId = getZoneId(proxy.currentState().asScala().controllerMetaState().timezone().string());
            //Instant instant = Instant.now().minusSeconds(3600);
            Instant instant = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
            //Flux<List<KeyedLogLine>> flux = proxy.keyedLogLineFlux(LogLevel.debug(), instant, OptionalLong.of(100l));
            //LogLineKey.
            Flux<List<KeyedLogLine>> flux = proxy.keyedLogLineFlux(serverId, LogLevel.debug(), Instant.parse(
                    "2026-03-03T17:35:00Z"), OptionalLong.of(Long.MAX_VALUE));
            // Error handling and completion
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            //flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);
            //flux.flatMapIterable(Function.identity());
            Map<String, Object> response = new HashMap<>();
            List<String> loglines = new ArrayList<>();
            flux.doOnNext(keyedLogLines -> {
                System.out.println(keyedLogLines);
                loglines.addAll(keyedLogLines.stream().peek(l -> response.put("key", l.key().toString())).map(KeyedLogLine::line).toList());
            }).reduce(0L, (a, lines) -> a + lines.size()).toFuture().get();
            response.put("loglines", loglines);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    private ZoneId getZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            // TODO LOGGER.warn
            return ZoneId.of("UTC");
        }
    }
    
    public void fluxDoOnComplete() {
        System.out.println("OnComplete");
    }
    
    public void fluxDoFinally(SignalType type) {
        System.out.println("OnFinally: " + type.toString());
    }
    
    public void fluxDoOnError(Throwable t) {
        t.printStackTrace();
    }

}
