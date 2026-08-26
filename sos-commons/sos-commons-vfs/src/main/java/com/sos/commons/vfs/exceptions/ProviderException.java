package com.sos.commons.vfs.exceptions;

import com.sos.commons.exception.SOSException;
import com.sos.commons.util.SOSString;

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
        super(msg + getMethodName() + cause, cause);
    }

    private static String getMethodName() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        if (st.length > 3) {
            String n = st[3].getMethodName();
            if (!SOSString.isEmpty(n) && !n.startsWith("<")) { // <init>
                return "[" + n + "]";
            }
        }
        return "";
    }
}
