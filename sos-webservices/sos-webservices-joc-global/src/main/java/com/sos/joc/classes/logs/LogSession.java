package com.sos.joc.classes.logs;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;
import js7.base.log.reader.LogLineKey;
import js7.data.node.Js7ServerId;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.log.JLogSelection;
import reactor.core.publisher.Flux;

public class LogSession {

    private final String controllerId;
    private final Js7ServerId serverId;
    private final LogLevel logLevel;
    private final Instant dateFrom;
    private final Optional<Instant> dateTo;
    private final Long requestedNumOfLines;
    private final Long chunkSize;
    private long responsedNumOfLines = 0;
    private final ZoneId zoneId;
    private final boolean instantToIsInPast;
    private Optional<LogLineKey> lastKey = Optional.empty();
    private Optional<LogLineKey> firstKey = Optional.empty();
    private Optional<LogLineKey> finalKey = Optional.empty();
    
    private final String token;
    
    public LogSession(String controllerId, Js7ServerId serverId, LogLevel logLevel, Instant dateFrom, Optional<Instant> dateTo,
            Long requestedNumOfLines, ZoneId zoneId, Long chunkSize, boolean instantToIsInPast, String token) {
        this.controllerId = controllerId;
        this.serverId = serverId;
        this.logLevel = logLevel;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.requestedNumOfLines = requestedNumOfLines;
        this.chunkSize = chunkSize;
        this.zoneId = zoneId;
        this.instantToIsInPast = instantToIsInPast;
        this.token = token;
    }

//    public LogSession(String controllerId, Js7ServerId serverId, LogLevel logLevel, Instant dateFrom, Optional<Instant> dateTo,
//            Long requestedNumOfLines, long responsedNumOfLines, ZoneId zoneId, Long chunkSize, List<Optional<LogLineKey>> keyedLogLines,
//            boolean instantToIsInPast, String token) {
//        this.controllerId = controllerId;
//        this.serverId = serverId;
//        this.logLevel = logLevel;
//        this.dateFrom = dateFrom;
//        this.dateTo = dateTo;
//        this.requestedNumOfLines = requestedNumOfLines;
//        this.responsedNumOfLines = responsedNumOfLines;
//        this.chunkSize = chunkSize;
//        this.zoneId = zoneId;
//        this.instantToIsInPast = instantToIsInPast;
//        this.firstKey = keyedLogLines.get(0);
//        this.lastKey = keyedLogLines.get(1);
//        this.token = token;
//    }

    public String getControllerId() {
        return controllerId;
    }

    public Js7ServerId getServerId() {
        return serverId;
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
        this.responsedNumOfLines += responsedNumOfLines;
    }
    
    public Long getNewRequestedNumOfLines(Long runningChunkSize) {
        //+1 because first line is skipped later (otherwise line is double)
        Long rChunkSize = runningChunkSize;
        if (runningChunkSize == null) {
            rChunkSize = chunkSize;
        } else if (runningChunkSize < 0) {
            rChunkSize = Long.MAX_VALUE - 2l;
        }
        //first is skipped, otherwise double -> + 1l
        if (requestedNumOfLines == null) {
           return rChunkSize + 1l; 
        }
        return Math.min(requestedNumOfLines, rChunkSize) + 1l;
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
        if (key != null) {
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
    
    public Optional<LogLineKey> getFinalKey() {
        return finalKey;
    }
    
    public void setFinalKey(LogLineKey key) {
        if (key != null) {
            this.finalKey = Optional.of(key);
        }
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public boolean isInstantToIsInPast() {
        return instantToIsInPast;
    }
    
    public Flux<List<KeyedLogLine>> getLogLineFlux(JControllerProxy proxy, JLogSelection selection, String key) {
        return proxy.keyedLogLineFlux(serverId, logLevel, createLogLineKey(key), selection);
    }
    
    public Flux<List<KeyedLogLine>> getLogLineFlux(JControllerProxy proxy, JLogSelection selection, LogLineKey key) {
        return proxy.keyedLogLineFlux(serverId, logLevel, key, selection);
    }

    public Long getChunkSize() {
        return chunkSize;
    }
    
    public LogLineKey createLogLineKey(String key) {
        return LogLineKey.parse(logLevel.toString() + "/" + key).toOption().get();
    }
}
