package com.sos.commons.util.loggers.impl;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;

import com.sos.commons.util.loggers.base.ISOSLogger;

/** An SLF4J logger implementation that delegates logging calls to an {@link ISOSLogger} instance. */
public class DelegatingSlf4jLogger implements Logger {

    private final ISOSLogger delegate;
    private final String name;

    private final boolean isInfoEnabled;
    private final boolean isWarnEnabled = true;
    private final boolean isErrorEnabled = true;

    public DelegatingSlf4jLogger(ISOSLogger provider) {
        this(provider, DelegatingSlf4jLogger.class.getSimpleName(), true, false);
    }

    public DelegatingSlf4jLogger(ISOSLogger provider, boolean isInfoEnabled) {
        this(provider, DelegatingSlf4jLogger.class.getSimpleName(), isInfoEnabled, false);
    }

    public DelegatingSlf4jLogger(ISOSLogger provider, boolean isInfoEnabled, boolean async) {
        this(provider, DelegatingSlf4jLogger.class.getSimpleName(), isInfoEnabled, async);
    }

    public DelegatingSlf4jLogger(ISOSLogger provider, String name, boolean isInfoEnabled, boolean async) {
        if (provider == null) {
            throw new IllegalArgumentException("Logger provider must not be null");
        }
        this.delegate = getDelegate(provider, isInfoEnabled, async);
        this.name = name;
        this.isInfoEnabled = isInfoEnabled;
    }

    private ISOSLogger getDelegate(ISOSLogger provider, boolean isInfoEnabled, boolean async) {
        if (provider instanceof PrintWriterLogger) { // JITL Job Logger (JUnit or Agent environment)
            if (((PrintWriterLogger) provider).getDelegator() == null) { // JUnit log delegator (Slf4jLogger) is not set
                // Agent environment - really JITL Job Logger
                return new PrintWriterLogger(provider, isInfoEnabled, async);
            }
        }
        // provider is a Slf4jLogger - Standalone APP or JUnit environment
        // Option 1) return new StdLogger(provider, provider.isDebugEnabled()); log to std out/err instead
        // --------- Problem: own StdLogger formatting differs from the original Log4J format
        // Option 2) provider
        // --------- Slf4jLogger using the original Log4J format
        // Wrap in a new Slf4jLogger with this class as FQCN so Log4j2 skips this wrapper frame and reports the real caller in %C:%L.
        return new Slf4jLogger(getClass(), provider);
    }

    @Override
    public String getName() {
        return name;
    }

    // ---------------------------------------------------------
    // TRACE
    // ---------------------------------------------------------

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (!isTraceEnabled()) {
            return;
        }

        delegate.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        trace(format, new Object[] { arg });
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        trace(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (!isTraceEnabled()) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        logTrace(tuple);
    }

    private void logTrace(FormattingTuple tuple) {
        delegate.trace(ISOSLogger.getMessage(tuple.getMessage(), tuple.getThrowable()));
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (!isTraceEnabled()) {
            return;
        }

        delegate.trace(ISOSLogger.getMessage(msg, t));
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        trace(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        trace(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        trace(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... args) {
        trace(format, args);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        trace(msg, t);
    }

    // ---------------------------------------------------------
    // DEBUG
    // ---------------------------------------------------------

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (!isDebugEnabled()) {
            return;
        }

        delegate.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        debug(format, new Object[] { arg });
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        debug(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (!isDebugEnabled()) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        logDebug(tuple);
    }

    private void logDebug(FormattingTuple tuple) {
        delegate.debug(ISOSLogger.getMessage(tuple.getMessage(), tuple.getThrowable()));
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (!isDebugEnabled()) {
            return;
        }

        delegate.debug(ISOSLogger.getMessage(msg, t));
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        debug(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        debug(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        debug(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... args) {
        debug(format, args);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        debug(msg, t);
    }

    // ---------------------------------------------------------
    // INFO
    // ---------------------------------------------------------

    @Override
    public boolean isInfoEnabled() {
        return isInfoEnabled;
    }

    @Override
    public void info(String msg) {
        if (!isInfoEnabled()) {
            return;
        }

        delegate.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        info(format, new Object[] { arg });
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        info(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void info(String format, Object... arguments) {
        if (!isInfoEnabled()) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        logInfo(tuple);
    }

    private void logInfo(FormattingTuple tuple) {
        delegate.info(ISOSLogger.getMessage(tuple.getMessage(), tuple.getThrowable()));
    }

    @Override
    public void info(String msg, Throwable t) {
        if (!isInfoEnabled()) {
            return;
        }

        delegate.info(ISOSLogger.getMessage(msg, t));
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... args) {
        info(format, args);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        info(msg, t);
    }

    // ---------------------------------------------------------
    // WARN
    // ---------------------------------------------------------

    @Override
    public boolean isWarnEnabled() {
        return isWarnEnabled;
    }

    @Override
    public void warn(String msg) {
        if (!isWarnEnabled()) {
            return;
        }

        delegate.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        warn(format, new Object[] { arg });
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        warn(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (!isWarnEnabled()) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        Throwable t = tuple.getThrowable();
        delegate.warn(tuple.getMessage(), t);
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (!isWarnEnabled()) {
            return;
        }

        delegate.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        warn(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... args) {
        warn(format, args);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        warn(msg, t);
    }

    // ---------------------------------------------------------
    // ERROR
    // ---------------------------------------------------------

    @Override
    public boolean isErrorEnabled() {
        return isErrorEnabled;
    }

    @Override
    public void error(String msg) {
        if (!isErrorEnabled()) {
            return;
        }

        delegate.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        error(format, new Object[] { arg });
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        error(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void error(String format, Object... arguments) {
        if (!isErrorEnabled()) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        Throwable t = tuple.getThrowable();
        delegate.error(tuple.getMessage(), t);
    }

    @Override
    public void error(String msg, Throwable t) {
        if (!isErrorEnabled()) {
            return;
        }

        delegate.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... args) {
        error(format, args);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        error(msg, t);
    }

    public void close() {
        if (delegate != null && delegate instanceof PrintWriterLogger) {
            ((PrintWriterLogger) delegate).close();
        }
    }

}