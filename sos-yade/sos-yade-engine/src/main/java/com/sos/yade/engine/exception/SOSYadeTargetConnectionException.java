package com.sos.yade.engine.exception;

import com.sos.commons.exception.SOSException;

/** Compatibility with YADE 1 serialized objects for history.<br/>
 * - to be removed (after rewriting YADE only SOSYADEEngineTargetConnectionException may be used) */
@Deprecated
public class SOSYadeTargetConnectionException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSYadeTargetConnectionException() {
        super();
    }

    public SOSYadeTargetConnectionException(String message) {
        super(message);
    }

    public SOSYadeTargetConnectionException(Throwable cause) {
        super(cause);
    }

    public SOSYadeTargetConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSYadeTargetConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
