package com.sos.jitl.jobs.db.oracle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

public class PLSQLJobArguments extends JobArguments {

    public enum ResultSetAs {
        CSV, XML, JSON
    }

    private JobArgument<Path> hibernateFile = new JobArgument<Path>("hibernate_configuration_file", false, JobHelper.getAgentHibernateFile());
    private JobArgument<String> command = new JobArgument<String>("command", false);
    private JobArgument<String> commandScriptFile = new JobArgument<String>("command_script_file", false);
    private JobArgument<String> variableParserRegExpr = new JobArgument<String>("variable_parser_reg_expr", false,
            "^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$");
    private JobArgument<String> dbPassword = new JobArgument<String>("db_password", false, DisplayMode.MASKED);
    private JobArgument<String> dbUrl = new JobArgument<String>("db_url", false);
    private JobArgument<String> dbUser = new JobArgument<String>("db_user", false);

    // CSV/XML/JSON export
    private JobArgument<ResultSetAs> resultSetAs = new JobArgument<ResultSetAs>("resultset_as", false);
    private JobArgument<Path> resultFile = new JobArgument<Path>("result_file", false);

    public PLSQLJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

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

    public JobArgument<String> getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword.setValue(dbPassword);
    }

    public JobArgument<String> getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl.setValue(dbUrl);
    }

    public JobArgument<String> getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser.setValue(dbUser);
    }

    public boolean useHibernateFile() {
        return ((dbUrl.getValue() == null) || dbUrl.getValue().isEmpty()) && ((dbUser.getValue() == null) || dbUser.getValue().isEmpty());
    }

    public String getCommandScriptFile() {
        return commandScriptFile.getValue();
    }

    public String getCommandScriptFileContent() throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        Stream<String> stream = null;
        try {
            stream = Files.lines(Paths.get(commandScriptFile.getValue()), StandardCharsets.UTF_8);
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return contentBuilder.toString();

    }

    public void setCommandScripFile(String commandScriptFile) {
        this.commandScriptFile.setValue(commandScriptFile);
    }

    public ResultSetAs getResultSetAs() {
        return resultSetAs.getValue();
    }

    protected void setResultSetAs(ResultSetAs val) {
        resultSetAs.setValue(val);
    }

    public Path getResultFile() {
        return resultFile.getValue();
    }

    protected void setResultFile(Path val) {
        resultFile.setValue(val);
    }

    public void checkRequired() throws JobRequiredArgumentMissingException {
        if ((command.getValue() == null || command.getValue().isEmpty()) && (commandScriptFile.getValue() == null || commandScriptFile.getValue()
                .isEmpty())) {
            throw new JobRequiredArgumentMissingException(command.getName() + " or " + commandScriptFile.getName());
        }
        if (!useHibernateFile()) {
            if (dbUrl.getValue() == null || dbUrl.getValue().isEmpty()) {
                throw new JobRequiredArgumentMissingException(dbUrl.getName());
            }
            if ((dbUser.getValue() == null || dbUser.getValue().isEmpty()) && (dbPassword.getValue() != null)) {
                throw new JobRequiredArgumentMissingException(dbUrl.getName());
            }

        } else {
            if (hibernateFile.getValue().toString().isEmpty()) {
                throw new JobRequiredArgumentMissingException(hibernateFile.getName() + " or " + dbUrl.getName() + " + username and password");
            }
        }

        if (getResultSetAs() != null && getResultFile() == null) {
            throw new JobRequiredArgumentMissingException(resultFile.getName());
        }

    }

}
