package com.sos.jitl.jobs.db.oracle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

public class PLSQLJobArguments extends JobArguments {

    private JobArgument<Path> hibernateFile = new JobArgument<Path>("hibernate_configuration_file", false, Job.getAgentHibernateFile());
    private JobArgument<String> command = new JobArgument<String>("command", false);
    private JobArgument<String> commandScriptFile = new JobArgument<String>("command_script_file", false);
    private JobArgument<String> variableParserRegExpr = new JobArgument<String>("variable_parser_reg_expr", false,
            "^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$");
    private JobArgument<String> dbPassword = new JobArgument<String>("db_password", false);
    private JobArgument<String> dbUrl = new JobArgument<String>("db_url", false);
    private JobArgument<String> dbUser = new JobArgument<String>("db_user", false);

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

    public void checkRequired() throws SOSJobRequiredArgumentMissingException {
        if ((command.getValue() == null || command.getValue().isEmpty()) && (commandScriptFile.getValue() == null || commandScriptFile.getValue()
                .isEmpty())) {
            throw new SOSJobRequiredArgumentMissingException(command.getName() + " or " + commandScriptFile.getName());
        }
        if (!useHibernateFile()) {
            if (dbUrl.getValue() == null || dbUrl.getValue().isEmpty()) {
                throw new SOSJobRequiredArgumentMissingException(dbUrl.getName());
            }
            if ((dbUser.getValue() == null || dbUser.getValue().isEmpty())  && (dbPassword.getValue() != null)) {
                throw new SOSJobRequiredArgumentMissingException(dbUrl.getName());
            }
         
        } else {
            if (hibernateFile.getValue().toString().isEmpty()) {
                throw new SOSJobRequiredArgumentMissingException(hibernateFile.getName() + " or " + dbUrl.getName() + " + username and password");
            }

        }

    }

}
