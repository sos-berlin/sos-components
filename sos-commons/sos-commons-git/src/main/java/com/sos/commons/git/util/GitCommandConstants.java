package com.sos.commons.git.util;


public class GitCommandConstants {

    public static final String CMD_GIT_STATUS = "git status";
    public static final String CMD_GIT_STATUS_SHORT = "git status -s";
    public static final String CMD_GIT_PULL = "git pull";
    // git add <FILE>
    public static final String CMD_GIT_ADD = "git add ";
    // git add all 
    public static final String CMD_GIT_ADD_ALL = "git add .";
    // git restore <FILE>
    // restores changes of the file in the file system
    public static final String CMD_GIT_RESTORE = "git restore ";
    // git restore --staged <FILE>
    // unstages an added file from the stage area
    public static final String CMD_GIT_RESTORE_STAGED = "git restore --staged ";
    // git diff
    // compares file from the filesystem with the file from the stage area 
    // or with the file from the last commit if no version is present in the stage area
    public static final String CMD_GIT_DIFF = "git diff";
    // git diff --staged
    // compares changes in the file from the stage area with the file from the last commit
    public static final String CMD_GIT_DIFF_STAGED = "git diff --staged";
    // git commit -m "[COMMENT]"
    public static final String CMD_GIT_COMMIT = "git commit -m ";
    public static final String CMD_GIT_COMMIT_FORMAT = "git commit -m \"%1$s\"";
    public static final String CMD_GIT_ADD_AND_COMMIT = "git commit -am ";
    public static final String CMD_GIT_ADD_AND_COMMIT_FORMAT = "git commit -am \"%1$s\"";
    // git remote -v
    public static final String CMD_GIT_REMOTE_V = "git remote -v";
    // git remote add
    public static final String CMD_GIT_REMOTE_ADD = "git remote add ";
    // git remote remove
    public static final String CMD_GIT_REMOTE_REMOVE = "git remote remove ";
    // git remote update
    public static final String CMD_GIT_REMOTE_UPDATE = "git remote update";
    // git log
    public static final String CMD_GIT_LOG = "git log";
    // git log --oneline
    public static final String CMD_GIT_LOG_ONE_LINE = "git log --oneline";
    // git tag
    public static final String CMD_GIT_TAG = "git tag";
    // git cherry-pick
    public static final String CMD_GIT_CHERRY_PICK = "git cherry-pick";
    // git push
    public static final String CMD_GIT_PUSH = "git push";
    public static final String CMD_SHELL_CD = "cd ";
    public static final String REGEX_CHANGES_COUNT = "\\s(\\d)\\s[a-z]{4}\\s[a-z]{1,},\\s(\\d)\\s[a-z]{1,}\\(\\+\\),\\s(\\d).*";

}
