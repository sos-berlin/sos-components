package com.sos.js7.job.exception;

import com.sos.js7.job.JobArgumentValueIterator;

public class JobArgumentException extends JobException {

    private static final long serialVersionUID = 1L;

    public JobArgumentException(String message) {
        super(message);
    }

    public JobArgumentException(String msg, Throwable e) {
        super(msg, e);
    }

    public JobArgumentException(JobArgumentValueIterator iterator, Throwable e) {
        super(String.format("[%s][%s]%s", iterator.getArgumentName(), iterator.current(), e.toString()), e);
    }
}
