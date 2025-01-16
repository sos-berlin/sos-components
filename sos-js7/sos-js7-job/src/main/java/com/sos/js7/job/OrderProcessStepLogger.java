package com.sos.js7.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.js7.job.JobArguments.LogLevel;

import js7.launcher.forjava.internal.BlockingInternalJob;

public class OrderProcessStepLogger implements ISOSLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderProcessStepLogger.class);

    private final BlockingInternalJob.Step step;

    private boolean isDebugEnabled;
    private boolean isTraceEnabled;

    protected OrderProcessStepLogger(BlockingInternalJob.Step step) {
        this.step = step;
    }

    protected void init(Object args) {
        if (step == null) {
            isTraceEnabled = LOGGER.isTraceEnabled();
            isDebugEnabled = LOGGER.isDebugEnabled();
        } else if (args != null && args instanceof JobArguments) {
            JobArguments ja = (JobArguments) args;
            isTraceEnabled = ja.getLogLevel().getValue().equals(LogLevel.TRACE);
            isDebugEnabled = ja.getLogLevel().getValue().equals(LogLevel.DEBUG) || isTraceEnabled;
        }
    }

    @Override
    public void info(final Object msg) {
        String m = getMessage(LogLevel.INFO, msg);
        if (step == null) {
            LOGGER.info(m);
            return;
        }
        step.out().println(m);
    }

    @Override
    public void info(final String format, final Object... args) {
        if (args.length == 0) {
            info(format);
        } else {
            info(String.format(format, args));
        }
    }

    @Override
    public void debug(final Object msg) {
        if (!isDebugEnabled) {
            return;
        }
        String m = getMessage(LogLevel.DEBUG, msg);
        if (step == null) {
            LOGGER.debug(m);
            return;
        }
        step.out().println(m);
    }

    @Override
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

    @Override
    public void trace(final Object msg) {
        if (!isTraceEnabled) {
            return;
        }
        String m = getMessage(LogLevel.TRACE, msg);
        if (step == null) {
            LOGGER.trace(m);
            return;
        }
        step.out().println(m);
    }

    @Override
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

    @Override
    public void warn(final Object msg) {
        String m = getMessage(LogLevel.WARN, msg);
        if (step == null) {
            LOGGER.warn(m);
            return;
        }
        step.out().println(m);
    }

    @Override
    public void warn(final String format, final Object... args) {
        if (args.length == 0) {
            warn(format);
        } else {
            warn(String.format(format, args));
        }
    }

    @Override
    public void warn(final String msg, Throwable e) {
        warn(warn2String(msg, e));
    }

    protected void warn2allLoggers(String stepInfo, final String msg, Throwable e) {
        Throwable ex = handleException(e);
        if (ex != null) {
            warn2slf4j(stepInfo, msg, ex);
            warn(warn2String(msg, ex));
        }
    }

    @Override
    public void error(final Object msg) {
        String m = getMessage(LogLevel.ERROR, msg);
        if (step == null) {
            LOGGER.error(m);
            return;
        }
        step.err().println(m);
    }

    @Override
    public void error(final String format, final Object... args) {
        if (args.length == 0) {
            error(format);
        } else {
            error(String.format(format, args));
        }
    }

    @Override
    public void error(final String msg, Throwable e) {
        error(throwable2String(msg, e));
    }

    @Override
    public void error(Throwable e) {
        error(throwable2String(null, e));
    }

    protected void error2allLoggers(final String stepInfo, final String msg, final Throwable e) {
        Throwable ex = handleException(e);
        if (ex != null) {
            error2slf4j(stepInfo, msg, ex);
            error(throwable2String(msg, ex));
        }
    }

    protected void log(LogLevel logLevel, final Object msg) {
        switch (logLevel) {
        case INFO:
            info(msg);
            break;
        case DEBUG:
            debug(msg);
            break;
        case TRACE:
            trace(msg);
            break;
        case WARN:
            warn(msg);
            break;
        case ERROR:
            error(msg);
            break;
        }
    }

    protected void log(LogLevel logLevel, final String format, final Object... args) {
        switch (logLevel) {
        case INFO:
            info(format, args);
            break;
        case DEBUG:
            debug(format, args);
            break;
        case TRACE:
            trace(format, args);
            break;
        case WARN:
            warn(format, args);
            break;
        case ERROR:
            error(format, args);
            break;
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    private String warn2String(String msg, Throwable e) {
        return throwable2String(msg, e);
    }

    protected Throwable handleException(Throwable e) {
        if (e == null || e instanceof ISOSRequiredArgumentMissingException) {
            return null;
        }
        return e;
    }

    protected void failed2slf4j(String stepInfo, String msg) {
        LOGGER.info(String.format("[failed]%s%s", stepInfo, SOSString.isEmpty(msg) ? "" : msg));
    }

    protected void failed2slf4j(String stepInfo, String msg, Throwable e) {
        LOGGER.info(String.format("[failed]%s%s", stepInfo, msg), e);
    }

    private void warn2slf4j(String stepInfo, String msg, Throwable e) {
        LOGGER.info(String.format("%s%s", stepInfo, msg), e);
    }

    private void error2slf4j(String stepInfo, String msg, Throwable e) {
        LOGGER.info(String.format("%s%s", stepInfo, msg), e);
    }

    private String getMessage(LogLevel logLevel, Object msg) {
        return String.format("[%s]%s", logLevel.name(), msg);
    }

    protected String throwable2String(String msg, Throwable e) {
        if (e == null) {
            return msg;
        }
        StringBuilder sb = new StringBuilder();
        if (!SOSString.isEmpty(msg)) {
            sb.append(msg);
            sb.append("\n");
        }
        sb.append(SOSString.toString(e));
        return sb.toString();
    }
}
