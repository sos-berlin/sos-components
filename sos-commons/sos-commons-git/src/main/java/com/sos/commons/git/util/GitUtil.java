package com.sos.commons.git.util;

import java.nio.file.Path;

import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.git.enums.GitConfigType;
import com.sos.commons.git.enums.GitConfigAction;
import com.sos.commons.git.results.GitAddCommandResult;
import com.sos.commons.git.results.GitCheckoutCommandResult;
import com.sos.commons.git.results.GitCherryPickCommandResult;
import com.sos.commons.git.results.GitCloneCommandResult;
import com.sos.commons.git.results.GitCommandResult;
import com.sos.commons.git.results.GitCommitCommandResult;
import com.sos.commons.git.results.GitConfigCommandResult;
import com.sos.commons.git.results.GitDiffCommandResult;
import com.sos.commons.git.results.GitLogCommandResult;
import com.sos.commons.git.results.GitLsRemoteCommandResult;
import com.sos.commons.git.results.GitPullCommandResult;
import com.sos.commons.git.results.GitPushCommandResult;
import com.sos.commons.git.results.GitRemoteCommandResult;
import com.sos.commons.git.results.GitRestoreCommandResult;
import com.sos.commons.git.results.GitStatusShortCommandResult;
import com.sos.commons.git.results.GitTagCommandResult;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;

public class GitUtil {
    
    private static final String ERR_MSG_UNSUPPORTED_OPTION_FORMAT = "Unsupported option %1$s. Options --local and --global supported only.";
    private static final String ERR_MSG_VALUE_MISSING = "New value for setting core.sshCommand missing.";
    private static final String ERR_MSG_KF_PATH_MISSING = "Path of the keyfile is missing.";
    private static final String ERR_MSG_USERNAME_MISSING = "Username is missing.";
    private static final String ERR_MSG_EMAIL_MISSING = "Email is missing.";

    
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

    public static final GitCommandResult createGitCommitCommandResult(SOSCommandResult commandResult) {
        return createGitCommitCommandResult(commandResult, null);
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

    public static final GitCommandResult createGitCheckoutCommandResult(SOSCommandResult commandResult) {
        return createGitCheckoutCommandResult(commandResult, null);
    }
    
    public static final GitCommandResult createGitCheckoutCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitCheckoutCommandResult.getInstance(commandResult, original);
        } else {
            result = GitCheckoutCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitLogCommandResult(SOSCommandResult commandResult) {
        return createGitLogCommandResult(commandResult, null);
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

    public static final GitCommandResult createGitPushCommandResult(SOSCommandResult commandResult) {
        return createGitPushCommandResult(commandResult, null);
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

    public static final GitCommandResult createGitCloneCommandResult(SOSCommandResult commandResult) {
        return createGitCloneCommandResult(commandResult, null);
    }

    public static final GitCommandResult createGitCloneCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitCloneCommandResult.getInstance(commandResult, original);
        } else {
            result = GitCloneCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitRemoteCommandResult(SOSCommandResult commandResult) {
        return createGitRemoteCommandResult(commandResult, null);
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

    public static final GitCommandResult createGitLsRemoteCommandResult(SOSCommandResult commandResult) {
        return createGitLsRemoteCommandResult(commandResult, null);
    }

    public static final GitCommandResult createGitLsRemoteCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitLsRemoteCommandResult.getInstance(commandResult, original);
        } else {
            result = GitLsRemoteCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitRestoreCommandResult(SOSCommandResult commandResult) {
        return createGitRestoreCommandResult(commandResult, null);
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

    public static final GitCommandResult createGitTagCommandResult(SOSCommandResult commandResult) {
        return createGitTagCommandResult(commandResult, null);
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

    public static final GitCommandResult createGitCherryPickCommandResult(SOSCommandResult commandResult) {
        return createGitCherryPickCommandResult(commandResult, null);
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

    public static final GitCommandResult createGitDiffCommandResult(SOSCommandResult commandResult) {
        return createGitDiffCommandResult(commandResult, null);
    }

    public static final GitCommandResult createGitDiffCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitDiffCommandResult.getInstance(commandResult, original);
        } else {
            result = GitDiffCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final GitCommandResult createGitConfigCommandResult(SOSCommandResult commandResult) {
        return createGitConfigCommandResult(commandResult, null);
    }

    public static final GitCommandResult createGitConfigCommandResult(SOSCommandResult commandResult, String original) {
        GitCommandResult result;
        if (original != null) {
            result = GitConfigCommandResult.getInstance(commandResult, original);
        } else {
            result = GitConfigCommandResult.getInstance(commandResult);
        }
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append(commandResult);
            result.setError(info.toString());
        }
        return result;
    }

    public static final String getConfigSshCommand(GitConfigType configType, GitConfigAction action) throws SOSException {
        return getConfigSshCommand(configType, action, false, null, null);
    }
    
    public static final String getConfigSshCommand(GitConfigType configType, GitConfigAction action, String newValue) throws SOSException {
        return getConfigSshCommand(configType, action, true, null, newValue);
    }
    
    public static final String getConfigSshCommand(GitConfigType configType, GitConfigAction action, Path keyFilePath) throws SOSException {
        return getConfigSshCommand(configType, action, false, keyFilePath, null);
    }
    
    public static final String getConfigSshCommand(GitConfigType configType, GitConfigAction action, boolean custom, Path keyFilePath, String newValue) throws SOSException {
        if (custom && (newValue == null || newValue.isEmpty())) {
            throw new SOSMissingDataException(ERR_MSG_VALUE_MISSING);
        }
        String command = null;
        switch(configType) {
        case LOCAL:
            switch(action) {
            case GET:
                command = GitCommandConstants.CMD_GIT_CONFIG_GET_LOCAL_SSH_COMMAND;
                break;
            case ADD:
                if(!custom && keyFilePath == null) {
                    throw new SOSMissingDataException(ERR_MSG_KF_PATH_MISSING);
                }
                if (SOSShell.IS_WINDOWS) {
                    if(custom) {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_PREFORMAT_WIN, newValue);
                    } else {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_LOCAL_SSH_COMMAND_PREFORMAT_WIN, keyFilePath.toString().replace('\\', '/'));
                    }
                } else {
                    if(custom) {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_LOCAL_SSH_COMMAND_FORMAT_LINUX, newValue);
                    } else {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_LOCAL_SSH_COMMAND_PREFORMAT_LINUX, keyFilePath.toString().replace('\\', '/'));
                    }
                }
                break;
            case UNSET:
                command = GitCommandConstants.CMD_GIT_CONFIG_UNSET_LOCAL_SSH_COMMAND;
                break;
            }
            break;
        case GLOBAL:
            switch(action) {
            case GET:
                command = GitCommandConstants.CMD_GIT_CONFIG_GET_GLOBAL_SSH_COMMAND;
                break;
            case ADD:
                if(!custom && keyFilePath == null) {
                    throw new SOSMissingDataException(ERR_MSG_KF_PATH_MISSING);
                }
                if (SOSShell.IS_WINDOWS) {
                    if(custom) {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_FORMAT_WIN, newValue);
                    } else {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_PREFORMAT_WIN, keyFilePath.toString().replace('\\', '/'));
                    }
                } else {
                    if(custom) {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_FORMAT_LINUX, newValue);
                    } else {
                        command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_PREFORMAT_LINUX, keyFilePath.toString().replace('\\', '/'));
                    }
                }
                break;
            case UNSET:
                command = GitCommandConstants.CMD_GIT_CONFIG_UNSET_GLOBAL_SSH_COMMAND;
                break;
            }
            break;
        case SYSTEM:
        case WORKTREE:
        case FILE:
        case BLOB:
            throw new SOSException(String.format(ERR_MSG_UNSUPPORTED_OPTION_FORMAT, configType.value()));
        }
        return command;
    }

    public static final String getConfigUsername(GitConfigType configType, GitConfigAction action) throws SOSException {
        return getConfigUsername(configType, action, null);
    }
    
    public static final String getConfigUsername(GitConfigType configType, GitConfigAction action, String username) throws SOSException {
        String command = null;
        switch(configType) {
        case LOCAL:
            switch(action) {
            case GET:
                command = GitCommandConstants.CMD_GIT_CONFIG_GET_LOCAL_USER_NAME;
                break;
            case ADD:
                if(username == null) {
                    throw new SOSMissingDataException(ERR_MSG_USERNAME_MISSING);
                }
                if (SOSShell.IS_WINDOWS) {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_LOCAL_USER_NAME_FORMAT_WIN, username);
                } else {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_LOCAL_USER_NAME_FORMAT_LINUX, username);
                }
                break;
            case UNSET:
                command = GitCommandConstants.CMD_GIT_CONFIG_UNSET_LOCAL_USER_NAME;
                break;
            }
            break;
        case GLOBAL:
            switch(action) {
            case GET:
                command = GitCommandConstants.CMD_GIT_CONFIG_GET_GLOBAL_USER_NAME;
                break;
            case ADD:
                if(username == null) {
                    throw new SOSMissingDataException(ERR_MSG_USERNAME_MISSING);
                }
                if (SOSShell.IS_WINDOWS) {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_USER_NAME_FORMAT_WIN, username);
                } else {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_USER_NAME_FORMAT_LINUX, username);
                }
                break;
            case UNSET:
                command = GitCommandConstants.CMD_GIT_CONFIG_UNSET_GLOBAL_USER_NAME;
                break;
            }
            break;
        case SYSTEM:
        case WORKTREE:
        case FILE:
        case BLOB:
            throw new SOSException(String.format(ERR_MSG_UNSUPPORTED_OPTION_FORMAT, configType.value()));
        }
        return command;
    }

    public static final String getConfigUserEmail(GitConfigType configType, GitConfigAction action) throws SOSException {
        return getConfigUserEmail(configType, action, null);
    }

    public static final String getConfigUserEmail(GitConfigType configType, GitConfigAction action, String email) throws SOSException {
        String command = null;
        switch(configType) {
        case LOCAL:
            switch(action) {
            case GET:
                command = GitCommandConstants.CMD_GIT_CONFIG_GET_LOCAL_USER_EMAIL;
                break;
            case ADD:
                if(email == null) {
                    throw new SOSMissingDataException(ERR_MSG_EMAIL_MISSING);
                }
                if (SOSShell.IS_WINDOWS) {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_LOCAL_USER_EMAIL_FORMAT_WIN, email);
                } else {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_LOCAL_USER_EMAIL_FORMAT_LINUX, email);
                }
                break;
            case UNSET:
                command = GitCommandConstants.CMD_GIT_CONFIG_UNSET_LOCAL_USER_EMAIL;
                break;
            }
            break;
        case GLOBAL:
            switch(action) {
            case GET:
                command = GitCommandConstants.CMD_GIT_CONFIG_GET_GLOBAL_USER_EMAIL;
                break;
            case ADD:
                if(email == null) {
                    throw new SOSMissingDataException(ERR_MSG_EMAIL_MISSING);
                }
                if (SOSShell.IS_WINDOWS) {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_USER_EMAIL_FORMAT_WIN, email);
                } else {
                    command = String.format(GitCommandConstants.CMD_GIT_CONFIG_ADD_GLOBAL_USER_EMAIL_FORMAT_LINUX, email);
                }
                break;
            case UNSET:
                command = GitCommandConstants.CMD_GIT_CONFIG_UNSET_GLOBAL_USER_EMAIL;
                break;
            }
            break;
        case SYSTEM:
        case WORKTREE:
        case FILE:
        case BLOB:
            throw new SOSException(String.format(ERR_MSG_UNSUPPORTED_OPTION_FORMAT, configType.value()));
        }
        return command;
    }
    
}
