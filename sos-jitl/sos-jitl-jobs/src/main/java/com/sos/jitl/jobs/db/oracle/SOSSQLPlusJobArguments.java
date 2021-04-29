package com.sos.jitl.jobs.db.oracle;

import com.sos.commons.util.SOSShell;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArgument.DisplayMode;
import com.sos.jitl.jobs.common.JobArguments;

public class SOSSQLPlusJobArguments extends JobArguments {

    private JobArgument<String> shellCommand = new JobArgument<String>("shell_command", false,"sqlplus");
    private JobArgument<String> osName = new JobArgument<String>("os_name", false);
    private JobArgument<String> ignoreOraMessages = new JobArgument<String>("ignore_ora_messages", false, "");
    private JobArgument<String> ignoreSp2Messages = new JobArgument<String>("ignore_sp2_messages", false, "");
    private JobArgument<String> commandScriptFile = new JobArgument<String>("command_script_file", false, "");
    private JobArgument<String> command = new JobArgument<String>("command", false, "");
    private JobArgument<String> commandLineOptions = new JobArgument<String>("command_line_options", false, "");
    private JobArgument<String> includeFiles = new JobArgument<String>("include_files", false, "");
    private JobArgument<String> sqlError = new JobArgument<String>("sqlError", false);
    private JobArgument<String> dbPassword = new JobArgument<String>("db_password", false, DisplayMode.MASKED);
    private JobArgument<String> dbUser = new JobArgument<String>("db_user", false);
    private JobArgument<String> dbUrl = new JobArgument<String>("db_url", false);
    private JobArgument<String> variableParserRegExpr = new JobArgument<String>("variable_parser_reg_expr", false,
            "^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$");

    private JobArgument<String> credentialStoreFile = new JobArgument<String>("credential_store_file", false);
    private JobArgument<String> credentialStoreKeyFile = new JobArgument<String>("credential_store_key_file", false);
    private JobArgument<String> credentialStorePassword = new JobArgument<String>("credential_store_password", false);
    private JobArgument<String> credentialStoreEntryPath = new JobArgument<String>("credential_store_entry_path", false);

    public String getVariableParserRegExpr() {
        return variableParserRegExpr.getValue();
    }

    public void setVariableParserRegExpr(String variableParserRegExpr) {
        this.variableParserRegExpr.setValue(variableParserRegExpr);
    }

    public String getDbPassword() {
        return dbPassword.getValue();
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword.setValue(dbPassword);
    }

    public String getDbUrl() {
        return dbUrl.getValue();
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl.setValue(dbUrl);
    }

    public String getDbUser() {
        return dbUser.getValue();
    }

    public void setDbUser(String dbUser) {
        this.dbUser.setValue(dbUser);
    }

    public String getCredentialStoreFile() {
        return credentialStoreFile.getValue();
    }

    public void setCredentialStoreFile(String credentialStoreFile) {
        this.credentialStoreFile.setValue(credentialStoreFile);
    }

    public String getCredentialStoreKeyFile() {
        return credentialStoreKeyFile.getValue();
    }

    public void setCredentialStoreKeyFile(String credentialStoreKeyFile) {
        this.credentialStoreKeyFile.setValue(credentialStoreKeyFile);
    }

    public String getCredentialStorePassword() {
        return credentialStorePassword.getValue();
    }

    public void setCredentialStorePassword(String credentialStorePassword) {
        this.credentialStorePassword.setValue(credentialStorePassword);
    }

    public String getCredentialStoreEntryPath() {
        return credentialStoreEntryPath.getValue();
    }

    public void setCredentialStoreEntryPath(String credentialStoreEntryPath) {
        this.credentialStoreEntryPath.setValue(credentialStoreEntryPath);
    }

    public String getShellCommand() {
        return shellCommand.getValue();
    }

    public void setShellCommand(String shellCommand) {
        this.shellCommand.setValue(shellCommand);
    }

    public String getOsName() {
        return osName.getValue();
    }

    public void setOsName(String osName) {
        this.osName.setValue(osName);
    }

    public String getIgnoreOraMessages() {
        return ignoreOraMessages.getValue();
    }

    public void setIgnoreOraMessages(String ignoreOraMessages) {
        this.ignoreOraMessages.setValue(ignoreOraMessages);
    }

    public String getIgnoreSp2Messages() {
        return ignoreSp2Messages.getValue();
    }

    public void setIgnoreSp2Messages(String ignoreSp2Messages) {
        this.ignoreSp2Messages.setValue(ignoreSp2Messages);
    }

    public String getCommandScriptFile() {
        return commandScriptFile.getValue();
    }

    public void setCommandScriptFile(String commandScriptFile) {
        this.commandScriptFile.setValue(commandScriptFile);
    }

    public String getCommandLineOptions() {
        return commandLineOptions.getValue();
    }

    public void setCommandLineOptions(String commandLineOptions) {
        this.commandLineOptions.setValue(commandLineOptions);
    }

    public String getIncludeFiles() {
        return includeFiles.getValue();
    }

    public void setIncludeFiles(String includeFiles) {
        this.includeFiles.setValue(includeFiles);
    }

    public String getSqlError() {
        return sqlError.getValue();
    }

    public void setSqlError(String sqlError) {
        this.sqlError.setValue(sqlError);
    }

    public String getCommand() {
        return command.getValue();
    }

    public void setCommand(String command) {
        this.command.setValue(command);
    }

    public String getConnectionString() {
        String connectionString = "";
        if (getDbUser() != null) {
            connectionString = getDbUser() + "/" + getDbPassword() + "@" + getDbUrl();
        }
        return connectionString;
    }

    private String getCommandParams(String tempFileName) {
        String commandParams = "";
        String dbConnectionString = getConnectionString();
        if (!dbConnectionString.isEmpty()) {
            commandParams += " " + dbConnectionString;
        }

        if (!getCommandScriptFile().isEmpty()) {
            commandParams += " @" + tempFileName;
        }
        return commandParams;

    }

    public String getCommandLine(String tempFileName) {
        String shellCommand = getShellCommand();
        if (SOSShell.IS_WINDOWS) {
            shellCommand = "echo 1 | " + shellCommand;
        }

        if (!getCommandLineOptions().isEmpty()) {
            shellCommand += " " + getCommandLineOptions();
        }

        return shellCommand + " " + getCommandParams(tempFileName);
    }

    public String getCommandLineForLog(String tempFileName) {
        String savPassword = this.getDbPassword();
        this.setDbPassword("************");
        String commandLine = this.getCommandLine(tempFileName);
        this.setDbPassword(savPassword);
        return commandLine;
    }

}
