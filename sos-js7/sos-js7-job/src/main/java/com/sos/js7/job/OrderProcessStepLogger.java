package com.sos.js7.job;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;
import com.sos.commons.util.loggers.base.ASOSPrintLoggerAdapter;
import com.sos.commons.util.loggers.impl.PrintWriterLogger;
import com.sos.commons.util.loggers.impl.Slf4jLogger;

import js7.launcher.forjava.internal.BlockingInternalJob;

/** Represents a logger object which supports five log levels:<br/>
 * INFO, DEBUG, TRACE, WARN, and ERROR. */
public class OrderProcessStepLogger extends PrintWriterLogger {

    protected OrderProcessStepLogger(BlockingInternalJob.Step internalStep) {
        super();
        if (internalStep == null) {// JUnit Test
            setDelegator(new Slf4jLogger(ASOSPrintLoggerAdapter.class));
        } else {
            setOutAndErrWriters(internalStep.out(), internalStep.err());
        }
    }

    protected void init(Object args) {
        if (args != null && args instanceof JobArguments) {
            JobArguments ja = (JobArguments) args;
            setLogLevel(ja.getLogLevel().getValue());
        }
    }

    protected Throwable handleException(Throwable e) {
        if (e == null || e instanceof ISOSRequiredArgumentMissingException) {
            return null;
        }
        return e;
    }

    @Override
    public void close() {
        super.close();
    }

}
