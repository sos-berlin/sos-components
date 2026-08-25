package com.sos.joc.classes.logs;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import java.util.regex.Pattern;

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
import com.sos.joc.model.log.JOCLogRequest;
import com.sos.joc.model.log.JOCServiceId;
import com.sos.joc.model.log.KeyedLogRequest;
import com.sos.joc.model.log.LogBaseRequest;
import com.sos.joc.model.log.LogLine;
import com.sos.joc.model.log.LogResponse;
import com.sos.joc.model.log.NextLogRequest;
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
    private static long maxChunkSize = 2500l;
    private static Duration timeout = Duration.ofSeconds(57);
    private static long runningWaitingTime = 2000l; // 2 seconds
//    private static final Logger LOGGER = LoggerFactory.getLogger(LogHelper.class);
    
    public static LogResponse getResponse(JControllerProxy proxy, String accessToken, String controllerId, LogBaseRequest in, Js7ServerId serverId,
            JOCServiceId serviceId, String timezone) throws InterruptedException, ExecutionException {
        Long chunkSize = in.getLimit();
        if (in.getLimit() == null) { // default
            chunkSize = maxChunkSize;
        } else if (in.getLimit() < 0) { //unlimited
            chunkSize = Long.MAX_VALUE - 2l;
        }
        
        LogResponse entity = initLogResponse(timezone);
        ZoneId zoneId = getZoneId(timezone);

        Instant instantFrom = getInstantFromZoneId(in, zoneId, false);
        Optional<Instant> instantTo = Optional.ofNullable(getInstantFromZoneId(in, zoneId, true));
        
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
        
        if (proxy != null) { // Controller/Agent logs

            LogSession ls = new LogSession(controllerId, serverId, logLevel, instantFrom, instantTo, in.getNumOfLines(), zoneId, chunkSize, entity
                    .getLogToken());

            setLogLines(proxy.keyedLogLineFlux(serverId, logLevel, instantFrom, selection), ls, chunk - 1, entity);
            
            LogSessions.addSession(accessToken, entity.getLogToken(), ls);

        } else { // JOC logs
            LogSession ls = new LogSession(serviceId, logLevel, instantFrom, instantTo, in.getNumOfLines(), zoneId, chunkSize, entity.getLogToken());

            JOCLogProxyContext.getJResource().use(logDirectoryIndex -> {
                return logDirectoryIndex.logIndex(serviceId.logPrefix(), logLevel).thenAccept(logIndex -> {
                    setLogLines(logIndex.keyedLogLineFlux(instantFrom, selection), ls, chunk - 1, entity);
                });
            }).get();
            
            LogSessions.addSession(accessToken, entity.getLogToken(), ls);
        }

        return entity;
    }
    
    public static LogResponse getResponse(String accessToken, JOCLogRequest in) throws InterruptedException, ExecutionException {
        return getResponse(null, accessToken, null, in, null, in.getServiceId(), JOCLogProxyContext.zoneId.getId());
    }
    
    public static LogResponse getResponse(JControllerProxy proxy, String accessToken, String controllerId, LogBaseRequest in, Js7ServerId serverId,
            String timezone) throws InterruptedException, ExecutionException {
        return getResponse(proxy, accessToken, controllerId, in, serverId, null, timezone);
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
    
    public static String getJOCDownloadFilename(String serviceId, RequestLevel level, Instant dateFrom,
            Optional<Instant> dateTo, Instant now, OptionalLong numOfLines, boolean compressed) {
        return getDownloadFilename(serviceId + "-", level, dateFrom, dateTo, now, numOfLines, compressed);
    }

    public static String getControllerDownloadFilename(DBItemInventoryJSInstance dbItem, RequestLevel level, Instant dateFrom,
            Optional<Instant> dateTo, Instant now, OptionalLong numOfLines, boolean compressed) {
        return getDownloadFilename(getControllerPrefix(dbItem), level, dateFrom, dateTo, now, numOfLines, compressed);
    }

    public static String getAgentDownloadFilename(SubagentId subagentId, RequestLevel level, Instant dateFrom, Optional<Instant> dateTo, Instant now,
            OptionalLong numOfLines, boolean compressed) {
        return getDownloadFilename(getAgentPrefix(subagentId), level, dateFrom, dateTo, now, numOfLines, compressed);
    }
    
    private static Instant getInstant(LogBaseRequest in, boolean to) {
        Instant instant;
        if (to) {
            if (in.getDateTo() == null) {
                return null;
            }
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateTo()), false, in.getTimeZone());
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, JobSchedulerDate.UTC);
            if (in.getDateTo().contains("y") && !Pattern.compile("[Mwdhms]").asPredicate().test(in.getDateTo())) {
                ldt = ldt.plusYears(1);  
            } else if (in.getDateTo().contains("M") && !Pattern.compile("[wdhms]").asPredicate().test(in.getDateTo())) {
                ldt = ldt.plusMonths(1);
            } else if (in.getDateTo().contains("w") && !Pattern.compile("[dhms]").asPredicate().test(in.getDateTo())) {
                ldt = ldt.plusWeeks(1);
            } else if (in.getDateTo().contains("d") && !Pattern.compile("[hms]").asPredicate().test(in.getDateTo())) {
                ldt = ldt.plusDays(1);
            }
            instant = ldt.toInstant(ZoneOffset.UTC);
        } else {
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateFrom()), false, in.getTimeZone());
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
    
//    private static Predicate<byte[]> dateToIsReached(Optional<Instant> instantTo, ZoneId zoneId) {
//        return logLine -> {
//            if (instantTo.isPresent()) {
//                String first35Chars = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(logLine, 0, Math.min(35, logLine.length))).toString();
//                try {
//                    Instant instant = JobSchedulerDate.getInstantOfZoneId(first35Chars, zoneId);
//                    return instant.isBefore(instantTo.get());
//                } catch (DateTimeException e) {
//                    return true;
//                }
//            }
//            return true;
//        };
//    }
    
    private static Predicate<KeyedLogLine> dateToIsReached(LogSession ls, LogResponse entity, boolean force) {
        return keyedLogLine -> {
            if (ls.getDateTo().isPresent()) {
                try {
                    if (ls.getFinalDateToKey().isPresent()) {
                        if (ls.getFinalDateToKey().get().asString().equals(keyedLogLine.key().asString())) {
                            entity.setDateToReached(true);
                            return force;
                        }
                    } else {
                        Instant instant = JobSchedulerDate.getInstantOfZoneId(keyedLogLine.line().substring(0, 35), ls.getZoneId());
                        if (!instant.isBefore(ls.getDateTo().get())) {
                            entity.setDateToReached(true);
                            ls.setFinalDateToKey(keyedLogLine.key());
                            return force;
                        }
                    }
                } catch (Exception e) {
                    return true;
                }
            }
            return true;
        };
    }
    
    private static Predicate<KeyedLogLine> numOfLinesAreReached(LogSession ls, AtomicBoolean startNextLineCount, LogResponse entity, boolean force) {
        return keyedLogLine -> {
            if (entity.getNumOfLinesReached() == Boolean.TRUE) {
                return force;
            }
            if (ls.getRequestedNumOfLines() != null) {
                if (ls.getFinalNumOfLinesKey().isPresent()) {
                    if (ls.getFinalNumOfLinesKey().get().asString().equals(keyedLogLine.key().asString())) {
                        entity.setNumOfLinesReached(true);
                    }
                } else {
                    if (startNextLineCount.get()) {
                        //long next = nextLinesCounter.incrementAndGet();
                        ls.addResponsedNumOfLines(1l);
                        if (ls.getResponsedNumOfLines() == ls.getRequestedNumOfLines()) {
                            entity.setNumOfLinesReached(true);
                            ls.setFinalNumOfLinesKey(keyedLogLine.key());
                        } else if (ls.getResponsedNumOfLines() > ls.getRequestedNumOfLines()) {
                            return force;
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
    
//    private static Predicate<KeyedLogLine> waitingTimeIsReached(AtomicLong waitingTime, AtomicBoolean waitingTimeReached, boolean running) {
//        return keyedLogLine -> {
//            if (waitingTimeReached.get()) {
//                return false;
//            }
//            if (running) {
//                if (waitingTime.get() == 0) {
//                    waitingTime.set(System.currentTimeMillis());
//                } else {
//                    if (System.currentTimeMillis() - waitingTime.get() > runningWaitingTime) { // longer than 5 seconds
//                        waitingTimeReached.set(true);
//                    }
//                }
//            }
//            return true;
//        };
//    }
    
    public static Predicate<KeyedLogLine> keyIsReached(LogLineKey inKey) {
        return keyedLogLine -> {
            if (inKey.asString().equals(keyedLogLine.key().asString())) {
                return false;
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
    
    private static void setLogLines(Flux<List<KeyedLogLine>> flux, LogSession ls, Long chunk, LogResponse entity) {
        int skipLogLevelFromKey = ls.getLogLevel().toString().length() + 1;
        AtomicLong linesCounter = new AtomicLong(1l);
        AtomicLong chunkLinesCounter = new AtomicLong(0);
        AtomicReference<LogLineKey> lastChunkKey = new AtomicReference<>();
        
        KeyedLogLine lastLine = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())).flatMapIterable(Function.identity()).takeWhile(
                dateToIsReached(ls, entity, false)).doOnNext(keyedLogLine -> {
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
            ls.setCurrentLastLogLineKey(lastKeyOpt);
            entity.setLastLogLineReached(true);
        }
        ls.setLastKey(lastChunkKey.get());
        ls.addResponsedNumOfLines(chunkLinesCounter.get());
        if (ls.getRequestedNumOfLines() != null && chunkLinesCounter.get() == ls.getRequestedNumOfLines()) {
            ls.setFinalNumOfLinesKey(preLastKeyOpt);
            entity.setNumOfLinesReached(true);
        }
    }
    
    private static void setPrevLogLines(Flux<KeyedLogLine> flux, LogSession ls, Long chunk, LogLineKey inKey, LogResponse entity) {
        int skipLogLevelFromKey = ls.getLogLevel().toString().length() + 1;

        AtomicLong linesCounter = new AtomicLong(1l);
        flux.takeWhile(keyIsReached(inKey)).doOnNext(keyedLogLine -> {
            long row = linesCounter.getAndIncrement();
            entity.getLogLines().add(getLogLine(keyedLogLine, skipLogLevelFromKey));
            if (row > chunk) {
                entity.getLogLines().remove(0);
            }
        }).count().block();

        if (!entity.getLogLines().isEmpty()) {
            if (ls.getFirstKey().isPresent() && ls.createLogLineKey(entity.getLogLines().get(0).getKey()).asString().equals(ls.getFirstKey().get()
                    .asString())) {
                entity.setFirstLogLineReached(true);
            }
        }
    }
    
    private static void setNextLogLines(Flux<KeyedLogLine> flux, LogLineKey inKey, LogSession ls, Long chunk, boolean force, boolean exactlyNextChunk,
            boolean running, LogResponse entity) {
        long skip = 1l;
        int skipLogLevelFromKey = ls.getLogLevel().toString().length() + 1;
        AtomicBoolean startNextLineCount = new AtomicBoolean(exactlyNextChunk);
        AtomicLong linesCounter = new AtomicLong(1l);
        AtomicReference<LogLineKey> lastChunkKey = new AtomicReference<>();
        
        Optional<LogLineKey> lastKeyOpt = Optional.empty();
        flux = flux.skip(skip)
                .take(timeout)
                .takeWhile(numOfLinesAreReached(ls, startNextLineCount, entity, force).and(dateToIsReached(ls, entity, force)))
                .doOnNext(keyedLogLine -> {
                    long row = linesCounter.getAndIncrement();
                    if (row < chunk) {
                        lastChunkKey.set(keyedLogLine.key());
                        entity.getLogLines().add(getLogLine(keyedLogLine, skipLogLevelFromKey));
                    }
                });

        if (running) {
            Duration bufferTimeout = Duration.ofMillis(runningWaitingTime);
            KeyedLogLine lastLine = flux.bufferTimeout(chunk.intValue(), bufferTimeout)
                    .takeUntil(l -> !l.isEmpty())
                    .flatMapIterable(Function.identity()).blockLast();
            lastKeyOpt = Optional.ofNullable(lastLine).map(KeyedLogLine::key);
        } else {
            KeyedLogLine lastLine = flux.blockLast();
            lastKeyOpt = Optional.ofNullable(lastLine).map(KeyedLogLine::key);
        }

        Optional<LogLineKey> preLastKeyOpt = Optional.ofNullable(lastChunkKey.get());
        if ((force || (entity.getDateToReached() != Boolean.TRUE && entity.getNumOfLinesReached() != Boolean.TRUE)) && preLastKeyOpt.isPresent()
                && lastKeyOpt.map(LogLineKey::asString).equals(preLastKeyOpt.map(LogLineKey::asString))) {
            ls.setCurrentLastLogLineKey(lastKeyOpt);
            entity.setLastLogLineReached(true);
        } else if (lastKeyOpt.isEmpty() && ls.getCurrentLastLogLineKey().isPresent() && isAfterOrEqual(inKey, ls.getCurrentLastLogLineKey().get())) {
            entity.setLastLogLineReached(true);
        }
        ls.setLastKey(lastChunkKey.get());
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
    
    public static LogResponse getNextResponse(LogSession logSession, NextLogRequest in) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException, InterruptedException {
        return getNextResponse(logSession, in, false);
    }

    public static LogResponse getNextResponse(LogSession logSession, NextLogRequest in, boolean running)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException, InterruptedException {

        LogResponse entity = initLogResponse(logSession.getZoneId().getId());
        entity.setLogToken(in.getLogToken());
        
        boolean forced = in.getForce() == Boolean.TRUE;
        Long chunk = logSession.getNextChunkSize(in.getLimit(), forced);
        LogLineKey inKey = logSession.createLogLineKey(in.getKey());
        boolean endReached = false;
        
        if (logSession.getFinalNumOfLinesKey().isPresent() && isAfterOrEqual(inKey, logSession.getFinalNumOfLinesKey().get())) {
            entity.setNumOfLinesReached(true);
            endReached = true;
        } else if (logSession.getFinalDateToKey().isPresent() && isAfterOrEqual(inKey, logSession.getFinalDateToKey().get())) {
            entity.setDateToReached(true);
            endReached = true;
        }
        if (forced || !endReached) {
            boolean exactlyNextChunk = logSession.getLastKey().isPresent() && inKey.asString().equals(logSession.getLastKey().get().asString());
            JLogSelection selection = JLogSelection.empty().withLineLimit(chunk + 1l).withGrowing(running);
            
            if (logSession.getControllerId() != null) { // Controller/Agent logs
                JControllerProxy proxy = Proxy.of(logSession.getControllerId());
                setNextLogLines(logSession.getNextLogLinesFlux(proxy, selection, inKey), inKey, logSession, chunk, forced, exactlyNextChunk,
                        running, entity);
            } else { // JOC logs
//                logSession.getResource().use(logDirectoryIndex -> {
//                    return logDirectoryIndex.logIndex(logSession.getServiceId().logPrefix(), logSession.getLogLevel()).thenAccept(logIndex -> {
//                        setNextLogLines(logIndex.keyedLogLineFlux(inKey, selection).publishOn(Schedulers.fromExecutor(ForkJoinPool
//                                .commonPool())).flatMapIterable(Function.identity()), inKey, logSession, chunk, forced, exactlyNextChunk,
//                                running, entity);
//                    });
//                }).get();
                
                logSession.getNextLogLinesFlux(flux -> setNextLogLines(flux, inKey, logSession, chunk, forced, exactlyNextChunk,
                        running, entity), selection, inKey).get();
            }
        }

        return entity;
    }

    public static LogResponse getRunningResponse(LogSession logSession, NextLogRequest in) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException, InterruptedException {
        return getNextResponse(logSession, in, true);
    }
    
    public static LogResponse getPrevResponse(LogSession logSession, KeyedLogRequest in) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException, InterruptedException {

        LogResponse entity = initLogResponse(logSession.getZoneId().getId());
        entity.setLogToken(in.getLogToken());

        Long chunk = logSession.getPrevChunkSize(in.getLimit());
        LogLineKey inKey = logSession.createLogLineKey(in.getKey());

        if (logSession.getFirstKey().isPresent() && isBeforeOrEqual(inKey, logSession.getFirstKey().get())) {
            entity.setFirstLogLineReached(true);
        } else {
            if (logSession.getControllerId() != null) { // Controller/Agent logs
                JControllerProxy proxy = Proxy.of(logSession.getControllerId());
                setPrevLogLines(logSession.getPrevLogLinesFlux(proxy, JLogSelection.empty(), logSession.getDateFrom()), logSession, chunk, inKey,
                        entity);
            } else { // JOC logs
//                logSession.getResource().use(logDirectoryIndex -> {
//                    return logDirectoryIndex.logIndex(logSession.getServiceId().logPrefix(), logSession.getLogLevel()).thenAccept(logIndex -> {
//                        setPrevLogLines(logIndex.keyedLogLineFlux(logSession.getDateFrom(), JLogSelection.empty()).publishOn(Schedulers.fromExecutor(
//                                ForkJoinPool.commonPool())).flatMapIterable(Function.identity()), logSession, chunk, inKey, entity);
//                    });
//                }).get();

                logSession.getPrevLogLinesFlux(flux -> setPrevLogLines(flux, logSession, chunk, inKey, entity), JLogSelection.empty(), logSession
                        .getDateFrom()).get();
            }
        }
        return entity;
    }
    
    private static boolean isBeforeOrEqual(LogLineKey llk1, LogLineKey llk2) {
        if (llk1.fileInstant().isBefore(llk2.fileInstant())) {
            return true;
        } else if (!llk1.fileInstant().isAfter(llk2.fileInstant())) {
            if (llk1.position() <= llk2.position()) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isAfterOrEqual(LogLineKey llk1, LogLineKey llk2) {
        if (llk1.fileInstant().isAfter(llk2.fileInstant())) {
            return true;
        } else if (!llk1.fileInstant().isBefore(llk2.fileInstant())) {
            if (llk1.position() >= llk2.position()) {
                return true;
            }
        }
        return false;
    }
    
    private static LogResponse initLogResponse(String timezone) {
        LogResponse entity = new LogResponse();
        entity.setTimeZone(timezone);
        entity.setLogLines(new ArrayList<>());
        entity.setDateToReached(null);
        entity.setNumOfLinesReached(null);
        entity.setFirstLogLineReached(null);
        entity.setLastLogLineReached(null);
        return entity;
    }
    
}
