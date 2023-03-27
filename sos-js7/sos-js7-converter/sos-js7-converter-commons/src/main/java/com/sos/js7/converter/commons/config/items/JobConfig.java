package com.sos.js7.converter.commons.config.items;

public class JobConfig extends AConfigItem {

    private static final String CONFIG_KEY = "jobConfig";

    private String forcedJitlLogLevel;
    private Integer forcedGraceTimeout;
    private Integer forcedParallelism;
    private Boolean forcedFailOnErrWritten;
    private Boolean forcedV1Compatible;

    private String unixDefaultShebang = "#!/bin/bash";
    private String unixNewLine = "\n";
    private String unixPowershellShebang = "#!/usr/bin/env pwsh";
    private String windowsNewLine = "\n"; // "\r\n";
    private String windowsPowershellShebang = "@@findstr/v \"^@@f.*&\" \"%~f0\"|pwsh.exe -&goto:eof";

    private String notificationMailDefaultTo;
    private String notificationMailDefaultCc;
    private String notificationMailDefaultBcc;

    public JobConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key) {
        // FORCED
        case "forcedGraceTimeout":
            withForcedGraceTimeout(Integer.parseInt(val));
            break;
        case "forcedParallelism":
            withForcedParallelism(Integer.parseInt(val));
            break;
        case "forcedFailOnErrWritten":
            withForcedFailOnErrWritten(Boolean.parseBoolean(val));
            break;
        case "forcedV1Compatible":
            withForcedV1Compatible(Boolean.parseBoolean(val));
            break;
        // JITL FORCED
        case "jitl.forcedLogLevel":
            withForcedJitlLogLevel(val);
            break;
        // SHELL UNIX
        case "shell.unix.defaultShebang":
            withUnixDefaultShebang(val);
            break;
        case "shell.unix.newLine":
            withUnixNewLine(val);
            break;
        case "shell.unix.powershellShebang":
            withUnixPowershellShebang(val);
            break;
        // SHELL WINDOWS
        case "shell.windows.newLine":
            withWindowsNewLine(val);
            break;
        case "shell.windows.powershellShebang":
            withWindowsPowershellShebang(val);
            break;
        // NOTIFICATION
        case "notification.mail.defaultTo":
            withNotificationMailDefaultTo(val);
            break;
        case "notification.mail.defaultCc":
            withNotificationMailDefaultCc(val);
            break;
        case "notification.mail.defaultBcc":
            withNotificationMailDefaultBcc(val);
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

    public JobConfig withForcedV1Compatible(Boolean val) {
        this.forcedV1Compatible = val;
        return this;
    }

    public JobConfig withUnixDefaultShebang(String val) {
        this.unixDefaultShebang = val;
        return this;
    }

    public JobConfig withUnixNewLine(String val) {
        this.unixNewLine = val;
        return this;
    }

    public JobConfig withUnixPowershellShebang(String val) {
        unixPowershellShebang = val;
        return this;
    }

    public JobConfig withWindowsNewLine(String val) {
        this.windowsNewLine = val;
        return this;
    }

    public JobConfig withWindowsPowershellShebang(String val) {
        windowsPowershellShebang = val;
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

    public Boolean getForcedV1Compatible() {
        return forcedV1Compatible;
    }

    public boolean isForcedV1Compatible() {
        return forcedV1Compatible != null && forcedV1Compatible;
    }

    public String getUnixDefaultShebang() {
        return unixDefaultShebang;
    }

    public String getUnixNewLine() {
        return unixNewLine;
    }

    public String getUnixPowershellShebang() {
        return unixPowershellShebang;
    }

    public String getWindowsNewLine() {
        return windowsNewLine;
    }

    public String getWindowsPowershellShebang() {
        return windowsPowershellShebang;
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

}
