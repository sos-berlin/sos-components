package com.sos.joc.agent.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.logs.FluxStreamingOutput;
import com.sos.joc.classes.logs.LogHelper;
import com.sos.joc.classes.logs.LogSession;
import com.sos.joc.classes.logs.LogSessions;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.resource.IControllerLogResource;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.log.AgentLogRequest;
import com.sos.joc.model.log.LogResponse;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;
import js7.data.agent.AgentPath;
import js7.data.node.Js7ServerId;
import js7.data.platform.PlatformInfo;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.OptionConverters;

@Path("agent")
public class AgentLogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./agent/log";
    private static final String LOG_DOWNLOAD_API_CALL = "./agent/log/download";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentLogImpl.class);

    @Override
    public JOCDefaultResponse postDownloadLog(String accessToken, byte[] filterBytes) {
        try {
            AgentLogRequest in = init(LOG_DOWNLOAD_API_CALL, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerProxy proxy = Proxy.of(in.getControllerId());
            JControllerState currentState = proxy.currentState();
            JAgentRef agent = currentState.pathToAgentRef().get(AgentPath.of(in.getAgentId()));
            if (agent == null) {
                throw new JocBadRequestException("Couldn't find Agent with ID " + in.getAgentId());
            }
            List<SubagentId> directors = agent.directors();
            // TODO check if standalone agent has also (one) director? I think yes
            SubagentId director = getDirector(in, directors);
            Integer isPrimary = getIsPrimary(directors, director);
            Js7ServerId serverId = Js7ServerId.subagent(director);
            Optional<PlatformInfo> platforminfo = OptionConverters.toJava(currentState.idToSubagentItemState().get(director).asScala()
                    .platformInfo());
            String timeZone = platforminfo.map(PlatformInfo::timezone).orElse("GMT");
            ZoneId zoneId = getZoneId(timeZone);
            Instant instantFrom = LogHelper.getInstantFromZoneId(in, zoneId, false);
            Optional<Instant> instantTo = Optional.ofNullable(LogHelper.getInstantFromZoneId(in, zoneId, true));
            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(in.getNumOfLines()) : OptionalLong.empty();
            Instant now = Instant.now();
            LogLevel logLevel = LogHelper.getLogLevel(in.getLevel());

            String targetFilename = LogHelper.getAgentDownloadFilename(director.string(), isPrimary, in.getLevel(), instantFrom, instantTo, now,
                    numOfLines, true);
            byte[] header = getHeader(director, platforminfo, instantFrom, timeZone);

            Flux<byte[]> flux = proxy.rawLogLineFlux(serverId, logLevel, instantFrom, numOfLines).publishOn(Schedulers.fromExecutor(ForkJoinPool
                    .commonPool())).flatMapIterable(Function.identity()).takeWhile(LogHelper.dateToIsReached(instantTo, zoneId));

            return responseOctetStreamDownloadStatus200(new FluxStreamingOutput(true, flux, header), targetFilename);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse getLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            AgentLogRequest in = init(LOG_API_CALL, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerProxy proxy = Proxy.of(in.getControllerId());
            JControllerState currentState = proxy.currentState();
            JAgentRef agent = currentState.pathToAgentRef().get(AgentPath.of(in.getAgentId()));
            if (agent == null) {
                throw new JocBadRequestException("Couldn't find Agent with ID " + in.getAgentId());
            }
            List<SubagentId> directors = agent.directors();
            // TODO check if standalone agent has also (one) director? I think yes
            SubagentId director = getDirector(in, directors);
            Js7ServerId serverId = Js7ServerId.subagent(director);
            Optional<PlatformInfo> platforminfo = OptionConverters.toJava(currentState.idToSubagentItemState().get(director).asScala()
                    .platformInfo());
            String timeZone = platforminfo.map(PlatformInfo::timezone).orElse("GMT");
            ZoneId zoneId = getZoneId(timeZone);

            LogResponse entity = new LogResponse();
            entity.setTimeZone(timeZone);

            // TODO token
            entity.setToken(UUID.randomUUID().toString());
            entity.setLogLines(new ArrayList<>());

            Instant instantFrom = LogHelper.getInstantFromZoneId(in, zoneId, false);
            Optional<Instant> instantTo = Optional.ofNullable(LogHelper.getInstantFromZoneId(in, zoneId, true));

            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(Math.min(in.getNumOfLines(), 2500L)) : OptionalLong.of(2500L);
            LogLevel logLevel = LogHelper.getLogLevel(in.getLevel());
            
//            LogSessions lss = LogSessions.getInstance();
//            
//            LogSession ls = new LogSession(serverId, logLevel, instantFrom, instantTo, numOfLines, entity.getToken());

            // TODO consider lastLine is null if Flux is empty
            KeyedLogLine lastLine = proxy.keyedLogLineFlux(serverId, logLevel, instantFrom, numOfLines).publishOn(Schedulers
                    .fromExecutor(ForkJoinPool.commonPool())).flatMapIterable(Function.identity())
                    .takeWhile(LogHelper.dateToIsReached(instantTo, zoneId, entity))
                    .doOnNext(keyedLogLine -> {
                        // LOGGER.info(keyedLogLine.line());
                        entity.getLogLines().add(keyedLogLine.line());
                    }).blockLast();
            // entity.setToken(lastLine.key().toString());
            if (!entity.getIsComplete() && in.getNumOfLines() != null && entity.getLogLines().size() <= in.getNumOfLines().intValue()) {
                if (in.getNumOfLines() <= 2500L) {
                    entity.setIsComplete(true);
                } else if (entity.getLogLines().size() < 2500L) {
                    entity.setIsComplete(true);
                }
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    private static SubagentId getDirector(AgentLogRequest in, List<SubagentId> directors) {
        // TODO check if standalone agent has also (one) director? I think yes
        SubagentId director = SubagentId.of(in.getAgentId());
        if (directors.size() == 1) {
            director = directors.get(0);
        } else if (directors.size() > 1) {
            checkRequiredParameter("subagentId", in.getSubagentId()); 
            director = directors.stream().filter(SubagentId.of(in.getSubagentId())::equals).findAny().orElseThrow(
                    () -> new JocBadRequestException("Couldn't find Director-Agent with ID " + in.getSubagentId()));
        }
        return director;
    }
    
    private static Integer getIsPrimary(List<SubagentId> directors, SubagentId director) {
        return directors.size() == 1 ? 0 : directors.indexOf(director) + 1;
    }

    private AgentLogRequest init(String apiCall, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.CONTROLLER);
        JsonValidator.validateFailFast(filterBytes, AgentLogRequest.class);
        return Globals.objectMapper.readValue(filterBytes, AgentLogRequest.class);
    }
    
    private static byte[] getHeader(SubagentId subagentId, Optional<PlatformInfo> platforminfo, Instant instantFrom, String timeZone) {
        // Begin JS7 Subagent:agent_003 · 2.9.0-SNAPSHOT+20260417.0607 (HEAD) + platforminfo
        return String.format("%s Begin JS7 Subagent:%s%s", LogHelper.formattedTimeStamp(instantFrom), subagentId.string(), getPlatformInfo(platforminfo,
                timeZone)).getBytes(StandardCharsets.UTF_8);
    }
    
    private static String getPlatformInfo(Optional<PlatformInfo> platforminfo, String timeZone) {
        // · Java 17.0.14 · OpenJDK 64-Bit Server VM 17.0.14+7-alpine-r0 (500MiB) · Linux (Alpine Linux v3.19) · 
        //Intel(R) Xeon(R) CPU E5-2630 v4 @ 2.20GHz (4 threads) 7.6GiB · 
        //host=agent-2-0-standalone GMT
        
        return platforminfo.map(pi -> {
            Map<String, String> sysProps = JavaConverters.asJava(pi.java().systemProperties());
            
            StringBuilder sb = new StringBuilder(" · ");
            sb.append(pi.js7Version().string());
            Optional.ofNullable(sysProps.get("java.version")).ifPresent(s -> sb.append(" · Java " + s));
            Optional.ofNullable(sysProps.get("java.vm.name")).ifPresent(s -> sb.append(" · " + s));
            sb.append(" " + pi.java().version());
            Optional<String> osName = Optional.ofNullable(sysProps.get("os.name"));
            String newline = osName.map(String::toLowerCase).map(s -> s.startsWith("windows") ? "\r\n" : "\n").orElse("\n");
            osName.ifPresent(s -> sb.append(" · " + s));
            OptionConverters.toJava(pi.operatingSystemDistribution()).ifPresent(s -> sb.append(" (" + s + ")"));
            OptionConverters.toJava(pi.cpuModel()).ifPresent(s -> sb.append(" · " + s));
            sb.append(" · host=").append(pi.hostname()).append(" ").append(timeZone).append(newline);
            
            return sb.toString();
            
        }).orElseGet(() -> timeZone + "\n");
    }

    private static ZoneId getZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            // TODO LOGGER.warn
            return ZoneId.of("UTC");
        }
    }

    public static void fluxDoOnComplete() {
        System.out.println("OnComplete");
    }

}
