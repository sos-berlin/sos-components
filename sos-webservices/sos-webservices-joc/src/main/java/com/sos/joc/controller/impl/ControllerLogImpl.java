package com.sos.joc.controller.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

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
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;
import js7.data.node.EngineServerId;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.scheduler.Schedulers;

@Path("controller")
public class ControllerLogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./controller/log";
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerLogImpl.class);

    @Override
    public JOCDefaultResponse getDownloadLog(String accessToken, byte[] filterBytes) {

        return JOCDefaultResponse.responseNotYetImplemented();
    }

    @Override
    public JOCDefaultResponse getLog(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(LOG_API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, ControllerLogRequest.class);
            ControllerLogRequest in = Globals.objectMapper.readValue(filterBytes, ControllerLogRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(in.getControllerId());
            DBItemInventoryJSInstance dbItem = null;
            if (controllerInstances.size() > 1) { // is cluster
                checkRequiredParameter("role", in.getRole());
                boolean isPrimary = !in.getRole().equals(Role.BACKUP);
                dbItem = controllerInstances.stream().filter(ci -> ci.getIsPrimary() == isPrimary).findAny().orElseThrow(
                        () -> new JocBadRequestException("Unknown Role: " + in.getRole()));
            } else {
                dbItem = controllerInstances.get(0);
            }

            JControllerProxy proxy = Proxy.of(dbItem.getControllerId());
            EngineServerId serverId = dbItem.getIsPrimary() ? EngineServerId.primaryController : EngineServerId.backupController;
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
            LogLevel logLevel = LogLevel.info();
            switch (in.getLevel()) {
            case ERROR:
                // TODO not yet implemented
            case INFO:
                break;
            case DEBUG:
                logLevel = LogLevel.debug();
            }

            // TODO consider lastLine is null if Flux is empty
            KeyedLogLine lastLine = proxy.keyedLogLineFlux(serverId, logLevel, instantFrom, OptionalLong.of(numOfLines)).publishOn(Schedulers
                    .fromExecutor(ForkJoinPool.commonPool())).flatMapIterable(Function.identity()).takeWhile(keyedLogLine -> {
                        if (instantTo != null) {
                            Instant instant = JobSchedulerDate.getInstantOfZoneId(keyedLogLine.line().substring(0, 35), zoneId);
                            entity.setIsComplete(!instant.isBefore(instantTo));
                            return !entity.getIsComplete();
                        }
                        return true;
                    }).doOnNext(keyedLogLine -> {
                        // System.out.println(keyedLogLine);
                        // LogLineKey
                        //LOGGER.info(keyedLogLine.line());
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
