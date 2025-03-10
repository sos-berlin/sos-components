package com.sos.commons.util.loggers.base;

/** Logging interface for applications that can be used in JS7 jobs (job logger) or standalone (slf4j API). */
public interface ISOSLogger {

    public void info(final Object msg);

    public void info(final String format, final Object... args);

    public void debug(final Object msg);

    public void debug(final String format, final Object... args);

    public void trace(final Object msg);

    public void trace(final String format, final Object... args);

    public void warn(final Object msg);

    public void warn(final String format, final Object... args);

    public void warn(final String msg, Throwable e);

    public void error(final Object msg);

    public void error(final String format, final Object... args);

    public void error(final String msg, Throwable e);

    public void error(Throwable e);

    public boolean isDebugEnabled();

    public boolean isTraceEnabled();

}
