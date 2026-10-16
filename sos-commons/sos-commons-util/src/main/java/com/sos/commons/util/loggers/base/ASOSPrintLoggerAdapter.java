package com.sos.commons.util.loggers.base;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;

import com.sos.commons.util.SOSClassUtil;

/** Abstract base class for print-based loggers.
 * <p>
 * Provides common logging functionality, including message formatting, throwable handling, and log-level configuration.<br />
 * Subclasses define the output destinations for log messages. */
public abstract class ASOSPrintLoggerAdapter implements ISOSLogger {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private final boolean isWarnEnabled = true;
    private final boolean isErrorEnabled = true;

    private ISOSLogger delegator;

    private boolean isInfoEnabled;
    private boolean isDebugEnabled;
    private boolean isTraceEnabled;

    /** Creates a logger with INFO, WARN, ERROR logging enabled and DEBUG and TRACE logging disabled. */
    public ASOSPrintLoggerAdapter() {
        this(true, false, false);
    }

    /** Creates a logger using the specified INFO logging setting and the DEBUG and TRACE settings provided by the given logger.
     * 
     * @param provider the logger providing the DEBUG and TRACE logging settings; may be {@code null}
     * @param isInfoEnabled whether INFO logging is enabled */
    public ASOSPrintLoggerAdapter(ISOSLogger provider, boolean isInfoEnabled) {
        this(isInfoEnabled, provider != null && provider.isDebugEnabled(), provider != null && provider.isTraceEnabled());
    }

    public ASOSPrintLoggerAdapter(boolean isInfoEnabled, boolean isDebugEnabled, boolean isTraceEnabled) {
        setLogLevels(isInfoEnabled, isDebugEnabled, isTraceEnabled);
    }

    public void setLogLevel(LogLevel level) {
        if (level == null) {
            return;
        }
        switch (level) {
        case DEBUG:
            setLogLevels(true, true, false);
            break;
        case INFO:
            setLogLevels(true, false, false);
            break;
        case TRACE:
            setLogLevels(true, true, true);
            break;
        case ERROR:
        case WARN:
        default:
            break;
        }
    }

    public void setDelegator(ISOSLogger val) {
        delegator = val;
    }

    public ISOSLogger getDelegator() {
        return delegator;
    }

    private void setLogLevels(boolean isInfoEnabled, boolean isDebugEnabled, boolean isTraceEnabled) {
        this.isInfoEnabled = isInfoEnabled;
        this.isDebugEnabled = isDebugEnabled;
        this.isTraceEnabled = isTraceEnabled;
    }

    /** Returns the output destination for TRACE, DEBUG, INFO, and WARN log messages.
     * 
     * @return a consumer that writes standard log messages */
    public abstract Consumer<String> getOut();

    /** Returns the output destination for ERROR log messages.
     * 
     * @return a consumer that writes error log messages */
    public abstract Consumer<String> getErr();

    // ---------------------------------------------------------
    // TRACE
    // ---------------------------------------------------------

    @Override
    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    @Override
    public void trace(Object msg) {
        trace(msg + "");
    }

    public void trace(String msg) {
        if (!isTraceEnabled) {
            return;
        }

        logTrace(msg, null);
    }

    public void trace(String format, Object arg) {
        trace(format, new Object[] { arg });
    }

    public void trace(String format, Object arg1, Object arg2) {
        trace(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (!isTraceEnabled) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        Throwable t = tuple.getThrowable();
        logTrace(tuple.getMessage(), t);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (!isTraceEnabled) {
            return;
        }

        logTrace(msg, t);
    }

    private void logTrace(String msg, Throwable t) {
        if (delegator != null) {
            delegator.trace(msg, t);
            return;
        }
        log(getOut(), LogLevel.TRACE.name(), getAdditionalInfos() + msg, t);
    }

    // ---------------------------------------------------------
    // DEBUG
    // ---------------------------------------------------------

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public void debug(Object msg) {
        debug(msg + "");
    }

    public void debug(String msg) {
        if (!isDebugEnabled) {
            return;
        }

        logDebug(msg, null);
    }

    public void debug(String format, Object arg) {
        debug(format, new Object[] { arg });
    }

    public void debug(String format, Object arg1, Object arg2) {
        debug(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (!isDebugEnabled) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        Throwable t = tuple.getThrowable();
        logDebug(tuple.getMessage(), t);
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (!isDebugEnabled) {
            return;
        }

        logDebug(msg, t);
    }

    private void logDebug(String msg, Throwable t) {
        if (delegator != null) {
            delegator.debug(msg, t);
            return;
        }
        log(getOut(), LogLevel.DEBUG.name(), getAdditionalInfos() + msg, t);
    }

    // ---------------------------------------------------------
    // INFO
    // ---------------------------------------------------------
    public boolean isInfoEnabled() {
        return isInfoEnabled;
    }

    @Override
    public void info(Object msg) {
        info(msg + "");
    }

    public void info(String msg) {
        if (!isInfoEnabled) {
            return;
        }

        logInfo(msg, null);
    }

    public void info(String format, Object arg) {
        info(format, new Object[] { arg });
    }

    public void info(String format, Object arg1, Object arg2) {
        info(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void info(String format, Object... arguments) {
        if (!isInfoEnabled) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        Throwable t = tuple.getThrowable();
        logInfo(tuple.getMessage(), t);
    }

    @Override
    public void info(String msg, Throwable t) {
        if (!isInfoEnabled) {
            return;
        }

        logInfo(msg, t);
    }

    private void logInfo(String msg, Throwable t) {
        String addInfo = "";
        if (isDebugEnabled) {
            addInfo = getAdditionalInfos();
        }
        if (delegator != null) {
            delegator.info(msg, t);
            return;
        }
        log(getOut(), LogLevel.INFO.name(), addInfo + msg, t);
    }

    // ---------------------------------------------------------
    // WARN
    // ---------------------------------------------------------

    public boolean isWarnEnabled() {
        return isWarnEnabled;
    }

    @Override
    public void warn(Object msg) {
        warn(msg + "");
    }

    public void warn(String msg) {
        if (!isWarnEnabled) {
            return;
        }

        logWarn(msg, null);
    }

    public void warn(String format, Object arg) {
        warn(format, new Object[] { arg });
    }

    public void warn(String format, Object arg1, Object arg2) {
        warn(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (!isWarnEnabled) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        Throwable t = tuple.getThrowable();
        logWarn(tuple.getMessage(), t);
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (!isWarnEnabled) {
            return;
        }

        logWarn(msg, t);
    }

    private void logWarn(String msg, Throwable t) {
        if (delegator != null) {
            delegator.warn(msg, t);
            return;
        }
        log(getOut(), LogLevel.WARN.name(), msg, t);
    }

    // ---------------------------------------------------------
    // ERROR
    // ---------------------------------------------------------

    public boolean isErrorEnabled() {
        return isErrorEnabled;
    }

    @Override
    public void error(Throwable e) {
        if (!isErrorEnabled) {
            return;
        }

        logError(null, e);
    }

    @Override
    public void error(Object msg) {
        error(msg + "");
    }

    public void error(String msg) {
        if (!isErrorEnabled) {
            return;
        }

        logError(msg, null);
    }

    public void error(String format, Object arg) {
        error(format, new Object[] { arg });
    }

    public void error(String format, Object arg1, Object arg2) {
        error(format, new Object[] { arg1, arg2 });
    }

    @Override
    public void error(String format, Object... arguments) {
        if (!isErrorEnabled) {
            return;
        }

        FormattingTuple tuple = ISOSLogger.format(format, arguments);
        Throwable t = tuple.getThrowable();
        error(tuple.getMessage(), t);
    }

    @Override
    public void error(String msg, Throwable t) {
        if (!isErrorEnabled) {
            return;
        }

        logError(msg, t);
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
            break;
        case WARN:
            warn(msg);
            break;
        case ERROR:
            error(msg);
            break;
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
            break;
        case WARN:
            warn(format, args);
            break;
        case ERROR:
            error(format, args);
            break;
        }
    }

    private void logError(String msg, Throwable t) {
        if (delegator != null) {
            delegator.error(msg, t);
            return;
        }
        log(getErr(), LogLevel.ERROR.name(), msg, t);
    }

    private void log(Consumer<String> out, String level, String msg, Throwable t) {
        log(out, level, ISOSLogger.getMessage(msg, t));
    }

    private void log(Consumer<String> out, String level, String msg) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        try {
            out.accept("[" + level + "] " + msg);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            } else {
                throw e;
            }
        }
    }

    private String getAdditionalInfos() {
        StringBuilder sb = new StringBuilder();
        sb.append("[" + Thread.currentThread().getName()).append("]").append(" ");

        StackWalker.StackFrame caller = findCaller(getClass());
        if (caller != null) {
            Class<?> callerClass = caller.getDeclaringClass();
            sb.append("(").append(SOSClassUtil.getSimpleName(callerClass.getName())).append(":").append(caller.getLineNumber()).append(") ");
        }
        return sb.toString();
    }

    private static StackWalker.StackFrame findCaller(Class<?> clazz) {
        return STACK_WALKER.walk(frames -> frames.dropWhile(frame -> ISOSLogger.class.isAssignableFrom(frame.getDeclaringClass()) || Logger.class
                .isAssignableFrom(frame.getDeclaringClass())).findFirst().orElse(null));
    }

}