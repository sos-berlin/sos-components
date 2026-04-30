package com.sos.joc.classes.logs;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

import js7.base.log.LogLevel;
import js7.data.node.Js7ServerId;

public class LogSession {

    private final Js7ServerId serverId;
    private final LogLevel logLevel;
    private final Instant dateFrom;
    private final Optional<Instant> dateTo;
    private final OptionalLong lines;
    private final String token;

    public LogSession(Js7ServerId serverId, LogLevel logLevel, Instant dateFrom, Optional<Instant> dateTo, OptionalLong lines, String token) {
        this.serverId = serverId;
        this.logLevel = logLevel;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.lines = lines;
        this.token = token;
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

    public OptionalLong getLines() {
        return lines;
    }

    public String getToken() {
        return token;
    }
}
