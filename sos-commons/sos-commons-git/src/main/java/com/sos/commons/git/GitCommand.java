package com.sos.commons.git;

import java.nio.file.Path;

import com.sos.commons.git.results.GitAddCommandResult;
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
            return (GitCommandResult)GitUtil.createGitStatusShortCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_STATUS_SHORT));
        } else {
            GitCommandResult result = GitUtil.createGitStatusShortCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_STATUS_SHORT);
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
            GitCommandResult result = GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_PULL);
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
            GitCommandResult result = GitUtil.createGitAddCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_ADD);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_ADD_ALL));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_ADD_ALL);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_COMMIT));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_COMMIT);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(String.format(GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message)));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), String.format(GitCommandConstants.CMD_GIT_COMMIT_FORMAT, message));
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_ADD_AND_COMMIT);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(String.format(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message)));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), String.format(GitCommandConstants.CMD_GIT_ADD_AND_COMMIT_FORMAT, message));
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_PUSH));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_PUSH);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_TAG));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_TAG);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_V));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_REMOTE_V);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteAdd() {
        return executeGitRemoteAdd(null, null);
    }
    
    public static GitCommandResult executeGitRemoteAdd(Path repository) {
        return executeGitRemoteAdd(repository, null);
    }
    
    public static GitCommandResult executeGitRemoteAdd(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_ADD));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_REMOTE_ADD);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    public static GitCommandResult executeGitRemoteRemove() {
        return executeGitRemoteRemove(null, null);
    }
    
    public static GitCommandResult executeGitRemoteRemove(Path repository) {
        return executeGitRemoteRemove(repository, null);
    }
    
    public static GitCommandResult executeGitRemoteRemove(Path repository, Path workingDir) {
        if (repository == null) {
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_REMOVE));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_ADD_ALL);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_REMOTE_UPDATE));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_REMOTE_UPDATE);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_LOG));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_LOG);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_LOG_ONE_LINE));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_LOG_ONE_LINE);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_CHERRY_PICK));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_CHERRY_PICK);
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
        if (repository == null) {
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_RESTORE));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_RESTORE);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_DIFF));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_DIFF);
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
            return GitUtil.createGitPullCommandResult(SOSShell.executeCommand(GitCommandConstants.CMD_GIT_DIFF_STAGED));
        } else {
            GitAddCommandResult result = (GitAddCommandResult)GitUtil.createGitPullCommandResult(
                    SOSShell.executeCommand(getPathifiedCommand(repository, workingDir)), GitCommandConstants.CMD_GIT_DIFF_STAGED);
            if(workingDir != null) {
                SOSShell.executeCommand(GitCommandConstants.CMD_SHELL_CD + workingDir.toString().replace('\\', '/')); // fire and forget
            }
            return result;
        }
    }
    
    private static String getPathifiedCommand(Path repository, Path workingDir) {
        String switchTo = GitCommandConstants.CMD_SHELL_CD + repository.toString().replace('\\', '/');
        // cd to repository path and execute command
        // switch back to working directory if needed
        String pathifiedCommand = null;
        if (SOSShell.IS_WINDOWS) {
            pathifiedCommand = switchTo + DELIMITER_WINDOWS + GitCommandConstants.CMD_GIT_STATUS_SHORT; 
        } else {
            pathifiedCommand = switchTo + DELIMITER_LINUX + GitCommandConstants.CMD_GIT_STATUS_SHORT;
        }
        return pathifiedCommand;
    }
}
