package com.sos.commons.util.common;

public class SOSCommandResult {

    private final StringBuilder stdOut;
    private final StringBuilder stdErr;

    private String command;
    private Integer exitCode;
    private Throwable exception;

    public SOSCommandResult(String cmd) {
        command = cmd;
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
    }

    public void setCommand(String val) {
        command = val;
    }

    public String getCommand() {
        return command;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer val) {
        exitCode = val;
    }

    public StringBuilder getStdOut() {
        return stdOut;
    }

    public void setStdOut(String val) {
        stdOut.append(val);
    }

    public StringBuilder getStdErr() {
        return stdErr;
    }

    public void setStdErr(String val) {
        stdErr.append(val);
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable val) {
        exception = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(command).append("]");
        sb.append("[exitCode=").append(exitCode).append("]");
        sb.append("[std:out=").append(stdOut.toString().trim()).append("]");
        sb.append("[std:err=").append(stdErr.toString().trim()).append("]");
        if (exception != null) {
            sb.append("[exception=").append(exception.toString()).append("]");
        }
        return sb.toString();
    }
}
