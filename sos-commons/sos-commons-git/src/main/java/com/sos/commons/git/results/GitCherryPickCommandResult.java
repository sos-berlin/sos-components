package com.sos.commons.git.results;

import com.sos.commons.util.common.SOSCommandResult;

public class GitCherryPickCommandResult extends GitCommandResult {

    protected GitCherryPickCommandResult(SOSCommandResult result) {
        super(result);
        // TODO Auto-generated constructor stub
    }

    protected GitCherryPickCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        // TODO Auto-generated constructor stub
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitCherryPickCommandResult(result, original);
    }
    
    @Override
    public void parseStdOut() {
        // TODO Auto-generated method stub
        
    }

}
