package com.sos.yade.engine.exceptions;

/** Creates a truncated exception to be thrown on the Host YADE Client.<br/>
 * - truncated - because otherwise the entire STD (including Jump Client headers/transfer/footer) is contained in the exception */
public class YADEEngineJumpHostCommandException extends YADEEngineCommandException {

    private static final long serialVersionUID = 1L;

    public YADEEngineJumpHostCommandException(YADEEngineCommandException cause) {
        super(getTruncatedError(cause));
    }

    private static String getTruncatedError(YADEEngineCommandException cause) {
        return cause.getPrefix() + "exitCode=" + cause.getExitCode();
    }
}
