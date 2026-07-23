package com.sos.joc.agent.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.logs.FluxStreamingOutput;
import com.sos.joc.classes.logs.LogHelper;
import com.sos.joc.classes.logs.LogSession;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.impl.ControllerLogImpl;
import com.sos.joc.controller.resource.IControllerLogResource;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.log.AgentLogRequest;
import com.sos.joc.model.log.KeyedLogRequest;
import com.sos.joc.model.log.LogResponse;
import com.sos.joc.model.log.NextLogRequest;
import com.sos.joc.model.log.RunningLogRequest;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.log.LogLevel;
import js7.data.agent.AgentPath;
import js7.data.node.Js7ServerId;
import js7.data.platform.PlatformInfo;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.subagent.JSubagentItem;
import js7.data_for_java.subagent.JSubagentItemState;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.log.JLogSelection;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.OptionConverters;

@Path("agent")
public class AgentLogImpl extends JOCResourceImpl implements IControllerLogResource {

    private static final String LOG_API_CALL = "./agent/log";
    private static final String LOG_DOWNLOAD_API_CALL = "./agent/log/download";
    private static final String LOG_RUNNING_API_CALL = "./agent/log/running";
    private static final String LOG_PREV_API_CALL = "./agent/log/prev";
    private static final String LOG_NEXT_API_CALL = "./agent/log/next";

    @Override
    public JOCDefaultResponse postDownloadLog(String accessToken, byte[] filterBytes) {
        try {
            AgentLogRequest in = init(LOG_DOWNLOAD_API_CALL, accessToken, filterBytes, AgentLogRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerProxy proxy = Proxy.of(in.getControllerId());
            JSubagentItemState subagent = getSubagent(proxy, in);
            Js7ServerId serverId = Js7ServerId.subagent(subagent.subagentId());
            Optional<PlatformInfo> platforminfo = OptionConverters.toJava(subagent.asScala().platformInfo());
            String timeZone = platforminfo.map(PlatformInfo::timezone).orElse("GMT");
            ZoneId zoneId = LogHelper.getZoneId(timeZone);
            Instant instantFrom = LogHelper.getInstantFromZoneId(in, zoneId, false);
            Optional<Instant> instantTo = Optional.ofNullable(LogHelper.getInstantFromZoneId(in, zoneId, true));
            OptionalLong numOfLines = in.getNumOfLines() != null ? OptionalLong.of(in.getNumOfLines()) : OptionalLong.empty();
            Instant now = Instant.now();
            LogLevel logLevel = LogHelper.getLogLevel(in.getLevel());

            String targetFilename = LogHelper.getAgentDownloadFilename(subagent.subagentId(), in.getLevel(), instantFrom, instantTo, now, numOfLines,
                    true);
            byte[] header = getHeader(subagent.subagentId(), platforminfo, instantFrom, timeZone);
            
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
            AgentLogRequest in = init(LOG_API_CALL, accessToken, filterBytes, AgentLogRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(in.getControllerId(), accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            ControllerLogImpl.checkAndGetDBInstances(in.getControllerId());
            JControllerProxy proxy = Proxy.of(in.getControllerId());
            JSubagentItemState subagent = getSubagent(proxy, in);
            Js7ServerId serverId = Js7ServerId.subagent(subagent.subagentId());
            Optional<PlatformInfo> platforminfo = OptionConverters.toJava(subagent.asScala().platformInfo());
            String timeZone = platforminfo.map(PlatformInfo::timezone).orElse("GMT");
            
//            if (LogHelper.checkIfEmpty(proxy, accessToken, in, serverId, timeZone)) {
//                
//            }
            
            LogResponse entity = LogHelper.getResponse(proxy, accessToken, in, serverId, timeZone);
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    private boolean isNotStandalone(AgentPath agentPath, Map<SubagentId, JSubagentItemState> subagents) {
        return subagents.values().stream().map(JSubagentItemState::subagentItem).map(JSubagentItem::agentPath).filter(
                agentPath::equals).count() > 1l;
    }
    
    private JSubagentItemState getSubagent(JControllerProxy proxy, AgentLogRequest in) {
        JControllerState currentState = proxy.currentState();
        AgentPath agentPath = AgentPath.of(in.getAgentId());
        JAgentRef agent = currentState.pathToAgentRef().get(agentPath);
        if (agent == null) {
            throw new JocBadRequestException("Couldn't find Agent with ID " + in.getAgentId());
        }
        List<SubagentId> directors = agent.directors();
        SubagentId subagentId = directors.get(0);
        Map<SubagentId, JSubagentItemState> subagents = currentState.idToSubagentItemState();
        if (isNotStandalone(agentPath, subagents)) {
            checkRequiredParameter("subagentId", in.getSubagentId());
            subagentId = SubagentId.of(in.getSubagentId());
        }
        JSubagentItemState subagent = subagents.get(subagentId);
        if (subagent == null) {
            throw new JocBadRequestException("Couldn't find Subagent with ID " + in.getSubagentId()); 
        }
        if (!subagent.subagentItem().agentPath().equals(agentPath)) {
            throw new JocBadRequestException("Subagent with ID " + in.getSubagentId() + " doesn't belong to Agent " + in.getAgentId());
        }
        return subagent;
    }
    
    @Override
    public JOCDefaultResponse getPrevLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            KeyedLogRequest in = init(LOG_PREV_API_CALL, accessToken, filterBytes, KeyedLogRequest.class);
            LogSession logSession = LogHelper.getLogSession(accessToken, in.getLogToken());
            String controllerId = logSession.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerId, accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            ControllerLogImpl.checkAndGetDBInstances(controllerId);
            
            return JOCDefaultResponse.responseNotYetImplemented();
            
//            LogResponse entity = LogHelper.getNextResponse(logSession, in);
//
//            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse getNextLog(String accessToken, String acceptEncoding, byte[] filterBytes) {
        try {
            NextLogRequest in = init(LOG_NEXT_API_CALL, accessToken, filterBytes, NextLogRequest.class);
            LogSession logSession = LogHelper.getLogSession(accessToken, in.getLogToken());
            String controllerId = logSession.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerId, accessToken).map(p -> p
                    .getGetLog()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            ControllerLogImpl.checkAndGetDBInstances(controllerId);
            LogResponse entity = LogHelper.getNextResponse(logSession, in);

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
            if (in.getTimeout() == null) {
                in.setTimeout(LogHelper.timeout);
            }
            ControllerLogImpl.checkAndGetDBInstances(controllerId);
            LogResponse entity = LogHelper.getNextResponse(logSession, in);

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

}
