package com.sos.js7.converter.commons.config.items;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobConfig extends AConfigItem {

    private static final String CONFIG_KEY = "jobConfig";

    private String forcedJitlLogLevel;
    private Integer forcedGraceTimeout;
    private Integer forcedParallelism;
    private Boolean forcedFailOnErrWritten;
    private Boolean forcedWarnOnErrWritten;
    private Boolean forcedV1Compatible;

    private String defaultUnixShebang = "#!/bin/bash";
    private String forcedUnixNewLine = "\n";
    private String forcedUnixPowershellShebang = "#!/usr/bin/env pwsh";
    private String forcedUnixCommandPrefix;

    private String forcedWindowsNewLine = "\n"; // "\r\n";
    private String forcedWindowsPowershellShebang = "@@findstr/v \"^@@f.*&\" \"%~f0\"|pwsh.exe -&goto:eof";
    private String forcedWindowsCommandPrefix;

    private String notificationMailDefaultTo;
    private String notificationMailDefaultCc;
    private String notificationMailDefaultBcc;

    private Integer forcedRetryMaxTries;
    private List<Integer> forcedRetryDelays;

    public JobConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        // FORCED
        case "forced.gracetimeout":
            withForcedGraceTimeout(Integer.parseInt(val));
            break;
        case "forced.parallelism":
            withForcedParallelism(Integer.parseInt(val));
            break;
        case "forced.failonerrwritten":
            withForcedFailOnErrWritten(Boolean.parseBoolean(val));
            break;
        case "forced.warnonerrwritten":
            withForcedWarnOnErrWritten(Boolean.parseBoolean(val));
            break;
        case "forced.v1compatible":
            withForcedV1Compatible(Boolean.parseBoolean(val));
            break;
        // JITL FORCED
        case "forced.jitl.loglevel":
            withForcedJitlLogLevel(val);
            break;
        // SHELL UNIX
        case "default.shell.unix.shebang":
            withDefaultUnixShebang(val);
            break;
        case "forced.shell.unix.newline":
            withForcedUnixNewLine(val);
            break;
        case "forced.shell.unix.powershellshebang":
            withForcedUnixPowershellShebang(val);
            break;
        case "forced.shell.unix.commandprefix":
            withForcedUnixCommandPrefix(val);
            break;
        // SHELL WINDOWS
        case "forced.shell.windows.newline":
            withForcedWindowsNewLine(val);
            break;
        case "forced.shell.windows.powershellshebang":
            withForcedWindowsPowershellShebang(val);
            break;
        case "forced.shell.windows.commandprefix":
            withForcedWindowsCommandPrefix(val);
            break;
        // NOTIFICATION
        case "default.notification.mail.to":
            withNotificationMailDefaultTo(val);
            break;
        case "default.notification.mail.cc":
            withNotificationMailDefaultCc(val);
            break;
        case "default.notification.mail.bcc":
            withNotificationMailDefaultBcc(val);
            break;
        // RETRY
        case "forced.retry.maxtries":
            withForcedRetryMaxTries(val);
            break;
        case "forced.retry.delays":
            withForcedRetryDelays(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return forcedGraceTimeout == null && forcedParallelism == null && forcedFailOnErrWritten == null && forcedV1Compatible == null
                && notificationMailDefaultTo == null && notificationMailDefaultCc == null && notificationMailDefaultBcc == null;
    }

    public JobConfig withForcedJitlLogLevel(String val) {
        this.forcedJitlLogLevel = val;
        return this;
    }

    public JobConfig withForcedGraceTimeout(Integer val) {
        this.forcedGraceTimeout = val;
        return this;
    }

    public JobConfig withForcedParallelism(Integer val) {
        this.forcedParallelism = val;
        return this;
    }

    public JobConfig withForcedFailOnErrWritten(Boolean val) {
        this.forcedFailOnErrWritten = val;
        return this;
    }

    public JobConfig withForcedWarnOnErrWritten(Boolean val) {
        this.forcedWarnOnErrWritten = val;
        return this;
    }

    public JobConfig withForcedV1Compatible(Boolean val) {
        this.forcedV1Compatible = val;
        return this;
    }

    public JobConfig withDefaultUnixShebang(String val) {
        this.defaultUnixShebang = val;
        return this;
    }

    public JobConfig withForcedUnixNewLine(String val) {
        this.forcedUnixNewLine = val;
        return this;
    }

    public JobConfig withForcedUnixPowershellShebang(String val) {
        forcedUnixPowershellShebang = val;
        return this;
    }

    public JobConfig withForcedUnixCommandPrefix(String val) {
        forcedUnixCommandPrefix = val;
        return this;
    }

    public JobConfig withForcedWindowsNewLine(String val) {
        this.forcedWindowsNewLine = val;
        return this;
    }

    public JobConfig withForcedWindowsPowershellShebang(String val) {
        forcedWindowsPowershellShebang = val;
        return this;
    }

    public JobConfig withForcedWindowsCommandPrefix(String val) {
        forcedWindowsCommandPrefix = val;
        return this;
    }

    public JobConfig withNotificationMailDefaultTo(String val) {
        this.notificationMailDefaultTo = val;
        return this;
    }

    public JobConfig withNotificationMailDefaultCc(String val) {
        this.notificationMailDefaultCc = val;
        return this;
    }

    public JobConfig withNotificationMailDefaultBcc(String val) {
        this.notificationMailDefaultBcc = val;
        return this;
    }

    public JobConfig withForcedRetryMaxTries(String val) {
        this.forcedRetryMaxTries = JS7ConverterHelper.integerValue(val);
        return this;
    }

    public JobConfig withForcedRetryDelays(String val) {
        if (SOSString.isEmpty(val)) {
            this.forcedRetryDelays = null;
        } else {
            this.forcedRetryDelays = Stream.of(val.trim().split(LIST_VALUE_DELIMITER)).filter(d -> !SOSString.isEmpty(d.trim())).map(d -> Integer
                    .valueOf(d.trim())).collect(Collectors.toList());
        }
        return this;
    }

    public String getForcedJitlLogLevel() {
        return forcedJitlLogLevel;
    }

    public Integer getForcedGraceTimeout() {
        return forcedGraceTimeout;
    }

    public Integer getForcedParallelism() {
        return forcedParallelism;
    }

    public Boolean getForcedFailOnErrWritten() {
        return forcedFailOnErrWritten;
    }

    public Boolean getForcedWarnOnErrWritten() {
        return forcedWarnOnErrWritten;
    }

    public Boolean getForcedV1Compatible() {
        return forcedV1Compatible;
    }

    public boolean isForcedV1Compatible() {
        return forcedV1Compatible != null && forcedV1Compatible;
    }

    public String getDefaultUnixShebang() {
        return defaultUnixShebang;
    }

    public String getForcedUnixNewLine() {
        return forcedUnixNewLine;
    }

    public String getForcedUnixPowershellShebang() {
        return forcedUnixPowershellShebang;
    }

    public String getForcedUnixCommandPrefix() {
        return forcedUnixCommandPrefix;
    }

    public String getForcedWindowsNewLine() {
        return forcedWindowsNewLine;
    }

    public String getForcedWindowsPowershellShebang() {
        return forcedWindowsPowershellShebang;
    }

    public String getForcedWindowsCommandPrefix() {
        return forcedWindowsCommandPrefix;
    }

    public String getNotificationMailDefaultTo() {
        return notificationMailDefaultTo;
    }

    public String getNotificationMailDefaultCc() {
        return notificationMailDefaultCc;
    }

    public String getNotificationMailDefaultBcc() {
        return notificationMailDefaultBcc;
    }

    public Integer getForcedRetryMaxTries() {
        return forcedRetryMaxTries;
    }

    public List<Integer> getForcedRetryDelays() {
        return forcedRetryDelays;
    }

}
