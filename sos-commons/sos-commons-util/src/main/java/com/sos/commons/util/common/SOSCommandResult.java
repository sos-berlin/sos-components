package com.sos.commons.util.common;

import java.nio.charset.Charset;

public class SOSCommandResult {

    private final StringBuilder stdOut;
    private final StringBuilder stdErr;
    private final Charset charset;
    private final SOSTimeout timeout;

    private Throwable exception;
    private String command;
    private Integer exitCode;
    private boolean timeoutExeeded;

    public SOSCommandResult(String cmd) {
        this(cmd, null, null);
    }

    public SOSCommandResult(String cmd, Charset charset) {
        this(cmd, charset, null);
    }

    public SOSCommandResult(String cmd, SOSTimeout timeout) {
        this(cmd, null, timeout);
    }

    public SOSCommandResult(String cmd, Charset charset, SOSTimeout timeout) {
        this.command = cmd;
        this.charset = charset;
        this.timeout = timeout;
        this.stdOut = new StringBuilder();
        this.stdErr = new StringBuilder();
    }

    public void setCommand(String val) {
        command = val;
    }

    public String getCommand() {
        return command;
    }

    public Charset getCharset() {
        return charset;
    }

    public SOSTimeout getTimeout() {
        return timeout;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer val) {
        exitCode = val;
    }

    public boolean isTimeoutExeeded() {
        return timeoutExeeded;
    }

    public void setTimeoutExeeded(boolean val) {
        timeoutExeeded = val;
    }

    public String getStdOut() {
        return stdOut.toString();
    }

    public void setStdOut(String val) {
        stdOut.append(val);
    }

    public boolean hasStdOut() {
        return stdOut.length() > 0;
    }

    public String getStdErr() {
        return stdErr.toString();
    }

    public void setStdErr(String val) {
        stdErr.append(val);
    }

    public boolean hasStdErr() {
        return stdErr.length() > 0;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable val) {
        exception = val;
    }

    public boolean hasError() {
        return hasError(true);
    }

    public boolean hasError(boolean checkStdError) {
        if (exception != null || timeoutExeeded) {
            return true;
        }
        if (exitCode != null && exitCode > 0) {
            return true;
        }
        if (checkStdError && stdErr.length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(command).append("]");
        sb.append("[exitCode=").append(exitCode).append("]");
        if (charset != null) {
            sb.append("[charset=").append(charset).append("]");
        }
        if (timeout != null) {
            sb.append("[timeout=").append(timeout);
            if (timeoutExeeded) {
                sb.append(",timeoutExeeded=true");
            }
            sb.append("]");
        }
        sb.append("[std:out=").append(stdOut.toString().trim()).append("]");
        sb.append("[std:err=").append(stdErr.toString().trim()).append("]");
        if (exception != null) {
            sb.append("[exception=").append(exception.toString()).append("]");
        }
        return sb.toString();
    }
}
