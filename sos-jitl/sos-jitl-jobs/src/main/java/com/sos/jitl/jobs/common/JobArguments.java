package com.sos.jitl.jobs.common;

public class JobArguments {

    public enum LogLevel {
        INFO, DEBUG, TRACE
    }

    private JobArgument<LogLevel> logLevel = new JobArgument<LogLevel>("log_level", LogLevel.INFO);

    public JobArgument<LogLevel> getLogLevel() {
        return logLevel;
    }

    public boolean isDebugEnabled() {
        return logLevel.getValue().equals(LogLevel.DEBUG) || logLevel.getValue().equals(LogLevel.TRACE);
    }

    public boolean isTraceEnabled() {
        return logLevel.getValue().equals(LogLevel.TRACE);
    }
}
