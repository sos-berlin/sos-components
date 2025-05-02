package com.sos.commons.util.loggers.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import com.sos.commons.util.loggers.base.ISOSLogger;

/** slf4j-based implementation for applications that can be used in JS7 jobs (job logger) or standalone (slf4j API). */
public class SLF4JLogger implements ISOSLogger {

    private static final String FQCN = SLF4JLogger.class.getName();

    private final Logger logger;
    private final LocationAwareLogger locationAwareLogger;

    private final boolean isDebugEnabled;
    private final boolean isTraceEnabled;

    public SLF4JLogger() {
        this(LoggerFactory.getLogger(SLF4JLogger.class));
    }

    public SLF4JLogger(Logger logger) {
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
            locationAwareLogger.log(null, FQCN, level, format(msg, args), null, t);
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