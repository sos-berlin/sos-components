package com.sos.commons.git;

import com.sos.commons.util.common.SOSCommandResult;

public class GitUtil {

    public static final GitCommandResult createGitCommandResult(SOSCommandResult commandResult) {
        return createGitCommandResult(commandResult, null);
    }
    
    public static final GitCommandResult createGitCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = new GitCommandResult(commandResult, original);
        } else {
            result = new GitCommandResult(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitStatusShortCommandResult createGitStatusShortCommandResult(SOSCommandResult commandResult) {
        return createGitStatusShortCommandResult(commandResult, null);
    }
    
    public static final GitStatusShortCommandResult createGitStatusShortCommandResult(SOSCommandResult commandResult, String original) {
        GitStatusShortCommandResult result;
        if (original != null) {
            result = new GitStatusShortCommandResult(commandResult, original);
        } else {
            result = new GitStatusShortCommandResult(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

}
