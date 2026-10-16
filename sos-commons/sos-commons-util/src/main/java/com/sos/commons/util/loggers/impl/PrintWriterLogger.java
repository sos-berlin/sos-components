package com.sos.commons.util.loggers.impl;

import java.io.PrintWriter;
import java.util.function.Consumer;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.loggers.base.ASOSPrintLoggerAdapter;
import com.sos.commons.util.loggers.base.AsyncPrintWriter;
import com.sos.commons.util.loggers.base.ISOSLogger;

/** An {@link ASOSPrintLoggerAdapter} implementation that writes log messages to separate output and error {@link java.io.PrintWriter} instances, with optional
 * asynchronous output. */
public class PrintWriterLogger extends ASOSPrintLoggerAdapter {

    private final boolean async;

    private PrintWriter out;
    private PrintWriter err;

    private AsyncPrintWriter asyncWriter;

    public PrintWriterLogger() {
        super();
        this.async = false;
    }

    public PrintWriterLogger(ISOSLogger provider, boolean isInfoEnabled, boolean async) {
        super(provider, isInfoEnabled);
        this.async = async;

        if (provider != null && provider instanceof PrintWriterLogger) {
            PrintWriterLogger p = (PrintWriterLogger) provider;
            setOutAndErrWriters(p.out, p.err);
        }
    }

    public void setOutAndErrWriters(PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;

        if (async) {
            this.asyncWriter = new AsyncPrintWriter(out, err, 100_000, "async-print-writer-logger");
        }
    }

    @Override
    public Consumer<String> getOut() {
        if (!async) {
            return out::println;
        }

        if (asyncWriter == null) {
            throw new IllegalStateException("AsyncPrintWriter is not initialized");
        }
        return asyncWriter.out();
    }

    @Override
    public Consumer<String> getErr() {
        if (!async) {
            return err::println;
        }

        if (asyncWriter == null) {
            throw new IllegalStateException("AsyncPrintWriter is not initialized");
        }
        return asyncWriter.err();
    }

    public void close() {
        SOSClassUtil.closeQuietly(asyncWriter);
        asyncWriter = null;
    }

}
