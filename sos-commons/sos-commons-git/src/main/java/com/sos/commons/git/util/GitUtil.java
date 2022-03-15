package com.sos.commons.git.util;

import com.sos.commons.git.results.GitAddCommandResult;
import com.sos.commons.git.results.GitCherryPickCommandResult;
import com.sos.commons.git.results.GitCommandResult;
import com.sos.commons.git.results.GitCommitCommandResult;
import com.sos.commons.git.results.GitLogCommandResult;
import com.sos.commons.git.results.GitPullCommandResult;
import com.sos.commons.git.results.GitPushCommandResult;
import com.sos.commons.git.results.GitRemoteCommandResult;
import com.sos.commons.git.results.GitRestoreCommandResult;
import com.sos.commons.git.results.GitStatusShortCommandResult;
import com.sos.commons.git.results.GitTagCommandResult;
import com.sos.commons.util.common.SOSCommandResult;

public class GitUtil {

    public static final GitCommandResult createGitStatusShortCommandResult(SOSCommandResult commandResult) {
        return createGitStatusShortCommandResult(commandResult, null);
    }
    
    public static final GitCommandResult createGitStatusShortCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitStatusShortCommandResult.getInstance(commandResult, original);
        } else {
            result = GitStatusShortCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitAddCommandResult(SOSCommandResult commandResult) {
        return createGitAddCommandResult(commandResult, null);
    }

    public static final GitCommandResult createGitAddCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitAddCommandResult.getInstance(commandResult, original);
        } else {
            result = GitAddCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitAddAllCommandResult(SOSCommandResult commandResult) {
        return createGitAddAllCommandResult(commandResult, null);
    }

    public static final GitCommandResult createGitAddAllCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitAddCommandResult.getInstance(commandResult, original);
        } else {
            result = GitAddCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitCommitCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitCommitCommandResult.getInstance(commandResult, original);
        } else {
            result = GitCommitCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitLogCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitLogCommandResult.getInstance(commandResult, original);
        } else {
            result = GitLogCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitPullCommandResult(SOSCommandResult commandResult) {
        return createGitPullCommandResult(commandResult, null);
    }

    public static final GitCommandResult createGitPullCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitPullCommandResult.getInstance(commandResult, original);
        } else {
            result = GitPullCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitPushCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitPushCommandResult.getInstance(commandResult, original);
        } else {
            result = GitPushCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitRemoteCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitRemoteCommandResult.getInstance(commandResult, original);
        } else {
            result = GitRemoteCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitRestoreCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitRestoreCommandResult.getInstance(commandResult, original);
        } else {
            result = GitRestoreCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitTagCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitTagCommandResult.getInstance(commandResult, original);
        } else {
            result = GitTagCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitCherryPickCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitCherryPickCommandResult.getInstance(commandResult, original);
        } else {
            result = GitCherryPickCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

}
