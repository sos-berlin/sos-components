package com.sos.commons.util.loggers.impl;

import java.util.function.Consumer;

import com.sos.commons.util.loggers.base.ASOSPrintLoggerAdapter;
import com.sos.commons.util.loggers.base.ISOSLogger;

/** An {@link ASOSPrintLoggerAdapter} implementation that writes log messages to the standard output and error streams. */
public class StdLogger extends ASOSPrintLoggerAdapter {

    public StdLogger() {
        super();
    }

    public StdLogger(ISOSLogger provider, boolean isInfoEnabled) {
        super(provider, isInfoEnabled);
    }

    public StdLogger(boolean isInfoEnabled, boolean isDebugEnabled, boolean isTraceEnabled) {
        super(isInfoEnabled, isDebugEnabled, isTraceEnabled);
    }

    @Override
    public Consumer<String> getOut() {
        return System.out::println;
    }

    @Override
    public Consumer<String> getErr() {
        return System.err::println;
    }

}