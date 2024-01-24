package com.sos.commons.git.util;


public class GitCommandConstants {

    // general commands
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
    // git checkout -B <branch>
    public static final String CMD_GIT_CHECKOUT_BRANCH = "git checkout -B ";
    // git checkout <tagname>
    public static final String CMD_GIT_CHECKOUT_TAG = "git checkout ";
    // git remote -v
    public static final String CMD_GIT_REMOTE_V = "git remote -v";
    // git remote add
    public static final String CMD_GIT_REMOTE_ADD = "git remote add ";
    // git remote remove
    public static final String CMD_GIT_REMOTE_REMOVE = "git remote remove ";
    // git remote update
    public static final String CMD_GIT_REMOTE_UPDATE = "git remote update";
    // git ls-remote - used to check connection
    public static final String CMD_GIT_LS_REMOTE = "git ls-remote ";
    // git log
    public static final String CMD_GIT_LOG = "git log";
    // git log --oneline
    public static final String CMD_GIT_LOG_ONE_LINE = "git log --oneline";
    // git tag
    public static final String CMD_GIT_TAG = "git tag ";
    // git cherry-pick
    public static final String CMD_GIT_CHERRY_PICK = "git cherry-pick ";
    // git push
    public static final String CMD_GIT_PUSH = "git push";
    // git clone
    public static final String CMD_GIT_CLONE = "git clone ";

    // GIT CONFIG COMMANDS
    
    // git config
    public static final String CMD_GIT_CONFIG = "git config ";
    // GLOBAL
    
    // git config command - usage to get key file setting from global config
    public static final String CMD_GIT_CONFIG_GET_GLOBAL_SSH_COMMAND = "git config --global --get core.sshCommand";
    // git config command - usage to get user.name setting from global config
    public static final String CMD_GIT_CONFIG_GET_GLOBAL_USER_NAME = "git config --global --get user.name";
    // git config command - usage to get user.email setting from global config
    public static final String CMD_GIT_CONFIG_GET_GLOBAL_USER_EMAIL = "git config --global --get user.email";
    // git config command - usage to get safe.directory setting from global config
    public static final String CMD_GIT_CONFIG_GET_GLOBAL_SAVE_DIRECTORY = "git config --global --get safe.directory";
    // git config command - usage to unset key file from global config
    public static final String CMD_GIT_CONFIG_UNSET_GLOBAL_SSH_COMMAND = "git config --global --unset core.sshCommand";
    // git config command - usage to unset user.name from global config
    public static final String CMD_GIT_CONFIG_UNSET_GLOBAL_USER_NAME = "git config --global --unset user.name";
    // git config command - usage to unset user.email from global config
    public static final String CMD_GIT_CONFIG_UNSET_GLOBAL_USER_EMAIL = "git config --global --unset user.email";
    // git config command - usage to unset safe.directory setting from global config
    public static final String CMD_GIT_CONFIG_UNSET_GLOBAL_SAVE_DIRECTORY = "git config --global --unset --global safe.directory";
    // git config command - usage to set different key file for global config
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_PREFORMAT_WIN = "git config --global --add core.sshCommand \"ssh -o StrictHostKeyChecking=no -i %1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_PREFORMAT_LINUX = "git config --global --add core.sshCommand 'ssh -o StrictHostKeyChecking=no -i %1$s'";
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_FORMAT_WIN = "git config --global --add core.sshCommand \"%1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_SSH_COMMAND_FORMAT_LINUX = "git config --global --add core.sshCommand '%1$s'";
    // git config command - usage to set different user.name for global config
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_USER_NAME_FORMAT_WIN = "git config --global --add user.name \"%1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_USER_NAME_FORMAT_LINUX = "git config --global --add user.name '%1$s'";
    // git config command - usage to set different user.email for global config
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_USER_EMAIL_FORMAT_WIN = "git config --global --add user.email \"%1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_USER_EMAIL_FORMAT_LINUX = "git config --global --add user.email '%1$s'";
    // git config command - usage to set shared local repo as save directory
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_SAVE_DIRECTORY_WIN = "git config --global --add safe.directory '%1$s'";
    public static final String CMD_GIT_CONFIG_ADD_GLOBAL_SAVE_DIRECTORY_LINUX = "git config --global --add safe.directory \"%1$s\"";

    // LOCAL
    
    // git config command - usage to get key file setting from global config
    public static final String CMD_GIT_CONFIG_GET_LOCAL_SSH_COMMAND = "git config --get --local core.sshCommand";
    // git config command - usage to get user.name setting from global config
    public static final String CMD_GIT_CONFIG_GET_LOCAL_USER_NAME = "git config --get --local user.name";
    // git config command - usage to get user.email setting from global config
    public static final String CMD_GIT_CONFIG_GET_LOCAL_USER_EMAIL = "git config --get --local user.email";
    // git config command - usage to get safe.directory setting from global config
    public static final String CMD_GIT_CONFIG_GET_LOCAL_SAVE_DIRECTORY = "git config --local --get safe.directory";
    // git config command - usage to unset key file from global config
    public static final String CMD_GIT_CONFIG_UNSET_LOCAL_SSH_COMMAND = "git config --local --unset core.sshCommand";
    // git config command - usage to unset user.name from global config
    public static final String CMD_GIT_CONFIG_UNSET_LOCAL_USER_NAME = "git config --local --unset user.name";
    // git config command - usage to unset user.email from global config
    public static final String CMD_GIT_CONFIG_UNSET_LOCAL_USER_EMAIL = "git config --local --unset user.email";
    // git config command - usage to unset safe.directory setting from local config
    public static final String CMD_GIT_CONFIG_UNSET_LOCAL_SAVE_DIRECTORY = "git config --local --unset --global safe.directory";
    // git config command - usage to set different key file for global config
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_SSH_COMMAND_PREFORMAT_WIN = "git config --local --add core.sshCommand \"ssh -o StrictHostKeyChecking=no -i %1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_SSH_COMMAND_PREFORMAT_LINUX = "git config --local --add core.sshCommand 'ssh -o StrictHostKeyChecking=no -i %1$s'";
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_SSH_COMMAND_FORMAT_WIN = "git config --local --add core.sshCommand \"%1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_SSH_COMMAND_FORMAT_LINUX = "git config --local --add core.sshCommand '%1$s'";
    // git config command - usage to set different user.name for global config
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_USER_NAME_FORMAT_WIN = "git config --local --add user.name \"%1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_USER_NAME_FORMAT_LINUX = "git config --local --add user.name '%1$s'";
    // git config command - usage to set different user.email for global config
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_USER_EMAIL_FORMAT_WIN = "git config --local --add user.email \"%1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_USER_EMAIL_FORMAT_LINUX = "git config --local --add user.email '%1$s'";
    // git config command - usage to set shared local repo as save directory
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_SAVE_DIRECTORY_WIN = "git config --local --add safe.directory \"%1$s\"";
    public static final String CMD_GIT_CONFIG_ADD_LOCAL_SAVE_DIRECTORY_LINUX = "git config --local --add safe.directory '%1$s'";

    // FILE
    
    // git config command - usage to get key file setting from global config
    public static final String CMD_GIT_CONFIG_GET_FILE_SSH_COMMAND_WIN = "git config --get --file \"%1$s\" core.sshCommand";
    public static final String CMD_GIT_CONFIG_GET_FILE_SSH_COMMAND_LINUX = "git config --get --file '%1$s' core.sshCommand";
    // git config command - usage to get user.name setting from global config
    public static final String CMD_GIT_CONFIG_GET_FILE_USER_NAME_WIN = "git config --get --file \"%1$s\" user.name";
    public static final String CMD_GIT_CONFIG_GET_FILE_USER_NAME_LINUX = "git config --get --file '%1$s' user.name";
    // git config command - usage to get user.email setting from global config
    public static final String CMD_GIT_CONFIG_GET_FILE_USER_EMAIL_WIN = "git config --get --file \"%1$s\" user.email";
    public static final String CMD_GIT_CONFIG_GET_FILE_USER_EMAIL_LINUX = "git config --get --file '%1$s' user.email";
    // git config command - usage to get safe.directory setting from config file
    public static final String CMD_GIT_CONFIG_GET_FILE_SAVE_DIRECTORY_WIN = "git config --file \"%1$s\" --get safe.directory";
    public static final String CMD_GIT_CONFIG_GET_FILE_SAVE_DIRECTORY_LINUX = "git config --file '%1$s' --get safe.directory";
    // git config command - usage to unset key file from global config
    public static final String CMD_GIT_CONFIG_UNSET_FILE_SSH_COMMAND_WIN = "git config --file \"%1$s\" --unset core.sshCommand";
    public static final String CMD_GIT_CONFIG_UNSET_FILE_SSH_COMMAND_LINUX = "git config --file '%1$s' --unset core.sshCommand";
    // git config command - usage to unset user.name from global config
    public static final String CMD_GIT_CONFIG_UNSET_FILE_USER_NAME_WIN = "git config --file \"%1$s\" --unset user.name";
    public static final String CMD_GIT_CONFIG_UNSET_FILE_USER_NAME_LINUX = "git config --file '%1$s' --unset user.name";
    // git config command - usage to unset user.email from global config
    public static final String CMD_GIT_CONFIG_UNSET_FILE_USER_EMAIL_WIN = "git config --file \"%1$s\" --unset user.email";
    public static final String CMD_GIT_CONFIG_UNSET_FILE_USER_EMAIL_LINUX = "git config --file '%1$s' --unset user.email";
    // git config command - usage to unset safe.directory setting from local config
    public static final String CMD_GIT_CONFIG_UNSET_FILE_SAVE_DIRECTORY_WIN = "git config --file \"%1$s\" --unset --global safe.directory";
    public static final String CMD_GIT_CONFIG_UNSET_FILE_SAVE_DIRECTORY_LINUX = "git config --file '%1$s' --unset --global safe.directory";
    // git config command - usage to set different key file for new config file
    public static final String CMD_GIT_CONFIG_ADD_FILE_SSH_COMMAND_PREFORMAT_WIN = "git config --file \"%1$s\" --add core.sshCommand \"ssh -o StrictHostKeyChecking=no -i %2$s\"";
    public static final String CMD_GIT_CONFIG_ADD_FILE_SSH_COMMAND_PREFORMAT_LINUX = "git config --file '%1$s' --add core.sshCommand 'ssh -o StrictHostKeyChecking=no -i %2$s'";
    public static final String CMD_GIT_CONFIG_ADD_FILE_SSH_COMMAND_FORMAT_WIN = "git config --file \"%1$s\" --add core.sshCommand \"%2$s\"";
    public static final String CMD_GIT_CONFIG_ADD_FILE_SSH_COMMAND_FORMAT_LINUX = "git config --file '%1$s' --add core.sshCommand '%2$s'";
    // git config command - usage to set different user.name for global config
    public static final String CMD_GIT_CONFIG_ADD_FILE_USER_NAME_FORMAT_WIN = "git config --file \"%1$s\" --add user.name \"%2$s\"";
    public static final String CMD_GIT_CONFIG_ADD_FILE_USER_NAME_FORMAT_LINUX = "git config --file '%1$s' --add user.name '%2$s'";
    // git config command - usage to set different user.email for global config
    public static final String CMD_GIT_CONFIG_ADD_FILE_USER_EMAIL_FORMAT_WIN = "git config --file \"%1$s\" --add user.email \"%2$s\"";
    public static final String CMD_GIT_CONFIG_ADD_FILE_USER_EMAIL_FORMAT_LINUX = "git config --file '%1$s' --add user.email '%2$s'";
    // git config command - usage to set shared local repo as save directory
    public static final String CMD_GIT_CONFIG_ADD_FILE_SAVE_DIRECTORY_WIN = "git config --file \"%1$s\" --add safe.directory \"%2$s\"";
    public static final String CMD_GIT_CONFIG_ADD_FILE_SAVE_DIRECTORY_LINUX = "git config --file '%1$s' --add safe.directory '%2$s'";
    
    // SHELL COMMANDS
    public static final String CMD_SHELL_CD = "cd ";
    public static final String CMD_SHELL_CD_WIN = "cd /D ";
    public static final String CMD_SHELL_COPY_LINUX = "cp '%1$s' '%2$s'";
    public static final String CMD_SHELL_REMOVE_LINUX = "rm '%1$s'";
    public static final String REGEX_CHANGES_COUNT = "\\s(\\d)\\s[a-z]{4}\\s[a-z]{1,},\\s(\\d)\\s[a-z]{1,}\\(\\+\\),\\s(\\d).*";

}
