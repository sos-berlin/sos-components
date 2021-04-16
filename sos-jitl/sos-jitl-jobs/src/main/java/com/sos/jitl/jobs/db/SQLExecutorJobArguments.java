package com.sos.jitl.jobs.db;

import com.sos.jitl.jobs.common.JobArgument;

public class SQLExecutorJobArguments {

    private JobArgument<String> hibernateFile = new JobArgument<String>("hibernate_configuration_file", "config/hibernate.cfg.xml");
    private JobArgument<String> command = new JobArgument<String>("command");
    private JobArgument<String> resultSetAsParameters = new JobArgument<String>("resultset_as_parameters", "false");
    private JobArgument<Boolean> execReturnsResultset = new JobArgument<Boolean>("exec_returns_resultset", false);
    private JobArgument<Boolean> resultSetAsWarning = new JobArgument<Boolean>("resultset_as_warning", false);

    public String getHibernateFile() {
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
