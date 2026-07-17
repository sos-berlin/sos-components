package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineCommandException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    private String prefix;
    private Integer exitCode;
    private String std;

    public YADEEngineCommandException(String prefix, Integer exitCode, String std, IYADEProviderDelegator delegator) {
        super(prefix + (exitCode == null ? "" : "[exitCode=" + exitCode + "]") + std, delegator);
        this.prefix = prefix;
        this.exitCode = exitCode;
        this.std = std;
    }

    public YADEEngineCommandException(String msg, IYADEProviderDelegator delegator) {
        super(msg, delegator);
    }

    public YADEEngineCommandException(String msg, Throwable cause, IYADEProviderDelegator delegator) {
        super(msg, cause, delegator);
    }

    public YADEEngineCommandException(YADEEngineCommandException cause) {
        super(cause, cause.getDelegator());
        if (cause != null) {
            this.prefix = cause.prefix;
            this.exitCode = cause.exitCode;
            this.std = cause.std;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setExitCode(Integer val) {
        exitCode = val;
    }

    public Integer getExitCode() {
        return exitCode;
    }

}
