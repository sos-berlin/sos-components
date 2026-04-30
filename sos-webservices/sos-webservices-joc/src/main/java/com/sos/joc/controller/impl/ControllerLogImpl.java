package com.sos.joc.controller.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.logs.FluxStreamingOutput;
import com.sos.joc.classes.logs.LogHelper;
import com.sos.joc.classes.logs.LogSession;
import com.sos.joc.classes.logs.LogSessions;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.resource.IControllerLogResource;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.Role;
import com.sos.joc.model.log.ControllerLogRequest;
import com.sos.joc.model.log.LogResponse;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;
import js7.data.node.Js7ServerId;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Path("controller")
public class ControllerLogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./controller/log";
    private static final String LOG_DOWNLOAD_API_CALL = "./controller/log/download";
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerLogImpl.class);

    @Override
    public JOCDefaultResponse postDownloadLog(String accessToken, byte[] filterBytes) {
        try {
            ControllerLogRequest in = init(LOG_DOWNLOAD_API_CALL, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemInventoryJSInstance dbItem = getDBInstance(in);

            JControllerProxy proxy = Proxy.of(dbItem.getControllerId());
            Js7ServerId serverId = dbItem.getIsPrimary() ? Js7ServerId.primaryController : Js7ServerId.backupController;
            String timeZone = proxy.currentState().asScala().controllerMetaState().timezone().string();
            ZoneId zoneId = getZoneId(timeZone);
            Instant instantFrom = LogHelper.getInstantFromZoneId(in, zoneId, false);
            Optional<Instant> instantTo = Optional.ofNullable(LogHelper.getInstantFromZoneId(in, zoneId, true));
            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(in.getNumOfLines()) : OptionalLong.empty();
            Instant now = Instant.now();
            LogLevel logLevel = LogHelper.getLogLevel(in.getLevel());

            String targetFilename = LogHelper.getControllerDownloadFilename(dbItem, in.getLevel(), instantFrom, instantTo, now, numOfLines, true);
            byte[] header = getHeader(dbItem, instantFrom, timeZone);

            Flux<byte[]> flux = proxy.rawLogLineFlux(serverId, logLevel, instantFrom, numOfLines).publishOn(Schedulers.fromExecutor(ForkJoinPool
                    .commonPool())).flatMapIterable(Function.identity()).takeWhile(LogHelper.dateToIsReached(instantTo, zoneId));

            return responseOctetStreamDownloadStatus200(new FluxStreamingOutput(true, flux, header), targetFilename);
            // return JOCDefaultResponse.responseStatus200(new MyStreamingOutput(withGzipEncoding, flux), MediaType.APPLICATION_OCTET_STREAM,
            // getGzipHeaders(withGzipEncoding), getJocAuditTrail());
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    // private Map<String, Object> getGzipHeaders(boolean withGzipEncoding) {
    // Map<String, Object> headers = new HashMap<String, Object>();
    // if (withGzipEncoding) {
    // headers.put("Content-Encoding", "gzip");
    // }
    // headers.put("Transfer-Encoding", "chunked");
    // return headers;
    // }

    @Override
    public JOCDefaultResponse getLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            ControllerLogRequest in = init(LOG_API_CALL, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemInventoryJSInstance dbItem = getDBInstance(in);

            JControllerProxy proxy = Proxy.of(dbItem.getControllerId());
            Js7ServerId serverId = dbItem.getIsPrimary() ? Js7ServerId.primaryController : Js7ServerId.backupController;
            // EngineServerId.subagent(SubagentId.of(""));

            LogResponse entity = new LogResponse();
            entity.setTimeZone(proxy.currentState().asScala().controllerMetaState().timezone().string());
            ZoneId zoneId = getZoneId(entity.getTimeZone());

            // TODO token
            entity.setToken(UUID.randomUUID().toString());
            entity.setLogLines(new ArrayList<>());

            Instant instantFrom = LogHelper.getInstantFromZoneId(in, zoneId, false);
            Optional<Instant> instantTo = Optional.ofNullable(LogHelper.getInstantFromZoneId(in, zoneId, true));

            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(Math.min(in.getNumOfLines(), 2500L)) : OptionalLong.of(2500L);
            LogLevel logLevel = LogHelper.getLogLevel(in.getLevel());
            
            LogSessions lss = LogSessions.getInstance();
            
            LogSession ls = new LogSession(serverId, logLevel, instantFrom, instantTo, numOfLines, entity.getToken());

            // TODO consider lastLine is null if Flux is empty
            KeyedLogLine lastLine = proxy.keyedLogLineFlux(serverId, logLevel, instantFrom, numOfLines).publishOn(Schedulers
                    .fromExecutor(ForkJoinPool.commonPool())).flatMapIterable(Function.identity())
                    .takeWhile(LogHelper.dateToIsReached(instantTo, zoneId, entity))
                    .doOnNext(keyedLogLine -> {
                        // System.out.println(keyedLogLine);
                        // LogLineKey
                        // LOGGER.info(keyedLogLine.line());
                        entity.getLogLines().add(keyedLogLine.line());
                        // response.put("key", keyedLogLine.key().toString());
                    }).blockLast();
            // entity.setToken(lastLine.key().toString());
            if (!entity.getIsComplete() && in.getNumOfLines() != null && entity.getLogLines().size() <= in.getNumOfLines().intValue()) {
                if (in.getNumOfLines() <= 2500L) {
                    entity.setIsComplete(true);
                } else if (entity.getLogLines().size() < 2500L) {
                    entity.setIsComplete(true);
                }
            }
            // System.out.println(lastLine.key().toString());
            // .reduce(0L, (a, keyedLogLine) -> a + 1)
            // //.collectList()
            // .toFuture()
            // .thenAccept(keyedLogLines -> {
            // entity.setLogLines(keyedLogLines.stream().map(KeyedLogLine::line).toList());
            // //response.put("key", keyedLogLines.get(keyedLogLines.size() - 1).key().toString());
            // });
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    private ControllerLogRequest init(String apiCall, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.CONTROLLER);
        JsonValidator.validateFailFast(filterBytes, ControllerLogRequest.class);
        return Globals.objectMapper.readValue(filterBytes, ControllerLogRequest.class);
    }

    private DBItemInventoryJSInstance getDBInstance(ControllerLogRequest in) {
        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(in.getControllerId());
        if (controllerInstances == null) {
            throw new JocBadRequestException("Couldn't find Controller with ID " + in.getControllerId());
        }
        if (controllerInstances.size() > 1) { // is cluster
            checkRequiredParameter("role", in.getRole());
            boolean isPrimary = !in.getRole().equals(Role.BACKUP);
            return controllerInstances.stream().filter(ci -> ci.getIsPrimary() == isPrimary).findAny().orElseThrow(() -> new JocBadRequestException(
                    "Unknown Role: " + in.getRole()));
        } else {
            return controllerInstances.get(0);
        }
    }
    
    private static byte[] getHeader(DBItemInventoryJSInstance dbItem, Instant instantFrom, String timeZone) {
        Optional<DBItemInventoryOperatingSystem> osItem = getOSItem(dbItem.getOsId());
        String newline = osItem.map(os -> os.getName().toLowerCase().startsWith("windows") ? "\r\n" : "\n").orElse("\n");
        return String.format("%s Begin JS7 Controller:%s · %s · Java %s · %s %s%s", LogHelper.formattedTimeStamp(instantFrom), dbItem.getControllerId(),
                dbItem.getVersion(), dbItem.getJavaVersion(), getPlatformInfo(osItem), timeZone, newline).getBytes(StandardCharsets.UTF_8);
    }

    private static Optional<DBItemInventoryOperatingSystem> getOSItem(Long osId) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(LOG_DOWNLOAD_API_CALL);
            InventoryOperatingSystemsDBLayer dbLayer = new InventoryOperatingSystemsDBLayer(session);
            return Optional.ofNullable(dbLayer.getInventoryOperatingSystem(osId));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static String getPlatformInfo(Optional<DBItemInventoryOperatingSystem> osItem) {
        return osItem.map(os -> String.format("%s (%s) · %s · host=%s", os.getName(), os.getDistribution(), os.getArchitecture(), os.getHostname()))
                .orElse("");
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

}
