package com.sos.commons.git;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import com.sos.commons.exception.SOSException;
import com.sos.commons.git.enums.GitConfigAction;
import com.sos.commons.git.enums.GitConfigType;
import com.sos.commons.git.results.GitCommandResult;
import com.sos.commons.git.util.GitCommandConstants;
import com.sos.commons.git.util.GitUtil;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSTimeout;

public class GitCommand {
    private static final String DELIMITER_WINDOWS = " && ";
    private static final String DELIMITER_LINUX = " ; ";
    private static final SOSTimeout TIMEMOUT_SHORT = new SOSTimeout(30, TimeUnit.SECONDS);
    private static final SOSTimeout TIMEMOUT_LONG = new SOSTimeout(5, TimeUnit.MINUTES);
    
    
    public static GitCommandResult executeGitStatusShort(Charset charset) {
        return executeGitStatusShort(null, null, charset);
    }

    public static GitCommandResult executeGitStatusShort(Path repository, Charset charset) {
        return executeGitStatusShort(repository, null, charset);
    }

    public static GitCommandResult executeGitStatusShort(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitStatusShortCommandResult(
                    SOSShell.executeCommand(GitCommandConstants.CMD_GIT_STATUS_SHORT, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitStatusShortCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_STATUS_SHORT), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_STATUS_SHORT);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }

    public static GitCommandResult executeGitPull(Charset charset) {
        return executeGitPull(null, null, charset);
    }
    
    public static GitCommandResult executeGitPull(Path repository, Charset charset) {
        return executeGitPull(repository, null, charset);
    }
    
    public static GitCommandResult executeGitPull(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_PULL, charset, TIMEMOUT_LONG));
        } else {
            GitCommandResult result = GitUtil.createGitPullCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_PULL), charset, TIMEMOUT_LONG), GitCommandConstants.CMD_GIT_PULL);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitAdd(Charset charset) {
        return executeGitAdd(null, null, charset);
    }
    
    public static GitCommandResult executeGitAdd(Path repository, Charset charset) {
        return executeGitAdd(repository, null, charset);
    }
    
    public static GitCommandResult executeGitAdd(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitAddCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_ADD, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitAddCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_ADD), charset, TIMEMOUT_SHORT), GitCommandConstants.CMD_GIT_ADD);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitAddAll(Charset charset) {
        return executeGitAddAll(null, null, charset);
    }
    
    public static GitCommandResult executeGitAddAll(Path repository, Charset charset) {
        return executeGitAddAll(repository, null, charset);
    }
    
    public static GitCommandResult executeGitAddAll(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitAddAllCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_ADD_ALL, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitAddAllCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_ADD_ALL), charset, TIMEMOUT_SHORT), GitCommandConstants.CMD_GIT_ADD_ALL);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitCommit(Charset charset) {
        return executeGitCommit(null, null, charset);
    }
    
    public static GitCommandResult executeGitCommit(Path repository, Charset charset) {
        return executeGitCommit(repository, null, charset);
    }
    
    public static GitCommandResult executeGitCommit(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_COMMIT, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_COMMIT), charset, TIMEMOUT_SHORT), GitCommandConstants.CMD_GIT_COMMIT);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitCommitFormatted(String message, Charset charset) {
        return executeGitCommitFormatted(message, null, null, charset);
    }
    
    public static GitCommandResult executeGitCommitFormatted(String message, Path repository, Charset charset) {
        return executeGitCommitFormatted(message, repository, null, charset);
    }
    
    public static GitCommandResult executeGitCommitFormatted(String message, Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(String.format(
                    GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message), charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, String.format(GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message)), charset, TIMEMOUT_SHORT), 
                    String.format(GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message));
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitCheckout(String branchOrTag, boolean isBranch, Charset charset) {
        if(isBranch) {
            return executeGitCheckout(branchOrTag, null, null, null, charset);
        } else {
            return executeGitCheckout(null, branchOrTag, null, null, charset);
        }
    }
    
    public static GitCommandResult executeGitCheckout(String branchOrTag, boolean isBranch, Path repository, Charset charset) {
        if(isBranch) {
            return executeGitCheckout(branchOrTag, null, repository, null, charset);
        } else {
            return executeGitCheckout(null, branchOrTag, repository, null, charset);
        }
    }
    
    public static GitCommandResult executeGitCheckout(String branch, String tagname, Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            if(branch != null) {
                return GitUtil.createGitCheckoutCommandResult(
                        SOSShell.executeCommand(GitCommandConstants.CMD_GIT_CHECKOUT_BRANCH + branch, charset, TIMEMOUT_SHORT));
            } else { // tagname != null
                return GitUtil.createGitCheckoutCommandResult(
                        SOSShell.executeCommand(GitCommandConstants.CMD_GIT_CHECKOUT_TAG + tagname, charset, TIMEMOUT_SHORT));
            }
        } else {
            GitCommandResult result = null;
            if(branch != null) {
                result = GitUtil.createGitCheckoutCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                        repository, workingDir, GitCommandConstants.CMD_GIT_CHECKOUT_BRANCH + branch), charset, TIMEMOUT_SHORT), 
                        GitCommandConstants.CMD_GIT_CHECKOUT_BRANCH + branch);
            } else { // tagname != null
                result = GitUtil.createGitCheckoutCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                        repository, workingDir, GitCommandConstants.CMD_GIT_CHECKOUT_TAG + tagname), charset, TIMEMOUT_SHORT), 
                        GitCommandConstants.CMD_GIT_CHECKOUT_TAG + tagname);
            }
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitAddAndCommitExisting(Charset charset) {
        return executeGitAddAndCommitExisting(null, null, charset);
    }
    
    public static GitCommandResult executeGitAddAndCommitExisting(Path repository, Charset charset) {
        return executeGitAddAndCommitExisting(repository, null, charset);
    }
    
    public static GitCommandResult executeGitAddAndCommitExisting(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(
                    GitCommandConstants.CMD_GIT_ADD_AND_COMMIT, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_ADD_AND_COMMIT), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_ADD_AND_COMMIT);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitAddAndCommitExistingFormatted(String message, Charset charset) {
        return executeGitAddAndCommitExistingFormatted(message, null, null, charset);
    }
    
    public static GitCommandResult executeGitAddAndCommitExistingFormatted(String message, Path repository, Charset charset) {
        return executeGitAddAndCommitExistingFormatted(message, repository, null, charset);
    }
    
    public static GitCommandResult executeGitAddAndCommitExistingFormatted(String message, Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(String.format(
                    GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message), charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitCommitCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, String.format(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message)), charset, TIMEMOUT_SHORT), 
                    String.format(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message));
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitPush(Charset charset) {
        return executeGitPush(null, null, charset);
    }
    
    public static GitCommandResult executeGitPush(Path repository, Charset charset) {
        return executeGitPush(repository, null, charset);
    }
    
    public static GitCommandResult executeGitPush(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitPushCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_PUSH, charset, TIMEMOUT_LONG));
        } else {
            GitCommandResult result = GitUtil.createGitPushCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_PUSH), charset, TIMEMOUT_LONG), GitCommandConstants.CMD_GIT_PUSH);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitClone(String remoteRepositoryUri, Path repository, Charset charset) {
        return executeGitClone(remoteRepositoryUri, null, repository, charset);
    }
    
    public static GitCommandResult executeGitClone(String remoteRepositoryUri, String targetFolder, Path repository, Charset charset) {
        return GitUtil.createGitCloneCommandResult(SOSShell.executeCommand(String.format("%1$s%2$s %3$s", 
                GitCommandConstants.CMD_GIT_CLONE, remoteRepositoryUri, repository.resolve(targetFolder)).toString(), charset, TIMEMOUT_LONG));
    }
    
    public static GitCommandResult executeGitTag(Charset charset) {
        return executeGitTag(null, null, charset);
    }
    
    public static GitCommandResult executeGitTag(Path repository, Charset charset) {
        return executeGitTag(repository, null, charset);
    }
    
    public static GitCommandResult executeGitTag(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitTagCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_TAG, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitTagCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_TAG), charset, TIMEMOUT_SHORT), GitCommandConstants.CMD_GIT_TAG);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteRead(Charset charset) {
        return executeGitRemoteRead(null, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteRead(Path repository, Charset charset) {
        return executeGitRemoteRead(repository, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteRead(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_V, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_V), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_REMOTE_V);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteAdd(String shortName, String remoteURI, Charset charset) {
        return executeGitRemoteAdd(shortName, remoteURI, null, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteAdd(String shortName, String remoteURI, Path repository, Charset charset) {
        return executeGitRemoteAdd(shortName, remoteURI, repository, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteAdd(String shortName, String remoteURI, Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(
                    GitCommandConstants.CMD_GIT_REMOTE_ADD + shortName + " " + remoteURI, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_ADD + shortName + " " + remoteURI), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_REMOTE_ADD + shortName + " " + remoteURI);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteRemove(String shortName, Charset charset) {
        return executeGitRemoteRemove(shortName, null, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteRemove(String shortName, Path repository, Charset charset) {
        return executeGitRemoteRemove(shortName, repository, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteRemove(String shortName, Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(
                    SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_REMOVE + shortName, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_REMOVE + shortName), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_REMOTE_REMOVE + shortName);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteUpdate(Charset charset) {
        return executeGitRemoteUpdate(null, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteUpdate(Path repository, Charset charset) {
        return executeGitRemoteUpdate(repository, null, charset);
    }
    
    public static GitCommandResult executeGitRemoteUpdate(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitRemoteCommandResult(
                    SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_UPDATE, charset, TIMEMOUT_LONG));
        } else {
            GitCommandResult result = GitUtil.createGitRemoteCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_REMOTE_UPDATE), charset, TIMEMOUT_LONG),
                    GitCommandConstants.CMD_GIT_REMOTE_UPDATE);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitCheckRemoteConnection(String repositoryUri, Charset charset) {
        return executeGitCheckRemoteConnection(repositoryUri, null, null, charset);
    }
    
    public static GitCommandResult executeGitCheckRemoteConnection(String repositoryUri, Path repository, Charset charset) {
        return executeGitCheckRemoteConnection(repositoryUri, repository, null, charset);
    }
    
    public static GitCommandResult executeGitCheckRemoteConnection(String repositoryUri, Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitLsRemoteCommandResult(
                    SOSShell.executeCommand(GitCommandConstants.CMD_GIT_LS_REMOTE + repositoryUri, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitLsRemoteCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_LS_REMOTE + repositoryUri), charset, TIMEMOUT_SHORT), 
                    GitCommandConstants.CMD_GIT_LS_REMOTE + repositoryUri);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitLog(Charset charset) {
        return executeGitLog(null, null, charset);
    }
    
    public static GitCommandResult executeGitLog(Path repository, Charset charset) {
        return executeGitLog(repository, null, charset);
    }
    
    public static GitCommandResult executeGitLog(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitLogCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_LOG, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitLogCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_LOG), charset, TIMEMOUT_SHORT), GitCommandConstants.CMD_GIT_LOG);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitLogParseable(Charset charset) {
        return executeGitLogParseable(null, null, charset);
    }
    
    public static GitCommandResult executeGitLogParseable(Path repository, Charset charset) {
        return executeGitLogParseable(repository, null, charset);
    }
    
    public static GitCommandResult executeGitLogParseable(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitLogCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_LOG_ONE_LINE, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitLogCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_LOG_ONE_LINE), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_LOG_ONE_LINE);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitCherryPick(Charset charset) {
        return executeGitCherryPick(null, null, charset);
    }
    
    public static GitCommandResult executeGitCherryPick(Path repository, Charset charset) {
        return executeGitCherryPick(repository, null, charset);
    }
    
    public static GitCommandResult executeGitCherryPick(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitCherryPickCommandResult(SOSShell.executeCommand(
                    GitCommandConstants.CMD_GIT_CHERRY_PICK, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitCherryPickCommandResult(SOSShell.executeCommand(
                    getPathifiedCommand(repository, workingDir, GitCommandConstants.CMD_GIT_CHERRY_PICK), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_CHERRY_PICK);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitRestore(Charset charset) {
        return executeGitRestore(null, null, charset);
    }
    
    public static GitCommandResult executeGitRestore(Path repository, Charset charset) {
        return executeGitRestore(repository, null, charset);
    }
    
    public static GitCommandResult executeGitRestore(Path repository, Path workingDir, Charset charset) {
        // use "git restore <file>..." to discard changes in working directory
        if (repository == null) {
            return GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_RESTORE, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_RESTORE), charset, TIMEMOUT_SHORT), GitCommandConstants.CMD_GIT_RESTORE);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitRestoreStaged(Charset charset) {
        return executeGitRestoreStaged(null, null, charset);
    }
    
    public static GitCommandResult executeGitRestoreStaged(Path repository, Charset charset) {
        return executeGitRestoreStaged(repository, null, charset);
    }
    
    public static GitCommandResult executeGitRestoreStaged(Path repository, Path workingDir, Charset charset) {
        // use "git restore --staged <file>..." to unstage
        if (repository == null) {
            return GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_RESTORE, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitRestoreCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_RESTORE), charset, TIMEMOUT_SHORT), GitCommandConstants.CMD_GIT_RESTORE);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfig(String params, Charset charset) {
        return executeGitConfig(params, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfig(String params, Path repository, Charset charset) {
        return executeGitConfig(params, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfig(String params, Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(
                    GitCommandConstants.CMD_GIT_CONFIG + params, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_CONFIG + params), charset, TIMEMOUT_SHORT),
                    GitCommandConstants.CMD_GIT_CONFIG + params);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshGet(GitConfigType configType, Charset charset) throws SOSException {
        return executeGitConfigSshGet(configType, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshGet(GitConfigType configType, Path repository, Charset charset) throws SOSException {
        return executeGitConfigSshGet(configType, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshGet(GitConfigType configType, Path repository, Path workingDir, Charset charset)
            throws SOSException {
        String command = GitUtil.getConfigSshCommand(configType, GitConfigAction.GET);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigUsernameGet(GitConfigType configType, Charset charset) throws SOSException {
        return executeGitConfigUsernameGet(configType, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUsernameGet(GitConfigType configType, Path repository, Charset charset)
            throws SOSException {
        return executeGitConfigUsernameGet(configType, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUsernameGet(GitConfigType configType, Path repository, Path workingDir, Charset charset)
            throws SOSException {
        String command = GitUtil.getConfigUsername(configType, GitConfigAction.GET, null);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigUserEmailGet(GitConfigType configType, Charset charset) throws SOSException {
        return executeGitConfigUserEmailGet(configType, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUserEmailGet(GitConfigType configType, Path repository, Charset charset)
            throws SOSException {
        return executeGitConfigUserEmailGet(configType, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUserEmailGet(GitConfigType configType, Path repository, Path workingDir, Charset charset)
            throws SOSException {
        String command = GitUtil.getConfigUserEmail(configType, GitConfigAction.GET, null);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshUnset(GitConfigType configType, Charset charset) throws SOSException {
        return executeGitConfigSshUnset(configType, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshUnset(GitConfigType configType, Path repository, Charset charset) throws SOSException {
        return executeGitConfigSshUnset(configType, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshUnset(GitConfigType configType, Path repository, Path workingDir, Charset charset)
            throws SOSException {
        String command = GitUtil.getConfigSshCommand(configType, GitConfigAction.UNSET);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigUsernameUnset(GitConfigType configType, Charset charset) throws SOSException {
        return executeGitConfigUsernameUnset(configType, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUsernameUnset(GitConfigType configType, Path repository, Charset charset) throws SOSException {
        return executeGitConfigUsernameUnset(configType, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUsernameUnset(GitConfigType configType, Path repository, Path workingDir, Charset charset)
            throws SOSException {
        String command = GitUtil.getConfigUsername(configType, GitConfigAction.UNSET, null);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigUserEmailUnset(GitConfigType configType, Charset charset) throws SOSException {
        return executeGitConfigUserEmailUnset(configType, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUserEmailUnset(GitConfigType configType, Path repository, Charset charset) throws SOSException {
        return executeGitConfigUserEmailUnset(configType, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUserEmailUnset(GitConfigType configType, Path repository, Path workingDir, Charset charset)
            throws SOSException {
        String command = GitUtil.getConfigUserEmail(configType, GitConfigAction.UNSET, null);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshAdd(GitConfigType configType, Path keyFilePath, Charset charset) throws SOSException {
        return executeGitConfigSshAdd(configType, keyFilePath, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshAdd(GitConfigType configType, Path keyFilePath, Path repository, Charset charset)
            throws SOSException {
        return executeGitConfigSshAdd(configType, keyFilePath, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshAdd(GitConfigType configType, Path keyFilePath, Path repository, Path workingDir,
            Charset charset) throws SOSException {
        String command = GitUtil.getConfigSshCommand(configType, GitConfigAction.ADD, keyFilePath);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigUsernameAdd(GitConfigType configType, String username, Charset charset) throws SOSException {
        return executeGitConfigUsernameAdd(configType, username, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUsernameAdd(GitConfigType configType, String username, Path repository, Charset charset)
            throws SOSException {
        return executeGitConfigUsernameAdd(configType, username, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUsernameAdd(GitConfigType configType, String username, Path repository, Path workingDir,
            Charset charset) throws SOSException {
        String command = GitUtil.getConfigUsername(configType, GitConfigAction.ADD, username);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigUserEmailAdd(GitConfigType configType, String email, Charset charset) throws SOSException {
        return executeGitConfigUserEmailAdd(configType, email, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUserEmailAdd(GitConfigType configType, String email, Path repository, Charset charset)
            throws SOSException {
        return executeGitConfigUserEmailAdd(configType, email, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigUserEmailAdd(GitConfigType configType, String email, Path repository, Path workingDir,
            Charset charset) throws SOSException {
        String command = GitUtil.getConfigUserEmail(configType, GitConfigAction.ADD, email);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    public static GitCommandResult executeGitConfigSshAddCustom(GitConfigType configType, String value, Charset charset) throws SOSException {
        return executeGitConfigSshAddCustom(configType, value, null, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshAddCustom(GitConfigType configType, String value, Path repository, Charset charset)
            throws SOSException {
        return executeGitConfigSshAddCustom(configType, value, repository, null, charset);
    }
    
    public static GitCommandResult executeGitConfigSshAddCustom(GitConfigType configType, String value, Path repository, Path workingDir,
            Charset charset) throws SOSException {
        String command = GitUtil.getConfigSshCommand(configType, GitConfigAction.ADD, value);
        if (repository == null) {
            return GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(command, charset, TIMEMOUT_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitConfigCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, command), charset, TIMEMOUT_SHORT), command);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    // experimental
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiff(Charset charset) {
        return executeGitDiff(null, null, charset);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiff(Path repository, Charset charset) {
        return executeGitDiff(repository, null, charset);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiff(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_DIFF, charset, TIMEMOUT_LONG));
        } else {
            GitCommandResult result = GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_DIFF), charset, TIMEMOUT_LONG), GitCommandConstants.CMD_GIT_DIFF);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    // experimental
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiffStaged(Charset charset) {
        return executeGitDiffStaged(null, null, charset);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiffStaged(Path repository, Charset charset) {
        return executeGitDiffStaged(repository, null, charset);
    }
    @SuppressWarnings("unused")
    private static GitCommandResult executeGitDiffStaged(Path repository, Path workingDir, Charset charset) {
        if (repository == null) {
            return GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_DIFF_STAGED, charset, TIMEMOUT_LONG));
        } else {
            GitCommandResult result = GitUtil.createGitDiffCommandResult(SOSShell.executeCommand(getPathifiedCommand(
                    repository, workingDir, GitCommandConstants.CMD_GIT_DIFF_STAGED), charset, TIMEMOUT_LONG),
                    GitCommandConstants.CMD_GIT_DIFF_STAGED);
            switchBackToWorkingDir(workingDir, charset);
            return result;
        }
    }
    
    private static String getPathifiedCommand(Path repository, Path workingDir, String command) {
        String switchTo = "";
        // cd to repository path and execute command
        // switch back to working directory if needed
        String pathifiedCommand = null;
        if (SOSShell.IS_WINDOWS) {
            switchTo = GitCommandConstants.CMD_SHELL_CD_WIN + repository.toString().replace('\\', '/');
            pathifiedCommand = switchTo + DELIMITER_WINDOWS + command; 
        } else {
            switchTo = GitCommandConstants.CMD_SHELL_CD + repository.toString().replace('\\', '/');
            pathifiedCommand = switchTo + DELIMITER_LINUX + command;
        }
        return pathifiedCommand;
    }
    
    private static void switchBackToWorkingDir(Path workingDir, Charset charset) {
        if(workingDir != null) {
            if (SOSShell.IS_WINDOWS) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD_WIN + workingDir.toString().replace('\\', '/'), charset, TIMEMOUT_SHORT);
            } else {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/'), charset, TIMEMOUT_SHORT);
            }
            
        }
    }
}
