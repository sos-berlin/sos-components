package com.sos.commons.git.results;

import com.sos.commons.util.common.SOSCommandResult;

public class GitPullCommandResult extends GitCommandResult {

    protected GitPullCommandResult(SOSCommandResult result) {
        super(result);
        // TODO Auto-generated constructor stub
    }

    protected GitPullCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        // TODO Auto-generated constructor stub
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitPullCommandResult(result, original);
    }
    
    @Override
    public void parseStdOut() {
        // TODO Auto-generated method stub
        
    }

}
