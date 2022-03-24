package com.sos.commons.git;

import java.nio.file.Path;

import com.sos.commons.exception.SOSException;
import com.sos.commons.git.enums.GitConfigType;
import com.sos.commons.git.enums.GitConfigAction;
import com.sos.commons.git.results.GitCommandResult;
import com.sos.commons.git.util.GitCommandConstants;
import com.sos.commons.git.util.GitUtil;
import com.sos.commons.util.SOSShell;

public class GitCommand {
    private static final String DELIMITER_WINDOWS = " && ";
    private static final String DELIMITER_LINUX = " ; ";
    
    public static GitCommandResult executeGitStatusShort() {
        return executeGitStatusShort(null, null);
    }

    public static GitCommandResult executeGitStatusShort(Path repository) {
        return executeGitStatusShort(repository, null);
    }

    public static GitCommandResult executeGitStatusShort(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitStatusShortCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_STATUS_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitStatusShortCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_STATUS_SHORT)), GitCommandConstants.CMD_GIT_STATUS_SHORT);
            if (workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }

    public static GitCommandResult executeGitPull() {
        return executeGitPull(null, null);
    }
    
    public static GitCommandResult executeGitPull(Path repository) {
        return executeGitPull(repository, null);
    }
    
    public static GitCommandResult executeGitPull(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_PULL));
        } else {
            GitCommandResult result = GitUtil.createGitPullCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_PULL)), GitCommandConstants.CMD_GIT_PULL);
            if (workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    

    public static GitCommandResult executeGitAdd() {
        return executeGitAdd(null, null);
    }
    
    public static GitCommandResult executeGitAdd(Path repository) {
        return executeGitAdd(repository, null);
    }
    
    public static GitCommandResult executeGitAdd(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitAddCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_ADD));
        } else {
            GitCommandResult result = GitUtil.createGitAddCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_ADD)), GitCommandConstants.CMD_GIT_ADD);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitAddAll() {
        return executeGitAddAll(null, null);
    }
    
    public static GitCommandResult executeGitAddAll(Path repository) {
        return executeGitAddAll(repository, null);
    }
    
    public static GitCommandResult executeGitAddAll(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitAddAllCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_ADD_ALL));
        } else {
            GitCommandResult result = GitUtil.createGitAddAllCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_ADD_ALL)), GitCommandConstants.CMD_GIT_ADD_ALL);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitCommit() {
        return executeGitCommit(null, null);
    }
    
    public static GitCommandResult executeGitCommit(Path repository) {
        return executeGitCommit(repository, null);
    }
    
    public static GitCommandResult executeGitCommit(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_COMMIT));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_COMMIT)), GitCommandConstants.CMD_GIT_COMMIT);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitCommitFormatted(String message) {
        return executeGitCommitFormatted(message, null, null);
    }
    
    public static GitCommandResult executeGitCommitFormatted(String message, Path repository) {
        return executeGitCommitFormatted(message, repository, null);
    }
    
    public static GitCommandResult executeGitCommitFormatted(String message, Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(String.format(GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message)));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, String.format(GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message))), 
                    String.format(GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message));
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitAddAndCommitExisting() {
        return executeGitAddAndCommitExisting(null, null);
    }
    
    public static GitCommandResult executeGitAddAndCommitExisting(Path repository) {
        return executeGitAddAndCommitExisting(repository, null);
    }
    
    public static GitCommandResult executeGitAddAndCommitExisting(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_ADD_AND_COMMIT)), GitCommandConstants.CMD_GIT_ADD_AND_COMMIT);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitAddAndCommitExistingFormatted(String message) {
        return executeGitAddAndCommitExistingFormatted(message, null, null);
    }
    
    public static GitCommandResult executeGitAddAndCommitExistingFormatted(String message, Path repository) {
        return executeGitAddAndCommitExistingFormatted(message, repository, null);
    }
    
    public static GitCommandResult executeGitAddAndCommitExistingFormatted(String message, Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(String.format(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message)));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, String.format(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message))), 
                    String.format(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message));
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitPush() {
        return executeGitPush(null, null);
    }
    
    public static GitCommandResult executeGitPush(Path repository) {
        return executeGitPush(repository, null);
    }
    
    public static GitCommandResult executeGitPush(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitPushCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_PUSH));
        } else {
            GitCommandResult result = GitUtil.createGitPushCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_PUSH)), GitCommandConstants.CMD_GIT_PUSH);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitClone(String remoteRepositoryUri) {
        return executeGitClone(remoteRepositoryUri, null, null);
    }
    
    public static GitCommandResult executeGitClone(String remoteRepositoryUri, Path repository) {
        return executeGitClone(remoteRepositoryUri, repository, null);
    }
    
    public static GitCommandResult executeGitClone(String remoteRepositoryUri, Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitCloneCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_CLONE + remoteRepositoryUri));
        } else {
            GitCommandResult result = GitUtil.createGitCloneCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_CLONE + remoteRepositoryUri)),
                    GitCommandConstants.CMD_GIT_CLONE + remoteRepositoryUri);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitTag() {
        return executeGitTag(null, null);
    }
    
    public static GitCommandResult executeGitTag(Path repository) {
        return executeGitTag(repository, null);
    }
    
    public static GitCommandResult executeGitTag(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitTagCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_TAG));
        } else {
            GitCommandResult result = GitUtil.createGitTagCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_TAG)), GitCommandConstants.CMD_GIT_TAG);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteRead() {
        return executeGitRemoteRead(null, null);
    }
    
    public static GitCommandResult executeGitRemoteRead(Path repository) {
        return executeGitRemoteRead(repository, null);
    }
    
    public static GitCommandResult executeGitRemoteRead(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_V));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_V)), GitCommandConstants.CMD_GIT_REMOTE_V);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteAdd(String shortName, String remoteURI) {
        return executeGitRemoteAdd(shortName, remoteURI, null, null);
    }
    
    public static GitCommandResult executeGitRemoteAdd(String shortName, String remoteURI, Path repository) {
        return executeGitRemoteAdd(shortName, remoteURI, repository, null);
    }
    
    public static GitCommandResult executeGitRemoteAdd(String shortName, String remoteURI, Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_ADD + shortName + " " + remoteURI));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_ADD + shortName + " " + remoteURI)),
                    GitCommandConstants.CMD_GIT_REMOTE_ADD + shortName + " " + remoteURI);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteRemove(String shortName) {
        return executeGitRemoteRemove(shortName, null, null);
    }
    
    public static GitCommandResult executeGitRemoteRemove(String shortName, Path repository) {
        return executeGitRemoteRemove(shortName, repository, null);
    }
    
    public static GitCommandResult executeGitRemoteRemove(String shortName, Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_REMOVE + shortName));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_REMOVE + shortName)),
                    GitCommandConstants.CMD_GIT_REMOTE_REMOVE + shortName);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteUpdate() {
        return executeGitRemoteUpdate(null, null);
    }
    
    public static GitCommandResult executeGitRemoteUpdate(Path repository) {
        return executeGitRemoteUpdate(repository, null);
    }
    
    public static GitCommandResult executeGitRemoteUpdate(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_UPDATE));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_UPDATE)), GitCommandConstants.CMD_GIT_REMOTE_UPDATE);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitLog() {
        return executeGitLog(null, null);
    }
    
    public static GitCommandResult executeGitLog(Path repository) {
        return executeGitLog(repository, null);
    }
    
    public static GitCommandResult executeGitLog(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitLogCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_LOG));
        } else {
            GitCommandResult result = GitUtil.createGitLogCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_LOG)), GitCommandConstants.CMD_GIT_LOG);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitLogParseable() {
        return executeGitLogParseable(null, null);
    }
    
    public static GitCommandResult executeGitLogParseable(Path repository) {
        return executeGitLogParseable(repository, null);
    }
    
    public static GitCommandResult executeGitLogParseable(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitLogCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_LOG_ONE_LINE));
        } else {
            GitCommandResult result = GitUtil.createGitLogCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_LOG_ONE_LINE)), GitCommandConstants.CMD_GIT_LOG_ONE_LINE);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitCherryPick() {
        return executeGitCherryPick(null, null);
    }
    
    public static GitCommandResult executeGitCherryPick(Path repository) {
        return executeGitCherryPick(repository, null);
    }
    
    public static GitCommandResult executeGitCherryPick(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitCherryPickCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_CHERRY_PICK));
        } else {
            GitCommandResult result = GitUtil.createGitCherryPickCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_CHERRY_PICK)), GitCommandConstants.CMD_GIT_CHERRY_PICK);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRestore() {
        return executeGitRestore(null, null);
    }
    
    public static GitCommandResult executeGitRestore(Path repository) {
        return executeGitRestore(repository, null);
    }
    
    public static GitCommandResult executeGitRestore(Path repository, Path workingDir) {
        // use "git restore <file>..." to discard changes in working directory
        if (repository == null) {
            return GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_RESTORE));
        } else {
            GitCommandResult result = GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_RESTORE)), GitCommandConstants.CMD_GIT_RESTORE);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRestoreStaged() {
        return executeGitRestoreStaged(null, null);
    }
    
    public static GitCommandResult executeGitRestoreStaged(Path repository) {
        return executeGitRestoreStaged(repository, null);
    }
    
    public static GitCommandResult executeGitRestoreStaged(Path repository, Path workingDir) {
        // use "git restore --staged <file>..." to unstage
        if (repository == null) {
            return GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_RESTORE));
        } else {
            GitCommandResult result = GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_RESTORE)), GitCommandConstants.CMD_GIT_RESTORE);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfig(String params) {
        return executeGitConfig(params, null, null);
    }
    
    public static GitCommandResult executeGitConfig(String params, Path repository) {
        return executeGitConfig(params, repository, null);
    }
    
    public static GitCommandResult executeGitConfig(String params, Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_CONFIG + params));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_CONFIG + params)), GitCommandConstants.CMD_GIT_CONFIG + params);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshGet(GitConfigType configType) throws SOSException {
        return executeGitConfigSshGet(configType, null, null);
    }
    
    public static GitCommandResult executeGitConfigSshGet(GitConfigType configType, Path repository) throws SOSException {
        return executeGitConfigSshGet(configType, repository, null);
    }
    
    public static GitCommandResult executeGitConfigSshGet(GitConfigType configType, Path repository, Path workingDir) throws SOSException {
        String command = GitUtil.getConfigCommand(configType, GitConfigAction.GET);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command)), command);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshUnset(GitConfigType configType) throws SOSException {
        return executeGitConfigSshUnset(configType, null, null);
    }
    
    public static GitCommandResult executeGitConfigSshUnset(GitConfigType configType, Path repository) throws SOSException {
        return executeGitConfigSshUnset(configType, repository, null);
    }
    
    public static GitCommandResult executeGitConfigSshUnset(GitConfigType configType, Path repository, Path workingDir) throws SOSException {
        String command = GitUtil.getConfigCommand(configType, GitConfigAction.UNSET);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command)), command);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshAdd(GitConfigType configType, Path keyFilePath) throws SOSException {
        return executeGitConfigSshAdd(configType, keyFilePath, null, null);
    }
    
    public static GitCommandResult executeGitConfigSshAdd(GitConfigType configType, Path keyFilePath, Path repository) throws SOSException {
        return executeGitConfigSshAdd(configType, keyFilePath, repository, null);
    }
    
    public static GitCommandResult executeGitConfigSshAdd(GitConfigType configType, Path keyFilePath, Path repository, Path workingDir) throws SOSException {
        String command = GitUtil.getConfigCommand(configType, GitConfigAction.ADD, keyFilePath);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command)), command);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshAddCustom(GitConfigType configType, String value) throws SOSException {
        return executeGitConfigSshAddCustom(configType, value, null, null);
    }
    
    public static GitCommandResult executeGitConfigSshAddCustom(GitConfigType configType, String value, Path repository) throws SOSException {
        return executeGitConfigSshAddCustom(configType, value, repository, null);
    }
    
    public static GitCommandResult executeGitConfigSshAddCustom(GitConfigType configType, String value, Path repository, Path workingDir) throws SOSException {
        String command = GitUtil.getConfigCommand(configType, GitConfigAction.ADD, value);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command)), command);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    // experimental
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiff() {
        return executeGitDiff(null, null);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiff(Path repository) {
        return executeGitDiff(repository, null);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiff(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_DIFF));
        } else {
            GitCommandResult result = GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_DIFF)), GitCommandConstants.CMD_GIT_DIFF);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    // experimental
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiffStaged() {
        return executeGitDiffStaged(null, null);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiffStaged(Path repository) {
        return executeGitDiffStaged(repository, null);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiffStaged(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_DIFF_STAGED));
        } else {
            GitCommandResult result = GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_DIFF_STAGED)), GitCommandConstants.CMD_GIT_DIFF_STAGED);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    private static String getPathifiedCommand(Path repository, Path workingDir, String command) {
        String switchTo = GitCommandConstants.CMD_SHELL_CD + repository.toString().replace('\\', '/');
        // cd to repository path and execute command
        // switch back to working directory if needed
        String pathifiedCommand = null;
        if (SOSShell.IS_WINDOWS) {
            pathifiedCommand = switchTo + DELIMITER_WINDOWS + command; 
        } else {
            pathifiedCommand = switchTo + DELIMITER_LINUX + command;
        }
        return pathifiedCommand;
    }
}
