package com.sos.joc.joc.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.logs.FutureStreamingOutput;
import com.sos.joc.classes.logs.JOCLogProxyContext;
import com.sos.joc.classes.logs.LogHelper;
import com.sos.joc.classes.logs.LogSession;
import com.sos.joc.controller.resource.IControllerLogResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.log.JOCLogRequest;
import com.sos.joc.model.log.KeyedLogRequest;
import com.sos.joc.model.log.LogResponse;
import com.sos.joc.model.log.NextLogRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.log.LogLevel;
import js7.proxy.javaapi.log.JLogSelection;

@Path("joc")
public class LogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./joc/log";
    private static final String LOG_RUNNING_API_CALL = "./joc/log/running";
    private static final String LOG_DOWNLOAD_API_CALL = "./joc/log/download";
    private static final String LOG_NEXT_API_CALL = "./joc/log/next";
    private static final String LOG_PREV_API_CALL = "./joc/log/prev";

    @Override
    public JOCDefaultResponse postDownloadLog(String accessToken, byte[] filterBytes) {
        try {
            JOCLogRequest in = init(LOG_DOWNLOAD_API_CALL, accessToken, filterBytes, JOCLogRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions().map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            ZoneId zoneId = JOCLogProxyContext.zoneId;
            Instant instantFrom = LogHelper.getInstantFromZoneId(in, zoneId, false);
            Optional<Instant> instantTo = Optional.ofNullable(LogHelper.getInstantFromZoneId(in, zoneId, true));
            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(in.getNumOfLines()) : OptionalLong.empty();
            Instant now = Instant.now();
            LogLevel logLevel = LogHelper.getLogLevel(in.getLevel());

            String targetFilename = LogHelper.getJOCDownloadFilename(in.getServiceId().logPrefix(), in.getLevel(), instantFrom, instantTo, now,
                    numOfLines, true);
            // TODO create Header line
            byte[] header = null;

            JLogSelection selection = JLogSelection.empty().withLineLimit(numOfLines).withEnd(instantTo);

            CompletableFuture<List<byte[]>> future = JOCLogProxyContext.getJResource().use(logDirectoryIndex -> {
                return logDirectoryIndex.logIndex(in.getServiceId().logPrefix(), logLevel).thenCompose(logIndex -> {
                    return logIndex.keyedByteLogLineFlux(instantFrom, selection)
                            // .publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))
                            .flatMapIterable(Function.identity())
                            .map(kbll -> kbll.lineAsString().getBytes(StandardCharsets.UTF_8)).collectList()
                            .toFuture();
                });
            });

            return responseOctetStreamDownloadStatus200(new FutureStreamingOutput(true, future, header), targetFilename);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse getLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            JOCLogRequest in = init(LOG_API_CALL, accessToken, filterBytes, JOCLogRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions().map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            LogResponse entity = LogHelper.getResponse(accessToken, in);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse getPrevLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            KeyedLogRequest in = init(LOG_PREV_API_CALL, accessToken, filterBytes, KeyedLogRequest.class);
            LogSession logSession = LogHelper.getLogSession(accessToken, in.getLogToken());
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions().map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            LogResponse entity = LogHelper.getPrevResponse(logSession, in);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse getNextLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            NextLogRequest in = init(LOG_NEXT_API_CALL, accessToken, filterBytes, NextLogRequest.class);
            LogSession logSession = LogHelper.getLogSession(accessToken, in.getLogToken());
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions().map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            LogResponse entity = LogHelper.getNextResponse(logSession, in);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse getRunningLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            NextLogRequest in = init(LOG_RUNNING_API_CALL, accessToken, filterBytes, NextLogRequest.class);
            LogSession logSession = LogHelper.getLogSession(accessToken, in.getLogToken());
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions().map(p -> p.getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            LogResponse entity = LogHelper.getRunningResponse(logSession, in);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private <T> T init(String apiCall, String accessToken, byte[] filterBytes, Class<T> clazz) throws Exception {
        filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.CONTROLLER);
        JsonValidator.validateFailFast(filterBytes, clazz);
        return Globals.objectMapper.readValue(filterBytes, clazz);
    }

//    private static Optional<DBItemInventoryOperatingSystem> getOSItem(Long osId) {
//        SOSHibernateSession session = null;
//        try {
//            session = Globals.createSosHibernateStatelessConnection(LOG_DOWNLOAD_API_CALL);
//            InventoryOperatingSystemsDBLayer dbLayer = new InventoryOperatingSystemsDBLayer(session);
//            return Optional.ofNullable(dbLayer.getInventoryOperatingSystem(osId));
//        } catch (Exception e) {
//            return Optional.empty();
//        } finally {
//            Globals.disconnect(session);
//        }
//    }
//    
//    private static String getPlatformInfo(Optional<DBItemInventoryOperatingSystem> osItem) {
//        return osItem.map(os -> String.format("%s (%s) · %s · host=%s", os.getName(), os.getDistribution(), os.getArchitecture(), os.getHostname()))
//                .orElse("");
//    }

}
