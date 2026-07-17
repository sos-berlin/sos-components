package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineInitializationException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineInitializationException(Throwable cause) {
        super(cause, (IYADEProviderDelegator) null);
    }

    public YADEEngineInitializationException(String msg) {
        super(msg, (IYADEProviderDelegator) null);
    }

    public YADEEngineInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
