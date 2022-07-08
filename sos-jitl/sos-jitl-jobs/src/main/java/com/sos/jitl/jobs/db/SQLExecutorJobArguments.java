package com.sos.jitl.jobs.db;

import java.nio.file.Path;

import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class SQLExecutorJobArguments extends JobArguments {

    public enum ResultSetAsVariables {
        COLUMN_VALUE, NAME_VALUE, CSV, XML, JSON
    }

    private JobArgument<Path> hibernateFile = new JobArgument<Path>("hibernate_configuration_file", true, Job.getAgentHibernateFile());
    private JobArgument<String> command = new JobArgument<String>("command", true);
    private JobArgument<ResultSetAsVariables> resultSetAsVariables = new JobArgument<ResultSetAsVariables>("resultset_as_variables", false);
    private JobArgument<Boolean> execReturnsResultset = new JobArgument<Boolean>("exec_returns_resultset", false, false);
    private JobArgument<Boolean> resultSetAsWarning = new JobArgument<Boolean>("resultset_as_warning", false, false);

    // CSV/XML/JSON export
    private JobArgument<Path> resultFile = new JobArgument<Path>("result_file", false);

    public JobArgument<Path> getHibernateFile() {
        return hibernateFile;
    }

    public String getCommand() {
        return command.getValue();
    }

    public void setCommand(String val) {
        command.setValue(val);
    }

    public ResultSetAsVariables getResultSetAsVariables() {
        return resultSetAsVariables.getValue();
    }

    public Boolean getExecReturnsResultset() {
        return execReturnsResultset.getValue();
    }

    public Boolean getResultSetAsWarning() {
        return resultSetAsWarning.getValue();
    }

    public JobArgument<Path> getResultFile() {
        return resultFile;
    }
}
