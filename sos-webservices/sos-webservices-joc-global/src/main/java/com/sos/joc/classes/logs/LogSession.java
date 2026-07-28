package com.sos.joc.classes.logs;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import com.sos.joc.model.log.JOCServiceId;

import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;
import js7.base.log.reader.LogLineKey;
import js7.data.node.Js7ServerId;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JResource;
import js7.proxy.javaapi.log.JLogDirectoryIndex;
import js7.proxy.javaapi.log.JLogSelection;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class LogSession {

    private final String controllerId;
    private final Js7ServerId serverId;
    private final JOCServiceId serviceId;
    private final LogLevel logLevel;
    private final Instant dateFrom;
    private final Optional<Instant> dateTo;
    private final Long requestedNumOfLines;
    private final Long chunkSize;
    private long responsedNumOfLines = 0;
    private final ZoneId zoneId;
    private Optional<LogLineKey> lastKey = Optional.empty();
    private Optional<LogLineKey> firstKey = Optional.empty();
    private Optional<LogLineKey> finalNumOfLinesKey = Optional.empty();
    private Optional<LogLineKey> finalDateToKey = Optional.empty();
    
    private final String token;
    
    public LogSession(String controllerId, Js7ServerId serverId, LogLevel logLevel, Instant dateFrom, Optional<Instant> dateTo,
            Long requestedNumOfLines, ZoneId zoneId, Long chunkSize, String token) {
        this.controllerId = controllerId;
        this.serverId = serverId;
        this.logLevel = logLevel;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.requestedNumOfLines = requestedNumOfLines;
        this.chunkSize = chunkSize;
        this.zoneId = zoneId;
        this.token = token;
        this.serviceId = null;
    }
    
    public LogSession(JOCServiceId serviceId, LogLevel logLevel, Instant dateFrom, Optional<Instant> dateTo, Long requestedNumOfLines, ZoneId zoneId,
            Long chunkSize, String token) {
        this.controllerId = null;
        this.serverId = null;
        this.logLevel = logLevel;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.requestedNumOfLines = requestedNumOfLines;
        this.chunkSize = chunkSize;
        this.zoneId = zoneId;
        this.token = token;
        this.serviceId = serviceId;
    }

    public String getControllerId() {
        return controllerId;
    }

//    public Js7ServerId getServerId() {
//        return serverId;
//    }

    public JOCServiceId getServiceId() {
        return serviceId;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public Instant getDateFrom() {
        return dateFrom;
    }

    public Optional<Instant> getDateTo() {
        return dateTo;
    }

    public Long getRequestedNumOfLines() {
        return requestedNumOfLines;
    }
    
    public long getResponsedNumOfLines() {
        return responsedNumOfLines;
    }
    
    public void addResponsedNumOfLines(long responsedNumOfLines) {
        if (requestedNumOfLines != null && responsedNumOfLines > 0) {
            this.responsedNumOfLines += responsedNumOfLines;
        }
    }
    
    public Long getNextChunkSize(Long runningChunkSize, boolean force) {
        //+1 because first line is skipped later (otherwise line is double)
        Long rChunkSize = runningChunkSize;
        if (runningChunkSize == null) {
            rChunkSize = chunkSize;
        } else if (runningChunkSize < 0) {
            rChunkSize = Long.MAX_VALUE - 2l;
        }
        //first is skipped, otherwise double -> + 1l
        if (requestedNumOfLines == null || force) {
           return rChunkSize + 1l; 
        }
        return Math.min(requestedNumOfLines, rChunkSize) + 1l;
    }
    
    public Long getPrevChunkSize(Long runningChunkSize) {
        Long rChunkSize = runningChunkSize;
        if (runningChunkSize == null) {
            rChunkSize = chunkSize;
        } else if (runningChunkSize < 0) {
            rChunkSize = Long.MAX_VALUE - 2l;
        }
        return rChunkSize; 
    }

    public String getToken() {
        return token;
    }
    
    public Optional<LogLineKey> getFirstKey() {
        return firstKey;
    }
    
    public void setFirstKey(LogLineKey key) {
        if (key != null && firstKey.isEmpty()) {
            this.firstKey = Optional.of(key);
        }
    }
    
    public Optional<LogLineKey> getLastKey() {
        return lastKey;
    }
    
    public void setLastKey(LogLineKey key) {
        if (requestedNumOfLines != null && key != null) {
            if (lastKey.isEmpty()) {
                this.lastKey = Optional.of(key);
            } else {
                if (key.fileInstant().isAfter(lastKey.get().fileInstant())) {
                    this.lastKey = Optional.of(key);
                } else if (!key.fileInstant().isBefore(lastKey.get().fileInstant())) {
                    if (key.position() > lastKey.get().position()) {
                        this.lastKey = Optional.of(key);
                    }
                }
            }
        }
    }
    
    public Optional<LogLineKey> getFinalNumOfLinesKey() {
        return finalNumOfLinesKey;
    }
    
    public void setFinalNumOfLinesKey(LogLineKey key) {
        if (key != null) {
            this.finalNumOfLinesKey = Optional.of(key);
        }
    }
    
    public void setFinalNumOfLinesKey(Optional<LogLineKey> key) {
        if (key.isPresent()) {
            this.finalNumOfLinesKey = key;
        }
    }
    
    public Optional<LogLineKey> getFinalDateToKey() {
        return finalDateToKey;
    }
    
    public void setFinalDateToKey(LogLineKey key) {
        if (key != null) {
            this.finalDateToKey = Optional.of(key);
        }
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
    
    public JResource<JLogDirectoryIndex> getResource() {
        return JOCLogProxyContext.getJResource(serviceId.logPrefix(), logLevel);
    }
    
    public Flux<KeyedLogLine> getNextLogLinesFlux(JControllerProxy proxy, JLogSelection selection, LogLineKey key) {
        return proxy.keyedLogLineFlux(serverId, logLevel, key, selection).publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))
                .flatMapIterable(Function.identity());
    }

    // It's a fake until proxy has a better API
    public Flux<KeyedLogLine> getPrevLogLinesFlux(JControllerProxy proxy, JLogSelection selection, Instant instantFrom) {
        return proxy.keyedLogLineFlux(serverId, logLevel, instantFrom, selection).publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))
                .flatMapIterable(Function.identity());
    }

    public Long getChunkSize() {
        return chunkSize;
    }
    
    public LogLineKey createLogLineKey(String key) {
        return LogLineKey.parse(logLevel.toString() + "/" + key).toOption().get();
    }
}
