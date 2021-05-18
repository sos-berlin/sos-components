package com.sos.jitl.jobs.examples;

import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class SSHJobArguments extends JobArguments {

    private JobArgument<String> command = new JobArgument<>("command", false);

    public SSHJobArguments() {
        super(new SSHProviderArguments());
    }

    public JobArgument<String> getCommand() {
        return command;
    }
}
