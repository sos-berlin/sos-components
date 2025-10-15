package com.sos.commons.git.results;

import com.sos.commons.util.beans.SOSCommandResult;

public abstract class GitCommandResult {

    private final String stdOut;
    private final String stdErr;
    private String command;
    private String originalCommand;
    private Integer exitCode;
    private Throwable exception;

    private GitCommandResultError error;

    protected GitCommandResult(SOSCommandResult result) {
        this.command = result.getCommand();
        this.originalCommand = result.getCommand();
        this.exitCode = result.getExitCode();
        this.exception = result.getException();
        this.stdErr = result.getStdErr();
        this.stdOut = result.getStdOut();
    }

    protected GitCommandResult(SOSCommandResult result, String original) {
        this.command = result.getCommand();
        this.originalCommand = original;
        this.exitCode = result.getExitCode();
        this.exception = result.getException();
        this.stdErr = result.getStdErr();
        this.stdOut = result.getStdOut();
    }

    public abstract void parseStdOut();

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public String getCommand() {
        return command;
    }

    /** can return null */
    public Integer getExitCode() {
        return exitCode;
    }

    public boolean isNonZeroExitCode() {
        return exitCode != null && exitCode.intValue() != 0;
    }

    public Throwable getException() {
        return exception;
    }

    public String getOriginalCommand() {
        return originalCommand;
    }

    public void setError(String msg) {
        setError(msg, null);
    }

    public void setError(String msg, Throwable e) {
        error = new GitCommandResultError(msg, e);
    }

    public GitCommandResultError getError() {
        return error;
    }

    public class GitCommandResultError {

        private final String message;
        private final Throwable exception;

        public GitCommandResultError(String error) {
            this(error, null);
        }

        public GitCommandResultError(String message, Throwable exception) {
            this.message = message;
            this.exception = exception;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getException() {
            return exception;
        }
    }
}
