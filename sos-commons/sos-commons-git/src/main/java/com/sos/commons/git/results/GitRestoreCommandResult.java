package com.sos.commons.git.results;

import com.sos.commons.util.common.SOSCommandResult;

public class GitRestoreCommandResult extends GitCommandResult {

    protected GitRestoreCommandResult(SOSCommandResult result) {
        super(result);
    }

    protected GitRestoreCommandResult(SOSCommandResult result, String original) {
        super(result, original);
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitRestoreCommandResult(result, original);
    }
    
    @Override
    public void parseStdOut() {
        // no stdout for git restore command, nothing to parse
    }

}
