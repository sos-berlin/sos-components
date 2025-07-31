package com.sos.commons.util.loggers.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import com.sos.commons.util.loggers.base.ISOSLogger;

/** slf4j-based implementation for applications that can be used in JS7 jobs (job logger) or standalone (slf4j API). */
public class SLF4JLogger implements ISOSLogger {

    private final String fqcn;
    private final Logger logger;
    private final LocationAwareLogger locationAwareLogger;

    private final boolean isDebugEnabled;
    private final boolean isTraceEnabled;

    /** Default constructor.
     * <p>
     * Initializes the logger using {@code SLF4JLogger.class} as both the SLF4J delegate and the fully qualified class name (FQCN).<br/>
     * This setup enables standard logging with the actual calling class and method being correctly reflected in the logs. */
    public SLF4JLogger() {
        this(LoggerFactory.getLogger(SLF4JLogger.class), SLF4JLogger.class);
    }

    /** Constructor for use by classes implementing {@link ISOSLogger}.
     * <p>
     * Initializes the logger with {@code SLF4JLogger.class} as the SLF4J backend, and uses the provided class as the fully qualified class name (FQCN).
     * <p>
     * This ensures that log messages reflect the intended logger class (e.g. {@code OrderProcessStepLogger}) when using SLF4J's location-aware logging.
     *
     * @param fqcnClazz the class implementing {@link ISOSLogger}; used to control caller location in logs */
    public SLF4JLogger(Class<?> fqcnClazz) {
        this(LoggerFactory.getLogger(SLF4JLogger.class), fqcnClazz);
    }

    /** Constructor with a provided SLF4J {@link Logger} instance.
     * <p>
     * Uses the given logger and sets the fully qualified class name (FQCN) to {@code SLF4JLogger.class}.<br/>
     * This ensures that log entries reflect the actual calling class and method that invoked the logger.
     *
     * @param logger the SLF4J logger instance to delegate to */
    public SLF4JLogger(Logger logger) {
        this(logger, SLF4JLogger.class);
    }

    /** Full constructor with custom SLF4J logger and fully qualified class name.
     * <p>
     * This constructor allows full control over both the SLF4J logger instance and the class used for log origin tracking.<br/>
     * The provided {@code fqcnClazz} is used internally for location-aware logging to determine the correct caller class and method in the log output.
     *
     * @param logger the SLF4J logger instance to delegate to
     * @param fqcnClazz the class implementing {@link ISOSLogger}, used to resolve the caller location;<br/>
     *            if {@code null}, defaults to {@code SLF4JLogger.class} */
    public SLF4JLogger(Logger logger, Class<?> fqcnClazz) {
        this.fqcn = fqcnClazz == null ? SLF4JLogger.class.getName() : fqcnClazz.getName();
        this.logger = logger;
        this.isDebugEnabled = logger.isDebugEnabled();
        this.isTraceEnabled = logger.isTraceEnabled();

        if (logger instanceof LocationAwareLogger) {
            this.locationAwareLogger = (LocationAwareLogger) logger;
        } else {
            this.locationAwareLogger = null;
        }
    }

    @Override
    public void info(Object msg) {
        log(LocationAwareLogger.INFO_INT, String.valueOf(msg), null);
    }

    @Override
    public void info(String format, Object... args) {
        log(LocationAwareLogger.INFO_INT, format, args);
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
    public void warn(Object msg) {
        log(LocationAwareLogger.WARN_INT, String.valueOf(msg), null);
    }

    @Override
    public void warn(String format, Object... args) {
        log(LocationAwareLogger.WARN_INT, format, args);
    }

    @Override
    public void warn(String msg, Throwable e) {
        log(LocationAwareLogger.WARN_INT, msg, null, e);
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
    public void error(String msg, Throwable e) {
        log(LocationAwareLogger.ERROR_INT, msg, null, e);
    }

    @Override
    public void error(Throwable e) {
        log(LocationAwareLogger.ERROR_INT, e.toString(), null, e);
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
        if (locationAwareLogger != null) {
            locationAwareLogger.log(null, fqcn, level, format(msg, args), null, t);
        } else {
            switch (level) {
            case LocationAwareLogger.TRACE_INT:
                if (t != null) {
                    logger.trace(format(msg, args), t);
                } else {
                    logger.trace(format(msg, args));
                }
                break;
            case LocationAwareLogger.DEBUG_INT:
                if (t != null) {
                    logger.debug(format(msg, args), t);
                } else {
                    logger.debug(format(msg, args));
                }
                break;
            case LocationAwareLogger.INFO_INT:
                if (t != null) {
                    logger.info(format(msg, args), t);
                } else {
                    logger.info(format(msg, args));
                }
                break;
            case LocationAwareLogger.WARN_INT:
                if (t != null) {
                    logger.warn(format(msg, args), t);
                } else {
                    logger.warn(format(msg, args));
                }
                break;
            case LocationAwareLogger.ERROR_INT:
                if (t != null) {
                    logger.error(format(msg, args), t);
                } else {
                    logger.error(format(msg, args));
                }
                break;
            }
        }
    }

    private String format(String format, Object[] args) {
        if (args == null || args.length == 0) {
            return format;
        }
        try {
            return String.format(format, args);
        } catch (Exception e) {
            return "[" + SLF4JLogger.class.getSimpleName() + "][" + format + "]" + e.getMessage();
        }
    }
}