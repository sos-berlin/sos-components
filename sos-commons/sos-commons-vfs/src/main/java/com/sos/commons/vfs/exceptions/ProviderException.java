package com.sos.commons.vfs.exceptions;

import com.sos.commons.exception.SOSException;

public class ProviderException extends SOSException {

    private static final long serialVersionUID = 1L;

    public ProviderException() {
        super();
    }

    public ProviderException(String msg) {
        super(getMethodName() + msg);
    }

    public ProviderException(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }

    public ProviderException(String msg, Throwable cause) {
        super(getMethodName() + msg + cause, cause);
    }

    private static String getMethodName() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        if (st.length > 3) {
            return "[" + st[3].getMethodName() + "]";
        }
        return "";
    }
}
