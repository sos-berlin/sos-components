package com.sos.commons.util.common.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/** slf4j-based implementation for applications that can be used in JS7 jobs (job logger) or standalone (slf4j API). */
public class SOSSlf4jLogger implements ISOSLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSlf4jLogger.class);

    private static final String LOGGER_CLASS_NAME = SOSSlf4jLogger.class.getName();
    private static final String THREAD_CLASS_NAME = Thread.class.getName();

    private final boolean isDebugEnabled;
    private final boolean isTraceEnabled;

    public SOSSlf4jLogger() {
        isDebugEnabled = LOGGER.isDebugEnabled();
        isTraceEnabled = LOGGER.isTraceEnabled();
    }

    @Override
    public void info(Object msg) {
        try {
            setMDC();
            LOGGER.info(String.valueOf(msg));
        } finally {
            clearMDC();
        }
    }

    @Override
    public void info(String format, Object... args) {
        try {
            setMDC();
            if (args.length > 0) {
                LOGGER.info(format, args);
            } else {
                LOGGER.info(format);
            }
        } finally {
            clearMDC();
        }
    }

    @Override
    public void debug(Object msg) {
        if (!isDebugEnabled()) {
            return;
        }

        try {
            setMDC();
            LOGGER.debug(String.valueOf(msg));
        } finally {
            clearMDC();
        }
    }

    @Override
    public void debug(String format, Object... args) {
        if (!isDebugEnabled()) {
            return;
        }

        try {
            setMDC();
            if (args.length > 0) {
                LOGGER.debug(format, args);
            } else {
                LOGGER.debug(format);
            }
        } finally {
            clearMDC();
        }
    }

    @Override
    public void trace(Object msg) {
        if (!isTraceEnabled()) {
            return;
        }

        try {
            setMDC();
            LOGGER.trace(String.valueOf(msg));
        } finally {
            clearMDC();
        }
    }

    @Override
    public void trace(String format, Object... args) {
        if (!isTraceEnabled()) {
            return;
        }

        try {
            setMDC();
            if (args.length > 0) {
                LOGGER.trace(format, args);
            } else {
                LOGGER.trace(format);
            }
        } finally {
            clearMDC();
        }
    }

    @Override
    public void warn(Object msg) {
        try {
            setMDC();
            LOGGER.warn(String.valueOf(msg));
        } finally {
            clearMDC();
        }
    }

    @Override
    public void warn(String format, Object... args) {
        try {
            setMDC();
            if (args.length > 0) {
                LOGGER.warn(format, args);
            } else {
                LOGGER.warn(format);
            }
        } finally {
            clearMDC();
        }
    }

    @Override
    public void warn(String msg, Throwable e) {
        try {
            setMDC();
            LOGGER.warn(msg, e);
        } finally {
            clearMDC();
        }
    }

    @Override
    public void error(Object msg) {
        try {
            setMDC();
            LOGGER.error(String.valueOf(msg));
        } finally {
            clearMDC();
        }
    }

    @Override
    public void error(String format, Object... args) {
        try {
            setMDC();
            if (args.length > 0) {
                LOGGER.error(format, args);
            } else {
                LOGGER.error(format);
            }
        } finally {
            clearMDC();
        }
    }

    @Override
    public void error(String msg, Throwable e) {
        try {
            setMDC();
            LOGGER.error(msg, e);
        } finally {
            clearMDC();
        }
    }

    @Override
    public void error(Throwable e) {
        try {
            setMDC();
            LOGGER.warn(e.toString(), e);
        } finally {
            clearMDC();
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

    private void setMDC() {
        StackTraceElement caller = findCaller();
        if (caller != null) {
            MDC.put("F", caller.getFileName());
            MDC.put("L", String.valueOf(caller.getLineNumber()));
        }
    }

    private void clearMDC() {
        MDC.clear();
    }

    private static StackTraceElement findCaller() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        // use a normal loop instead of a stream solution...
        // 0 - info/debug/... , 1 - findCaller() self, from 2 - caller methods
        // do not use st[2] directly, but from 2 to the end:
        // - because, for example, in some JVM implementations additional stack trace elements can be added, or nested calls, or ...
        for (int i = 2; i < st.length; i++) {
            StackTraceElement e = st[i];
            String cn = e.getClassName();
            if (!cn.equals(LOGGER_CLASS_NAME) && !cn.startsWith(THREAD_CLASS_NAME)) {// e.g. java.lang.Thread$Worker
                return e;
            }
        }
        return null;
    }

}
