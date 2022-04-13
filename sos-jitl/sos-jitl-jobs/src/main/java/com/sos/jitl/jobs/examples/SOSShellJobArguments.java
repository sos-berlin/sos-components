package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class SOSShellJobArguments extends JobArguments {

    private JobArgument<String> command = new JobArgument<>("command", true);
    private JobArgument<String> encoding = new JobArgument<>("encoding", false);
    private JobArgument<String> timeout = new JobArgument<>("timeout", false);

    public JobArgument<String> getCommand() {
        return command;
    }

    public JobArgument<String> getEncoding() {
        return encoding;
    }

    public JobArgument<String> getTimeout() {
        return timeout;
    }
}
