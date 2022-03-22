package com.sos.commons.git.results;

import com.sos.commons.util.common.SOSCommandResult;

public class GitDiffCommandResult extends GitCommandResult {

    protected GitDiffCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitDiffCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitDiffCommandResult(result, original);
    }
    
    @Override
    public void parseStdOut() {
        // no stdout for git add command, nothing to parse
    }

}
