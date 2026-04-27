package com.sos.joc.controller.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.resource.IControllerLogResource;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.Role;
import com.sos.joc.model.log.ControllerLogRequest;
import com.sos.joc.model.log.LogResponse;
import com.sos.joc.model.log.RequestLevel;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;
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

    public final class MyStreamingOutput implements StreamingOutput {

        private final boolean withGzipEncoding;
        private final Flux<byte[]> flux;

        public MyStreamingOutput(boolean withGzipEncoding, Flux<byte[]> flux) {
            this.withGzipEncoding = withGzipEncoding;
            this.flux = flux;
        }

        @Override
        public void write(OutputStream output) throws IOException {
            OutputStream out = withGzipEncoding ? new GZIPOutputStream(output) : output;
            flux.doOnComplete(() -> onComplete(out)).doOnError(t -> onError(t, out)).doOnNext(l -> onNext(l, out)).blockLast();
        }

        private static void onNext(byte[] logLine, OutputStream out) {
            try {
                out.write(logLine);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static void onError(Throwable t, OutputStream out) {
            close(out);
            throw new RuntimeException(t);
        }

        private static void onComplete(OutputStream out) {
            try {
                out.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                close(out);
            }
        }

        private static void close(OutputStream out) {
            try {
                out.close();
            } catch (IOException e) {
                //
            }
        }
    }

    @Override
    public JOCDefaultResponse postDownloadLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
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
            ZoneId zoneId = getZoneId(proxy.currentState().asScala().controllerMetaState().timezone().string());
            Instant instantFrom = getInstantFromZoneId(in, zoneId, false);
            Instant instantTo = getInstantFromZoneId(in, zoneId, true);
            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(in.getNumOfLines()) : OptionalLong.empty();
            Instant now = Instant.now();
            Instant to = (numOfLines.isEmpty() && instantTo == null) ? now : instantTo;
            LogLevel logLevel = getLogLevel(in);
            boolean withGzipEncoding = true;// acceptEncoding != null && acceptEncoding.contains("gzip");

            String targetFilename = getDownloadFilename(dbItem, in, instantFrom, instantTo, now, numOfLines);

            Flux<byte[]> flux = proxy.rawLogLineFlux(serverId, logLevel, instantFrom, numOfLines).publishOn(Schedulers.fromExecutor(ForkJoinPool
                    .commonPool())).flatMapIterable(Function.identity()).takeWhile(logLine -> {
                        if (to != null) {
                            String first35Chars = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(logLine, 0, 35)).toString();
                            try {
                                Instant instant = JobSchedulerDate.getInstantOfZoneId(first35Chars, zoneId);
                                return instant.isBefore(to);
                            } catch (DateTimeException e) {
                                return true;
                            }
                        }
                        return true;
                    });

            return responseOctetStreamDownloadStatus200(new MyStreamingOutput(withGzipEncoding, flux), targetFilename);
            // return JOCDefaultResponse.responseStatus200(new MyStreamingOutput(withGzipEncoding, flux), MediaType.APPLICATION_OCTET_STREAM,
            // getGzipHeaders(withGzipEncoding), getJocAuditTrail());
            // return JOCDefaultResponse.responseNotYetImplemented();
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
    public JOCDefaultResponse getLog(String accessToken, byte[] filterBytes) {
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

            Instant instantFrom = getInstantFromZoneId(in, zoneId, false);
            Instant instantTo = getInstantFromZoneId(in, zoneId, true);

            long numOfLines = in.getNumOfLines() != null ? Math.min(in.getNumOfLines(), 2500L) : 2500L;
            LogLevel logLevel = getLogLevel(in);

            // TODO consider lastLine is null if Flux is empty
            KeyedLogLine lastLine = proxy.keyedLogLineFlux(serverId, logLevel, instantFrom, OptionalLong.of(numOfLines)).publishOn(Schedulers
                    .fromExecutor(ForkJoinPool.commonPool())).flatMapIterable(Function.identity()).takeWhile(keyedLogLine -> {
                        if (instantTo != null) {
                            try {
                                Instant instant = JobSchedulerDate.getInstantOfZoneId(keyedLogLine.line().substring(0, 35), zoneId);
                                entity.setIsComplete(!instant.isBefore(instantTo));
                                return !entity.getIsComplete();
                            } catch (DateTimeException e) {
                                return true;
                            }
                        }
                        return true;
                    }).doOnNext(keyedLogLine -> {
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
        if (controllerInstances.size() > 1) { // is cluster
            checkRequiredParameter("role", in.getRole());
            boolean isPrimary = !in.getRole().equals(Role.BACKUP);
            return controllerInstances.stream().filter(ci -> ci.getIsPrimary() == isPrimary).findAny().orElseThrow(() -> new JocBadRequestException(
                    "Unknown Role: " + in.getRole()));
        } else {
            return controllerInstances.get(0);
        }
    }

    private LogLevel getLogLevel(ControllerLogRequest in) {
        LogLevel logLevel = LogLevel.info();
        switch (in.getLevel()) {
        case ERROR:
            // TODO not yet implemented
        case INFO:
            break;
        case DEBUG:
            logLevel = LogLevel.debug();
        }
        return logLevel;
    }

    private String getDownloadFilename(DBItemInventoryJSInstance dbItem, ControllerLogRequest in, Instant dateFrom, Instant dateTo,
            Instant now, OptionalLong numOfLines) {
        String serverRoleSuffix = !dbItem.getIsCluster() ? "" : dbItem.getIsPrimary() ? "-primary" : "-backup";
        String logLevelSuffix = Optional.ofNullable(in.getLevel()).filter(l -> !l.equals(RequestLevel.INFO)).map(RequestLevel::value).map(
                String::toLowerCase).map(s -> "-" + s).orElse("");
        String dtFrom = dateFrom.toString().replaceAll("[^0-9]", "").substring(0, 14);
        Optional<String> dtTo = Optional.ofNullable(dateTo).map(Object::toString).map(s -> s.replaceAll("[^0-9]", "")).map(s -> "-" + s.substring(0,
                14));
        String dtToOrLines = "";
        if (dtTo.isPresent()) {
            dtToOrLines = dtTo.get();
        } else if (numOfLines.isPresent()) {
            dtToOrLines = "-l" + numOfLines.getAsLong();
        } else {
            dtToOrLines = "-" + now.toString().replaceAll("[^0-9]", "").substring(0, 14);
        }
        return String.format("%s-controller%s-%s%s%s.log.gz", dbItem.getControllerId(), serverRoleSuffix, dtFrom, dtToOrLines, logLevelSuffix);
    }

    private Instant getInstantFromZoneId(ControllerLogRequest in, ZoneId zoneId, boolean dateTo) {
        Instant instant;
        if (dateTo) {
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateTo()), dateTo, in.getTimeZone());
        } else {
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateFrom()), dateTo, in.getTimeZone());
            Instant now = Instant.now();
            if (now.isBefore(instant)) {
                instant = now;
            }
        }
        return JobSchedulerDate.getInstantFromZoneId(instant, zoneId);
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
