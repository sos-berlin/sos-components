package com.sos.commons.git;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.commons.util.SOSShell;

public class GitCommand {
    private static final String CMD_GIT_STATUS = "git status";
    private static final String CMD_GIT_STATUS_SHORT = "git status -s";
    private static final String CMD_GIT_PULL = "git pull";
    private static final String DELIMITER_WINDOWS = " && ";
    private static final String DELIMITER_LINUX = " ; ";
    private static final String C_CMD_CD = "cd ";
    
    public static GitCommandResult executeGitStatus() {
//      SOSCommandResult commandResult = SOSShell.executeCommand(cmd, getEnvVariables(cmd));
        return GitUtil.createGitCommandResult(SOSShell.executeCommand(CMD_GIT_STATUS));
    }

    public static GitStatusShortCommandResult executeGitStatusShort() {
        return executeGitStatusShort(null);
    }

    public static GitStatusShortCommandResult executeGitStatusShort(Path repository) {
        if (repository == null) {
            return GitUtil.createGitStatusShortCommandResult(SOSShell.executeCommand(CMD_GIT_STATUS_SHORT));
        } else {
            Path userDir = Paths.get(System.getProperty("user.dir"));
            String switchTo = C_CMD_CD + repository.toString().replace('\\', '/');
            String switchBack = C_CMD_CD + userDir.toString().replace('\\', '/'); 
            String pathifiedCommand = null;
            if (SOSShell.IS_WINDOWS) {
                pathifiedCommand = switchTo + DELIMITER_WINDOWS + CMD_GIT_STATUS_SHORT + DELIMITER_WINDOWS + switchBack; 
            } else {
                pathifiedCommand = switchTo + DELIMITER_LINUX + CMD_GIT_STATUS_SHORT + DELIMITER_LINUX + switchBack;
            }
            return GitUtil.createGitStatusShortCommandResult(SOSShell.executeCommand(pathifiedCommand), CMD_GIT_STATUS_SHORT);
        }
    }

    public static GitCommandResult executeGitPull() {
        return GitUtil.createGitCommandResult(SOSShell.executeCommand(CMD_GIT_PULL));
    }
    
}
