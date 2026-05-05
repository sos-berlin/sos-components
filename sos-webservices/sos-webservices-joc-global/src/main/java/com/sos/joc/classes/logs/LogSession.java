package com.sos.joc.classes.logs;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.OptionalLong;

import js7.base.log.LogLevel;
import js7.base.log.reader.LogLineKey;
import js7.data.node.Js7ServerId;

public class LogSession {

    private final String controllerId;
    private final Js7ServerId serverId;
    private final LogLevel logLevel;
    private final Instant dateFrom;
    private final Optional<Instant> dateTo;
    private final OptionalLong numOfLines;
    private final ZoneId zoneId;
    private final LogLineKey key;
    private final String token;

    public LogSession(String controllerId, Js7ServerId serverId, LogLevel logLevel, Instant dateFrom, Optional<Instant> dateTo, OptionalLong numOfLines, ZoneId zoneId,
            LogLineKey key, String token) {
        this.controllerId = controllerId;
        this.serverId = serverId;
        this.logLevel = logLevel;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.numOfLines = numOfLines;
        this.zoneId = zoneId;
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

    public OptionalLong getNumOfLines() {
        return numOfLines;
    }

    public String getToken() {
        return token;
    }
    
    public LogLineKey getKey() {
        return key;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}
