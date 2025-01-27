package com.sos.yade.engine.exception;

import com.sos.commons.exception.SOSException;

/** Compatibility with YADE 1 serialized objects for history.<br/>
 * - to be removed (after rewriting YADE only SOSYADEEngineSourceConnectionException may be used) */
@Deprecated
public class SOSYadeSourceConnectionException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSYadeSourceConnectionException() {
        super();
    }

    public SOSYadeSourceConnectionException(String message) {
        super(message);
    }

    public SOSYadeSourceConnectionException(Throwable cause) {
        super(cause);
    }

    public SOSYadeSourceConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSYadeSourceConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
