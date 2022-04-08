package com.sos.jitl.jobs.db.oracle;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

public class SQLPlusJobArguments extends JobArguments {

    private JobArgument<String> shellCommand = new JobArgument<String>("shell_command", false, "sqlplus");
    private JobArgument<String> ignoreOraMessages = new JobArgument<String>("ignore_ora_messages", false, "");
    private JobArgument<String> ignoreSp2Messages = new JobArgument<String>("ignore_sp2_messages", false, "");
    private JobArgument<String> commandScriptFile = new JobArgument<String>("command_script_file", false, "");
    private JobArgument<String> command = new JobArgument<String>("command", false, "");
    private JobArgument<Integer> timeout = new JobArgument<Integer>("timeout", false,0);
    private JobArgument<String> commandLineOptions = new JobArgument<String>("command_line_options", false, "");
    private JobArgument<String> includeFiles = new JobArgument<String>("include_files", false, "");
    private JobArgument<String> sqlError = new JobArgument<String>("sql_error", false);
    private JobArgument<String> dbPassword = new JobArgument<String>("db_password", false, DisplayMode.MASKED);
    private JobArgument<String> dbUser = new JobArgument<String>("db_user", false);
    private JobArgument<String> dbUrl = new JobArgument<String>("db_url", false);
    private JobArgument<String> variableParserRegExpr = new JobArgument<String>("variable_parser_reg_expr", false,
            "^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$");

    public SQLPlusJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public String getVariableParserRegExpr() {
        return variableParserRegExpr.getValue();
    }

    public void setVariableParserRegExpr(String variableParserRegExpr) {
        this.variableParserRegExpr.setValue(variableParserRegExpr);
    }

    public JobArgument<String> getDbPassword() {
        return dbPassword;
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

    public String getShellCommand() {
        return shellCommand.getValue();
    }

    public void setShellCommand(String shellCommand) {
        this.shellCommand.setValue(shellCommand);
    }

    public Integer getTimeout() {
        return timeout.getValue();
    }

    public void setTimeout(Integer timeout) {
        this.timeout.setValue(timeout);
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
        if (getDbUser() != null && !getDbUser().isEmpty()) {
            connectionString = getDbUser() + "/" + getDbPassword().getValue() + "@" + getDbUrl();
        } else {
            connectionString = "/@" + getDbUrl();
        }
        return connectionString;
    }

    private String getCommandParams(String tempFileName) {
        String commandParams = "";
        String dbConnectionString = getConnectionString();
        if (!dbConnectionString.isEmpty()) {
            commandParams += " " + dbConnectionString;
        }

        commandParams += " @" + tempFileName;

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
        String savPassword = this.getDbPassword().getValue();
        this.setDbPassword(this.getDbPassword().getDisplayValue());
        String commandLine = this.getCommandLine(tempFileName);
        this.setDbPassword(savPassword);
        return commandLine;
    }

    public void checkRequired() throws SOSJobRequiredArgumentMissingException {
        if ((command.getValue() == null || command.getValue().isEmpty()) && (commandScriptFile.getValue() == null || commandScriptFile.getValue()
                .isEmpty())) {
            throw new SOSJobRequiredArgumentMissingException(command.getName() + " or " + commandScriptFile.getName());
        }

        if ((shellCommand.getValue() == null) || (shellCommand.getValue().isEmpty())) {
            throw new SOSJobRequiredArgumentMissingException(dbUrl.getName());
        }
        if ((dbUrl.getValue() == null) || dbUrl.getValue().isEmpty()) {
            throw new SOSJobRequiredArgumentMissingException(dbUrl.getName());
        }
        if ((dbUser.getValue() == null || dbUser.getValue().isEmpty()) && dbPassword.getValue() != null && !dbPassword.getValue().isEmpty()) {
            throw new SOSJobRequiredArgumentMissingException(dbUser.getName());
        }

    }

}
