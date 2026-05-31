package com.sos.joc.classes.logs;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

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
    private int responsedNumOfLines = 0;
    private final ZoneId zoneId;
    private final boolean instantToIsInPast;
    private Optional<LogLineKey> key = Optional.empty();
    
    private final String token;

    public LogSession(String controllerId, Js7ServerId serverId, LogLevel logLevel, Instant dateFrom, Optional<Instant> dateTo,
            Long requestedNumOfLines, int responsedNumOfLines, ZoneId zoneId, Long chunkSize, Optional<LogLineKey> key, boolean instantToIsInPast,
            String token) {
        this.controllerId = controllerId;
        this.serverId = serverId;
        this.logLevel = logLevel;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.requestedNumOfLines = requestedNumOfLines;
        this.responsedNumOfLines = responsedNumOfLines;
        this.chunkSize = chunkSize;
        this.zoneId = zoneId;
        this.instantToIsInPast = instantToIsInPast;
        this.key = key;
        this.token = token;
    }

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
    
    public int getResponsedNumOfLines() {
        return responsedNumOfLines;
    }
    
    public void addResponsedNumOfLines(int responsedNumOfLines) {
        this.responsedNumOfLines += responsedNumOfLines;
    }
    
    public OptionalLong getNewRequestedNumOfLines() {
        if (requestedNumOfLines == null) {
           return OptionalLong.of(chunkSize); 
        }
        Long rest = requestedNumOfLines - responsedNumOfLines;
        if (rest <= 0) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(Math.min(rest, chunkSize));
    }

    public String getToken() {
        return token;
    }
    
    public Optional<LogLineKey> getKey() {
        return key;
    }
    
    public void setKey(LogLineKey key) {
        if (key != null) {
            this.key = Optional.of(key);
        }
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public boolean isInstantToIsInPast() {
        return instantToIsInPast;
    }
    
    public Flux<List<KeyedLogLine>> getLogLineFlux(JControllerProxy proxy, JLogSelection selection) {
        if (key.isPresent()) {
            return proxy.keyedLogLineFlux(serverId, logLevel, key.get(), selection);
        } else {
            return proxy.keyedLogLineFlux(serverId, logLevel, dateFrom, selection);
        }
    }

    public Long getChunkSize() {
        return chunkSize;
    }
}
