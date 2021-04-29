package com.sos.jitl.jobs.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import js7.executor.forjava.internal.BlockingInternalJob;

public class JobLogger {

    public enum LogLevel {
        INFO, DEBUG, TRACE
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JobLogger.class);

    private final BlockingInternalJob.Step step;
    private boolean isDebugEnabled;
    private boolean isTraceEnabled;

    protected JobLogger(BlockingInternalJob.Step step) {
        this(step, null);
    }

    public JobLogger(BlockingInternalJob.Step step, Object args) {
        this(step, false, false);
        if (args != null && args instanceof JobArguments) {
            JobArguments ja = (JobArguments) args;
            isTraceEnabled = ja.getLogLevel().getValue().equals(LogLevel.TRACE);
            isDebugEnabled = ja.getLogLevel().getValue().equals(LogLevel.DEBUG) || isTraceEnabled;
        }
    }

    public JobLogger(BlockingInternalJob.Step step, boolean isDebugEnabled, boolean isTraceEnabled) {
        this.step = step;
        this.isDebugEnabled = isDebugEnabled;
        this.isTraceEnabled = isTraceEnabled;
    }

    public void info(final Object msg) {
        step.out().println(msg);
    }

    public void info(final String format, final Object... msg) {
        info(String.format(format, msg));
    }

    public void debug(final String format, final Object... msg) {
        debug(String.format(format, msg));
    }

    public void debug(final Object msg) {
        if (!isDebugEnabled) {
            return;
        }
        step.out().println(String.format("[DEBUG]%s", msg));
    }

    public void trace(final String format, final Object... msg) {
        trace(String.format(format, msg));
    }

    public void trace(final Object msg) {
        if (!isTraceEnabled) {
            return;
        }
        step.out().println(String.format("[TRACE]%s", msg));
    }

    public void warn(final Object msg) {
        warn(msg, null);
    }

    public void warn(final Object msg, Throwable e) {
        LOGGER.warn(String.format("[WARN]%s", msg), e);
        step.out().println(String.format("[WARN]%s", msg));
    }

    public void warn(final String format, final Object... msg) {
        warn(String.format(format, msg));
    }

    public void error(final Object msg) {
        error(msg, null);
    }

    public void error(final String format, final Object... msg) {
        error(String.format(format, msg));
    }

    public void error(final Object msg, final Throwable e) {
        LOGGER.error(String.format("[ERROR]%s", msg), e);
        step.err().println(String.format("[ERROR]%s", msg));
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }
}
