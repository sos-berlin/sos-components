package com.sos.jitl.jobs.common;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

import js7.data.value.Value;
import js7.executor.forjava.internal.BlockingInternalJob;

public class JobLogger {

    public enum LogLevel {
        INFO, DEBUG, TRACE
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JobLogger.class);

    private final BlockingInternalJob.Step step;
    private final String stepInfo;
    private boolean isDebugEnabled;
    private boolean isTraceEnabled;

    protected JobLogger(BlockingInternalJob.Step step, String stepInfo) {
        this.step = step;
        this.stepInfo = stepInfo;
    }

    protected void init(Object args) {
        if (args != null && args instanceof JobArguments) {
            JobArguments ja = (JobArguments) args;
            isTraceEnabled = ja.getLogLevel().getValue().equals(LogLevel.TRACE);
            isDebugEnabled = ja.getLogLevel().getValue().equals(LogLevel.DEBUG) || isTraceEnabled;
        }
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

    public void warn(final String msg, Throwable e) {
        Throwable ex = handleException(e);
        warn2slf4j(msg, ex);
        step.out().println(String.format("[WARN]%s", warn2String(msg, ex)));
    }

    public void warn(final String format, final Object... msg) {
        warn(String.format(format, msg));
    }

    public void error(final String format, final Object... msg) {
        error(String.format(format, msg));
    }

    public void error(final String msg, final Throwable e) {
        Throwable ex = handleException(e);
        error2slf4j(msg, ex);
        step.err().println(String.format("[ERROR]%s", err2String(msg, ex)));
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    protected String warn2String(String msg, Throwable e) {
        return err2String(msg, e);
    }

    protected Throwable handleException(Throwable e) {
        if (e == null || e instanceof SOSJobRequiredArgumentMissingException) {
            return null;
        }
        return e;
    }

    protected void failed2slf4j() {
        LOGGER.error(String.format("[failed]%s", stepInfo));
    }

    protected void failed2slf4j(String msg) {
        LOGGER.error(String.format("[failed]%s%s", stepInfo, msg));
    }

    // TODO
    protected void failed2slf4j(String msg, Map<String, Value> returnValues) {
        LOGGER.error(String.format("[failed]%s%s", stepInfo, msg));
    }

    protected void failed2slf4j(String msg, Throwable e) {
        LOGGER.error(String.format("[failed]%s%s", stepInfo, msg), e);
    }

    private void warn2slf4j(String msg, Throwable e) {
        LOGGER.warn(String.format("%s%s", stepInfo, msg), e);
    }

    private void error2slf4j(String msg, Throwable e) {
        LOGGER.error(String.format("%s%s", stepInfo, msg), e);
    }

    protected String err2String(String msg, Throwable e) {
        if (e == null) {
            return msg;
        }
        StringBuilder sb = new StringBuilder(msg);
        sb.append("\n").append(SOSString.toString(e)).toString();
        return sb.toString();
    }
}
