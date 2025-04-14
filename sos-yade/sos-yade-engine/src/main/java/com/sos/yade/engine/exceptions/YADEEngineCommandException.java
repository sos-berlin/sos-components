package com.sos.yade.engine.exceptions;

public class YADEEngineCommandException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    private String prefix;
    private int exitCode;
    private String std;

    public YADEEngineCommandException(String prefix, int exitCode, String std) {
        super(prefix + "[exitCode=" + exitCode + "]" + std);
        this.prefix = prefix;
        this.exitCode = exitCode;
        this.std = std;
    }

    public YADEEngineCommandException(String msg) {
        super(msg);
    }

    public YADEEngineCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineCommandException(YADEEngineCommandException cause) {
        super(cause);
        if (cause != null) {
            this.prefix = cause.prefix;
            this.exitCode = cause.exitCode;
            this.std = cause.std;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public int getExitCode() {
        return exitCode;
    }

}
