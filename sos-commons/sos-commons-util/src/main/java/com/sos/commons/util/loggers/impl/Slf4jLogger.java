package com.sos.commons.util.loggers.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import com.sos.commons.util.loggers.base.ISOSLogger;

/** An {@link ISOSLogger} implementation that delegates log messages to SLF4J. */
public class Slf4jLogger implements ISOSLogger {

    private final String fqcn;
    private final Logger logger;
    private final LocationAwareLogger locationAwareLogger;

    private final boolean isInfoEnabled;
    private final boolean isDebugEnabled;
    private final boolean isTraceEnabled;

    /** Default constructor.
     * <p>
     * Initializes the logger using {@code SLF4JLogger.class} as both the SLF4J delegate and the fully qualified class name (FQCN).<br/>
     * This setup enables standard logging with the actual calling class and method being correctly reflected in the logs. */
    public Slf4jLogger() {
        this((ISOSLogger) null);
    }

    public Slf4jLogger(ISOSLogger provider) {
        this(LoggerFactory.getLogger(Slf4jLogger.class), Slf4jLogger.class, provider);
    }

    /** Constructor for use by classes implementing {@link ISOSLogger}.
     * <p>
     * Initializes the logger with {@code SLF4JLogger.class} as the SLF4J backend, and uses the provided class as the fully qualified class name (FQCN).
     * <p>
     * This ensures that log messages reflect the intended logger class (e.g. {@code OrderProcessStepLogger}) when using SLF4J's location-aware logging.
     *
     * @param fqcnClazz the class implementing {@link ISOSLogger}; used to control caller location in logs */
    public Slf4jLogger(Class<?> fqcnClazz) {
        this(LoggerFactory.getLogger(Slf4jLogger.class), fqcnClazz, null);
    }

    public Slf4jLogger(Class<?> fqcnClazz, ISOSLogger provider) {
        this(LoggerFactory.getLogger(Slf4jLogger.class), fqcnClazz, provider);
    }

    /** Constructor with a provided SLF4J {@link Logger} instance.
     * <p>
     * Uses the given logger and sets the fully qualified class name (FQCN) to {@code SLF4JLogger.class}.<br/>
     * This ensures that log entries reflect the actual calling class and method that invoked the logger.
     *
     * @param logger the SLF4J logger instance to delegate to */
    public Slf4jLogger(Logger logger) {
        this(logger, Slf4jLogger.class, null);
    }

    public Slf4jLogger(Logger logger, ISOSLogger provider) {
        this(logger, Slf4jLogger.class, provider);
    }

    /** Full constructor with custom SLF4J logger and fully qualified class name.
     * <p>
     * This constructor allows full control over both the SLF4J logger instance and the class used for log origin tracking.<br/>
     * The provided {@code fqcnClazz} is used internally for location-aware logging to determine the correct caller class and method in the log output.
     *
     * @param logger the SLF4J logger instance to delegate to
     * @param fqcnClazz the class implementing {@link ISOSLogger}, used to resolve the caller location;<br/>
     *            if {@code null}, defaults to {@code SLF4JLogger.class} */
    public Slf4jLogger(Logger logger, Class<?> fqcnClazz) {
        this(logger, fqcnClazz, null);
    }

    public Slf4jLogger(Logger logger, Class<?> fqcnClazz, ISOSLogger provider) {
        this.fqcn = fqcnClazz == null ? Slf4jLogger.class.getName() : fqcnClazz.getName();
        this.logger = logger;

        this.isInfoEnabled = provider == null ? logger.isInfoEnabled() : provider.isInfoEnabled();
        this.isDebugEnabled = provider == null ? logger.isDebugEnabled() : provider.isDebugEnabled();
        this.isTraceEnabled = provider == null ? logger.isTraceEnabled() : provider.isTraceEnabled();

        if (logger instanceof LocationAwareLogger) {
            this.locationAwareLogger = (LocationAwareLogger) logger;
        } else {
            this.locationAwareLogger = null;
        }
    }

    @Override
    public void info(Object msg) {
        if (!isInfoEnabled) {
            return;
        }
        log(LocationAwareLogger.INFO_INT, String.valueOf(msg), null);
    }

    @Override
    public void info(String format, Object... args) {
        if (!isInfoEnabled) {
            return;
        }
        log(LocationAwareLogger.INFO_INT, format, args);
    }

    @Override
    public void info(String format, Throwable t) {
        if (!isInfoEnabled) {
            return;
        }
        log(LocationAwareLogger.INFO_INT, format, null, t);
    }

    @Override
    public void debug(Object msg) {
        if (!isDebugEnabled) {
            return;
        }
        log(LocationAwareLogger.DEBUG_INT, String.valueOf(msg), null);
    }

    @Override
    public void debug(String format, Object... args) {
        if (!isDebugEnabled) {
            return;
        }
        log(LocationAwareLogger.DEBUG_INT, format, args);
    }

    @Override
    public void debug(String format, Throwable t) {
        if (!isDebugEnabled) {
            return;
        }
        log(LocationAwareLogger.DEBUG_INT, format, null, t);
    }

    @Override
    public void trace(Object msg) {
        if (!isTraceEnabled) {
            return;
        }
        log(LocationAwareLogger.TRACE_INT, String.valueOf(msg), null);
    }

    @Override
    public void trace(String format, Object... args) {
        if (!isTraceEnabled) {
            return;
        }
        log(LocationAwareLogger.TRACE_INT, format, args);
    }

    @Override
    public void trace(String format, Throwable t) {
        if (!isTraceEnabled) {
            return;
        }
        log(LocationAwareLogger.TRACE_INT, format, null, t);
    }

    @Override
    public void warn(Object msg) {
        log(LocationAwareLogger.WARN_INT, String.valueOf(msg), null);
    }

    @Override
    public void warn(String format, Object... args) {
        log(LocationAwareLogger.WARN_INT, format, args);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log(LocationAwareLogger.WARN_INT, msg, null, t);
    }

    @Override
    public void error(Object msg) {
        log(LocationAwareLogger.ERROR_INT, String.valueOf(msg), null);
    }

    @Override
    public void error(String format, Object... args) {
        log(LocationAwareLogger.ERROR_INT, format, args);
    }

    @Override
    public void error(String msg, Throwable t) {
        log(LocationAwareLogger.ERROR_INT, msg, null, t);
    }

    @Override
    public void error(Throwable t) {
        log(LocationAwareLogger.ERROR_INT, t == null ? null : t.toString(), null, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return isInfoEnabled;
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    private void log(int level, String msg, Object[] args) {
        log(level, msg, args, null);
    }

    private void log(int level, String msg, Object[] args, Throwable t) {
        String message = ISOSLogger.format(msg, args).getMessage();

        if (locationAwareLogger != null) {
            locationAwareLogger.log(null, fqcn, level, message, args, t);
            return;
        }

        switch (level) {
        case LocationAwareLogger.TRACE_INT:
            if (t != null) {
                logger.trace(message, t);
            } else {
                logger.trace(message);
            }
            break;
        case LocationAwareLogger.DEBUG_INT:
            if (t != null) {
                logger.debug(message, t);
            } else {
                logger.debug(message);
            }
            break;
        case LocationAwareLogger.INFO_INT:
            if (t != null) {
                logger.info(message, t);
            } else {
                logger.info(message);
            }
            break;
        case LocationAwareLogger.WARN_INT:
            if (t != null) {
                logger.warn(message, t);
            } else {
                logger.warn(message);
            }
            break;
        case LocationAwareLogger.ERROR_INT:
            if (t != null) {
                logger.error(message, t);
            } else {
                logger.error(message);
            }
            break;
        }
    }
}