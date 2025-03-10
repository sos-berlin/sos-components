package com.sos.commons.git.results;

import com.sos.commons.util.beans.SOSCommandResult;

public class GitAddCommandResult extends GitCommandResult {

    protected GitAddCommandResult(SOSCommandResult result) {
        super(result);
    }

    protected GitAddCommandResult(SOSCommandResult result, String original) {
        super(result, original);
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitAddCommandResult(result, original);
    }
    
    @Override
    public void parseStdOut() {
        // no stdout for git add command, nothing to parse
    }

}
