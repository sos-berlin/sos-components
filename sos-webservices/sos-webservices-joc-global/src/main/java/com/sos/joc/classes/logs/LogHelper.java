package com.sos.joc.classes.logs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.log.KeyedLogRequest;
import com.sos.joc.model.log.LogBaseRequest;
import com.sos.joc.model.log.LogLine;
import com.sos.joc.model.log.LogResponse;
import com.sos.joc.model.log.RequestLevel;

import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;
import js7.base.log.reader.LogLineKey;
import js7.data.node.Js7ServerId;
import js7.data.subagent.SubagentId;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.log.JLogSelection;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class LogHelper {
    
    private static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss,SSS").withZone(ZoneId.of("UTC"));
    private static long maxChunkSize = 250L;
//    private static final Logger LOGGER = LoggerFactory.getLogger(LogHelper.class);
    
    public static LogResponse getResponse(JControllerProxy proxy, String accessToken, LogBaseRequest in, Js7ServerId serverId, String timezone) {
        Long chunkSize = in.getLimit();
        if (in.getLimit() == null) { // default
            chunkSize = maxChunkSize;
        } else if (in.getLimit() < 0) { //unlimited
            chunkSize = Long.MAX_VALUE - 2l;
        }
        
        LogResponse entity = new LogResponse();
        entity.setTimeZone(timezone);
        ZoneId zoneId = getZoneId(entity.getTimeZone());

        entity.setLogLines(new ArrayList<>());
        entity.setDateToReached(null);
        entity.setNumOfLinesReached(null);
        entity.setFirstLogLineReached(null);
        entity.setLastLogLineReached(null);

        Instant instantFrom = getInstantFromZoneId(in, zoneId, false);
        Optional<Instant> utcInstantTo = Optional.ofNullable(getInstant(in, true));
        Optional<Instant> instantTo = utcInstantTo.map(to -> JobSchedulerDate.getInstantFromZoneId(to, zoneId));
        // Optional.ofNullable(getInstantFromZoneId(in, zoneId, true));
        
        boolean instantToIsInFuture = utcInstantTo.filter(Instant.now()::isBefore).isPresent();
        
        instantTo.ifPresent(to -> { 
            if (!to.isAfter(instantFrom)) {
                throw new JocBadRequestException("'date to' is not after 'date from'");
            }
        });

        Long chunk = in.getNumOfLines() != null ? Math.min(in.getNumOfLines(), chunkSize) + 1l : chunkSize + 1l;
        LogLevel logLevel = getLogLevel(in.getLevel());

        // consider lastLine is null if flux stream is empty
        JLogSelection selection = JLogSelection.empty().withLineLimit(chunk);//.withEnd(instantTo);
        
        entity.setLogToken(UUID.randomUUID().toString());
        LogSession ls = new LogSession(in.getControllerId(), serverId, logLevel, instantFrom, instantTo, in.getNumOfLines(), zoneId, chunkSize,
                !instantToIsInFuture, entity.getLogToken());

        setLogLines(proxy.keyedLogLineFlux(serverId, logLevel, instantFrom, selection), ls, logLevel,
                instantTo, zoneId, in.getNumOfLines(), chunk - 1, entity);

        LogSessions.addSession(accessToken, entity.getLogToken(), ls);
        return entity;
    }
    
    public static LogLevel getLogLevel(RequestLevel level) {
        if (level == null) {
            return LogLevel.info();
        }
        switch (level) {
        case ERROR:
        case WARN:
            return LogLevel.error();
        case DEBUG:
            return LogLevel.debug();
        default:
            return LogLevel.info();
        }
    }

    public static String getControllerDownloadFilename(DBItemInventoryJSInstance dbItem, RequestLevel level, Instant dateFrom,
            Optional<Instant> dateTo, Instant now, OptionalLong numOfLines, boolean compressed) {
        return getDownloadFilename(getControllerPrefix(dbItem), level, dateFrom, dateTo, now, numOfLines, compressed);
    }

    public static String getAgentDownloadFilename(SubagentId subagentId, RequestLevel level, Instant dateFrom, Optional<Instant> dateTo, Instant now,
            OptionalLong numOfLines, boolean compressed) {
        return getDownloadFilename(getAgentPrefix(subagentId), level, dateFrom, dateTo, now, numOfLines, compressed);
    }
    
    public static Instant getInstant(LogBaseRequest in, boolean to) {
        Instant instant;
        if (to) {
            if (in.getDateTo() == null) {
                return null;
            }
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateTo()), to, in.getTimeZone());
        } else {
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateFrom()), to, in.getTimeZone());
            Instant now = Instant.now();
            if (now.isBefore(instant)) {
                instant = now;
            }
        }
        return instant;
    }
    
    public static Instant getInstantFromZoneId(LogBaseRequest in, ZoneId zoneId, boolean to) {
        Instant instant = getInstant(in, to);
        return JobSchedulerDate.getInstantFromZoneId(instant, zoneId);
    }
    
    public static Predicate<byte[]> dateToIsReached(Optional<Instant> instantTo, ZoneId zoneId) {
        return logLine -> {
            if (instantTo.isPresent()) {
                String first35Chars = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(logLine, 0, Math.min(35, logLine.length))).toString();
                try {
                    Instant instant = JobSchedulerDate.getInstantOfZoneId(first35Chars, zoneId);
                    return instant.isBefore(instantTo.get());
                } catch (DateTimeException e) {
                    return true;
                }
            }
            return true;
        };
    }
    
    public static Predicate<KeyedLogLine> dateToIsReached(Optional<Instant> instantTo, ZoneId zoneId, LogResponse entity) {
        return keyedLogLine -> {
            if (instantTo.isPresent()) {
                try {
                    Instant instant = JobSchedulerDate.getInstantOfZoneId(keyedLogLine.line().substring(0, 35), zoneId);
                    entity.setDateToReached(!instant.isBefore(instantTo.get()));
                    return !entity.getDateToReached();
                } catch (DateTimeException e) {
                    return true;
                }
            }
            return true;
        };
    }
    
    public static Predicate<KeyedLogLine> numOfLinesIsReached(LogSession ls, AtomicBoolean startNextLineCount, AtomicLong nextLinesCounter,
            LogResponse entity) {
        return keyedLogLine -> {
            if (entity.getNumOfLinesReached() == Boolean.TRUE) {
                return false;
            }
            if (ls.getRequestedNumOfLines() != null) {
                if (ls.getFinalKey().isPresent()) {
                    if (ls.getFinalKey().get().asString().equals(keyedLogLine.key().asString())) {
                        entity.setNumOfLinesReached(true);
                    }
                } else {
                    if (startNextLineCount.get()) {
                        long next = nextLinesCounter.incrementAndGet();
                        if (ls.getResponsedNumOfLines() + next >= ls.getRequestedNumOfLines()) {
                            entity.setNumOfLinesReached(true);
                            ls.setFinalKey(keyedLogLine.key());
                        }
                    }
                    if (!startNextLineCount.get() && ls.getLastKey().isPresent() && keyedLogLine.key().asString().equals(ls.getLastKey().get()
                            .asString())) {
                        startNextLineCount.set(true);
                    }
                }
            }
            return true;
        };
    }
    
    public static String formattedTimeStamp(Instant instant) {
        return dateTimeFormat.format(instant);
    }

    private static String getDownloadFilename(String productPrefix, RequestLevel level, Instant dateFrom, Optional<Instant> dateTo, Instant now,
            OptionalLong numOfLines, boolean compressed) {
        String dtToOrLines = getDateToOrLines(dateTo, now, numOfLines);
        // length: prefix + 15 + <=15 + <=6 + 4 + <=3 --> max. prefix + 43 (<=162)
        return String.format("%s%s%s%s.log%s", productPrefix, getFirst14Digits(dateFrom), dtToOrLines, getLogLevelSuffix(level), compressed ? ".gz"
                : "");
    }

    private static String getControllerPrefix(DBItemInventoryJSInstance dbItem) {
        String serverRoleSuffix = !dbItem.getIsCluster() ? "" : (dbItem.getIsPrimary() ? "-primary" : "-backup");
        // length: <=100 + <=8 --> max. 119
        return String.format("%s%s-", dbItem.getControllerId(), serverRoleSuffix);
    }

    private static String getAgentPrefix(final SubagentId subagentId) {
        // truncate AgentId!!! because of filename length
        // length: <=100
        String saId = subagentId.string();
        return String.format("%s-", (saId.length() > 100) ? saId.substring(0, 100) : saId);
    }

    private static String getDateToOrLines(Optional<Instant> dateTo, Instant now, OptionalLong numOfLines) {
        Optional<String> dtTo = dateTo.map(LogHelper::getFirst14Digits);
        String dtToOrLines = "";
        if (dtTo.isPresent()) {
            dtToOrLines = "-" + dtTo.get();
        } else if (numOfLines.isPresent()) {
            dtToOrLines = "-l" + numOfLines.getAsLong();
        } else {
            dtToOrLines = "-" + getFirst14Digits(now);
        }
        return dtToOrLines;
    }

    private static String getLogLevelSuffix(RequestLevel level) {
        return Optional.ofNullable(level).filter(l -> !l.equals(RequestLevel.INFO)).map(RequestLevel::value).map(String::toLowerCase).map(s -> "-"
                + s).orElse("");
    }

    private static String getFirst14Digits(Instant instant) {
        return instant.toString().replaceAll("[^0-9]", "").substring(0, 14);
    }
    
    private static void setLogLines(Flux<List<KeyedLogLine>> flux, LogSession ls, LogLevel logLevel, Optional<Instant> dateTo,
            ZoneId zoneId, Long requestedNumOfLines, Long chunk, LogResponse entity) {
        int skipLogLevelFromKey = logLevel.toString().length() + 1;
        AtomicLong linesCounter = new AtomicLong(1l);
        AtomicLong chunkLinesCounter = new AtomicLong(0);
        AtomicReference<LogLineKey> lastChunkKey = new AtomicReference<>();
        
        KeyedLogLine lastLine = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())).flatMapIterable(Function.identity())
                .takeWhile(dateToIsReached(dateTo, zoneId, entity)).doOnNext(keyedLogLine -> {
                    long row = linesCounter.getAndIncrement();
                    if (row == 1l) {
                        ls.setFirstKey(keyedLogLine.key());
                    }
                    if (row <= chunk) {
                        lastChunkKey.set(keyedLogLine.key());
                        entity.getLogLines().add(getLogLine(keyedLogLine, skipLogLevelFromKey));
                        chunkLinesCounter.incrementAndGet();
                    }
                }).blockLast();
        
        Optional<LogLineKey> lastKeyOpt = Optional.ofNullable(lastLine).map(KeyedLogLine::key);
        Optional<LogLineKey> preLastKeyOpt = Optional.ofNullable(lastChunkKey.get());
        if (entity.getDateToReached() != Boolean.TRUE && entity.getNumOfLinesReached() != Boolean.TRUE && preLastKeyOpt.isPresent() && lastKeyOpt.map(
                LogLineKey::asString).equals(preLastKeyOpt.map(LogLineKey::asString))) {
            entity.setLastLogLineReached(true);
        }
        ls.setLastKey(lastChunkKey.get());
        ls.addResponsedNumOfLines(chunkLinesCounter.get());
        if (requestedNumOfLines != null && chunkLinesCounter.get() == requestedNumOfLines) {
            entity.setNumOfLinesReached(true);
        }
        if (entity.getDateToReached() != Boolean.TRUE) {
            entity.setDateToReached(null);
        }
    }
    
    private static void setNextLogLines(Flux<List<KeyedLogLine>> flux, LogSession ls, Long chunk, boolean exactlyNextChunk, LogResponse entity) {
        Optional<Instant> dateTo = ls.getDateTo();
        ZoneId zoneId = ls.getZoneId();
        long skip = 1l;
        int skipLogLevelFromKey = ls.getLogLevel().toString().length() + 1;
        AtomicBoolean startNextLineCount = new AtomicBoolean(exactlyNextChunk);
        AtomicLong linesCounter = new AtomicLong(1l);
        AtomicLong nextLinesCounter = new AtomicLong(0);
        AtomicLong chunkLinesCounter = new AtomicLong(0);
        AtomicReference<LogLineKey> lastChunkKey = new AtomicReference<>();
        KeyedLogLine lastLine = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())).flatMapIterable(Function.identity()).skip(skip)
                .takeWhile(numOfLinesIsReached(ls, startNextLineCount, nextLinesCounter, entity).and(dateToIsReached(dateTo, zoneId, entity)))
                .doOnNext(keyedLogLine -> {
                    long row = linesCounter.getAndIncrement();
                    if (row < chunk) {
                        lastChunkKey.set(keyedLogLine.key());
                        entity.getLogLines().add(getLogLine(keyedLogLine, skipLogLevelFromKey));
                        chunkLinesCounter.incrementAndGet();
                    }
                }).blockLast();

        Optional<LogLineKey> lastKeyOpt = Optional.ofNullable(lastLine).map(KeyedLogLine::key);
        Optional<LogLineKey> preLastKeyOpt = Optional.ofNullable(lastChunkKey.get());
        if (entity.getDateToReached() != Boolean.TRUE && entity.getNumOfLinesReached() != Boolean.TRUE && preLastKeyOpt.isPresent() && lastKeyOpt.map(
                LogLineKey::asString).equals(preLastKeyOpt.map(LogLineKey::asString))) {
            entity.setLastLogLineReached(true);
            ls.addResponsedNumOfLines(nextLinesCounter.get());
        } else {
            ls.addResponsedNumOfLines(nextLinesCounter.get() - 1l);
        }
        ls.setLastKey(lastChunkKey.get());
        if (entity.getDateToReached() != Boolean.TRUE) {
            entity.setDateToReached(null);
        }
    }
    
    private static LogLine getLogLine(KeyedLogLine keyedLogLine, int skipLogLevelFromKey) {
        LogLine line = new LogLine();
        line.setLine(keyedLogLine.line());
        line.setKey(keyedLogLine.key().asString().substring(skipLogLevelFromKey));
        return line;
    }
    
    public static ZoneId getZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }
    
    public static LogSession getLogSession(String accessToken, String token) {
        LogSession logSession = LogSessions.getInstance().getSession(accessToken, token);
        if (logSession == null) {
            throw new JocBadRequestException("Log session not available. Start a new log request!");
        }
        return logSession;
    }

    public static LogResponse getNextResponse(LogSession logSession, KeyedLogRequest in) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {

        JControllerProxy proxy = Proxy.of(logSession.getControllerId());

        ZoneId zoneId = logSession.getZoneId();
        LogResponse entity = new LogResponse();
        entity.setTimeZone(zoneId.getId());
        entity.setLogLines(new ArrayList<>());
        entity.setLogToken(in.getLogToken());
        entity.setDateToReached(null);
        entity.setNumOfLinesReached(null);
        entity.setFirstLogLineReached(null);
        entity.setLastLogLineReached(null);
        
        Long chunk = logSession.getNewRequestedNumOfLines(in.getLimit());
        LogLineKey inKey = logSession.createLogLineKey(in.getKey());
        boolean exactlyNextChunk = logSession.getLastKey().isPresent() && inKey.asString().equals(logSession.getLastKey().get().asString());

        JLogSelection selection = JLogSelection.empty().withLineLimit(chunk + 1l);
        setNextLogLines(logSession.getLogLineFlux(proxy, selection, inKey), logSession, chunk, exactlyNextChunk, entity);

        return entity;
    }
    
}
