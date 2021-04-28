package com.sos.jitl.jobs.db;

import java.nio.file.Path;

import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class SQLExecutorJobArguments extends JobArguments {

    public enum ResultSetAsParameters {
        NAME_VALUE, TRUE, FALSE
    }

    private JobArgument<Path> hibernateFile = new JobArgument<Path>("hibernate_configuration_file", Job.getAgentHibernateFile());
    private JobArgument<String> command = new JobArgument<String>("command");
    private JobArgument<ResultSetAsParameters> resultSetAsParameters = new JobArgument<ResultSetAsParameters>("resultset_as_parameters",
            ResultSetAsParameters.FALSE);
    private JobArgument<Boolean> execReturnsResultset = new JobArgument<Boolean>("exec_returns_resultset", false);
    private JobArgument<Boolean> resultSetAsWarning = new JobArgument<Boolean>("resultset_as_warning", false);

    public JobArgument<Path> getHibernateFile() {
        return hibernateFile;
    }

    public String getCommand() {
        return command.getValue();
    }

    public void setCommand(String val) {
        command.setValue(val);
    }

    public ResultSetAsParameters getResultSetAsParameters() {
        return resultSetAsParameters.getValue();
    }

    public Boolean getExecReturnsResultset() {
        return execReturnsResultset.getValue();
    }

    public Boolean getResultSetAsWarning() {
        return resultSetAsWarning.getValue();
    }
}
