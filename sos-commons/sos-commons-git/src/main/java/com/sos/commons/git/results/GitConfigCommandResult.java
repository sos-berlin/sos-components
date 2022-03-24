package com.sos.commons.git.results;

import com.sos.commons.util.common.SOSCommandResult;

public class GitConfigCommandResult extends GitCommandResult {

    private String currentValue = "";

    protected GitConfigCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitConfigCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitConfigCommandResult(result, original);
    }
    
    public String getCurrentValue() {
        return currentValue;
    }
    
    @Override
    public void parseStdOut() {
        if (getStdOut() != null && !getStdOut().isEmpty()) {
            currentValue = getStdOut();
        }
    }

}
