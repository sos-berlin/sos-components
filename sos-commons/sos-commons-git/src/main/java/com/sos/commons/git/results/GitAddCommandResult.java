package com.sos.commons.git.results;

import com.sos.commons.util.common.SOSCommandResult;

public class GitAddCommandResult extends GitCommandResult {

    protected GitAddCommandResult(SOSCommandResult result) {
        super(result);
        // TODO Auto-generated constructor stub
    }

    protected GitAddCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        // TODO Auto-generated constructor stub
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitAddCommandResult(result, original);
    }
    
    @Override
    public void parseStdOut() {
        // nothing to parse
        
    }

}
