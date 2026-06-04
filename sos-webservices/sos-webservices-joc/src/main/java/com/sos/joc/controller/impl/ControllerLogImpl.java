package com.sos.joc.controller.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.logs.FluxStreamingOutput;
import com.sos.joc.classes.logs.LogHelper;
import com.sos.joc.classes.logs.LogSession;
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
import com.sos.joc.model.log.RunningLogRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.log.LogLevel;
import js7.data.node.Js7ServerId;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.log.JLogSelection;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Path("controller")
public class ControllerLogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./controller/log";
    private static final String LOG_RUNNING_API_CALL = "./controller/log/running";
    private static final String LOG_DOWNLOAD_API_CALL = "./controller/log/download";

    @Override
    public JOCDefaultResponse postDownloadLog(String accessToken, byte[] filterBytes) {
        try {
            ControllerLogRequest in = init(LOG_DOWNLOAD_API_CALL, accessToken, filterBytes, ControllerLogRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemInventoryJSInstance dbItem = getDBInstance(in);

            JControllerProxy proxy = Proxy.of(dbItem.getControllerId());
            Js7ServerId serverId = dbItem.getIsPrimary() ? Js7ServerId.primaryController : Js7ServerId.backupController;
            String timeZone = proxy.currentState().asScala().controllerMetaState().timezone().string();
            
            ZoneId zoneId = LogHelper.getZoneId(timeZone);
            Instant instantFrom = LogHelper.getInstantFromZoneId(in, zoneId, false);
            Optional<Instant> instantTo = Optional.ofNullable(LogHelper.getInstantFromZoneId(in, zoneId, true));
            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(in.getNumOfLines()) : OptionalLong.empty();
            Instant now = Instant.now();
            LogLevel logLevel = LogHelper.getLogLevel(in.getLevel());

            String targetFilename = LogHelper.getControllerDownloadFilename(dbItem, in.getLevel(), instantFrom, instantTo, now, numOfLines, true);
            byte[] header = getHeader(dbItem, instantFrom, timeZone);

            JLogSelection selection = JLogSelection.empty().withLineLimit(numOfLines).withEnd(instantTo);
            Flux<byte[]> flux = proxy.byteLogLineFlux(serverId, logLevel, instantFrom, selection)
                    .publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))
                    .flatMapIterable(Function.identity());
                    //.takeWhile(LogHelper.dateToIsReached(instantTo, zoneId));

            return responseOctetStreamDownloadStatus200(new FluxStreamingOutput(true, flux, header), targetFilename);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse getLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            ControllerLogRequest in = init(LOG_API_CALL, accessToken, filterBytes, ControllerLogRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemInventoryJSInstance dbItem = getDBInstance(in);

            JControllerProxy proxy = Proxy.of(dbItem.getControllerId());
            Js7ServerId serverId = dbItem.getIsPrimary() ? Js7ServerId.primaryController : Js7ServerId.backupController;
            String timezone = proxy.currentState().asScala().controllerMetaState().timezone().string();

            LogResponse entity = LogHelper.getResponse(proxy, accessToken, in, serverId, timezone);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse getRunningLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            RunningLogRequest in = init(LOG_RUNNING_API_CALL, accessToken, filterBytes, RunningLogRequest.class);
            LogSession logSession = LogHelper.getLogSession(accessToken, in.getLogToken());
            String controllerId = logSession.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerId, accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            checkAndGetDBInstances(controllerId);
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
    
    public static List<DBItemInventoryJSInstance> checkAndGetDBInstances(String controllerId) {
        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
        if (controllerInstances == null) {
            throw new JocBadRequestException("Couldn't find Controller with ID " + controllerId);
        }
        return controllerInstances;
    }

    private DBItemInventoryJSInstance getDBInstance(ControllerLogRequest in) {
        List<DBItemInventoryJSInstance> controllerInstances = checkAndGetDBInstances(in.getControllerId());
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

}
