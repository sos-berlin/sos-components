package com.sos.jitl.jobs.db;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;

public class SQLExecutorJobArguments {

    public enum ArgResultSetAsParametersValues {
        NAME_VALUE, TRUE, FALSE
    }

    private JobArgument<Path> hibernateFile = new JobArgument<Path>("hibernate_configuration_file", Paths.get(Job.getAgentConfigDir(),
            "hibernate.cfg.xml"));
    private JobArgument<String> command = new JobArgument<String>("command");
    private JobArgument<String> resultSetAsParameters = new JobArgument<String>("resultset_as_parameters", ArgResultSetAsParametersValues.FALSE
            .name());
    private JobArgument<Boolean> execReturnsResultset = new JobArgument<Boolean>("exec_returns_resultset", false);
    private JobArgument<Boolean> resultSetAsWarning = new JobArgument<Boolean>("resultset_as_warning", false);

    public Path getHibernateFile() {
        return hibernateFile.getValue();
    }

    public String getCommand() {
        return command.getValue();
    }

    public void setCommand(String val) {
        command.setValue(val);
    }

    public String getResultSetAsParameters() {
        return resultSetAsParameters.getValue();
    }

    public Boolean getExecReturnsResultset() {
        return execReturnsResultset.getValue();
    }

    public Boolean getResultSetAsWarning() {
        return resultSetAsWarning.getValue();
    }
}
