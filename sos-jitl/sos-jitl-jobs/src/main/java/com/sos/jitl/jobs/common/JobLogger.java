package com.sos.jitl.jobs.common;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;

import js7.data.value.Value;
import js7.launcher.forjava.internal.BlockingInternalJob;

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

    public void info(final String format, final Object... args) {
        if (args.length == 0) {
            info(format);
        } else {
            info(String.format(format, args));
        }
    }

    public void debug(final String format, final Object... args) {
        if (!isDebugEnabled) {
            return;
        }
        if (args.length == 0) {
            debug(format);
        } else {
            debug(String.format(format, args));
        }
    }

    public void debug(final Object msg) {
        if (!isDebugEnabled) {
            return;
        }
        step.out().println(String.format("[DEBUG]%s", msg));
    }

    public void trace(final String format, final Object... args) {
        if (!isTraceEnabled) {
            return;
        }
        if (args.length == 0) {
            trace(format);
        } else {
            trace(String.format(format, args));
        }
    }

    public void trace(final Object msg) {
        if (!isTraceEnabled) {
            return;
        }
        step.out().println(String.format("[TRACE]%s", msg));
    }

    public void log(LogLevel logLevel, final Object msg) {
        switch (logLevel) {
        case INFO:
            info(msg);
            break;
        case DEBUG:
            debug(msg);
            break;
        case TRACE:
            trace(msg);
        }
    }

    public void log(LogLevel logLevel, final String format, final Object... args) {
        switch (logLevel) {
        case INFO:
            info(format, args);
            break;
        case DEBUG:
            debug(format, args);
            break;
        case TRACE:
            trace(format, args);
        }
    }

    public void warn(final String msg, Throwable e) {
        Throwable ex = handleException(e);
        warn2slf4j(msg, ex);
        step.out().println(String.format("[WARN]%s", warn2String(msg, ex)));
    }

    public void warn(final String format, final Object... args) {
        if (args.length == 0) {
            warn(format);
        } else {
            warn(String.format(format, args));
        }
    }

    public void error(final String format, final Object... args) {
        if (args.length == 0) {
            error(format);
        } else {
            error(String.format(format, args));
        }
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
        if (e == null || e instanceof ISOSRequiredArgumentMissingException) {
            return null;
        }
        return e;
    }

    protected void failed2slf4j(String msg) {
        LOGGER.error(String.format("[failed]%s%s", stepInfo, SOSString.isEmpty(msg) ? "" : msg));
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
