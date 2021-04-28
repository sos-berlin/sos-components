package com.sos.jitl.jobs.db.oracle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class SOSPLSQLJobArguments extends JobArguments {

    private JobArgument<Path> hibernateFile = new JobArgument<Path>("hibernate_configuration_file", Job.getAgentHibernateFile());
    private JobArgument<String> command = new JobArgument<String>("command");
    private JobArgument<String> commandScripFile = new JobArgument<String>("command_script_file");
    private JobArgument<String> variableParserRegExpr = new JobArgument<String>("variable_parser_reg_expr", "^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$");
    private JobArgument<String> dbPassword = new JobArgument<String>("db_password");
    private JobArgument<String> dbUrl = new JobArgument<String>("db_url");
    private JobArgument<String> dbUser = new JobArgument<String>("db_user");
    private JobArgument<String> credentialStoreFile = new JobArgument<String>("credential_store_file");
    private JobArgument<String> credentialStoreKeyFile = new JobArgument<String>("credential_store_key_file");
    private JobArgument<String> credentialStorePassword = new JobArgument<String>("credential_store_password");
    private JobArgument<String> credentialStoreEntryPath = new JobArgument<String>("credential_store_entry_path");
    private JobArgument<Boolean> resultSetAsParameters = new JobArgument<Boolean>("resultset_as_parameters", false);
    private JobArgument<Boolean> resultSetAsWarning = new JobArgument<Boolean>("resultset_as_warning", false);

    public Path getHibernateFile() {
        return hibernateFile.getValue();
    }

    public void setHibernateFile(Path hibernateFile) {
        this.hibernateFile.setValue(hibernateFile);
    }

    public String getCommand() {
        return command.getValue();
    }

    public void setCommand(String command) {
        this.command.setValue(command);
    }

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

    public void setCredentialStoreEntryPath(JobArgument<String> credentialStoreEntryPath) {
        this.credentialStoreEntryPath = credentialStoreEntryPath;
    }

    public Boolean getResultSetAsParameters() {
        return resultSetAsParameters.getValue();
    }

    public void setResultSetAsParameters(Boolean resultSetAsParameters) {
        this.resultSetAsParameters.setValue(resultSetAsParameters);
    }

    public Boolean getResultSetAsWarning() {
        return resultSetAsWarning.getValue();
    }

    public void setResultSetAsWarning(Boolean resultSetAsWarning) {
        this.resultSetAsWarning.setValue(resultSetAsWarning);
    }

    public boolean useHibernateFile() {
        return ((dbUrl.getValue() == null) && (dbUser.getValue() == null)) || ((dbUrl.getValue().isEmpty()) && (dbUser.getValue().isEmpty()))  ;
    }

    public String getCommandScripFile() {
        return commandScripFile.getValue();
    }

    public String getCommandScriptFileContent() throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        Stream<String> stream = null;
        try {
            stream = Files.lines(Paths.get(commandScripFile.getValue()), StandardCharsets.UTF_8);
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return contentBuilder.toString();

    }

    public void setCommandScripFile(String commandScripFile) {
        this.commandScripFile.setValue(commandScripFile);
    }

}
