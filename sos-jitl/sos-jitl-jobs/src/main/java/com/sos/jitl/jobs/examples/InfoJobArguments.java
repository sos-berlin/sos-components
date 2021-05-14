package com.sos.jitl.jobs.examples;

import java.util.List;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArgument.DisplayMode;
import com.sos.jitl.jobs.common.JobArguments;

public class InfoJobArguments extends JobArguments {

    private JobArgument<Boolean> showEnv = new JobArgument<>("show_env", false);
    private JobArgument<Boolean> redefineShowEnv = new JobArgument<>("redefine_show_env", false);
    private JobArgument<String> stringArgument = new JobArgument<>("string_argument", false);
    private JobArgument<String> returnVariables = new JobArgument<>("return_variables", false);
    private JobArgument<String> password = new JobArgument<>("password", false, DisplayMode.MASKED);
    private JobArgument<List<String>> list = new JobArgument<>("list", false);
    private JobArgument<String> shellCommand = new JobArgument<>("shell_command", false);

    public JobArgument<Boolean> getShowEnv() {
        return showEnv;
    }

    public JobArgument<Boolean> getRedefineShowEnv() {
        return redefineShowEnv;
    }

    public JobArgument<String> getStringArgument() {
        return stringArgument;
    }

    public JobArgument<String> getReturnVariables() {
        return returnVariables;
    }

    public JobArgument<String> getPassword() {
        return password;
    }

    public JobArgument<List<String>> getList() {
        return list;
    }

    public JobArgument<String> getShellCommand() {
        return shellCommand;
    }
}
